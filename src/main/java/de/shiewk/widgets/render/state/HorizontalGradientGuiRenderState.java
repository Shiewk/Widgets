package de.shiewk.widgets.render.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public final class HorizontalGradientGuiRenderState {

    public static void draw(DrawContext context, int x, int y, int endX, int endY, int colorLeft, int colorRight) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, endY, 0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90));
        context.fillGradient(0, 0, endY - y, endX - x, colorLeft, colorRight);
        matrices.pop();
    }

}
