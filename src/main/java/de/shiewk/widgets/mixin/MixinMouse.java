package de.shiewk.widgets.mixin;

import de.shiewk.widgets.widgets.CPSWidget;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;leftButtonClicked:Z"), method = "onMouseButton")
    public void onLeftClick(long window, MouseInput input, int action, CallbackInfo ci){
        if (action == 1) CPSWidget.clickLeft();
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;middleButtonClicked:Z"), method = "onMouseButton")
    public void onMiddleClick(long window, MouseInput input, int action, CallbackInfo ci){
        if (action == 1) CPSWidget.clickMiddle();
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;rightButtonClicked:Z"), method = "onMouseButton")
    public void onRightClick(long window, MouseInput input, int action, CallbackInfo ci){
        if (action == 1) CPSWidget.clickRight();
    }
}
