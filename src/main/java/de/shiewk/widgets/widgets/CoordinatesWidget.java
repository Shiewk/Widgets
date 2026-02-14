package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.widgets.settings.GradientWidgetSetting;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.List;

public class CoordinatesWidget extends ResizableWidget {
    public CoordinatesWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("x", Text.translatable("widgets.widgets.coordinates.showX"), true),
                new ToggleWidgetSetting("y", Text.translatable("widgets.widgets.coordinates.showY"), true),
                new ToggleWidgetSetting("z", Text.translatable("widgets.widgets.coordinates.showZ"), true),
                new GradientWidgetSetting("backgroundcolor", Text.translatable("widgets.widgets.basictext.background"), 0x50_00_00_00),
                new GradientWidgetSetting("textcolor", Text.translatable("widgets.widgets.basictext.textcolor"), 0xffffffff),
                new IntSliderWidgetSetting("width", Text.translatable("widgets.widgets.basictext.width"), 10, WIDTH, 80*3),
                new IntSliderWidgetSetting("paddingX", Text.translatable("widgets.widgets.basictext.paddingX"), 0, 5, 20),
                new IntSliderWidgetSetting("paddingY", Text.translatable("widgets.widgets.basictext.paddingY"), 0, 5, 20),
                new ToggleWidgetSetting("shadow", Text.translatable("widgets.widgets.basictext.textshadow"), true)
        ));
    }

    private String textX = "X", textY = "Y", textZ = "Z";
    private int txc = 0, tyc = 0, tzc = 0;
    private boolean shadow = true;

    @Override
    public void renderScaled(DrawContext context, long mt, TextRenderer textRenderer, int posX, int posY) {
        this.backgroundColor.fillHorizontal(context, mt, posX, posY, posX + width(), posY + height());
        int y = this.paddingY;
        if (showX){
            y++;
            this.textColor.drawText(context, textRenderer, mt, "X: ", posX + paddingX, posY + y, shadow);
            this.textColor.drawText(context, textRenderer, mt, textX, posX + txc, posY + y, shadow);
            y += textRenderer.fontHeight + 1;
        }
        if (showY){
            y++;
            this.textColor.drawText(context, textRenderer, mt, "Y: ", posX + paddingX, posY + y, shadow);
            this.textColor.drawText(context, textRenderer, mt, textY, posX + tyc, posY + y, shadow);
            y += textRenderer.fontHeight + 1;
        }
        if (showZ){
            y++;
            this.textColor.drawText(context, textRenderer, mt, "Z: ", posX + paddingX, posY + y, shadow);
            this.textColor.drawText(context, textRenderer, mt, textZ, posX + tzc, posY + y, shadow);
        }
    }

    @Override
    public void tick() {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        txc = width() - textRenderer.getWidth(textX) - paddingX;
        tyc = width() - textRenderer.getWidth(textY) - paddingX;
        tzc = width() - textRenderer.getWidth(textZ) - paddingX;

        final ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null){
            textX = "?";
            textY = "?";
            textZ = "?";
        } else {
            textX = String.valueOf(player.getBlockX());
            textY = String.valueOf(player.getBlockY());
            textZ = String.valueOf(player.getBlockZ());
        }
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.coordinates");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.coordinates.description");
    }

    protected static final int WIDTH = 80, PADDING = 6;

    protected GradientOptions backgroundColor, textColor;
    protected int paddingX = PADDING, paddingY = PADDING, width = WIDTH;
    protected boolean showX = true, showY = true, showZ = true;

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.backgroundColor = (GradientOptions) settings.optionById("backgroundcolor").getValue();
        this.textColor = (GradientOptions) settings.optionById("textcolor").getValue();
        this.showX = (boolean) settings.optionById("x").getValue();
        this.showY = (boolean) settings.optionById("y").getValue();
        this.showZ = (boolean) settings.optionById("z").getValue();
        this.paddingX = (int) settings.optionById("paddingX").getValue();
        this.paddingY = (int) settings.optionById("paddingY").getValue();
        this.width = (int) settings.optionById("width").getValue();
        this.shadow = (boolean) settings.optionById("shadow").getValue();
    }

    @Override
    public int width() {
        return width + paddingX * 2;
    }

    @Override
    public int height() {
        int height = 2 * paddingY;
        if (showX) height += 11;
        if (showY) height += 11;
        if (showZ) height += 11;
        return height;
    }
}
