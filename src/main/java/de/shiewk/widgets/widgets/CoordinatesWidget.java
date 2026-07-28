package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.widgets.settings.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

import static net.minecraft.network.chat.Component.translatable;

public class CoordinatesWidget extends ResizableWidget {

    @SuppressWarnings("unused") // used through reflection
    public enum Layout {
        XZ("XZ", new char[][]{{'X', 'Z'}}),
        XYZ("XYZ", new char[][]{{'X', 'Y', 'Z'}}),
        XZD("XZD", new char[][]{{'X', 'Z', 'D'}}),
        XYZD("XYZD", new char[][]{{'X', 'Y', 'Z', 'D'}}),
        X_Z("X / Z", new char[][]{{'X'}, {'Z'}}),
        X_Y_Z("X / Y / Z", new char[][]{{'X'}, {'Y'}, {'Z'}}),
        X_Z_D("X / Z / D", new char[][]{{'X'}, {'Z'}, {'D'}}),
        X_Y_Z_D("X / Y / Z / D", new char[][]{{'X'}, {'Y'}, {'Z'}, {'D'}}),
        XZ_D("XZ / D", new char[][]{{'X', 'Z'}, {'D'}}),
        XYZ_D("XYZ / D", new char[][]{{'X', 'Y', 'Z'}, {'D'}});

        public final String displayName;
        public final char[][] layout;

        Layout(String displayName, char[][] layout) {
            this.displayName = displayName;
            this.layout = layout;
        }

        public Component displayName(){
            return Component.literal(displayName);
        }

        public int lineCount(){
            return layout.length;
        }

        public boolean hasComponent(char comp){
            for (char[] chars : this.layout) {
                for (char c : chars) {
                    if (c == comp) return true;
                }
            }
            return false;
        }

        public boolean hasDirection(){
            return hasComponent('D');
        }
    }

    public CoordinatesWidget(Identifier id) {
        super(id, List.of(
                new EnumWidgetSetting<>(
                        "layout",
                        translatable("widgets.widgets.coordinates.layout"),
                        Layout.class,
                        Layout.X_Y_Z,
                        Layout::displayName
                ),
                new EnumWidgetSetting<>(
                        "directionFormat",
                        translatable("widgets.widgets.coordinates.directionFormat"),
                        DirectionWidget.DisplayFormat.class,
                        DirectionWidget.DisplayFormat.DIRECTION_YAW,
                        DirectionWidget.DisplayFormat::formatDefault
                ),
                new ToggleWidgetSetting("directionIntermediate", translatable("widgets.widgets.direction.intermediate"), false),
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
        getSettings().optionById("directionIntermediate").setShowCondition(() -> this.layout.hasDirection());
        getSettings().optionById("directionFormat").setShowCondition(() -> this.layout.hasDirection());
        //noinspection unchecked (always works)
        ((EnumWidgetSetting<DirectionWidget.DisplayFormat>) getSettings().optionById("directionFormat"))
                .setNameFunction(f -> f.format(this.intermediateDirections));

        getSettings().optionById("hiddenX").setShowCondition(() -> this.hideCoordinates);
        getSettings().optionById("hiddenY").setShowCondition(() -> this.hideCoordinates);
        getSettings().optionById("hiddenZ").setShowCondition(() -> this.hideCoordinates);
    }

    public record Line(String label, String value, int xLabel, int xValue) {}

    // these are computed by the tick() method and used by the rendering
    protected List<Line> lines = new ObjectArrayList<>(0);

    @Override
    public void renderScaled(GuiGraphicsExtractor context, long mt, Font textRenderer, int posX, int posY) {
        this.backgroundColor.fillHorizontal(context, mt, posX, posY, posX + width(), posY + height());
        int y = this.paddingY + 1;
        for (Line line : lines) {
            this.textColor.drawText(context, textRenderer, mt, line.label, posX + line.xLabel, posY + y, shadow);
            this.textColor.drawText(context, textRenderer, mt, line.value, posX + line.xValue, posY + y, shadow);
            y += 11;
        }
    }

    @Override
    public void tick() {
        final Font textRenderer = Minecraft.getInstance().font;
        final LocalPlayer player = Minecraft.getInstance().player;

        ObjectArrayList<Line> lines = new ObjectArrayList<>(layout.lineCount());
        for (char[] lineContent : layout.layout) {
            StringBuilder label = new StringBuilder(5);
            StringBuilder value = new StringBuilder(64);

            for (int i = 0; i < lineContent.length; i++) {
                char c = lineContent[i];
                label.append(c);
                if (i != 0){
                    value.append(", ");
                }
                value.append(
                    switch (c) {
                        case 'X' -> {
                            if (hideCoordinates){
                                yield textHiddenX;
                            } else if (player == null){
                                yield "?";
                            } else {
                                yield player.getBlockX();
                            }
                        }
                        case 'Y' -> {
                            if (hideCoordinates){
                                yield textHiddenY;
                            } else if (player == null){
                                yield "?";
                            } else {
                                yield player.getBlockY();
                            }
                        }
                        case 'Z' -> {
                            if (hideCoordinates){
                                yield textHiddenZ;
                            } else if (player == null){
                                yield "?";
                            } else {
                                yield player.getBlockZ();
                            }
                        }
                        case 'D' -> directionFormat.format(intermediateDirections).getString();
                        default -> throw new IllegalStateException("Unexpected layout element: " + c);
                    }
                );
            }

            label.append(':');
            String finalValue = value.toString();
            lines.add(new Line(label.toString(), finalValue, this.paddingX, width() - textRenderer.width(finalValue) - paddingX));
        }

        this.lines = lines;
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
    protected boolean intermediateDirections, hideCoordinates, shadow;
    protected String textHiddenX, textHiddenY, textHiddenZ;
    protected DirectionWidget.DisplayFormat directionFormat;
    protected Layout layout;

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.backgroundColor = (GradientOptions) settings.optionById("backgroundcolor").getValue();
        this.textColor = (GradientOptions) settings.optionById("textcolor").getValue();
        this.layout = (Layout) settings.optionById("layout").getValue();
        this.intermediateDirections = (boolean) settings.optionById("directionIntermediate").getValue();
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
        return 2 * paddingY + (layout.lineCount() * 11) - 1;
    }
}
