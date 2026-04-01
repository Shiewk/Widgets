package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.utils.WidgetUtils;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import java.util.List;
import java.util.function.LongFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.debugchart.LocalSampleLogger;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public class BandwidthWidget extends BasicTextWidget {

    public enum Unit {
        @SuppressWarnings("IntegerDivisionInFloatingPointContext")
        KB("kB", bytes -> {
            if (bytes > 1000) {
                double kB = bytes / 100 / 10d;
                return kB + " kB/s";
            } else {
                return bytes + " B/s";
            }
        }),
        KiB("KiB", bytes -> {
            if (bytes > 1000) {
                double kB = Math.round(bytes / 102.4) / 10d;
                return kB + " KiB/s";
            } else {
                return bytes + " B/s";
            }
        });

        public final String name;
        public final LongFunction<String> sizeFormatter;

        Unit(String name, LongFunction<String> sizeFormatter) {
            this.name = name;
            this.sizeFormatter = sizeFormatter;
        }
    }

    public BandwidthWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("dynamic_color", Component.translatable("widgets.widgets.bandwidth.dynamicColor"), true),
                new ToggleWidgetSetting("hide_in_singleplayer", Component.translatable("widgets.widgets.common.hideInSingleplayer"), false),
                new EnumWidgetSetting<>("unit", Component.translatable("widgets.widgets.bandwidth.unit"), Unit.class, Unit.KB, unit -> literal(unit.name)),
                new ToggleWidgetSetting("fastupdate", translatable("widgets.widgets.bandwidth.fastupdate"), false)
        ));
        getSettings().optionById("textcolor").setShowCondition(() -> !this.dynamicColor);
    }

    private int t = 0;
    private boolean dynamicColor = false;
    private boolean hideInSingleplayer = false;
    private Unit unit = Unit.KB;
    protected boolean fastUpdate = false;

    @Override
    public void tickWidget() {
        shouldRender = !(hideInSingleplayer && WidgetUtils.isInSingleplayer());
        if (!shouldRender) return;
        float tickRate = WidgetUtils.getClientTickRate();
        t++;
        if (t >= tickRate || fastUpdate){
            t = 0;
            long avgBytesPerSecond = getAvgBytesPerSecond(Minecraft.getInstance(), tickRate);
            formatAndSetRenderText(unit.sizeFormatter.apply(avgBytesPerSecond));
            if (this.dynamicColor){
                if (avgBytesPerSecond < 100000){
                    this.textColor = GradientOptions.solidColor(0xff00ff00);
                } else if (avgBytesPerSecond < 750000) {
                    this.textColor = GradientOptions.solidColor(0xffffff00);
                } else {
                    this.textColor = GradientOptions.solidColor(0xffff3030);
                }
            }
        }
    }

    private static long getAvgBytesPerSecond(Minecraft client, float tickRate) {
        final LocalSampleLogger packetSizeLog = client.getDebugOverlay().getBandwidthLogger();
        final int logLength = packetSizeLog.size();
        final int avgCompileLength = (int) (3 * tickRate);
        long size = 0;
        for (int i = logLength-1; i > logLength-avgCompileLength; i--) {
            if (i < 0) break;
            size += packetSizeLog.get(i);
        }
        return (long) ((float) size / avgCompileLength * tickRate);
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.dynamicColor = (boolean) settings.optionById("dynamic_color").getValue();
        this.hideInSingleplayer = (boolean) settings.optionById("hide_in_singleplayer").getValue();
        this.unit = (Unit) settings.optionById("unit").getValue();
        this.fastUpdate = (boolean) settings.optionById("fastupdate").getValue();
    }

    @Override
    public Component getName() {
        return Component.translatable("widgets.widgets.bandwidth");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("widgets.widgets.bandwidth.description");
    }
}
