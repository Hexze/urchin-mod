//? if = 1.8.9 {
/*package gg.urchin.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import gg.urchin.UrchinConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resource.Resource;
import net.minecraft.resource.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "dev.rdh.argentum.impl.render.gui.font.FontSet", remap = false)
public abstract class ArgentumFontSetMixin {
    @Unique
    private static final Identifier URCHIN_ICONS = new Identifier(UrchinConstants.ID, "font/icons.json");
    @Unique
    private static final Logger URCHIN_LOG = LogManager.getLogger("Urchin/Font");

    @ModifyExpressionValue(
        method = "load",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resource/manager/ResourceManager;getResources(Lnet/minecraft/resource/Identifier;)Ljava/util/List;",
            remap = true
        ),
        require = 0
    )
    private List<Resource> urchin$stackIcons(List<Resource> definitions) {
        List<Resource> stacked = new ArrayList<>(definitions);
        try {
            stacked.add(Minecraft.getInstance().getResourceManager().getResource(URCHIN_ICONS));
        } catch (IOException e) {
            URCHIN_LOG.warn("Could not add the urchin icons to Argentum's font", e);
        }
        return stacked;
    }
}
*///?}
