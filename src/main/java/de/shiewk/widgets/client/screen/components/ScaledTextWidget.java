package de.shiewk.widgets.client.screen.components;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ScaledTextWidget extends ClickableWidget {

    private final TextRenderer textRenderer;
    private final float scale;

    public ScaledTextWidget(int x, int y, Text message, TextRenderer textRenderer, float scale) {
        super(x, y, 0, (int) (textRenderer.fontHeight * scale), message);
        this.textRenderer = textRenderer;
        this.scale = scale;
    }

    @Override
    public int getWidth() {
        return (int) (textRenderer.getWidth(getMessage()) * scale);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        MatrixStack stack = context.getMatrices();
        stack.push();
        stack.scale(scale, scale, 1);
        context.drawText(textRenderer, getMessage(), (int) (getX() / scale), (int) (getY() / scale), 0xffffffff, true);
        stack.pop();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
