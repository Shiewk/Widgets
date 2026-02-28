package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.utils.WidgetUtils;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Locale;

import static net.minecraft.text.Text.*;

public class DirectionWidget extends BasicTextWidget {

    public enum DisplayFormat {
        YAW_ONLY(true),
        DIRECTION_ONLY(false),
        YAW_DIRECTION(true),
        DIRECTION_YAW(true);

        public final boolean showsYaw;

        DisplayFormat(boolean showsYaw) {
            this.showsYaw = showsYaw;
        }

        public Text format(int digits) {
            String yaw = "0";
            MutableText direction = literal("Direction");
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                yaw = WidgetUtils.reduceDigits(MathHelper.wrapDegrees(player.getYaw()), digits);
                direction = translatable("widgets.widgets.direction." + player.getHorizontalFacing().name().toLowerCase(Locale.ROOT));
            }
            return switch (this){
                case YAW_ONLY -> literal(yaw);
                case DIRECTION_ONLY -> direction;
                case YAW_DIRECTION -> literal(yaw+" (").append(direction).append(")");
                case DIRECTION_YAW -> direction.append(" ("+yaw+")");
            };
        }

        public Text format(){
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
                new IntSliderWidgetSetting("digits", Text.translatable("widgets.widgets.direction.digits"), 0, 1, 3),
                new ToggleWidgetSetting("realtime", translatable("widgets.widgets.common.realtime"), false)
        ));
        getSettings().optionById("digits").setShowCondition(() -> displayFormat.showsYaw);
    }

    @Override
    public void tickWidget() {
        if (!realtime) refresh();
    }

    @Override
    public void renderScaled(DrawContext context, long n, TextRenderer textRenderer, int posX, int posY) {
        if (realtime) refresh();
        super.renderScaled(context, n, textRenderer, posX, posY);
    }

    private void refresh() {
        formatAndSetRenderText(displayFormat.format(digits).getString());
    }

    @Override
    public Text getName() {
        return translatable("widgets.widgets.direction");
    }

    @Override
    public Text getDescription() {
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
