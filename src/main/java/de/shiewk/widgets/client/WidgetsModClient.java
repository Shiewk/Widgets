package de.shiewk.widgets.client;

import de.shiewk.widgets.WidgetsMod;
import de.shiewk.widgets.client.screen.WidgetConfigScreen;
import de.shiewk.widgets.widgets.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.File;

public class WidgetsModClient implements ClientModInitializer {

    static KeyBinding configKeyBinding;


    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(new WidgetRenderer());
        ClientTickEvents.START_CLIENT_TICK.register(new WidgetRenderer());
        ClientLifecycleEvents.CLIENT_STARTED.register(new WidgetRenderer());

        // manage widgets keybind
        configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "widgets.key.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "widgets.key.category"
        ));

        // in-game /widgetsmod command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("widgetsmod").executes(ctx -> {
                    WidgetsMod.LOGGER.info("Ran in-game command");
                    final MinecraftClient client = ctx.getSource().getClient();
                    client.send(() -> client.setScreen(new WidgetConfigScreen(client.currentScreen)));
                    return 0;
                })
            )
        );

        WidgetManager.saveFileFactory = id -> new File(MinecraftClient.getInstance().runDirectory.getPath() + "/config/widgets/" + id.getNamespace() + "/" + id.getPath() + ".json");

        WidgetManager.register(new FPSWidget(Identifier.of(WidgetsMod.MOD_ID, "fps")));
        WidgetManager.register(new ClockWidget(Identifier.of(WidgetsMod.MOD_ID, "clock")));
        WidgetManager.register(new CoordinatesWidget(Identifier.of(WidgetsMod.MOD_ID, "coordinates")));
        WidgetManager.register(new BandwidthWidget(Identifier.of(WidgetsMod.MOD_ID, "bandwidth")));
        WidgetManager.register(new PingWidget(Identifier.of(WidgetsMod.MOD_ID, "ping")));
        WidgetManager.register(new ServerIPWidget(Identifier.of(WidgetsMod.MOD_ID, "server_ip")));
        WidgetManager.register(new PlayerCountWidget(Identifier.of(WidgetsMod.MOD_ID, "player_count")));
        WidgetManager.register(new CPSWidget(Identifier.of(WidgetsMod.MOD_ID, "cps")));
    }
}
