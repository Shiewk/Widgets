package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static net.minecraft.network.chat.Component.translatable;

public class FPSWidget extends BasicTextWidget {

    protected boolean realtime = false;
    protected LinkedList<Long> timedFrames = null;

    public FPSWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("realtime", translatable("widgets.widgets.common.realtime"), false)
        ));
    }

    @Override
    public void renderScaled(GuiGraphicsExtractor context, long n, Font textRenderer, int posX, int posY) {
        if (realtime){
            timedFrames.add(n);
            while (timedFrames.getFirst() < n - 500_000_100L){
                timedFrames.removeFirst();
            }
            formatAndSetRenderText(timedFrames.size() * 2 + " FPS");
        }
        super.renderScaled(context, n, textRenderer, posX, posY);
    }

    @Override
    public void tickWidget() {
        if (!realtime){
            formatAndSetRenderText(Minecraft.getInstance().getFps() + " FPS");
        }
    }

    @Override
    public Component getName() {
        return translatable("widgets.widgets.fps");
    }

    @Override
    public Component getDescription() {
        return translatable("widgets.widgets.fps.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.realtime = (boolean) settings.optionById("realtime").getValue();

        timedFrames = this.realtime ? new LinkedList<>() : null;
    }
}
