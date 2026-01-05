package de.shiewk.widgets.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

@Environment(EnvType.CLIENT)
public final class HorizontalGradientGuiRenderState implements SimpleGuiElementRenderState {

    private final RenderPipeline pipeline;
    private final TextureSetup textureSetup;
    private final Matrix3x2f pose;
    private final ScreenRect scissorArea;
    private final ScreenRect bounds;

    private final int x;
    private final int y;

    private final int endX;
    private final int endY;

    private final int colorLeft;
    private final int colorRight;

    private HorizontalGradientGuiRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            ScreenRect scissorArea,
            int x,
            int y,
            int endX,
            int endY, int colorLeft, int colorRight
    ) {
        this.pipeline = pipeline;
        this.textureSetup = textureSetup;
        this.pose = pose;
        this.scissorArea = scissorArea;
        this.x = x;
        this.y = y;
        this.endX = endX;
        this.endY = endY;
        this.colorLeft = colorLeft;
        this.colorRight = colorRight;
        this.bounds = createBounds(x, y, endX, endY, pose, scissorArea);
    }

    @Override
    public void setupVertices(VertexConsumer vertices) {
        vertices.vertex(pose, x, y).color(colorLeft);
        vertices.vertex(pose, x, endY).color(colorLeft);
        vertices.vertex(pose, endX, endY).color(colorRight);
        vertices.vertex(pose, endX, y).color(colorRight);
    }

    @Override
    public RenderPipeline pipeline() {
        return pipeline;
    }

    @Override
    public TextureSetup textureSetup() {
        return textureSetup;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return scissorArea;
    }

    @Nullable
    private static ScreenRect createBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRect scissorArea) {
        ScreenRect screenRect = (new ScreenRect(x0, y0, x1 - x0 + 1, y1 - y0)).transformEachVertex(pose);
        return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
    }

    @Override
    public @Nullable ScreenRect bounds() {
        return bounds;
    }

    public static void draw(DrawContext context, int x, int y, int endX, int endY, int colorLeft, int colorRight) {
        context.state.addSimpleElement(
                new HorizontalGradientGuiRenderState(
                        RenderPipelines.GUI,
                        TextureSetup.empty(),
                        new Matrix3x2f(context.getMatrices()),
                        context.scissorStack.peekLast(),
                        x, y, endX, endY,
                        colorLeft, colorRight
                )
        );
    }
}
