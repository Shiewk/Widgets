package de.shiewk.widgets.fabric;

import com.mojang.brigadier.CommandDispatcher;
import de.shiewk.widgets.WidgetsMod;
import de.shiewk.widgets.WidgetRenderer;
import de.shiewk.widgets.platform.PlatformAccessors;
import de.shiewk.widgets.platform.PlatformInterface;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FabricPlatformInterface
        implements PlatformInterface,
                    ClientLifecycleEvents.ClientStarted,
                    ClientTickEvents.StartTick,
                    AttackEntityCallback,
                    ClientEntityEvents.Load,
                    ClientCommandRegistrationCallback {

    private final PlatformAccessors accessors = new FabricPlatformAccessors();
    private final WidgetsMod widgets;
    private final KeyMapping settingsMapping;

    public FabricPlatformInterface() {
        this.widgets = new WidgetsMod(this);

        ClientLifecycleEvents.CLIENT_STARTED.register(this);
        ClientTickEvents.START_CLIENT_TICK.register(this);
        AttackEntityCallback.EVENT.register(this);
        ClientEntityEvents.ENTITY_LOAD.register(this);
        ClientCommandRegistrationCallback.EVENT.register(this);

        HudElementRegistry.addLast(
                WidgetRenderer.LAYER_ID,
                (ctx, _) -> widgets.getRenderer().renderWidgets(ctx)
        );
        KeyMappingHelper.registerKeyMapping(
                this.settingsMapping = widgets.createSettingsKeyMapping()
        );
    }

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(WidgetsMod.MOD_ID);
    }

    @Override
    public KeyMapping getRegisteredSettingsKeyMapping() {
        return this.settingsMapping;
    }

    @Override
    public PlatformAccessors accessors() {
        return accessors;
    }

    @Override
    public void onClientStarted(@NonNull Minecraft client) {
        widgets.onClientStarted();
    }

    @Override
    public void onStartTick(@NonNull Minecraft client) {
        widgets.onClientStartTick(client);
    }

    @Override
    public @NonNull InteractionResult interact(
            @NonNull Player p,
            @NonNull Level l,
            @NonNull InteractionHand h,
            @NonNull Entity e,
            @Nullable EntityHitResult r
    ) {
        widgets.onEntityAttack(e.getId());
        return InteractionResult.PASS;
    }

    @Override
    public void onLoad(@NonNull Entity e, @NonNull ClientLevel l) {
        if (e == Minecraft.getInstance().player) {
            // player switched world
            widgets.onWorldSwitch();
        }
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> d, @NonNull CommandBuildContext c) {
        // in-game /widgetsmod command
        d.register(
                ClientCommands.literal("widgetsmod")
                        .executes(_ -> widgets.runSettingsCommand())
        );
    }
}
