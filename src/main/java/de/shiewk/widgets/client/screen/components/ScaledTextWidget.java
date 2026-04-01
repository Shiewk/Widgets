package de.shiewk.widgets.client.screen.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

public class ScaledTextWidget extends AbstractWidget {

    private final Font textRenderer;
    private final float scale;

    public ScaledTextWidget(int x, int y, Component message, Font textRenderer, float scale) {
        super(x, y, 0, (int) (textRenderer.lineHeight * scale), message);
        this.textRenderer = textRenderer;
        this.scale = scale;
    }

    @Override
    public int getWidth() {
        return (int) (textRenderer.width(getMessage()) * scale);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        Matrix3x2fStack stack = context.pose().pushMatrix();
        stack.scale(scale);
        context.text(textRenderer, getMessage(), (int) (getX() / scale), (int) (getY() / scale), 0xffffffff, true);
        stack.popMatrix();
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput builder) {}

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        return false;
    }
}
