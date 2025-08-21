package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.utils.WidgetUtils;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;

import java.util.List;

import static net.minecraft.text.Text.literal;

public class PingWidget extends BasicTextWidget {

    public PingWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("dynamic_color", Text.translatable("widgets.widgets.ping.dynamicColor"), true),
                new ToggleWidgetSetting("hide_in_singleplayer", Text.translatable("widgets.widgets.common.hideInSingleplayer"), false)
        ));
        getSettings().optionById("textcolor").setShowCondition(() -> !this.dynamicColor && !this.rainbow);
        getSettings().optionById("rainbow").setShowCondition(() -> !this.dynamicColor);
        getSettings().optionById("rainbow_speed").setShowCondition(() -> !this.dynamicColor && this.rainbow);
    }

    private boolean dynamicColor = false;
    private boolean hideInSingleplayer = false;

    long lastPingQuery = 0;

    @Override
    public void tickWidget() {
        shouldRender = !(hideInSingleplayer && WidgetUtils.isInSingleplayer());
        if (!shouldRender) return;
        final ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler != null){
            if (lastPingQuery < Util.getMeasuringTimeMs() - 5000){
                networkHandler.pingMeasurer.ping();
                lastPingQuery = Util.getMeasuringTimeMs();
            }
            final MultiValueDebugSampleLogImpl pingLog = networkHandler.pingMeasurer.log;
            final int logLength = pingLog.getLength();
            final int avgCompileLength = 3;
            long ping = 0;
            int valuesRead = 0;
            for (int i = logLength-1; i > logLength-avgCompileLength-1; i--) {
                if (i < 0) break;
                ping += pingLog.get(i);
                valuesRead++;
            }
            if (valuesRead == 0){
                formatAndSetRenderText(literal("??? ms"));
                if (this.dynamicColor) this.textColor = 0xff00ff00;
                return;
            }
            long avgPing = ping / valuesRead;
            formatAndSetRenderText(literal(avgPing + " ms"));
            if (this.dynamicColor){
                if (avgPing < 50){
                    this.textColor = 0xff00ff00;
                } else if (avgPing < 120) {
                    this.textColor = 0xffffff00;
                } else {
                    this.textColor = 0xffff3030;
                }
            }
        }
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.dynamicColor = ((ToggleWidgetSetting) settings.optionById("dynamic_color")).getValue();
        this.hideInSingleplayer = ((ToggleWidgetSetting) settings.optionById("hide_in_singleplayer")).getValue();
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.ping");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.ping.description");
    }
}
