package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.utils.WidgetUtils;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public class DirectionWidget extends BasicTextWidget {

    public enum DisplayFormat {
        YAW_ONLY(true),
        DIRECTION_ONLY(false),
        YAW_DIRECTION(true),
        DIRECTION_YAW(true),
        DIRECTION_SHORT(false),
        DIRECTION_SHORT_YAW(true),
        YAW_DIRECTION_SHORT(true);

        public final boolean showsYaw;

        DisplayFormat(boolean showsYaw) {
            this.showsYaw = showsYaw;
        }

        public Component format(int digits, boolean intermediateDirections) {
            String yaw = "0";
            String direction = "unknown";
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                float numYaw = Mth.wrapDegrees(player.getYRot());
                yaw = WidgetUtils.reduceDigits(numYaw, digits);
                if (intermediateDirections){
                    // ---- 180/-180 ----  45 degrees per direction (360/8)
                    // -       N        -  N: -180 until -157.5
                    // -    NW    NE    -  NE: until -112.5; E: until -67.5
                    // 90 W    +     E -90 SE: until -22.5; S: until 22.5
                    // -    SW    SE    -  SW: until 67.5; W: until 112.5
                    // -       S        -  NW: until 157.5; N: the rest
                    // ------- 0 --------
                    numYaw += 180; // Start (N) is now at 0; end at 360
                    numYaw += 22.5f; // because north is short
                    String[] directions = {
                            "north", "northeast", "east", "southeast",
                            "south", "southwest", "west", "northwest",
                            "north"
                    };
                    direction = directions[(int) (numYaw / 45)];
                } else {
                    direction = player.getDirection().name().toLowerCase(Locale.ROOT);
                }
            }
            return switch (this){
                case YAW_ONLY -> literal(yaw);
                case DIRECTION_ONLY -> translatable("widgets.widgets.direction." + direction);
                case YAW_DIRECTION -> literal(yaw+" (").append(translatable("widgets.widgets.direction." + direction)).append(")");
                case DIRECTION_YAW -> translatable("widgets.widgets.direction." + direction).append(" ("+yaw+")");
                case DIRECTION_SHORT -> translatable("widgets.widgets.direction.short." + direction);
                case DIRECTION_SHORT_YAW -> translatable("widgets.widgets.direction.short." + direction).append(" ("+yaw+")");
                case YAW_DIRECTION_SHORT -> literal(yaw+" (").append(translatable("widgets.widgets.direction.short." + direction)).append(")");
            };
        }

        public Component format(boolean intermediateDirections){
            return format(1, intermediateDirections);
        }

        public Component formatDefault(){
            return format(false);
        }

    }

    protected DisplayFormat displayFormat;
    protected int digits = 1;
    protected boolean realtime = false;
    protected boolean intermediate = false;

    public DirectionWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("intermediate", translatable("widgets.widgets.direction.intermediate"), false),
                new EnumWidgetSetting<>(
                        "format",
                        translatable("widgets.widgets.direction.display"),
                        DisplayFormat.class,
                        DisplayFormat.DIRECTION_YAW,
                        DisplayFormat::formatDefault
                ),
                new IntSliderWidgetSetting("digits", translatable("widgets.widgets.direction.digits"), 0, 1, 3),
                new ToggleWidgetSetting("realtime", translatable("widgets.widgets.common.realtime"), false)
        ));
        getSettings().optionById("digits").setShowCondition(() -> displayFormat.showsYaw);

        //noinspection unchecked (always works)
        ((EnumWidgetSetting<DisplayFormat>) getSettings().optionById("format"))
                .setNameFunction(f -> f.format(this.intermediate));
    }

    @Override
    public void tickWidget() {
        if (!realtime) refresh();
    }

    @Override
    public void renderScaled(GuiGraphicsExtractor context, long n, Font textRenderer, int posX, int posY) {
        if (realtime) refresh();
        super.renderScaled(context, n, textRenderer, posX, posY);
    }

    private void refresh() {
        formatAndSetRenderText(displayFormat.format(digits, this.intermediate).getString());
    }

    @Override
    public Component getName() {
        return translatable("widgets.widgets.direction");
    }

    @Override
    public Component getDescription() {
        return translatable("widgets.widgets.direction.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.displayFormat = (DisplayFormat) settings.optionById("format").getValue();
        this.realtime = (boolean) settings.optionById("realtime").getValue();
        this.digits = (int) settings.optionById("digits").getValue();
        this.intermediate = (boolean) settings.optionById("intermediate").getValue();
    }
}
