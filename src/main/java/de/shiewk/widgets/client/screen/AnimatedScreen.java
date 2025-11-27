package de.shiewk.widgets.client.screen;

import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public abstract class AnimatedScreen extends Screen {
    protected final Screen parent;
    private final int animationDurationMs;
    private final long creationTime = Util.getMeasuringTimeNano();
    protected AnimatedScreen(Text title, Screen parent, int animationDurationMs) {
        super(title);
        this.parent = parent;
        this.animationDurationMs = animationDurationMs;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        double timeMs = (Util.getMeasuringTimeNano() - creationTime) / 1000000d;
        final boolean shouldAnimate = timeMs < animationDurationMs;
        if (shouldAnimate){
            double translation = WidgetUtils.computeEasing(timeMs / animationDurationMs) * this.width;
            MatrixStack stack = context.getMatrices();
            stack.push();

            stack.translate((float) -translation, 0, 0);
            parent.render(context, -67, -67, delta);
            stack.translate(this.width, 0, 0);
            mouseX -= (int) translation;
        }
        super.render(context, mouseX, mouseY, delta);
        this.renderScreenContents(context, mouseX, mouseY, delta);
        if (shouldAnimate){
            context.getMatrices().pop();
        }
    }

    public abstract void renderScreenContents(DrawContext context, int mouseX, int mouseY, float delta);
}
