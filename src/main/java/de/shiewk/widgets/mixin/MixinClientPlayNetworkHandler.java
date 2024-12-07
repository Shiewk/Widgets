package de.shiewk.widgets.mixin;

import de.shiewk.widgets.widgets.TPSWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(at = @At("HEAD"), method = "onWorldTimeUpdate")
    public void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet, CallbackInfo ci){
        if (MinecraftClient.getInstance().isOnThread()) return; // run this on the networking thread as soon as possible
        // server sends this packet every 20 ticks
        TPSWidget.worldTimeUpdated(System.nanoTime());
    }
}
