package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static net.minecraft.network.chat.Component.translatable;

public class MemoryUsageWidget extends BasicTextWidget {

    private boolean showPercentage = true;
    private boolean showLabel = true;
    protected boolean realtime = false;

    public MemoryUsageWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("percentage", translatable("widgets.widgets.memory.showPercentage"), true),
                new ToggleWidgetSetting("label", translatable("widgets.widgets.common.showLabel"), true),
                new ToggleWidgetSetting("realtime", translatable("widgets.widgets.common.realtime"), false)
        ));
    }

    @Override
    public void renderScaled(GuiGraphicsExtractor context, long n, Font textRenderer, int posX, int posY) {
        if (realtime) refresh();
        super.renderScaled(context, n, textRenderer, posX, posY);
    }

    @Override
    public void tickWidget() {
        if (!realtime) refresh();
    }

    private void refresh() {
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
            formatAndSetRenderText(translatable("widgets.widgets.memory.withLabel", memUsageString).getString());
        } else {
            formatAndSetRenderText(memUsageString);
        }
    }

    private long mib(long bytes) {
        return bytes / 0x100000;
    }

    @Override
    public Component getName() {
        return translatable("widgets.widgets.memory");
    }

    @Override
    public Component getDescription() {
        return translatable("widgets.widgets.memory.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.showPercentage = (boolean) settings.optionById("percentage").getValue();
        this.showLabel = (boolean) settings.optionById("label").getValue();
        this.realtime = (boolean) settings.optionById("realtime").getValue();
    }
}
