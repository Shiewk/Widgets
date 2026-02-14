package de.shiewk.widgets.client.screen.components;

import de.shiewk.widgets.ModWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.Util;

public class WidgetDisplayWidget extends ClickableWidget {

    protected final ModWidget widget;
    protected final TextRenderer textRenderer;
    protected final int centerX;
    protected final int centerY;

    public WidgetDisplayWidget(ModWidget widget, TextRenderer textRenderer, int centerX, int centerY) {
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
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        widget.render(context, Util.getMeasuringTimeNano(), textRenderer, getX(), getY());
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.HINT, widget.getName());
    }
}
