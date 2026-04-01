package de.shiewk.widgets.client.screen.gradienteditor;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.shiewk.widgets.WidgetsMod;
import de.shiewk.widgets.client.WidgetManager;
import de.shiewk.widgets.client.screen.ContextMenuScreen;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.color.GradientPreset;
import de.shiewk.widgets.widgets.settings.GradientWidgetSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

import static de.shiewk.widgets.utils.WidgetUtils.colorARGBToHexRGBA;
import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.translatable;

public class GradientEditorSettingsSection extends ObjectSelectionList<GradientEditorSettingsSection.ListEntry> {

    private final GradientEditorScreen editor;
    private final HexValueInputEntry hexInput;
    private final ColorPickerHueSliderEntry hueSlider;

    public GradientEditorSettingsSection(GradientEditorScreen editor, Minecraft client, int x, int y, int width, int height) {
        super(client, width, height, y, 110);
        setX(x);
        this.editor = editor;

        addEntry(new HeadingEntry(translatable("widgets.ui.gradientEditor.gradientSettings")), 18);

        addEntry(new SliderEntry(1, 100, editor.gradientSize, d -> translatable("widgets.ui.gradientEditor.size", d), editor::setGradientSize), 20);
        addEntry(new SliderEntry(0, 100, editor.gradientSpeed, d -> translatable("widgets.ui.gradientEditor.speed", d), editor::setGradientSpeed), 20);
        addEntry(new ToggleModeButtonEntry(), 20);
        addEntry(new ImportButtonEntry(), 20);
        addEntry(new UsePresetButtonEntry(), 20);

        addEntry(new HeadingEntry(translatable("widgets.ui.gradientEditor.editColor")), 18);

        addEntry(new ColorChangerEntry(0), 22);
        addEntry(new ColorChangerEntry(1), 22);
        addEntry(new ColorChangerEntry(2), 22);
        addEntry(new ColorChangerEntry(3), 22);

        addEntry(new HeadingEntry(translatable("widgets.ui.gradientEditor.editColor.orPick")), 18);

        addEntry(new ColorPickerEntry(), getRowWidth());
        addEntry(hueSlider = new ColorPickerHueSliderEntry(), 22);
        addEntry(new ColorChangerEntry(3, false), 22);
        addEntry(hexInput = new HexValueInputEntry(), 20);

        addEntry(new RemoveColorButtonEntry(), 20);
    }

    @Override
    public int getRowWidth() {
        return 110;
    }

    @Override
    protected void extractListBackground(GuiGraphicsExtractor context) {
        context.fill(getX(), getY(), getX()+getWidth(), getY()+getHeight(), 0x20_00_00_00);
    }

    @Override protected void extractListSeparators(@NonNull GuiGraphicsExtractor context) {}
    @Override protected void extractSelection(@NonNull GuiGraphicsExtractor context, @NonNull ListEntry entry, int color) {}
    @Override protected void extractScrollbar(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY) {}

    public abstract static class ListEntry extends Entry<ListEntry> {

        @Override
        public @NonNull Component getNarration() {
            return empty();
        }
    }

    public void onUpdatedColor(){
        hexInput.refreshText();
        hueSlider.refreshHue();
    }

    private float getCurrentHue(){
        float[] hsb = getHsb();
        return hsb[0];
    }

    private float @NotNull [] getHsb() {
        int currentRGBA = editor.getCurrentColor();
        return Color.RGBtoHSB((currentRGBA >> 16) & 0xff, (currentRGBA >> 8) & 0xff, currentRGBA & 0xff, null);
    }


    private class HeadingEntry extends ListEntry {

        private final Component text;

        private HeadingEntry(Component text) {
            this.text = text;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int y = this.getContentY() + 2;
            int rowWidth = 110;

            int textWidth = minecraft.font.width(text);
            int textX = getX() + (rowWidth - textWidth) / 2;
            context.text(minecraft.font, text, textX, y, 0xffffffff, true);
        }
    }

    private class SliderEntry extends ListEntry {

        private final AbstractSliderButton slider;

