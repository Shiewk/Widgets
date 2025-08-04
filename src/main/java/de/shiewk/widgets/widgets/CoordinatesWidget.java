package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.RGBAColorWidgetSetting;
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
                new RGBAColorWidgetSetting("backgroundcolor", Text.translatable("widgets.widgets.basictext.background"), 0, 0, 0, 80),
                new RGBAColorWidgetSetting("textcolor", Text.translatable("widgets.widgets.basictext.textcolor"), 255, 255, 255, 255),
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
    public void renderScaled(DrawContext context, long measuringTimeNano, TextRenderer textRenderer, int posX, int posY) {
        context.fill(posX, posY, posX + width(), posY + height(), this.backgroundColor);
        int y = this.paddingY;
        if (showX){
            y++;
            context.drawText(textRenderer, "X: ", posX + paddingX, posY + y, textColor, shadow);
            context.drawText(textRenderer, textX, posX + txc, posY + y, textColor, shadow);
            y += textRenderer.fontHeight + 1;
        }
        if (showY){
            y++;
            context.drawText(textRenderer, "Y: ", posX + paddingX, posY + y, textColor, shadow);
            context.drawText(textRenderer, textY, posX + tyc, posY + y, textColor, shadow);
            y += textRenderer.fontHeight + 1;
        }
        if (showZ){
            y++;
            context.drawText(textRenderer, "Z: ", posX + paddingX, posY + y, textColor, shadow);
            context.drawText(textRenderer, textZ, posX + tzc, posY + y, textColor, shadow);
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

    protected static final int
            WIDTH = 80,
            PADDING = 6,
            DEFAULT_BACKGROUND_COLOR = new Color(0, 0, 0, 80).getRGB(),
            DEFAULT_TEXT_COLOR = new Color(255, 255 ,255, 255).getRGB();

    protected int backgroundColor = DEFAULT_BACKGROUND_COLOR, textColor = DEFAULT_TEXT_COLOR, paddingX = PADDING, paddingY = PADDING, width = WIDTH;
    protected boolean showX = true, showY = true, showZ = true;

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.backgroundColor = ((RGBAColorWidgetSetting) settings.optionById("backgroundcolor")).getColor();
        this.textColor = ((RGBAColorWidgetSetting) settings.optionById("textcolor")).getColor();
        this.showX = ((ToggleWidgetSetting) settings.optionById("x")).getValue();
        this.showY = ((ToggleWidgetSetting) settings.optionById("y")).getValue();
        this.showZ = ((ToggleWidgetSetting) settings.optionById("z")).getValue();
        this.paddingX = ((IntSliderWidgetSetting) settings.optionById("paddingX")).getValue();
        this.paddingY = ((IntSliderWidgetSetting) settings.optionById("paddingY")).getValue();
        this.width = ((IntSliderWidgetSetting) settings.optionById("width")).getValue();
        this.shadow = ((ToggleWidgetSetting) settings.optionById("shadow")).getValue();
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
