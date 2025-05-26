package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.WidgetUtils;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;

import java.util.List;
import java.util.function.LongFunction;

public class BandwidthWidget extends BasicTextWidget {

    public enum Unit {
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
                new ToggleWidgetSetting("dynamic_color", Text.translatable("widgets.widgets.bandwidth.dynamicColor"), true),
                new ToggleWidgetSetting("hide_in_singleplayer", Text.translatable("widgets.widgets.common.hideInSingleplayer"), false),
                new EnumWidgetSetting<>("unit", Text.translatable("widgets.widgets.bandwidth.unit"), Unit.class, Unit.KB, unit -> Text.literal(unit.name))
        ));
        getSettings().optionById("textcolor").setShowCondition(() -> !this.dynamicColor);
    }

    private int t = 0;
    private boolean dynamicColor = false;
    private boolean hideInSingleplayer = false;
    private Unit unit = Unit.KB;

    @Override
    public void tickWidget() {
        shouldRender = !(hideInSingleplayer && WidgetUtils.isInSingleplayer());
        if (!shouldRender) return;
        float tickRate = WidgetUtils.getClientTickRate();
        t++;
        if (t >= tickRate){
            t = 0;
            long avgBytesPerSecond = getAvgBytesPerSecond(MinecraftClient.getInstance(), tickRate);
            this.renderText = Text.of(unit.sizeFormatter.apply(avgBytesPerSecond));
            if (this.dynamicColor){
                if (avgBytesPerSecond < 100000){
                    this.textColor = 0x00ff00;
                } else if (avgBytesPerSecond < 750000) {
                    this.textColor = 0xffff00;
                } else {
                    this.textColor = 0xff3030;
                }
            }
        }
    }

    private static long getAvgBytesPerSecond(MinecraftClient client, float tickRate) {
        final MultiValueDebugSampleLogImpl packetSizeLog = client.getDebugHud().getPacketSizeLog();
        final int logLength = packetSizeLog.getLength();
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
        this.dynamicColor = ((ToggleWidgetSetting) settings.optionById("dynamic_color")).getValue();
        this.hideInSingleplayer = ((ToggleWidgetSetting) settings.optionById("hide_in_singleplayer")).getValue();
        this.unit = (Unit) ((EnumWidgetSetting<?>) settings.optionById("unit")).getValue();
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.bandwidth");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.bandwidth.description");
    }
}
