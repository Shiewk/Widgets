package de.shiewk.widgets.mixin;

import de.shiewk.widgets.widgets.CPSWidget;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouse {

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;isLeftPressed:Z", opcode = Opcodes.PUTFIELD), method = "onButton")
    public void onLeftClick(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci){
        if (action == 1) CPSWidget.clickLeft();
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;isMiddlePressed:Z", opcode = Opcodes.PUTFIELD), method = "onButton")
    public void onMiddleClick(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci){
        if (action == 1) CPSWidget.clickMiddle();
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;isRightPressed:Z", opcode = Opcodes.PUTFIELD), method = "onButton")
    public void onRightClick(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci){
        if (action == 1) CPSWidget.clickRight();
    }
}
