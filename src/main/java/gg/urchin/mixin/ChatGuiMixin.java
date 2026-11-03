//? if = 1.8.9 {
/*package gg.urchin.mixin;

import gg.urchin.client.tags.ChatLabels;
import net.minecraft.client.gui.chat.ChatGui;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatGui.class)
public abstract class ChatGuiMixin {
    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), argsOnly = true)
    private Text urchin$labelTaggedPlayers(Text message) {
        return ChatLabels.labelIncoming(message);
    }
}
*///?}
