package de.shiewk.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.Command;
import de.shiewk.widgets.screen.WidgetConfigScreen;
import de.shiewk.widgets.platform.PlatformInterface;
import de.shiewk.widgets.widgets.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

public final class WidgetsMod {
    public static final String MOD_ID = "widgets";
    public static WidgetsMod INSTANCE;
    public static Logger LOGGER;
    private final PlatformInterface platform;
    private final WidgetRenderer renderer;

    private final ComboWidget comboWidget;
    private final TPSWidget tpsWidget;

    public WidgetsMod(PlatformInterface platform) {
        INSTANCE = this;
        LOGGER = platform.getLogger();
        this.platform = platform;
        this.renderer = new WidgetRenderer();

        LOGGER.info("Initialize Widgets on platform {}", platform.getPlatformName());

        WidgetManager.register(new FPSWidget(createId("fps")));
        WidgetManager.register(new ClockWidget(createId("clock")));
        WidgetManager.register(new CoordinatesWidget(createId("coordinates")));
        WidgetManager.register(new BandwidthWidget(createId("bandwidth")));
        WidgetManager.register(new PingWidget(createId("ping")));
        WidgetManager.register(new ServerIPWidget(createId("server_ip")));
        WidgetManager.register(new PlayerCountWidget(createId("player_count")));
        WidgetManager.register(new CPSWidget(createId("cps")));
        WidgetManager.register(new PlayTimeWidget(createId("playtime")));
        WidgetManager.register(new MemoryUsageWidget(createId("memory")));
        WidgetManager.register(new KeyStrokesWidget(createId("keystrokes")));
        WidgetManager.register(new PlainTextWidget(createId("plaintext")));
        WidgetManager.register(new BiomeWidget(createId("biome")));
        WidgetManager.register(new SpeedWidget(createId("speed")));
        WidgetManager.register(new ArmorHudWidget(createId("armor")));
        WidgetManager.register(new InventoryWidget(createId("inventory")));
        WidgetManager.register(new WorldTimeWidget(createId("worldtime")));
        WidgetManager.register(new DirectionWidget(createId("direction")));
        WidgetManager.register(this.tpsWidget = new TPSWidget(createId("tps")));
        WidgetManager.register(this.comboWidget = new ComboWidget(createId("combo")));
    }

    public static Identifier createId(String path){
        return Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, path);
    }

    public WidgetRenderer getRenderer() {
        return renderer;
    }

    public void onClientStarted(){
        renderer.refreshAllSettings();
    }

    public void onClientStartTick(Minecraft client){
        renderer.tickWidgets(client);
    }

    public void onEntityAttack(int entityId){
        comboWidget.clientHitEntity(entityId);
    }

    public void onWorldSwitch(){
        tpsWidget.worldChanged();
    }

    public KeyMapping createSettingsKeyMapping(){
        return new KeyMapping(
                "widgets.key.config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "main"))
        );
    }

    public int runSettingsCommand(){
        Minecraft client = Minecraft.getInstance();
        client.schedule(() -> client.gui.setScreen(new WidgetConfigScreen(client.gui.screen())));
        return Command.SINGLE_SUCCESS;
    }

    public static PlatformInterface platform() {
        return INSTANCE.platform;
    }

    public TPSWidget getTpsWidget() {
        return this.tpsWidget;
    }
}
