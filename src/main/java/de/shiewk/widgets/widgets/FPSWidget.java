package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

import static net.minecraft.text.Text.literal;
import static net.minecraft.text.Text.translatable;

public class FPSWidget extends BasicTextWidget {

    protected boolean realtime = false;
    protected LinkedList<Long> timedFrames = null;

    public FPSWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("realtime", translatable("widgets.widgets.common.realtime"), false)
        ));
    }

    @Override
    public void renderScaled(DrawContext context, long n, TextRenderer textRenderer, int posX, int posY) {
        if (realtime){
            timedFrames.add(n);
            while (timedFrames.getFirst() < n - 500_000_100L){
                timedFrames.removeFirst();
            }
            formatAndSetRenderText(literal(timedFrames.size() * 2 + " FPS"));
        }
        super.renderScaled(context, n, textRenderer, posX, posY);
    }

    @Override
    public void tickWidget() {
        if (!realtime){
            formatAndSetRenderText(literal(MinecraftClient.getInstance().getCurrentFps() + " FPS"));
        }
    }

    @Override
    public Text getName() {
        return translatable("widgets.widgets.fps");
    }

    @Override
    public Text getDescription() {
        return translatable("widgets.widgets.fps.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.realtime = ((ToggleWidgetSetting) settings.optionById("realtime")).getValue();

        timedFrames = this.realtime ? new LinkedList<>() : null;
    }
}
