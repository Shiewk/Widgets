package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.utils.WidgetUtils;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.debugchart.LocalSampleLogger;
import java.util.List;

public class PingWidget extends BasicTextWidget {

    public PingWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("dynamic_color", Component.translatable("widgets.widgets.ping.dynamicColor"), true),
                new ToggleWidgetSetting("hide_in_singleplayer", Component.translatable("widgets.widgets.common.hideInSingleplayer"), false)
        ));
        getSettings().optionById("textcolor").setShowCondition(() -> !this.dynamicColor);
    }

    private boolean dynamicColor = false;
    private boolean hideInSingleplayer = false;

    long lastPingQuery = 0;

    @Override
    public void tickWidget() {
        shouldRender = !(hideInSingleplayer && WidgetUtils.isInSingleplayer());
        if (!shouldRender) return;
        final ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
        if (networkHandler != null){
            if (lastPingQuery < Util.getMillis() - 5000){
                networkHandler.pingDebugMonitor.tick();
                lastPingQuery = Util.getMillis();
            }
            final LocalSampleLogger pingLog = networkHandler.pingDebugMonitor.delayTimer;
            final int logLength = pingLog.size();
            final int avgCompileLength = 3;
            long ping = 0;
            int valuesRead = 0;
            for (int i = logLength-1; i > logLength-avgCompileLength-1; i--) {
                if (i < 0) break;
                ping += pingLog.get(i);
                valuesRead++;
            }
            if (valuesRead == 0){
                formatAndSetRenderText("??? ms");
                if (this.dynamicColor) this.textColor = GradientOptions.solidColor(0xff00ff00);
                return;
            }
            long avgPing = ping / valuesRead;
            formatAndSetRenderText(avgPing + " ms");
            if (this.dynamicColor){
                if (avgPing < 50){
                    this.textColor = GradientOptions.solidColor(0xff00ff00);
                } else if (avgPing < 120) {
                    this.textColor = GradientOptions.solidColor(0xffffff00);
                } else {
                    this.textColor = GradientOptions.solidColor(0xffff3030);
                }
            }
        }
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.dynamicColor = (boolean) settings.optionById("dynamic_color").getValue();
        this.hideInSingleplayer = (boolean) settings.optionById("hide_in_singleplayer").getValue();
    }

    @Override
    public Component getName() {
        return Component.translatable("widgets.widgets.ping");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("widgets.widgets.ping.description");
    }
}
