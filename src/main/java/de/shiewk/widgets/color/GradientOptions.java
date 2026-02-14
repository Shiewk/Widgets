package de.shiewk.widgets.color;

import de.shiewk.widgets.render.state.HorizontalGradientGuiRenderState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.util.Objects;

import static de.shiewk.widgets.utils.WidgetUtils.fadeColor;

public record GradientOptions(GradientMode mode, float gradientSize, float gradientSpeed, int[] colors) {

    public GradientOptions(GradientMode mode, float gradientSize, float gradientSpeed, int[] colors) {
        this.mode = Objects.requireNonNullElse(mode, GradientMode.SWEEP);
        this.gradientSize = gradientSize;
        this.gradientSpeed = gradientSpeed;
        this.colors = Objects.requireNonNullElse(colors, new int[]{ -1 });
    }

    public GradientOptions(float gradientSize, float gradientSpeed, int[] colors) {
        this(GradientMode.SWEEP, gradientSize, gradientSpeed, colors);
    }

    public void fillHorizontal(DrawContext context, long timeNanos, int x, int y, int endX, int endY) {
        if (colors.length == 1) {
            context.fill(x, y, endX, endY, colors[0]);
            return;
        }
        switch (mode){
            case SWEEP -> fillHorizonalSweep(context, timeNanos, x, y, endX, endY);
            case PULSE -> fillHorizonalPulse(context, timeNanos, x, y, endX, endY);
        }
    }

    private void fillHorizonalSweep(DrawContext context, long timeNanos, int x, int y, int endX, int endY) {
        context.enableScissor(x, y, endX, endY);

        int width = endX - x;
        float sizeFactor = gradientSize / 100f;
        int partSize = Math.max(1, Math.round(width * sizeFactor));

        int totalGradientCycle = colors.length * partSize;

        float speedPxPerSec = width * (gradientSpeed / 100f);
        float timeSeconds = timeNanos / 1_000_000_000f;
        float offset = gradientSpeed == 0 ? 0 : (timeSeconds * speedPxPerSec + x) % totalGradientCycle;

        Matrix3x2fStack matrices = context.getMatrices().pushMatrix();
        matrices.translate(-offset, 0);

        int currentPos = 0;
        int colorIndex = 0;

        while (currentPos < width + totalGradientCycle) {
            int color1 = colors[colorIndex % colors.length];
            int color2 = colors[(colorIndex + 1) % colors.length];

            HorizontalGradientGuiRenderState.draw(
                    context, x + currentPos, y, x + currentPos + partSize, endY,
                    color1, color2
            );

            currentPos += partSize;
            colorIndex++;
        }

        matrices.popMatrix();
        context.disableScissor();
    }

    private void fillHorizonalPulse(DrawContext context, long timeNanos, int x, int y, int endX, int endY) {
        context.fill(x, y, endX, endY, getCurrentPulseColor(timeNanos));
    }

    private int getCurrentPulseColor(long timeNanos) {
        float progress = timeNanos / 1_000_000_000f * gradientSpeed / gradientSize;
        int color1 = colors[(int) (progress % colors.length)];
        int color2 = colors[(int) ((progress + 1) % colors.length)];
        float delta = progress % 1;
        return fadeColor(color1, color2, delta);
    }

    public void drawHorizontalLine(DrawContext context, long mt, int posX, int endX, int posY) {
        this.fillHorizontal(context, mt, posX, posY, endX, posY + 1);
    }

    public void drawText(DrawContext context, TextRenderer textRenderer, long timeNanos, String displayText, int x, int y, boolean shadow) {
        if (colors.length == 1){
            context.drawText(textRenderer, displayText, x, y, colors[0], shadow);
        } else {
            switch (mode){
                case SWEEP -> drawTextSweep(context, textRenderer, timeNanos, displayText, x, y, shadow);
                case PULSE -> drawTextPulse(context, textRenderer, timeNanos, displayText, x, y, shadow);
            }
        }
    }

    public void drawText(DrawContext context, TextRenderer textRenderer, long timeNanos, Text displayText, int x, int y, boolean shadow) {
        if (colors.length == 1){
            context.drawText(textRenderer, displayText, x, y, colors[0], shadow);
        }
        this.drawText(context, textRenderer, timeNanos, displayText.getString(), x, y, shadow);
    }

    private void drawTextSweep(DrawContext context, TextRenderer textRenderer, long timeNanos, String displayText, int x, int y, boolean shadow) {
        int pos = 0;
        for (int i = 0; i < displayText.length(); i++) {
            String s = String.valueOf(displayText.charAt(i));
            int w = textRenderer.getWidth(s);
            int col = computeSweepTextColorAt(gradientSpeed == 0 ? pos : pos + x, timeNanos);
            context.drawText(textRenderer, s, x +pos, y, col, shadow);
            pos += w;
        }
    }

    private int computeSweepTextColorAt(float pos, long timeNanos) {
        pos += timeNanos / 500_000_000f * gradientSpeed;

        float partSize = gradientSize * 2;
        int part = (int) (pos / partSize);
        double off = pos % partSize / partSize;

        int color1 = colors[part % colors.length];
        int color2 = colors[(part+1) % colors.length];

        return fadeColor(color1, color2, off);
    }

    private void drawTextPulse(DrawContext context, TextRenderer textRenderer, long timeNanos, String displayText, int x, int y, boolean shadow) {
        context.drawText(textRenderer, displayText, x, y, getCurrentPulseColor(timeNanos), shadow);
    }

    public static GradientOptions solidColor(int color) {
        return new GradientOptions(1, 0, new int[]{color});
    }

    public void drawVerticalLine(DrawContext context, long timeNanos, int posX, int posY, int endY) {
        fillHorizontal(context, timeNanos, posX, posY, posX + 1, endY);
    }

    public GradientOptions multiplyAlpha(double v) {
        int[] newColors = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            int color = colors[i];
            int rgb = color & 0xffffff;
            int newAlpha = (int) (((color & 0xff000000) >>> 24) * v);
            newColors[i] = (newAlpha << 24) | rgb;
        }
        return new GradientOptions(mode, gradientSize, gradientSpeed, newColors);
    }
}