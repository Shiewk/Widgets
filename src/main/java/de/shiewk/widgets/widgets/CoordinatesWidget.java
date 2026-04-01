package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.widgets.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

import static net.minecraft.network.chat.Component.translatable;

public class CoordinatesWidget extends ResizableWidget {
    public CoordinatesWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("x", translatable("widgets.widgets.coordinates.showX"), true),
                new ToggleWidgetSetting("y", translatable("widgets.widgets.coordinates.showY"), true),
                new ToggleWidgetSetting("z", translatable("widgets.widgets.coordinates.showZ"), true),
                new ToggleWidgetSetting("direction", translatable("widgets.widgets.coordinates.showDirection"), false),
                new EnumWidgetSetting<>(
                        "directionFormat",
                        translatable("widgets.widgets.coordinates.directionFormat"),
                        DirectionWidget.DisplayFormat.class,
                        DirectionWidget.DisplayFormat.DIRECTION_YAW,
                        DirectionWidget.DisplayFormat::format
                ),
                new ToggleWidgetSetting("hideCoordinates", translatable("widgets.widgets.coordinates.hideCoordinates"), false),
                new TextFieldWidgetSettingOption(
                        "hiddenX",
                        translatable("widgets.widgets.coordinates.hiddenX"),
                        translatable("widgets.widgets.coordinates.hidden"),
                        translatable("widgets.widgets.coordinates.hidden"),
                        true, 32
                ),
                new TextFieldWidgetSettingOption(
                        "hiddenY",
                        translatable("widgets.widgets.coordinates.hiddenY"),
                        translatable("widgets.widgets.coordinates.hidden"),
                        translatable("widgets.widgets.coordinates.hidden"),
                        true, 32
                ),
                new TextFieldWidgetSettingOption(
                        "hiddenZ",
                        translatable("widgets.widgets.coordinates.hiddenZ"),
                        translatable("widgets.widgets.coordinates.hidden"),
                        translatable("widgets.widgets.coordinates.hidden"),
                        true, 32
                ),
                new GradientWidgetSetting("backgroundcolor", translatable("widgets.widgets.basictext.background"), 0x50_00_00_00),
                new GradientWidgetSetting("textcolor", translatable("widgets.widgets.basictext.textcolor"), 0xffffffff),
                new IntSliderWidgetSetting("width", translatable("widgets.widgets.basictext.width"), 10, WIDTH, 80*3),
                new IntSliderWidgetSetting("paddingX", translatable("widgets.widgets.basictext.paddingX"), 0, 5, 20),
                new IntSliderWidgetSetting("paddingY", translatable("widgets.widgets.basictext.paddingY"), 0, 5, 20),
                new ToggleWidgetSetting("shadow", translatable("widgets.widgets.basictext.textshadow"), true)
        ));
        getSettings().optionById("directionFormat").setShowCondition(() -> this.showDirection);
        getSettings().optionById("hiddenX").setShowCondition(() -> this.hideCoordinates);
        getSettings().optionById("hiddenY").setShowCondition(() -> this.hideCoordinates);
        getSettings().optionById("hiddenZ").setShowCondition(() -> this.hideCoordinates);
    }

    protected String textX = "X", textY = "Y", textZ = "Z", textDirection = "direction";
    protected String textHiddenX, textHiddenY, textHiddenZ;
    protected int txc = 0, tyc = 0, tzc = 0, tdc = 0;
    protected boolean shadow = true, hideCoordinates = false;
    protected DirectionWidget.DisplayFormat directionFormat;

    @Override
    public void renderScaled(GuiGraphicsExtractor context, long mt, Font textRenderer, int posX, int posY) {
        this.backgroundColor.fillHorizontal(context, mt, posX, posY, posX + width(), posY + height());
        int y = this.paddingY + 1;
        if (showX){
            this.textColor.drawText(context, textRenderer, mt, "X:", posX + paddingX, posY + y, shadow);
            this.textColor.drawText(context, textRenderer, mt, textX, posX + txc, posY + y, shadow);
            y += 11;
        }
        if (showY){
            this.textColor.drawText(context, textRenderer, mt, "Y:", posX + paddingX, posY + y, shadow);
            this.textColor.drawText(context, textRenderer, mt, textY, posX + tyc, posY + y, shadow);
            y += 11;
        }
        if (showZ){
            this.textColor.drawText(context, textRenderer, mt, "Z:", posX + paddingX, posY + y, shadow);
            this.textColor.drawText(context, textRenderer, mt, textZ, posX + tzc, posY + y, shadow);
            y += 11;
        }
        if (showDirection){
            this.textColor.drawText(context, textRenderer, mt, "D:", posX + paddingX, posY + y, shadow);
            this.textColor.drawText(context, textRenderer, mt, textDirection, posX + tdc, posY + y, shadow);
        }
    }

    @Override
    public void tick() {
        final Font textRenderer = Minecraft.getInstance().font;
        final LocalPlayer player = Minecraft.getInstance().player;
        if (hideCoordinates){
            textX = textHiddenX;
            textY = textHiddenY;
            textZ = textHiddenZ;
        } else if (player == null){
            textX = "?";
            textY = "?";
            textZ = "?";
        } else {
            textX = String.valueOf(player.getBlockX());
            textY = String.valueOf(player.getBlockY());
            textZ = String.valueOf(player.getBlockZ());
        }
        textDirection = directionFormat.format().getString();

        txc = width() - textRenderer.width(textX) - paddingX;
        tyc = width() - textRenderer.width(textY) - paddingX;
        tzc = width() - textRenderer.width(textZ) - paddingX;
        tdc = width() - textRenderer.width(textDirection) - paddingX;
    }

    @Override
    public Component getName() {
        return translatable("widgets.widgets.coordinates");
    }

    @Override
    public Component getDescription() {
        return translatable("widgets.widgets.coordinates.description");
    }

    protected static final int WIDTH = 80, PADDING = 6;

    protected GradientOptions backgroundColor, textColor;
    protected int paddingX = PADDING, paddingY = PADDING, width = WIDTH;
    protected boolean showX = true, showY = true, showZ = true, showDirection = false;

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.backgroundColor = (GradientOptions) settings.optionById("backgroundcolor").getValue();
        this.textColor = (GradientOptions) settings.optionById("textcolor").getValue();
        this.showX = (boolean) settings.optionById("x").getValue();
        this.showY = (boolean) settings.optionById("y").getValue();
        this.showZ = (boolean) settings.optionById("z").getValue();
        this.showDirection = (boolean) settings.optionById("direction").getValue();
        this.directionFormat = (DirectionWidget.DisplayFormat) settings.optionById("directionFormat").getValue();
        this.hideCoordinates = (boolean) settings.optionById("hideCoordinates").getValue();
        this.textHiddenX = (String) settings.optionById("hiddenX").getValue();
        this.textHiddenY = (String) settings.optionById("hiddenY").getValue();
        this.textHiddenZ = (String) settings.optionById("hiddenZ").getValue();
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
        if (showDirection) height += 11;
        return height;
    }
}
