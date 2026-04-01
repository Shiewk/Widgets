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

        public Component format(int digits) {
            String yaw = "0";
            String direction = "unknown";
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                yaw = WidgetUtils.reduceDigits(Mth.wrapDegrees(player.getYRot()), digits);
                direction = player.getDirection().name().toLowerCase(Locale.ROOT);
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

        public Component format(){
            return format(1);
        }
    }

    protected DisplayFormat displayFormat;
    protected int digits = 1;
    protected boolean realtime = false;

    public DirectionWidget(Identifier id) {
        super(id, List.of(
                new EnumWidgetSetting<>(
                        "format",
                        translatable("widgets.widgets.direction.display"),
                        DisplayFormat.class,
                        DisplayFormat.DIRECTION_YAW,
                        DisplayFormat::format
                ),
                new IntSliderWidgetSetting("digits", translatable("widgets.widgets.direction.digits"), 0, 1, 3),
                new ToggleWidgetSetting("realtime", translatable("widgets.widgets.common.realtime"), false)
        ));
        getSettings().optionById("digits").setShowCondition(() -> displayFormat.showsYaw);
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
        formatAndSetRenderText(displayFormat.format(digits).getString());
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
    }
}
