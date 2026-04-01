package de.shiewk.widgets.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.shiewk.widgets.WidgetsMod;
import de.shiewk.widgets.client.screen.WidgetConfigScreen;
import de.shiewk.widgets.widgets.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class WidgetsModClient implements ClientModInitializer {

    static KeyMapping configKeyBinding;

    @Override
    public void onInitializeClient() {
        WidgetRenderer widgetRenderer = new WidgetRenderer();
        ClientTickEvents.START_CLIENT_TICK.register(widgetRenderer);
        ClientLifecycleEvents.CLIENT_STARTED.register(widgetRenderer);

        // manage widgets keybind
        configKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "widgets.key.config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "main"))
        ));

        // in-game /widgetsmod command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) ->
                dispatcher.register(ClientCommands.literal("widgetsmod").executes(ctx -> {
                    WidgetsMod.LOGGER.info("Ran in-game command");
                    final Minecraft client = ctx.getSource().getClient();
                    client.schedule(() -> client.setScreen(new WidgetConfigScreen(client.screen)));
                    return 0;
                })
            )
        );

        ClientEntityEvents.ENTITY_LOAD.register((entity, _) -> {
            if (entity == Minecraft.getInstance().player){
                // player switched world
                TPSWidget.worldChanged();
            }
        });

        WidgetManager.register(new FPSWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "fps")));
        WidgetManager.register(new ClockWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "clock")));
        WidgetManager.register(new CoordinatesWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "coordinates")));
        WidgetManager.register(new BandwidthWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "bandwidth")));
        WidgetManager.register(new PingWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "ping")));
        WidgetManager.register(new ServerIPWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "server_ip")));
        WidgetManager.register(new PlayerCountWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "player_count")));
        WidgetManager.register(new CPSWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "cps")));
        WidgetManager.register(new PlayTimeWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "playtime")));
        WidgetManager.register(new MemoryUsageWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "memory")));
        WidgetManager.register(new KeyStrokesWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "keystrokes")));
        WidgetManager.register(new PlainTextWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "plaintext")));
        WidgetManager.register(new BiomeWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "biome")));
        WidgetManager.register(new SpeedWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "speed")));
        WidgetManager.register(new ArmorHudWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "armor")));
        WidgetManager.register(new InventoryWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "inventory")));
        WidgetManager.register(new WorldTimeWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "worldtime")));
        WidgetManager.register(new DirectionWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "direction")));
        WidgetManager.register(TPSWidget.INSTANCE);

        ComboWidget comboWidget = new ComboWidget(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "combo"));
        WidgetManager.register(comboWidget);
        AttackEntityCallback.EVENT.register(comboWidget);
    }
}
