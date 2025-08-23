package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static net.minecraft.text.Text.literal;
import static net.minecraft.text.Text.translatable;

public class MemoryUsageWidget extends BasicTextWidget {

    private boolean showPercentage = true;
    private boolean showLabel = true;

    public MemoryUsageWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("percentage", translatable("widgets.widgets.memory.showPercentage"), true),
                new ToggleWidgetSetting("label", translatable("widgets.widgets.common.showLabel"), true)
        ));
    }

    @Override
    public void tickWidget() {
        Runtime runtime = Runtime.getRuntime();
        long memTotal = runtime.maxMemory();
        long memAllocated = runtime.totalMemory();
        long memFree = runtime.freeMemory();
        long memUsed = memAllocated - memFree;
        short memUsagePercent = (short) (((float) memUsed / memTotal) * 100);
        String memUsageString = showPercentage ?
                mib(memUsed) + "MiB / " + mib(memTotal) + "MiB (" + memUsagePercent + "%)" :
                mib(memUsed) + "MiB / " + mib(memTotal) + "MiB";
        if (showLabel){
            formatAndSetRenderText(literal(translatable("widgets.widgets.memory.withLabel", memUsageString).getString()));
        } else {
            formatAndSetRenderText(literal(memUsageString));
        }

    }

    private long mib(long bytes) {
        return bytes / 0x100000;
    }

    @Override
    public Text getName() {
        return translatable("widgets.widgets.memory");
    }

    @Override
    public Text getDescription() {
        return translatable("widgets.widgets.memory.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        showPercentage = ((ToggleWidgetSetting) settings.optionById("percentage")).getValue();
        showLabel = ((ToggleWidgetSetting) settings.optionById("label")).getValue();
    }
}
