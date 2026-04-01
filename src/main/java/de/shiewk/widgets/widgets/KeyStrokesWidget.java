package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.widgets.settings.GradientWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Objects;

public class KeyStrokesWidget extends ResizableWidget {
    public KeyStrokesWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("showjump", Component.translatable("widgets.widgets.keystrokes.showJumpKey"), true),
                new GradientWidgetSetting("bgpressed", Component.translatable("widgets.widgets.keystrokes.colorBackgroundPressed"), 0x50ffffff),
                new GradientWidgetSetting("bgunpressed", Component.translatable("widgets.widgets.keystrokes.colorBackgroundUnpressed"), 0x50000000),
                new GradientWidgetSetting("keypressed", Component.translatable("widgets.widgets.keystrokes.colorKeyPressed"), 0xffffffff),
                new GradientWidgetSetting("keyunpressed", Component.translatable("widgets.widgets.keystrokes.colorKeyUnpressed"), 0xffffffff)
        ));
    }

    private GradientOptions colorBackgroundPressed, colorBackgroundUnpressed, colorKeyUnpressed, colorKeyPressed;
    private boolean showJumpKey = true;

    protected static class Key {
        protected final KeyMapping binding;
        protected boolean isPressed;
        protected long lastChanged;

        private Key(KeyMapping binding) {
            Objects.requireNonNull(binding);
            this.binding = binding;
        }
    }

    protected static class KeyLarge extends Key {
        protected String boundToKey;
        protected int boundToLength;

        private KeyLarge(KeyMapping binding) {
            super(binding);
        }
    }

    private KeyLarge KEY_FWD, KEY_BWD, KEY_LEFT, KEY_RIGHT;
    private Key KEY_JUMP;

    @Override
    public void renderScaled(GuiGraphicsExtractor context, long measuringTimeNano, Font textRenderer, int posX, int posY) {
        if (KEY_JUMP == null) return;
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX + 22, posY, KEY_FWD);
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX, posY + 22, KEY_LEFT);
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX + 22, posY + 22, KEY_BWD);
        renderKeyStroke(context, textRenderer, measuringTimeNano, posX + 44, posY + 22, KEY_RIGHT);
        if (showJumpKey) renderSpaceBar(context, measuringTimeNano, posX, posY + 44, KEY_JUMP);
    }

    protected void renderSpaceBar(final GuiGraphicsExtractor context, long mt, int posX, int posY, Key key){
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

    protected void renderKeyStroke(GuiGraphicsExtractor context, Font textRenderer, long mt, int posX, int posY, KeyLarge key){
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
        if (KEY_FWD == null) KEY_FWD = new KeyLarge(Minecraft.getInstance().options.keyUp);
        if (KEY_BWD == null) KEY_BWD = new KeyLarge(Minecraft.getInstance().options.keyDown);
        if (KEY_LEFT == null) KEY_LEFT = new KeyLarge(Minecraft.getInstance().options.keyLeft);
        if (KEY_RIGHT == null) KEY_RIGHT = new KeyLarge(Minecraft.getInstance().options.keyRight);
        if (KEY_JUMP == null) KEY_JUMP = new Key(Minecraft.getInstance().options.keyJump);
        Font renderer = Minecraft.getInstance().font;
        for (Key key : new Key[]{KEY_FWD, KEY_BWD, KEY_LEFT, KEY_RIGHT, KEY_JUMP}){
            if (key instanceof KeyLarge keyLarge){
                keyLarge.boundToKey = getKeyName(key);
                keyLarge.boundToLength = renderer.width(keyLarge.boundToKey);
            }
            final boolean pressed = key.binding.isDown();
            if (pressed != key.isPressed){
                key.isPressed = pressed;
                key.lastChanged = Util.getNanos();
            }
        }
    }

    private static String getKeyName(Key key) {
        return switch (key.binding.saveString()) {
            case "key.keyboard.up" -> "\u2191";
            case "key.keyboard.down" -> "\u2193";
            case "key.keyboard.left" -> "\u2190";
            case "key.keyboard.right" -> "\u2192";
            default -> key.binding.getTranslatedKeyMessage().getString();
        };
    }

    @Override
    public Component getName() {
        return Component.translatable("widgets.widgets.keystrokes");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("widgets.widgets.keystrokes.description");
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
