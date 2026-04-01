package de.shiewk.widgets.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public final class HorizontalGradientGuiRenderState implements GuiElementRenderState {

    private final RenderPipeline pipeline;
    private final TextureSetup textureSetup;
    private final Matrix3x2f pose;
    private final ScreenRectangle scissorArea;
    private final ScreenRectangle bounds;

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
            ScreenRectangle scissorArea,
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
    public void buildVertices(VertexConsumer vertices) {
        vertices.addVertexWith2DPose(pose, x, y).setColor(colorLeft);
        vertices.addVertexWith2DPose(pose, x, endY).setColor(colorLeft);
        vertices.addVertexWith2DPose(pose, endX, endY).setColor(colorRight);
        vertices.addVertexWith2DPose(pose, endX, y).setColor(colorRight);
    }

    @Override
    public @NonNull RenderPipeline pipeline() {
        return pipeline;
    }

    @Override
    public @NonNull TextureSetup textureSetup() {
        return textureSetup;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return scissorArea;
    }

    @Nullable
    private static ScreenRectangle createBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle screenRect = (new ScreenRectangle(x0, y0, x1 - x0 + 1, y1 - y0)).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return bounds;
    }

    public static void draw(GuiGraphicsExtractor context, int x, int y, int endX, int endY, int colorLeft, int colorRight) {
        context.guiRenderState.addGuiElement(
                new HorizontalGradientGuiRenderState(
                        RenderPipelines.GUI,
                        TextureSetup.noTexture(),
                        new Matrix3x2f(context.pose()),
                        context.scissorStack.peek(),
                        x, y, endX, endY,
                        colorLeft, colorRight
                )
        );
    }
}
