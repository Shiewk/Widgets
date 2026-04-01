package de.shiewk.widgets.client.screen.components;

import de.shiewk.widgets.ModWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

public class WidgetDisplayWidget extends AbstractWidget {

    protected final ModWidget widget;
    protected final Font textRenderer;
    protected final int centerX;
    protected final int centerY;

    public WidgetDisplayWidget(ModWidget widget, Font textRenderer, int centerX, int centerY) {
        super(0, 0, (int) widget.scaledWidth(), (int) widget.scaledHeight(), widget.getName());
        this.widget = widget;
        this.textRenderer = textRenderer;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    @Override
    public int getX() {
        return (int) (centerX - (widget.scaledWidth() / 2f));
    }

    @Override
    public int getY() {
        return (int) (centerY - (widget.scaledHeight() / 2f));
    }

    @Override
    public int getWidth() {
        return (int) widget.scaledWidth();
    }

    @Override
    public int getHeight() {
        return (int) widget.scaledHeight();
    }

    @Override
    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        widget.render(context, Util.getNanos(), textRenderer, getX(), getY());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.HINT, widget.getName());
    }
}
