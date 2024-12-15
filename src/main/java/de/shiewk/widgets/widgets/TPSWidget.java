package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class TPSWidget extends BasicTextWidget {
    public TPSWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("show_label", Text.translatable("widgets.widgets.common.showLabel"), true),
                new ToggleWidgetSetting("dynamic_color", Text.translatable("widgets.widgets.tps.dynamicColor"), true)
        ));
        getSettings().optionById("textcolor").setShowCondition(() -> !this.dynamicColor);
        INSTANCE = this;
    }

    private static TPSWidget INSTANCE;

    private static final long[] lastUpdates = new long[5];
    private static int updatePointer = 0;
    private static int updatesSinceWorldChange = 0;

    private boolean dynamicColor = true;
    private boolean showLabel = true;

    public static void worldChanged(){
        updatesSinceWorldChange = 0;
    }

    public static void worldTimeUpdated(long nanoTime) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isInSingleplayer()){
            IntegratedServer server = client.getServer();
            if (server != null) {
                ServerTickManager tickManager = server.getTickManager();
                float tps = 1000f / server.getAverageTickTime();
                float targetTickRate = tickManager.getTickRate();
                if (tickManager.isSprinting()){
                    INSTANCE.updateTPS(tps, targetTickRate, true);
                } else {
                    INSTANCE.updateTPS(Math.min(tps, targetTickRate), targetTickRate, true);
                }
            }
        } else {
            updatesSinceWorldChange++;
            lastUpdates[updatePointer] = nanoTime;
            updatePointer++;
            if (updatePointer >= lastUpdates.length) updatePointer = 0;

            long totalDifference = 0;
            for (int i = 0; i < lastUpdates.length-1; i++){
                long difference = lastUpdates[(updatePointer + i + 1) % lastUpdates.length] - lastUpdates[(updatePointer + i) % lastUpdates.length];
                totalDifference += difference;
            }

            long avgDifference = totalDifference / (lastUpdates.length-1); // this is how long 20 ticks took on the server on average
            float mspt = avgDifference / 20000000f;
            float ticksPerSecond = 1000f / mspt;

            boolean loadingFinished = updatesSinceWorldChange > 5;
            if (client.world != null) {
                INSTANCE.updateTPS(ticksPerSecond, client.world.getTickManager().getTickRate(), loadingFinished);
            } else {
                INSTANCE.updateTPS(ticksPerSecond, 20, loadingFinished);
            }
        }
    }

    private void updateTPS(float tps, float targetTickRate, boolean loadingFinished) {
        if (!loadingFinished){
            if (showLabel){
                this.renderText = Text.literal(Text.translatable("widgets.widgets.tps.tps", "???").getString());
            } else {
                this.renderText = Text.literal("???");
            }
            if (dynamicColor) this.textColor = 0x00ff00;
        } else {
            tps = Math.round(tps * 10f) / 10f;
            if (showLabel){
                this.renderText = Text.literal(Text.translatable("widgets.widgets.tps.tps", tps).getString());
            } else {
                this.renderText = Text.literal(String.valueOf(tps));
            }
            if (dynamicColor){
                if (tps >= targetTickRate * 0.990){
                    this.textColor = 0x00ff00;
                } else if (tps >= targetTickRate * 0.740){
                    this.textColor = 0xffff00;
                } else {
                    this.textColor = 0xff0000;
                }
            }
        }
    }

    @Override
    public void tickWidget() {

    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.tps");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.tps.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.dynamicColor = ((ToggleWidgetSetting) settings.optionById("dynamic_color")).getValue();
        this.showLabel = ((ToggleWidgetSetting) settings.optionById("show_label")).getValue();
    }
}
