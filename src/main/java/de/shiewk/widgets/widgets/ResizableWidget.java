package de.shiewk.widgets.widgets;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.WidgetSettingOption;
import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;

public abstract class ResizableWidget extends ModWidget {

    protected ResizableWidget(Identifier id, List<WidgetSettingOption> customSettings) {
        super(id, addScaleSetting(customSettings));
    }

    private static List<WidgetSettingOption> addScaleSetting(List<WidgetSettingOption> target) {
        ArrayList<WidgetSettingOption> settings = new ArrayList<>(target);
        settings.add(new IntSliderWidgetSetting("size", Text.translatable("widgets.widgets.common.sizePercent"), 25, 100, 400));
        return settings;
    }

    private float size = 1f;

    @Override
    public final void render(DrawContext context, long measuringTimeNano, TextRenderer textRenderer, int posX, int posY) {
        if (size != 1f){
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(-(size-1) * posX, -(size-1) * posY, matrices);
            matrices.scale(size, size, matrices);
        }
        this.renderScaled(context, measuringTimeNano, textRenderer, posX, posY);
        if (size != 1f) context.getMatrices().popMatrix();
    }

    public abstract void renderScaled(DrawContext context, long measuringTimeNano, TextRenderer textRenderer, int posX, int posY);

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        this.size = 0.01f * ((IntSliderWidgetSetting) settings.optionById("size")).getValue();
    }

    @Override
    public final float getScaleFactor() {
        return size;
    }
}
