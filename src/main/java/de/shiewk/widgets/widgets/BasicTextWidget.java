package de.shiewk.widgets.widgets;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.WidgetSettingOption;
import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.RGBAColorWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.List;

public abstract class BasicTextWidget extends ModWidget {

    public enum TextAlignment {
        RIGHT("right"),
        CENTER("center"),
        LEFT("left");

        public final String key;

        TextAlignment(String key) {
            this.key = key;
        }

        public Text text(){
            return Text.translatable("widgets.widgets.basictext.alignment." + key);
        }
    }

    protected Text renderText = Text.empty();
    protected boolean shouldRender = true;
    private int textX = 0;
    private int textY = 0;
    private int padding = 0;
    private TextRenderer renderer = null;
    private boolean textShadow = true;

    private static ObjectArrayList<WidgetSettingOption> getCustomSettings(List<WidgetSettingOption> otherCustomOptions) {
        final ObjectArrayList<WidgetSettingOption> list = new ObjectArrayList<>(otherCustomOptions);
        list.add(new RGBAColorWidgetSetting("backgroundcolor", Text.translatable("widgets.widgets.basictext.background"), 0, 0, 0, 80));
        list.add(new RGBAColorWidgetSetting("textcolor", Text.translatable("widgets.widgets.basictext.textcolor"), 255, 255, 255, 255));
        list.add(new IntSliderWidgetSetting("width", Text.translatable("widgets.widgets.basictext.width"), 10, DEFAULT_WIDTH, 80*3));
        list.add(new IntSliderWidgetSetting("height", Text.translatable("widgets.widgets.basictext.height"), 9, DEFAULT_HEIGHT, 80));
        list.add(new IntSliderWidgetSetting("size", Text.translatable("widgets.widgets.common.sizePercent"), 25, 100, 400));
        list.add(new ToggleWidgetSetting("shadow", Text.translatable("widgets.widgets.basictext.textshadow"), true));
        list.add(new EnumWidgetSetting<>("alignment", Text.translatable("widgets.widgets.basictext.alignment"), TextAlignment.class, TextAlignment.CENTER, TextAlignment::text));
        list.add(new IntSliderWidgetSetting("padding", Text.translatable("widgets.widgets.basictext.padding"), 0, 5, 20));
        return list;
    }
    protected BasicTextWidget(Identifier id, List<WidgetSettingOption> otherCustomOptions) {
        super(id, getCustomSettings(otherCustomOptions));
        getSettings().optionById("padding").setShowCondition(() -> this.textAlignment != TextAlignment.CENTER);
    }

    protected static final int
            DEFAULT_WIDTH = 80,
            DEFAULT_HEIGHT = 9 + 12,
            DEFAULT_BACKGROUND_COLOR = new Color(0, 0, 0, 80).getRGB(),
            DEFAULT_TEXT_COLOR = new Color(255, 255 ,255, 255).getRGB();

    protected float size = 2f;

    protected int backgroundColor = DEFAULT_BACKGROUND_COLOR, textColor = DEFAULT_TEXT_COLOR, width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT;
    protected TextAlignment textAlignment = TextAlignment.CENTER;

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void render(DrawContext context, long n, TextRenderer textRenderer, int posX, int posY) {
        if (!shouldRender) return;
        MatrixStack matrices = context.getMatrices();
        if (size != 1f){
            matrices.push();
            matrices.translate(-(size-1) * posX, -(size-1) * posY, 0);
            matrices.scale(size, size, 1);
        }
        renderer = textRenderer;
        context.fill(posX, posY, posX + width(), posY + height(), this.backgroundColor);
        context.drawText(textRenderer, renderText, posX + textX,  posY + (textShadow ? textY : textY + 1 /* offset 1 without text shadow so that it looks more aligned */), this.textColor, textShadow);
        if (size != 1f) matrices.pop();
    }

    @Override
    public float getScaleFactor() {
        return size;
    }

    @Override
    public final void tick() {
        tickWidget();
        if (renderer != null){
            int textWidth = renderer.getWidth(renderText);
            switch (textAlignment){
                case LEFT -> textX = padding;
                case CENTER -> textX = width() / 2 - textWidth / 2;
                case RIGHT -> textX = width() - padding - textWidth;
            }
        }
        textY = (height-9) / 2;
    }

    public abstract void tickWidget();

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        this.backgroundColor = ((RGBAColorWidgetSetting) settings.optionById("backgroundcolor")).getColor();
        this.textColor = ((RGBAColorWidgetSetting) settings.optionById("textcolor")).getColor();
        this.width = ((IntSliderWidgetSetting) settings.optionById("width")).getValue();
        this.height = ((IntSliderWidgetSetting) settings.optionById("height")).getValue();
        this.textAlignment = (TextAlignment) ((EnumWidgetSetting<?>) settings.optionById("alignment")).getValue();
        this.padding = ((IntSliderWidgetSetting) settings.optionById("padding")).getValue();
        this.size = 0.01f * ((IntSliderWidgetSetting) settings.optionById("size")).getValue();
        this.textShadow = ((ToggleWidgetSetting) settings.optionById("shadow")).getValue();
    }
}
