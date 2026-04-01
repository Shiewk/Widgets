package de.shiewk.widgets.mixin;

import de.shiewk.widgets.widgets.ComboWidget;
import de.shiewk.widgets.widgets.TPSWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPlayNetworkHandler {

    @Inject(at = @At("HEAD"), method = "handleSetTime")
    public void onWorldTimeUpdate(ClientboundSetTimePacket packet, CallbackInfo ci){
        if (Minecraft.getInstance().isSameThread()) return; // run this on the networking thread as soon as possible
        // server sends this packet every 20 ticks
        TPSWidget.worldTimeUpdated(System.nanoTime());
    }

    @Inject(at = @At("HEAD"), method = "handleHurtAnimation")
    public void onDamageTilt(ClientboundHurtAnimationPacket packet, CallbackInfo ci){
        if (Minecraft.getInstance().isSameThread()) return;
        int entityId = packet.id();
        LocalPlayer player = Minecraft.getInstance().player;
        int playerId = player == null ? Integer.MIN_VALUE : player.getId();
        ComboWidget.entityTakeDamage(playerId, entityId);
    }

    @Inject(at = @At("HEAD"), method = "handleDamageEvent")
    public void onEntityDamage(ClientboundDamageEventPacket packet, CallbackInfo ci){
        if (Minecraft.getInstance().isSameThread()) return;
        int entityId = packet.entityId();
        LocalPlayer player = Minecraft.getInstance().player;
        int playerId = player == null ? Integer.MIN_VALUE : player.getId();
        ComboWidget.entityTakeDamage(playerId, entityId);
    }
}
