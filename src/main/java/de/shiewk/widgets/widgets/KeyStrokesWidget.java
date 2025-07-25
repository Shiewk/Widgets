package de.shiewk.widgets.widgets;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.RGBAColorWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class KeyStrokesWidget extends ModWidget {
    public KeyStrokesWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("showjump", Text.translatable("widgets.widgets.keystrokes.showJumpKey"), true),
                new IntSliderWidgetSetting("size", Text.translatable("widgets.widgets.common.sizePercent"), 25, 150, 400),
                new RGBAColorWidgetSetting("bgpressed", Text.translatable("widgets.widgets.keystrokes.colorBackgroundPressed"), 255, 255, 255, 80),
                new RGBAColorWidgetSetting("bgunpressed", Text.translatable("widgets.widgets.keystrokes.colorBackgroundUnpressed"), 0, 0, 0, 80),
                new RGBAColorWidgetSetting("keypressed", Text.translatable("widgets.widgets.keystrokes.colorKeyPressed"), 255, 255, 255, 255),
                new RGBAColorWidgetSetting("keyunpressed", Text.translatable("widgets.widgets.keystrokes.colorKeyUnpressed"), 255, 255, 255, 255)
        ));
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        showJumpKey = ((ToggleWidgetSetting) settings.optionById("showjump")).getValue();
        colorBackgroundPressed = ((RGBAColorWidgetSetting) settings.optionById("bgpressed")).getColor();
        colorBackgroundUnpressed = ((RGBAColorWidgetSetting) settings.optionById("bgunpressed")).getColor();
        colorKeyPressed = ((RGBAColorWidgetSetting) settings.optionById("keypressed")).getColor();
        colorKeyUnpressed = ((RGBAColorWidgetSetting) settings.optionById("keyunpressed")).getColor();
        size = 0.01f * ((IntSliderWidgetSetting) settings.optionById("size")).getValue();
    }

    private boolean showJumpKey = true;

    private int colorBackgroundPressed = new Color(255, 255, 255, 80).getRGB(),
            colorBackgroundUnpressed = new Color(0, 0, 0, 80).getRGB(),
            colorKeyUnpressed = 0xffffffff,
            colorKeyPressed = 0xffffffff;

    protected float size = 2;

    protected static class Key {
        protected final KeyBinding binding;
        protected boolean isPressed;
        protected long lastChanged;

        private Key(KeyBinding binding) {
            Objects.requireNonNull(binding);
            this.binding = binding;
        }
    }

    protected static class KeyLarge extends Key {
        protected String boundToKey;
        protected int boundToLength;

        private KeyLarge(KeyBinding binding) {
            super(binding);
        }
    }

    private KeyLarge KEY_FWD, KEY_BWD, KEY_LEFT, KEY_RIGHT;
    private Key KEY_JUMP;

    @Override
    public void render(DrawContext context, long measuringTimeNano, TextRenderer textRenderer, int posX, int posY) {
        if (KEY_JUMP == null) return;
        Matrix3x2fStack matrices = context.getMatrices();
        if (size != 1) {
            matrices.pushMatrix();
            matrices.translate(-(size-1) * posX, -(size-1) * posY, matrices);
            matrices.scale(size, size, matrices);
        }
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX + 22, posY, KEY_FWD);
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX, posY + 22, KEY_LEFT);
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX + 22, posY + 22, KEY_BWD);
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX + 44, posY + 22, KEY_RIGHT);
        if (showJumpKey) renderSpaceBar(context, measuringTimeNano, posX, posY + 44, KEY_JUMP);
        if (size != 1){
            matrices.popMatrix();
        }
    }

    protected void renderSpaceBar(final DrawContext context,
                                  final long measuringTimeNano,
                                  final int posX,
                                  final int posY,
                                  final Key key){
        long l = measuringTimeNano - key.lastChanged;
        if (l < 100000000){
            if (key.isPressed){
                context.fill(posX, posY, posX + 64, posY + 10, fadeColor(colorBackgroundUnpressed, colorBackgroundPressed, 0.00000001d * l));
            } else {
                context.fill(posX, posY, posX + 64, posY + 10, fadeColor(colorBackgroundPressed, colorBackgroundUnpressed, 0.00000001d * l));
            }
        } else {
            context.fill(posX, posY, posX + 64, posY + 10, key.isPressed ? colorBackgroundPressed : colorBackgroundUnpressed);
        }
        context.fill(posX + 5, posY + 4, posX + 59, posY + 5, key.isPressed ? colorKeyPressed : colorKeyUnpressed);
    }

    protected void renderKeyStroke(final DrawContext context,
                                   final TextRenderer textRenderer,
                                   final long measuringTimeNano,
                                   final int posX,
                                   final int posY,
                                   final KeyLarge key){
        long l = measuringTimeNano - key.lastChanged;
        if (l < 100000000){
            if (key.isPressed){
                context.fill(posX, posY, posX+20, posY+20, fadeColor(colorBackgroundUnpressed, colorBackgroundPressed, 0.00000001d * l));
            } else {
                context.fill(posX, posY, posX+20, posY+20, fadeColor(colorBackgroundPressed, colorBackgroundUnpressed, 0.00000001d * l));
            }
        } else {
            context.fill(posX, posY, posX+20, posY+20, key.isPressed ? colorBackgroundPressed : colorBackgroundUnpressed);
        }
        context.drawText(textRenderer, key.boundToKey, posX+10-(key.boundToLength/2), posY + 6, key.isPressed ? colorKeyPressed : colorKeyUnpressed, true);
    }

    private int fadeColor(int color1, int color2, double delta) {
        int alpha = (int) MathHelper.lerp(delta, (color1 >> 24) & 0xff, (color2 >> 24) & 0xff);
        int red = (int) MathHelper.lerp(delta, (color1 >> 16) & 0xff, (color2 >> 16) & 0xff);
        int green = (int) MathHelper.lerp(delta, (color1 >> 8) & 0xff, (color2 >> 8) & 0xff);
        int blue = (int) MathHelper.lerp(delta, color1 & 0xff, color2 & 0xff);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @Override
    public void tick() {
        if (KEY_FWD == null) KEY_FWD = new KeyLarge(MinecraftClient.getInstance().options.forwardKey);
        if (KEY_BWD == null) KEY_BWD = new KeyLarge(MinecraftClient.getInstance().options.backKey);
        if (KEY_LEFT == null) KEY_LEFT = new KeyLarge(MinecraftClient.getInstance().options.leftKey);
        if (KEY_RIGHT == null) KEY_RIGHT = new KeyLarge(MinecraftClient.getInstance().options.rightKey);
        if (KEY_JUMP == null) KEY_JUMP = new Key(MinecraftClient.getInstance().options.jumpKey);
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        for (Key key : new Key[]{KEY_FWD, KEY_BWD, KEY_LEFT, KEY_RIGHT, KEY_JUMP}){
            if (key instanceof KeyLarge keyLarge){
                keyLarge.boundToKey = key.binding.getBoundKeyLocalizedText().getString();
                keyLarge.boundToLength = renderer.getWidth(keyLarge.boundToKey);
            }
            final boolean pressed = key.binding.isPressed();
            if (pressed != key.isPressed){
                key.isPressed = pressed;
                key.lastChanged = Util.getMeasuringTimeNano();
            }
        }
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.keystrokes");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.keystrokes.description");
    }

    @Override
    public int width() {
        return (int) (64 * size);
    }

    @Override
    public int height() {
        return (int) ((showJumpKey ? 56 : 44) * size);
    }
}
