package de.shiewk.widgets.mixin;

import de.shiewk.widgets.widgets.ComboWidget;
import de.shiewk.widgets.widgets.TPSWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.DamageTiltS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
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

    @Inject(at = @At("HEAD"), method = "onDamageTilt")
    public void onDamageTilt(DamageTiltS2CPacket packet, CallbackInfo ci){
        if (MinecraftClient.getInstance().isOnThread()) return;
        int entityId = packet.id();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        int playerId = player == null ? Integer.MIN_VALUE : player.getId();
        ComboWidget.entityTakeDamage(playerId, entityId);
    }

    @Inject(at = @At("HEAD"), method = "onEntityDamage")
    public void onEntityDamage(EntityDamageS2CPacket packet, CallbackInfo ci){
        if (MinecraftClient.getInstance().isOnThread()) return;
        int entityId = packet.entityId();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        int playerId = player == null ? Integer.MIN_VALUE : player.getId();
        ComboWidget.entityTakeDamage(playerId, entityId);
    }
}
