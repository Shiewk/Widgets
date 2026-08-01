package de.shiewk.widgets.screen;

import de.shiewk.widgets.utils.WidgetUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

public abstract class AnimatedScreen extends Screen {
    protected final Screen parent;
    private final int animationDurationMs;
    private final long creationTime = Util.getNanos();
    protected AnimatedScreen(Component title, Screen parent, int animationDurationMs) {
        super(title);
        this.parent = parent;
        this.animationDurationMs = animationDurationMs;
    }

    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        double timeMs = (Util.getNanos() - creationTime) / 1000000d;
        final boolean shouldAnimate = timeMs < animationDurationMs;
        if (shouldAnimate){
            double translation = WidgetUtils.computeEasing(timeMs / animationDurationMs) * this.width;
            Matrix3x2fStack stack = context.pose().pushMatrix();

            stack.translate((float) -translation, 0, stack);
            parent.render(context, -67, -67, delta);
            stack.translate(this.width, 0, stack);
            mouseX -= (int) translation;
        }
        super.render(context, mouseX, mouseY, delta);
        this.renderScreenContents(context, mouseX, mouseY, delta);
        if (shouldAnimate){
            context.pose().popMatrix();
        }
    }

    public abstract void renderScreenContents(GuiGraphics context, int mouseX, int mouseY, float delta);
}
