package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.widgets.settings.GradientWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Objects;

public class KeyStrokesWidget extends ResizableWidget {
    public KeyStrokesWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("showjump", Text.translatable("widgets.widgets.keystrokes.showJumpKey"), true),
                new GradientWidgetSetting("bgpressed", Text.translatable("widgets.widgets.keystrokes.colorBackgroundPressed"), 0x50ffffff),
                new GradientWidgetSetting("bgunpressed", Text.translatable("widgets.widgets.keystrokes.colorBackgroundUnpressed"), 0x50000000),
                new GradientWidgetSetting("keypressed", Text.translatable("widgets.widgets.keystrokes.colorKeyPressed"), 0xffffffff),
                new GradientWidgetSetting("keyunpressed", Text.translatable("widgets.widgets.keystrokes.colorKeyUnpressed"), 0xffffffff)
        ));
    }

    private GradientOptions colorBackgroundPressed, colorBackgroundUnpressed, colorKeyUnpressed, colorKeyPressed;
    private boolean showJumpKey = true;

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
    public void renderScaled(DrawContext context, long measuringTimeNano, TextRenderer textRenderer, int posX, int posY) {
        if (KEY_JUMP == null) return;
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX + 22, posY, KEY_FWD);
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX, posY + 22, KEY_LEFT);
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX + 22, posY + 22, KEY_BWD);
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX + 44, posY + 22, KEY_RIGHT);
        if (showJumpKey) renderSpaceBar(context, measuringTimeNano, posX, posY + 44, KEY_JUMP);
    }

    protected void renderSpaceBar(final DrawContext context, long mt, int posX, int posY, Key key){
        long l = mt - key.lastChanged;
        if (l < 100000000){
            double alpha = 0.00000001d * l;
            colorBackgroundUnpressed.multiplyAlpha(key.isPressed ? 1-alpha : alpha).fillHorizontal(context, mt, posX, posY, posX + 64, posY + 10);
            colorBackgroundPressed.multiplyAlpha(key.isPressed ? alpha : 1-alpha).fillHorizontal(context, mt, posX, posY, posX + 64, posY + 10);
        } else {
            (key.isPressed ? colorBackgroundPressed : colorBackgroundUnpressed).fillHorizontal(context, mt, posX, posY, posX + 64, posY + 10);
        }
        (key.isPressed ? colorKeyPressed : colorKeyUnpressed).fillHorizontal(context, mt, posX + 5, posY + 4, posX + 59, posY + 5);
    }

    protected void renderKeyStroke(DrawContext context, TextRenderer textRenderer, long mt, int posX, int posY, KeyLarge key){
        long l = mt - key.lastChanged;
        if (l < 100000000){
            double alpha = 0.00000001d * l;
            colorBackgroundUnpressed.multiplyAlpha(key.isPressed ? 1-alpha : alpha).fillHorizontal(context, mt, posX, posY, posX + 20, posY + 20);
            colorBackgroundPressed.multiplyAlpha(key.isPressed ? alpha : 1-alpha).fillHorizontal(context, mt, posX, posY, posX + 20, posY + 20);
        } else {
            (key.isPressed ? colorBackgroundPressed : colorBackgroundUnpressed).fillHorizontal(context, mt, posX, posY, posX + 20, posY + 20);
        }
        (key.isPressed ? colorKeyPressed : colorKeyUnpressed).drawText(context, textRenderer, mt, key.boundToKey, posX+10-(key.boundToLength/2), posY+6, true);
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
        return 64;
    }

    @Override
    public int height() {
        return showJumpKey ? 56 : 44;
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.showJumpKey = (boolean) settings.optionById("showjump").getValue();
        this.colorBackgroundPressed = (GradientOptions) settings.optionById("bgpressed").getValue();
        this.colorBackgroundUnpressed = (GradientOptions) settings.optionById("bgunpressed").getValue();
        this.colorKeyPressed = (GradientOptions) settings.optionById("keypressed").getValue();
        this.colorKeyUnpressed = (GradientOptions) settings.optionById("keyunpressed").getValue();
    }
}
