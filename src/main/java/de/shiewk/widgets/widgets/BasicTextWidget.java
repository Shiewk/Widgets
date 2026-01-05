package de.shiewk.widgets.widgets;

import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.widgets.settings.*;
import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.client.WidgetRenderer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minecraft.text.Text.*;

public abstract class BasicTextWidget extends ResizableWidget {

    public enum TextAlignment {
        RIGHT("right"),
        CENTER("center"),
        LEFT("left");

        public final String key;

        TextAlignment(String key) {
            this.key = key;
        }

        public Text displayText(){
            return translatable("widgets.widgets.basictext.alignment." + key);
        }
    }

    public enum TextStyle {
        PLAIN("plain", UnaryOperator.identity()),
        SQUARE_BRACKETS("squareBrackets", t -> surround("[", t, "]")),
        PARENTHESES("parentheses", t -> surround("(", t, ")"));

        public final String key;
        public final UnaryOperator<Text> operator;

        TextStyle(String key, UnaryOperator<Text> operator) {
            this.key = key;
            this.operator = operator;
        }

        public Text displayText(){
            return translatable("widgets.widgets.basictext.style." + key);
        }

        public static Text surround(String prefix, Text subject, String suffix){
            return literal(prefix).append(subject).append(literal(suffix));
        }
    }

    protected Text renderText = empty();
    protected boolean shouldRender = true;
    private float textX = 0;
    private float textY = 0;
    private int padding = 0;
    private TextRenderer renderer = null;
    private boolean textShadow = true;

    private static ObjectArrayList<WidgetSettingOption<?>> getCustomSettings(List<WidgetSettingOption<?>> otherCustomOptions) {
        final ObjectArrayList<WidgetSettingOption<?>> list = new ObjectArrayList<>(otherCustomOptions);
        list.add(new GradientWidgetSetting("backgroundcolor", translatable("widgets.widgets.basictext.background"), 0x50_00_00_00));
        list.add(new GradientWidgetSetting("textcolor", translatable("widgets.widgets.basictext.textcolor"), 0xff_ff_ff_ff));
        list.add(new IntSliderWidgetSetting("width", translatable("widgets.widgets.basictext.width"), 10, DEFAULT_WIDTH, 80*3));
        list.add(new IntSliderWidgetSetting("height", translatable("widgets.widgets.basictext.height"), 9, DEFAULT_HEIGHT, 80));
        list.add(new ToggleWidgetSetting("shadow", translatable("widgets.widgets.basictext.textshadow"), true));
        list.add(new EnumWidgetSetting<>("alignment", translatable("widgets.widgets.basictext.alignment"), TextAlignment.class, TextAlignment.CENTER, TextAlignment::displayText));
        list.add(new IntSliderWidgetSetting("padding", translatable("widgets.widgets.basictext.padding"), 0, 5, 20));
        list.add(new EnumWidgetSetting<>("text_style", translatable("widgets.widgets.basictext.textstyle"), TextStyle.class, TextStyle.PLAIN, TextStyle::displayText));
        return list;
    }
    protected BasicTextWidget(Identifier id, List<WidgetSettingOption<?>> otherCustomOptions) {
        super(id, getCustomSettings(otherCustomOptions));
        getSettings().optionById("padding").setShowCondition(() -> this.textAlignment != TextAlignment.CENTER);
    }

    protected static final int
            DEFAULT_WIDTH = 80,
            DEFAULT_HEIGHT = 9 + 12;

    protected int width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT;
    protected GradientOptions backgroundColor, textColor;
    protected TextAlignment textAlignment = TextAlignment.CENTER;
    protected TextStyle textStyle = TextStyle.PLAIN;

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void renderScaled(DrawContext context, long n, TextRenderer textRenderer, int posX, int posY) {
        if (!shouldRender) return;
        renderer = textRenderer;
        this.backgroundColor.fillHorizontal(context, n, posX, posY, posX + width(), posY + height());
        Matrix3x2fStack matrices = context.getMatrices()
                .pushMatrix();
        matrices.translate(posX + textX, posY + textY, matrices);
        this.textColor.drawText(context, textRenderer, n, renderText, 0, 0, textShadow);
        matrices.popMatrix();
    }

    @Override
    public final void tick() {
        tickWidget();
        if (renderer != null){
            int textWidth = renderer.getWidth(renderText);
            switch (textAlignment){
                case LEFT -> textX = padding;
                case CENTER -> {
                    if (textShadow){
                        textX = (width() - textWidth) / 2f;
                    } else {
                        textX = (width() - textWidth + 1) / 2f;
                    }
                }
                case RIGHT -> textX = width() - padding - textWidth;
            }
            float textHeight = textShadow ? 8 : 7;
            if (WidgetRenderer.guiScale == 1 && size <= 1){
                textY = (int) ((height() - textHeight) / 2);
            } else {
                textY = (height() - textHeight) / 2f;
            }
        }
    }

    protected void formatAndSetRenderText(Text renderText) {
        if (textStyle != TextStyle.PLAIN){
            this.renderText = textStyle.operator.apply(renderText);
        } else {
            this.renderText = renderText;
        }
    }

    public abstract void tickWidget();

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.backgroundColor = (GradientOptions) settings.optionById("backgroundcolor").getValue();
        this.textColor = (GradientOptions) settings.optionById("textcolor").getValue();
        this.width = (int) settings.optionById("width").getValue();
        this.height = (int) settings.optionById("height").getValue();
        this.textAlignment = (TextAlignment) settings.optionById("alignment").getValue();
        this.padding = (int) settings.optionById("padding").getValue();
        this.textShadow = (boolean) settings.optionById("shadow").getValue();
        this.textStyle = (TextStyle) settings.optionById("text_style").getValue();
    }
}
