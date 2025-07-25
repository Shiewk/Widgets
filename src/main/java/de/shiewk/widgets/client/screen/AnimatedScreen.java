package de.shiewk.widgets.client.screen;

import de.shiewk.widgets.WidgetUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;

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
            Matrix3x2fStack stack = context.getMatrices().pushMatrix();

            stack.translate((float) -translation, 0, stack);
            parent.render(context, (int) (mouseX + translation), mouseY, delta);
            stack.translate(this.width, 0, stack);
            mouseX -= (int) translation;
        }
        super.render(context, mouseX, mouseY, delta);
        this.renderScreenContents(context, mouseX, mouseY, delta);
        if (shouldAnimate){
            context.getMatrices().popMatrix();
        }
    }

    public abstract void renderScreenContents(DrawContext context, int mouseX, int mouseY, float delta);
}
