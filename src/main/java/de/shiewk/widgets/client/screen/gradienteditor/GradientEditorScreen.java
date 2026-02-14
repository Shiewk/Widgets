package de.shiewk.widgets.client.screen.gradienteditor;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.client.screen.WidgetVisibilityToggle;
import de.shiewk.widgets.client.screen.components.WidgetDisplayWidget;
import de.shiewk.widgets.color.GradientMode;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.widgets.settings.GradientWidgetSetting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;

import static net.minecraft.text.Text.translatable;

public class GradientEditorScreen extends Screen implements WidgetVisibilityToggle {

    private static final GradientOptions topBarGradient = new GradientOptions(50, 20, new int[]{0xa0_00_00_00, 0x50_00_00_00});

    final Screen parent;
    final ModWidget widget;
    final GradientWidgetSetting setting;
    final Runnable onChange;

    GradientMode mode;
    final IntArrayList colors;
    int gradientSpeed;
    int gradientSize;

    int currentColorIndex = 0;

    private GradientEditorColorSection colorSection;
    private GradientEditorSettingsSection settingsSection;

    public GradientEditorScreen(Screen parent, ModWidget widget, GradientWidgetSetting setting, Runnable onChange) {
        super(translatable("widgets.ui.gradientEditor"));
        this.parent = parent;
        this.widget = widget;
        this.setting = setting;
        this.onChange = onChange;
        GradientOptions options = setting.getValue();
        this.mode = options.mode();
        this.gradientSpeed = (int) options.gradientSpeed();
        this.gradientSize = (int) options.gradientSize();
        this.colors = IntArrayList.of(options.colors());
    }

    @Override
    protected void init() {
        super.init();
        reloadComponents();
    }

    private int getTopBarHeight() {
        return 8 + this.textRenderer.fontHeight * 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        Matrix3x2fStack matrices = context.getMatrices();
        long timeNanos = Util.getMeasuringTimeNano();

        // Render top bar
        topBarGradient.fillHorizontal(context, timeNanos, 0, 0, this.width, this.getTopBarHeight());

        Text topBarText = translatable("widgets.ui.gradientEditor");
        int width = textRenderer.getWidth(topBarText);

        matrices.pushMatrix().translate(this.width / 2f - width, 5).scale(2);
        context.drawText(textRenderer, topBarText, 0, 0, 0xff_ff_ff_ff, true);
        matrices.popMatrix();
    }

    public void reloadComponents(){
        clearChildren();

        double colorScrollY = 0;
        double settingsScrollY = 0;
        if (this.colorSection != null){
            colorScrollY = colorSection.getScrollY();
            settingsScrollY = settingsSection.getScrollY();
        }
        int topBarHeight = getTopBarHeight();


        int colorSectionWidth = 90;
        int settingsSectionWidth = 110;
        int mainAreaWidth = this.width - colorSectionWidth - settingsSectionWidth;
        int mainAreaHeight = this.height - topBarHeight;

        // sidebar
        this.colorSection = new GradientEditorColorSection(this, this.client, 0, topBarHeight, colorSectionWidth, mainAreaHeight, this.getCurrentColorIndex());
        this.settingsSection = new GradientEditorSettingsSection(this, this.client, colorSectionWidth, topBarHeight, settingsSectionWidth, mainAreaHeight);
        addDrawableChild(this.colorSection);
        addDrawableChild(this.settingsSection);
        colorSection.setScrollY(colorScrollY);
        settingsSection.setScrollY(settingsScrollY);

        // main area
        int mainAreaX = colorSectionWidth + settingsSectionWidth;
        int mainAreaCenterX = mainAreaX + (mainAreaWidth / 2);

        addDrawable(new WidgetDisplayWidget(this.widget, this.textRenderer, mainAreaCenterX, (mainAreaHeight) / 2 + topBarHeight));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void close() {
        refreshSettingValue();
        client.setScreen(parent);
    }

    private void refreshSettingValue() {
        settingsSection.onUpdatedColor();
        setting.setValue(toGradientOptions());
        widget.onSettingsChanged();
        onChange.run();
    }

    private GradientOptions toGradientOptions() {
        return new GradientOptions(this.mode, this.gradientSize, this.gradientSpeed, this.colors.toIntArray());
    }

    @Override
    public boolean shouldRenderWidgets() {
        return false;
    }

    public void addNewColor(int initialColor) {
        colors.add(initialColor);
        this.reloadComponents();
        this.colorSection.focusLast();
        refreshSettingValue();
    }

    public void setGradientSize(double v) {
        this.gradientSize = (int) v;
        refreshSettingValue();
    }

    public void setGradientSpeed(double v) {
        this.gradientSpeed = (int) v;
        refreshSettingValue();
    }

    public int getCurrentColor() {
        return colors.getInt(currentColorIndex);
    }

    public int getCurrentColorIndex() {
        return currentColorIndex;
    }

    public void setCurrentColorIndex(int i) {
        this.currentColorIndex = i;
        if (settingsSection != null) settingsSection.onUpdatedColor();
    }

    public void setCurrentColor(int newColor) {
        colors.set(getCurrentColorIndex(), newColor);
        refreshSettingValue();
    }

    public void removeCurrentColor() {
        colors.removeInt(getCurrentColorIndex());
        setCurrentColorIndex(0);
        refreshSettingValue();
        reloadComponents();
    }

    public void cycleMode() {
        this.mode = GradientMode.values()[(this.mode.ordinal() + 1) % GradientMode.values().length];
        refreshSettingValue();
    }

    public void swapColors(int[] colors) {
        this.colors.clear();
        for (int color : colors) {
            this.colors.add(color);
        }
        setCurrentColorIndex(0);
        this.reloadComponents();
        refreshSettingValue();
    }

    public void swap(GradientOptions options) {
        setGradientSize(options.gradientSize());
        setGradientSpeed(options.gradientSpeed());
        this.mode = options.mode();
        swapColors(options.colors());
    }
}