        private SliderEntry(int min, int max, int initial, IntFunction<Component> textGetter, IntConsumer onUpdateValue) {
            double value = (double) (initial - min) / (max - min);
            slider = new Slider(textGetter, value, onUpdateValue, min, max);
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            slider.active = editor.colors.size() > 1;
            slider.setRectangle(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            slider.extractRenderState(context, mouseX, mouseY, deltaTicks);
            if (hovered && !slider.active) {
                context.requestCursor(CursorTypes.NOT_ALLOWED);
                slider.setTooltip(Tooltip.create(translatable("widgets.ui.gradientEditor.gradientSettings.addMoreColors")));
            } else {
                slider.setTooltip(null);
            }
        }

        @Override public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) { return slider.mouseClicked(click, doubled); }
        @Override public boolean mouseDragged(@NonNull MouseButtonEvent click, double offsetX, double offsetY) { return slider.mouseDragged(click, offsetX, offsetY); }
        @Override public boolean mouseReleased(@NonNull MouseButtonEvent click) { return slider.mouseReleased(click); }
        @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { return slider.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount); }
        @Override public void mouseMoved(double mouseX, double mouseY) { slider.mouseMoved(mouseX, mouseY); }

        private static class Slider extends AbstractSliderButton {

            private final IntFunction<Component> textGetter;
            private final IntConsumer onUpdateValue;
            private final int min;
            private final int max;

            public Slider(IntFunction<Component> textGetter, double value, IntConsumer onUpdateValue, int min, int max) {
                super(0, 0, 0, 0, empty(), value);
                this.textGetter = textGetter;
                this.onUpdateValue = onUpdateValue;
                this.min = min;
                this.max = max;
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(textGetter.apply(getTranslatedValue()));
            }

            @Override
            protected void applyValue() {
                onUpdateValue.accept(getTranslatedValue());
            }

