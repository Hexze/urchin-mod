package gg.urchin.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
//? if = 1.8.9 {
/*import net.minecraft.client.gui.ChatMessage;
import net.minecraft.client.gui.chat.ChatGui;
*///?} else {
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?}

//? if = 1.8.9 {
/*@Mixin(ChatGui.class)
public interface ChatHistoryAccessor {
    @Accessor("messages")
    List<ChatMessage> urchin$messages();
}
*///?} else {
@Mixin(ChatComponent.class)
public interface ChatHistoryAccessor {
    @Accessor("allMessages")
    List<GuiMessage> urchin$messages();
}
//?}
