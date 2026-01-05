package de.shiewk.widgets.client.screen.gradienteditor;

import de.shiewk.widgets.WidgetsMod;
import de.shiewk.widgets.client.WidgetManager;
import de.shiewk.widgets.client.screen.ContextMenuScreen;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.color.GradientPreset;
import de.shiewk.widgets.widgets.settings.GradientWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

import static de.shiewk.widgets.utils.WidgetUtils.colorARGBToHexRGBA;
import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.translatable;

public class GradientEditorSettingsSection extends AlwaysSelectedEntryListWidget<GradientEditorSettingsSection.ListEntry> {

    private final GradientEditorScreen editor;
    private final HexValueInputEntry hexInput;
    private final ColorPickerHueSliderEntry hueSlider;

    public GradientEditorSettingsSection(GradientEditorScreen editor, MinecraftClient client, int x, int y, int width, int height) {
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
    protected void drawMenuListBackground(DrawContext context) {
        context.fill(getX(), getY(), getX()+getWidth(), getY()+getHeight(), 0x20_00_00_00);
    }

    @Override protected void drawHeaderAndFooterSeparators(DrawContext context) {}
    @Override protected void drawSelectionHighlight(DrawContext context, ListEntry entry, int color) {}
    @Override protected void drawScrollbar(DrawContext context, int mouseX, int mouseY) {}

    public abstract static class ListEntry extends Entry<ListEntry> {

        @Override
        public Text getNarration() {
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

        private final Text text;

        private HeadingEntry(Text text) {
            this.text = text;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int y = this.getContentY() + 2;
            int rowWidth = 110;

            int textWidth = client.textRenderer.getWidth(text);
            int textX = getX() + (rowWidth - textWidth) / 2;
            context.drawText(client.textRenderer, text, textX, y, 0xffffffff, true);
        }
    }

    private class SliderEntry extends ListEntry {

        private final SliderWidget slider;

        private SliderEntry(int min, int max, int initial, IntFunction<Text> textGetter, IntConsumer onUpdateValue) {
            double value = (double) (initial - min) / (max - min);
            slider = new Slider(textGetter, value, onUpdateValue, min, max);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            slider.active = editor.colors.size() > 1;
            slider.setDimensionsAndPosition(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            slider.render(context, mouseX, mouseY, deltaTicks);
            if (hovered && !slider.active) {
                context.setCursor(StandardCursors.NOT_ALLOWED);
                slider.setTooltip(Tooltip.of(translatable("widgets.ui.gradientEditor.gradientSettings.addMoreColors")));
            } else {
                slider.setTooltip(null);
            }
        }

        @Override public boolean mouseClicked(Click click, boolean doubled) { return slider.mouseClicked(click, doubled); }
        @Override public boolean mouseDragged(Click click, double offsetX, double offsetY) { return slider.mouseDragged(click, offsetX, offsetY); }
        @Override public boolean mouseReleased(Click click) { return slider.mouseReleased(click); }
        @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { return slider.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount); }
        @Override public void mouseMoved(double mouseX, double mouseY) { slider.mouseMoved(mouseX, mouseY); }

        private static class Slider extends SliderWidget {

            private final IntFunction<Text> textGetter;
            private final IntConsumer onUpdateValue;
            private final int min;
            private final int max;

            public Slider(IntFunction<Text> textGetter, double value, IntConsumer onUpdateValue, int min, int max) {
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

        private final ButtonWidget button = new ButtonWidget.Builder(empty(), this::press).build();

        public ToggleModeButtonEntry() {
            refreshButtonValues();
        }

        private void press(ButtonWidget button) {
            editor.cycleMode();
            refreshButtonValues();
        }

        private void refreshButtonValues() {
            button.setMessage(translatable("widgets.ui.gradientEditor.mode", editor.mode.name));
            if (editor.colors.size() > 1){
                button.setTooltip(Tooltip.of(editor.mode.description));
            } else {
                button.setTooltip(Tooltip.of(translatable("widgets.ui.gradientEditor.gradientSettings.addMoreColors")));
            }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            button.active = editor.colors.size() > 1;
            button.setDimensionsAndPosition(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            button.render(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            return button.mouseClicked(click, doubled);
        }
    }

    private class ImportButtonEntry extends ListEntry {

        private final ButtonWidget button = new ButtonWidget.Builder(translatable("widgets.ui.gradientEditor.importOther"), this::press).build();

        private void press(ButtonWidget button) {
            Screen currentScreen = client.currentScreen;
            int menuX = (int) client.mouse.getScaledX(client.getWindow());
            int menuY = (int) client.mouse.getScaledY(client.getWindow());
            client.setScreen(new ContextMenuScreen(
                    button.getMessage(),
                    currentScreen,
                    menuX, menuY,
                    WidgetManager.getAllWidgets()
                            .stream()
                            .map(widget -> new ContextMenuScreen.Option(
                                    widget.getName(),
                                    () -> client.setScreen(new ContextMenuScreen(
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
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            button.setDimensionsAndPosition(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            button.render(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            return button.mouseClicked(click, doubled);
        }
    }

    private class UsePresetButtonEntry extends ListEntry {

        private final ButtonWidget button = new ButtonWidget.Builder(translatable("widgets.ui.gradientEditor.usePreset"), this::press).build();

        private void press(ButtonWidget button) {
            Screen currentScreen = client.currentScreen;
            int menuX = (int) client.mouse.getScaledX(client.getWindow());
            int menuY = (int) client.mouse.getScaledY(client.getWindow());
            client.setScreen(new ContextMenuScreen(
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
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            button.setDimensionsAndPosition(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            button.render(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
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
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            double sliderProg = getComponentValue() / 255d;

            GradientOptions bg = new GradientOptions(100, 0, new int[]{getBackgroundStartColor(), getBackgroundEndColor()});
            int contentY = getContentY();
            int contentWidth = getContentWidth();
            int contentX = getContentX();
            int contentHeight = getContentHeight();

            bg.fillHorizontal(context, 0, contentX, contentY + 1, contentX + contentWidth, contentY + contentHeight - 1);

            context.drawVerticalLine((int) (contentX + (contentWidth * sliderProg)), contentY - 1, contentY + contentHeight, 0xff_ff_ff_ff);
            if (showLabel){
                String label = getLabelWithValue();
                int w = client.textRenderer.getWidth(label);
                context.drawText(client.textRenderer, label, contentX + (contentWidth - w) / 2, contentY + 5, 0xffffffff, true);
            }
            if (hovered){
                context.setCursor(StandardCursors.RESIZE_EW);
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
        public boolean mouseDragged(Click click, double offsetX, double offsetY) {
            double mouseX = click.x();
            int newComponentValue = (int) ((mouseX - getContentX()) / getContentWidth() * 255);
            setComponentValue(newComponentValue);
            return true;
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            return mouseDragged(click, 0, 0);
        }

        @Override
        public boolean keyPressed(KeyInput input) {
            if (input.isLeft()) {
                setComponentValue(getComponentValue() - 1);
            } else if (input.isRight()) {
                setComponentValue(getComponentValue() + 1);
            }
            return true;
        }

        private void setComponentValue(int val) {
            val = MathHelper.clamp(val, 0, 255);
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

        private final ButtonWidget button = new ButtonWidget.Builder(translatable("widgets.ui.gradientEditor.removeColor"), this::press).build();

        private void press(ButtonWidget button) {
            button.active = false;
            editor.removeCurrentColor();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            button.active = editor.colors.size() > 1;
            button.setDimensionsAndPosition(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            button.render(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            return button.mouseClicked(click, doubled);
        }
    }

    private class HexValueInputEntry extends ListEntry {

        private final TextFieldWidget inputField = new TextFieldWidget(client.textRenderer, 0, 0, empty());

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

            inputField.setEditableColor(valid ? 0xffffffff : 0xffff0000);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            inputField.setDimensionsAndPosition(getContentWidth(), getContentHeight(), getContentX(), getContentY());
            inputField.render(context, mouseX, mouseY, deltaTicks);
        }

        public void refreshText(){
            inputField.setChangedListener(null);
            inputField.setText('#' + colorARGBToHexRGBA(editor.getCurrentColor()));
            inputField.setCursorToStart(false);
            inputField.setEditableColor(0xffffffff);
            inputField.setChangedListener(this::onChangeInputField);
        }

        @Override
        public void setFocused(boolean focused) {
            inputField.setFocused(focused);
        }

        @Override public boolean mouseClicked(Click click, boolean doubled) { return inputField.mouseClicked(click, doubled); }
        @Override public boolean mouseDragged(Click click, double offsetX, double offsetY) { return inputField.mouseDragged(click, offsetX, offsetY); }
        @Override public boolean mouseReleased(Click click) { return inputField.mouseReleased(click); }
        @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { return inputField.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount); }
        @Override public void mouseMoved(double mouseX, double mouseY) { inputField.mouseMoved(mouseX, mouseY); }
        @Override public boolean charTyped(CharInput input) { return inputField.charTyped(input); }
        @Override public boolean keyReleased(KeyInput input) { return inputField.keyReleased(input); }
        @Override public boolean keyPressed(KeyInput input) { return inputField.keyPressed(input); }
    }

    private class ColorPickerEntry extends ListEntry {

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            context.fill(
                    getContentX(),
                    getContentY(),
                    getContentX() + getContentWidth(),
                    getContentY() + getContentHeight(),
                    getBackgroundFillColor()
            );
            context.drawTexture(
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
                context.setCursor(StandardCursors.ARROW);
            }
        }

        @Override
        public boolean mouseDragged(Click click, double offsetX, double offsetY) {
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
        public boolean mouseClicked(Click click, boolean doubled) {
            return mouseDragged(click, 0, 0);
        }

        private int getBackgroundFillColor() {
            return Color.HSBtoRGB(hueSlider.sliderProgress, 1, 1);
        }

        private static Identifier OVERLAY_TEXTURE_ID;

        public Identifier getOverlayTextureIdentifier(){
            if (OVERLAY_TEXTURE_ID == null){
                NativeImageBackedTexture texture = generateOverlayTexture();
                OVERLAY_TEXTURE_ID = Identifier.of(WidgetsMod.MOD_ID, "textures/gui/generated/color-picker-overlay");
                client.getTextureManager().registerTexture(OVERLAY_TEXTURE_ID, texture);
            }
            return OVERLAY_TEXTURE_ID;
        }

        private NativeImageBackedTexture generateOverlayTexture() {
            NativeImageBackedTexture texture = new NativeImageBackedTexture("widgets:textures/gui/generated/color-picker-overlay", 256, 256, false);
            NativeImage image = texture.getImage();

            for (int x = 0; x < 256; x++) {
                for (int y = 0; y < 256; y++) {
                    double alphaWhite = (255d - x) / 255d;
                    double alphaBlack = y / 255d;
                    double outAlpha = alphaBlack + alphaWhite * (1d - alphaBlack);

                    int rgb = (int) Math.round(outAlpha > 0 ? (255d * alphaWhite * (1d - alphaBlack)) / outAlpha : 0);
                    int a = (int) Math.round(outAlpha * 255.0);

                    image.setColorArgb(x, y, new Color(rgb, rgb, rgb, a).getRGB());
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
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            sliderGradient.fillHorizontal(context, 0, getContentX(), getContentY() + 1, getContentX() + getContentWidth(), getContentY() + getContentHeight() - 1);
            context.drawVerticalLine((int) (getContentX() + (getContentWidth() * sliderProgress)), getContentY() - 1, getContentY() + getContentHeight(), 0xff_ff_ff_ff);

            if (hovered){
                context.setCursor(StandardCursors.RESIZE_EW);
            }
        }

        @Override
        public boolean mouseDragged(Click click, double offsetX, double offsetY) {
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
        public boolean mouseClicked(Click click, boolean doubled) {
            return this.mouseDragged(click, 0, 0);
        }

        public void refreshHue() {
            this.sliderProgress = getCurrentHue();
        }
    }
}
