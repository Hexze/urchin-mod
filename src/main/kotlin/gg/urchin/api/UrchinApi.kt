package gg.urchin.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.polyfrost.oneconfig.utils.v1.Multithreading
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.concurrent.CompletableFuture

private const val HOST = "https://api.urchin.gg"
private const val UNAUTHORIZED = 401
private const val NOT_FOUND = 404
private const val FIRST_ERROR_STATUS = 400
private val TIMEOUT: Duration = Duration.ofSeconds(10)

internal val apiJson = Json { ignoreUnknownKeys = true }

class ApiException(val status: Int, val error: String?) : IOException(error ?: "request failed ($status)")

object UrchinApi {
    private val http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build()
    private var token: CompletableFuture<String>? = null

    fun lookupTags(uuids: List<String>): CompletableFuture<Map<String, List<Tag>>> =
        authorized(request("/v3/players").json("POST", BatchRequest(uuids))).thenApply { decode<BatchResponse>(it).players }

    fun lookupPlayer(identifier: String): CompletableFuture<PlayerTags?> =
        authorized(request("/v3/player/tags?${playerQuery(identifier)}").GET())
            .thenApply { if (it.statusCode() == NOT_FOUND) null else decode<PlayerTags>(it) }

    fun addTag(uuid: String, type: TagType, reason: String): CompletableFuture<Void> =
        writeTag(uuid, "POST", AddTag(type.id, reason))

    fun overwriteTag(uuid: String, current: TagType, replacement: TagType, reason: String): CompletableFuture<Void> =
        writeTag(uuid, "PATCH", OverwriteTag(current.id, replacement.id, reason))

    fun removeTag(uuid: String, type: TagType): CompletableFuture<Void> =
        writeTag(uuid, "DELETE", RemoveTag(type.id))

    private inline fun <reified T> writeTag(uuid: String, method: String, body: T): CompletableFuture<Void> =
        authorized(request("/v3/tags?${playerQuery(uuid)}").json(method, body)).thenAccept { it.checked() }

    private fun playerQuery(identifier: String): String = "player=${URLEncoder.encode(identifier, Charsets.UTF_8)}"

    private fun authorized(request: HttpRequest.Builder): CompletableFuture<HttpResponse<String>> {
        val session = currentToken()
        return session
            .thenCompose { send(request.signed(it)) }
            .thenCompose { response ->
                if (response.statusCode() != UNAUTHORIZED) return@thenCompose CompletableFuture.completedFuture(response)
                renewToken(session).thenCompose { send(request.signed(it)) }
            }
    }

    private fun signIn(): CompletableFuture<String> =
        send(request("/v3/auth/minecraft/challenge").POST(BodyPublishers.noBody()))
            .thenApply { decode<Challenge>(it).serverId }
            .thenApplyAsync({ serverId -> serverId.also(MinecraftSession::joinServer) }, Multithreading.getExecutor())
            .thenCompose { serverId -> send(request("/v3/auth/minecraft").json("POST", Login(MinecraftSession.username, serverId))) }
            .thenApply { decode<LoginResponse>(it).token }

    @Synchronized
    private fun currentToken(): CompletableFuture<String> = token ?: startSignIn()

    @Synchronized
    private fun renewToken(stale: CompletableFuture<String>): CompletableFuture<String> =
        token?.takeUnless { it === stale } ?: startSignIn()

    @Synchronized
    private fun startSignIn(): CompletableFuture<String> {
        val attempt = signIn()
        token = attempt
        attempt.whenComplete { _, error -> if (error != null) forget(attempt) }
        return attempt
    }

    @Synchronized
    private fun forget(attempt: CompletableFuture<String>) {
        if (token === attempt) token = null
    }

    private fun send(request: HttpRequest.Builder): CompletableFuture<HttpResponse<String>> =
        http.sendAsync(request.build(), BodyHandlers.ofString())

    private fun request(path: String): HttpRequest.Builder =
        HttpRequest.newBuilder(URI.create(HOST + path)).timeout(TIMEOUT)

    private inline fun <reified T> HttpRequest.Builder.json(method: String, body: T): HttpRequest.Builder =
        header("Content-Type", "application/json").method(method, BodyPublishers.ofString(apiJson.encodeToString(body)))

    private fun HttpRequest.Builder.signed(token: String): HttpRequest.Builder =
        copy().header("Authorization", "Bearer $token")

    private inline fun <reified T> decode(response: HttpResponse<String>): T =
        apiJson.decodeFromString(response.checked().body())

    private fun HttpResponse<String>.checked(): HttpResponse<String> {
        if (statusCode() < FIRST_ERROR_STATUS) return this
        throw ApiException(statusCode(), runCatching { apiJson.decodeFromString<ErrorResponse>(body()).error }.getOrNull())
    }
}

@Serializable
private class Challenge(@SerialName("server_id") val serverId: String)

@Serializable
private class Login(val username: String, @SerialName("server_id") val serverId: String)

@Serializable
private class LoginResponse(val token: String)

@Serializable
private class BatchRequest(val uuids: List<String>)

@Serializable
private class BatchResponse(val players: Map<String, List<Tag>> = emptyMap())

@Serializable
private class AddTag(val type: String, val reason: String)

@Serializable
private class OverwriteTag(
    val type: String,
    @SerialName("new_type") val newType: String,
    @SerialName("new_reason") val newReason: String,
)

@Serializable
private class RemoveTag(val type: String)

@Serializable
private class ErrorResponse(val error: String? = null)