            private int getTranslatedValue() {
                return (int) (min + (this.value * (max - min)));
            }

        }
    }

    private class ToggleModeButtonEntry extends ListEntry {

        private final Button button = new Button.Builder(empty(), this::press).build();

        public ToggleModeButtonEntry() {
            refreshButtonValues();
        }

        private void press(Button button) {
            editor.cycleMode();
            refreshButtonValues();
        }

        private void refreshButtonValues() {
            button.setMessage(translatable("widgets.ui.gradientEditor.mode", editor.mode.name));
            if (editor.colors.size() > 1){
                button.setTooltip(Tooltip.create(editor.mode.description));
            } else {
                button.setTooltip(Tooltip.create(translatable("widgets.ui.gradientEditor.gradientSettings.addMoreColors")));
            }
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            button.active = editor.colors.size() > 1;
            button.setRectangle(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            button.extractRenderState(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
            return button.mouseClicked(click, doubled);
        }
    }

    private class ImportButtonEntry extends ListEntry {

        private final Button button = new Button.Builder(translatable("widgets.ui.gradientEditor.importOther"), this::press).build();

        private void press(Button button) {
            Screen currentScreen = minecraft.screen;
            int menuX = (int) minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
            int menuY = (int) minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());
            minecraft.setScreen(new ContextMenuScreen(
                    button.getMessage(),
                    currentScreen,
                    menuX, menuY,
                    WidgetManager.getAllWidgets()
                            .stream()
                            .map(widget -> new ContextMenuScreen.Option(
                                    widget.getName(),
                                    () -> minecraft.setScreen(new ContextMenuScreen(
                                            widget.getName(),
                                            currentScreen,
                                            menuX,
                                            menuY,
                                            widget.getSettings().getCustomSettings()
                                                    .stream()
                                                    .filter(o -> o instanceof GradientWidgetSetting)
                                                    .map(option -> new ContextMenuScreen.Option(
                                                            option.getName(),
                                                            () -> editor.swap(((GradientWidgetSetting) option).getValue())
                                                    )).toList()
                                    ))
                            )).toList()
            ));
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            button.setRectangle(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            button.extractRenderState(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
            return button.mouseClicked(click, doubled);
        }
    }

    private class UsePresetButtonEntry extends ListEntry {

        private final Button button = new Button.Builder(translatable("widgets.ui.gradientEditor.usePreset"), this::press).build();

        private void press(Button button) {
            Screen currentScreen = minecraft.screen;
            int menuX = (int) minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
            int menuY = (int) minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());
            minecraft.setScreen(new ContextMenuScreen(
                    translatable("widgets.ui.gradientEditor.usePreset"),
                    currentScreen,
                    menuX, menuY,
                    Arrays.stream(GradientPreset.presets).map(preset -> new ContextMenuScreen.Option(
                            preset.name(),
                            () -> editor.swap(preset.gradient())
                    )).toList()
            ));
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            button.setRectangle(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            button.extractRenderState(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
            return button.mouseClicked(click, doubled);
        }

    }

    private class ColorChangerEntry extends ListEntry {

        private final int component;
        private final boolean showLabel;

        public ColorChangerEntry(int component) {
            this(component, true);
        }

        public ColorChangerEntry(int component, boolean showLabel) {
            this.component = component;
            this.showLabel = showLabel;
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            double sliderProg = getComponentValue() / 255d;

            GradientOptions bg = new GradientOptions(100, 0, new int[]{getBackgroundStartColor(), getBackgroundEndColor()});
            int contentY = getContentY();
            int contentWidth = getContentWidth();
            int contentX = getContentX();
            int contentHeight = getContentHeight();

            bg.fillHorizontal(context, 0, contentX, contentY + 1, contentX + contentWidth, contentY + contentHeight - 1);

            context.verticalLine((int) (contentX + (contentWidth * sliderProg)), contentY - 1, contentY + contentHeight, 0xff_ff_ff_ff);
            if (showLabel){
                String label = getLabelWithValue();
                int w = minecraft.font.width(label);
                context.text(minecraft.font, label, contentX + (contentWidth - w) / 2, contentY + 5, 0xffffffff, true);
            }
            if (hovered){
                context.requestCursor(CursorTypes.RESIZE_EW);
            }
        }

        private int getComponentValue() {
            int currentColor = editor.getCurrentColor();
            return switch (component) {
                case 0 -> (currentColor & 0x00_ff_00_00) >>> 16;
                case 1 -> (currentColor & 0x00_00_ff_00) >>> 8;
                case 2 -> (currentColor & 0x00_00_00_ff);
                case 3 -> (currentColor & 0xff_00_00_00) >>> 24;
                default -> throw new IllegalStateException();
            };
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
            double mouseX = click.x();
            int newComponentValue = (int) ((mouseX - getContentX()) / getContentWidth() * 255);
            setComponentValue(newComponentValue);
            return true;
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
            return mouseDragged(click, 0, 0);
        }

        @Override
        public boolean keyPressed(KeyEvent input) {
            if (input.isLeft()) {
                setComponentValue(getComponentValue() - 1);
            } else if (input.isRight()) {
                setComponentValue(getComponentValue() + 1);
            }
            return true;
        }

        private void setComponentValue(int val) {
            val = Mth.clamp(val, 0, 255);
            final int currentColor = editor.getCurrentColor();
            int newColor = switch (component){
                case 0 -> (currentColor & 0xff_00_ff_ff) | val << 16;
                case 1 -> (currentColor & 0xff_ff_00_ff) | val << 8;
                case 2 -> (currentColor & 0xff_ff_ff_00) | val;
                case 3 -> (currentColor & 0x00_ff_ff_ff) | val << 24;
                default -> throw new IllegalStateException();
            };
            editor.setCurrentColor(newColor);
        }

        private String getLabelWithValue() {
            return switch (component){
                case 0 -> "R: " + getComponentValue();
                case 1 -> "G: " + getComponentValue();
                case 2 -> "B: " + getComponentValue();
                case 3 -> "A: " + getComponentValue();
                default -> throw new IllegalStateException();
            };
        }

        private int getBackgroundEndColor() {
            final int currentColor = editor.getCurrentColor();
            return switch (component){
                case 0 -> currentColor | 0x00_ff_00_00;
                case 1 -> currentColor | 0x00_00_ff_00;
                case 2 -> currentColor | 0x00_00_00_ff;
                case 3 -> currentColor | 0xff_00_00_00;
                default -> throw new IllegalStateException();
            };
        }

        private int getBackgroundStartColor() {
            final int currentColor = editor.getCurrentColor();
            return switch (component){
                case 0 -> currentColor & 0xff_00_ff_ff;
                case 1 -> currentColor & 0xff_ff_00_ff;
                case 2 -> currentColor & 0xff_ff_ff_00;
                case 3 -> currentColor & 0x00_ff_ff_ff;
                default -> throw new IllegalStateException();
            };
        }
    }

    private class RemoveColorButtonEntry extends ListEntry {

        private final Button button = new Button.Builder(translatable("widgets.ui.gradientEditor.removeColor"), this::press).build();

        private void press(Button button) {
            button.active = false;
            editor.removeCurrentColor();
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            button.active = editor.colors.size() > 1;
            button.setRectangle(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            button.extractRenderState(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
            return button.mouseClicked(click, doubled);
        }
    }

    private class HexValueInputEntry extends ListEntry {

        private final EditBox inputField = new EditBox(minecraft.font, 0, 0, empty());

        public HexValueInputEntry() {
            this.refreshText();
            inputField.setMaxLength(9);
        }

        private void onChangeInputField(String s) {
            boolean valid = false;
            if (s.startsWith("#")) s = s.substring(1);
            if (s.length() == 6) {
                s += "ff";
            }
            if (s.length() == 8) {
                try {
                    String alpha = s.substring(6, 8);
                    String rgb = s.substring(0, 6);
                    String argbHex = alpha + rgb;

                    int cl = Integer.parseUnsignedInt(argbHex, 16);

                    if (cl != editor.getCurrentColor()) {
                        editor.setCurrentColor(cl);
                    }
                    valid = true;
                } catch (NumberFormatException ignored) {}
            }

            inputField.setTextColor(valid ? 0xffffffff : 0xffff0000);
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            inputField.setRectangle(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            inputField.extractRenderState(context, mouseX, mouseY, deltaTicks);
        }

        public void refreshText(){
            inputField.setResponder(_ -> {});
            inputField.setValue('#' + colorARGBToHexRGBA(editor.getCurrentColor()));
            inputField.moveCursorToStart(false);
            inputField.setTextColor(0xffffffff);
            inputField.setResponder(this::onChangeInputField);
        }

        @Override
        public void setFocused(boolean focused) {
            inputField.setFocused(focused);
        }

        @Override public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) { return inputField.mouseClicked(click, doubled); }
        @Override public boolean mouseDragged(@NonNull MouseButtonEvent click, double offsetX, double offsetY) { return inputField.mouseDragged(click, offsetX, offsetY); }
        @Override public boolean mouseReleased(@NonNull MouseButtonEvent click) { return inputField.mouseReleased(click); }
        @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { return inputField.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount); }
        @Override public void mouseMoved(double mouseX, double mouseY) { inputField.mouseMoved(mouseX, mouseY); }
        @Override public boolean charTyped(@NonNull CharacterEvent input) { return inputField.charTyped(input); }
        @Override public boolean keyReleased(@NonNull KeyEvent input) { return inputField.keyReleased(input); }
        @Override public boolean keyPressed(@NonNull KeyEvent input) { return inputField.keyPressed(input); }
    }

    private class ColorPickerEntry extends ListEntry {

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            context.fill(
                    getContentX(),
                    getContentY(),
                    getContentX() + getContentWidth(),
                    getContentY() + getContentHeight(),
                    getBackgroundFillColor()
            );
            context.blit(
                    RenderPipelines.GUI_TEXTURED,
                    getOverlayTextureIdentifier(),
                    getContentX(), getContentY(),
                    0, 0,
                    getContentWidth(), getContentHeight(),
                    getContentWidth(), getContentHeight()
            );

            float[] hsb = getHsb();
            int pointerOffsetX = (int) (hsb[1] * getContentWidth());
            int pointerOffsetY = (int) ((1f - hsb[2]) * getContentHeight());
            context.fill(
                    getContentX() + pointerOffsetX - 2, getContentY() + pointerOffsetY - 2,
                    getContentX() + pointerOffsetX + 2, getContentY() + pointerOffsetY + 2,
                    0xffffffff
            );
            context.fill(
                    getContentX() + pointerOffsetX - 1, getContentY() + pointerOffsetY - 1,
                    getContentX() + pointerOffsetX + 1, getContentY() + pointerOffsetY + 1,
                    editor.getCurrentColor() | 0xff000000
            );

            if (hovered){
                context.requestCursor(CursorTypes.ARROW);
            }
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
            double x = click.x() - getContentX();
            double y = click.y() - getContentY();
            float saturation = (float) Math.clamp(x / getContentWidth(), 0d, 1d);
            float brightness = (float) Math.clamp(1d - y / getContentHeight(), 0d, 1d);

            int newColorRgb = Color.HSBtoRGB(hueSlider.sliderProgress, saturation, brightness) & 0x00ffffff;
            int alpha = editor.getCurrentColor() & 0xff000000;

            editor.setCurrentColor(newColorRgb | alpha);
            return true;
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
            return mouseDragged(click, 0, 0);
        }

        private int getBackgroundFillColor() {
            return Color.HSBtoRGB(hueSlider.sliderProgress, 1, 1);
        }

        private static Identifier OVERLAY_TEXTURE_ID;

        public Identifier getOverlayTextureIdentifier(){
            if (OVERLAY_TEXTURE_ID == null){
                DynamicTexture texture = generateOverlayTexture();
                OVERLAY_TEXTURE_ID = Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "textures/gui/generated/color-picker-overlay");
                minecraft.getTextureManager().register(OVERLAY_TEXTURE_ID, texture);
            }
            return OVERLAY_TEXTURE_ID;
        }

        private DynamicTexture generateOverlayTexture() {
            DynamicTexture texture = new DynamicTexture("widgets:textures/gui/generated/color-picker-overlay", 256, 256, false);
            NativeImage image = texture.getPixels();

            for (int x = 0; x < 256; x++) {
                for (int y = 0; y < 256; y++) {
                    double alphaWhite = (255d - x) / 255d;
                    double alphaBlack = y / 255d;
                    double outAlpha = alphaBlack + alphaWhite * (1d - alphaBlack);

                    int rgb = (int) Math.round(outAlpha > 0 ? (255d * alphaWhite * (1d - alphaBlack)) / outAlpha : 0);
                    int a = (int) Math.round(outAlpha * 255.0);

                    image.setPixel(x, y, new Color(rgb, rgb, rgb, a).getRGB());
                }
            }

            texture.upload();
            return texture;
        }

    }

    private class ColorPickerHueSliderEntry extends ListEntry {

        private static final GradientOptions sliderGradient = new GradientOptions(
                16.666666f,
                0,
                new int[]{0xffff0000, 0xffffff00, 0xff00ff00, 0xff00ffff, 0xff0000ff, 0xffff00ff, 0xffff0000}
        );
        private float sliderProgress;

        public ColorPickerHueSliderEntry(){
            refreshHue();
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            sliderGradient.fillHorizontal(context, 0, getContentX(), getContentY() + 1, getContentX() + getContentWidth(), getContentY() + getContentHeight() - 1);
            context.verticalLine( (int) (getContentX() + (getContentWidth() * sliderProgress)), getContentY() - 1, getContentY() + getContentHeight(), 0xff_ff_ff_ff);

            if (hovered){
                context.requestCursor(CursorTypes.RESIZE_EW);
            }
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
            double x = click.x() - getContentX();
            float newHue = (float) Math.clamp(x / getContentWidth(), 0, 1);
            this.sliderProgress = newHue;

            float[] hsb = getHsb();
            int newRgb = Color.HSBtoRGB(newHue, hsb[1], hsb[2]) & 0x00ffffff;
            int alpha = editor.getCurrentColor() & 0xff000000;
            int newColor = newRgb | alpha;

            if (newColor != editor.getCurrentColor()){
                editor.setCurrentColor(newColor);
            }
            return true;
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
            return this.mouseDragged(click, 0, 0);
        }

        public void refreshHue() {
            this.sliderProgress = getCurrentHue();
        }
    }
}
