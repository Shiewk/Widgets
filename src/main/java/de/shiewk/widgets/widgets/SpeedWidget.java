package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.utils.WidgetUtils;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static net.minecraft.text.Text.literal;

public class SpeedWidget extends BasicTextWidget {

    public enum Unit {
        METERS_PER_SECOND("m/s"),
        BLOCKS_PER_SECOND("b/s"),
        CENTIMETERS_PER_TICK("cm/t");

        public final String displayName;

        Unit(String displayName) {
            this.displayName = displayName;
        }
    }

    public SpeedWidget(Identifier id) {
        super(id, List.of(
                new EnumWidgetSetting<>(
                        "unit",
                        Text.translatable("widgets.widgets.speed.unit"),
                        Unit.class,
                        Unit.METERS_PER_SECOND,
                        unit -> Text.of(unit.displayName)
                ),
                new ToggleWidgetSetting("with_x", Text.translatable("widgets.widgets.speed.withX"), true),
                new ToggleWidgetSetting("with_y", Text.translatable("widgets.widgets.speed.withY"), false),
                new ToggleWidgetSetting("with_z", Text.translatable("widgets.widgets.speed.withZ"), true),
                new IntSliderWidgetSetting("digits", Text.translatable("widgets.widgets.speed.digits"), 0, 1, 3),
                new IntSliderWidgetSetting("window_size", Text.translatable("widgets.widgets.speed.windowSize"), 3, 10, 60)
        ));
    }

    private Unit unit = Unit.METERS_PER_SECOND;
    private boolean withXVelocity = true;
    private boolean withYVelocity = false;
    private boolean withZVelocity = true;
    private int digitsAfterComma = 1;
    private double[] averagingWindow = new double[10];
    private int windowPointer = 0;

    private Vec3d lastPos = new Vec3d(0, 0, 0);

    @Override
    public void tickWidget() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            Vec3d lastPos = this.lastPos;
            Vec3d newPos = this.lastPos = player.getEntityPos();
            Vec3d velocity = lastPos.subtract(newPos);
            double rt = 0;
            if (withXVelocity) rt += velocity.getX() * velocity.getX();
            if (withYVelocity) rt += velocity.getY() * velocity.getY();
            if (withZVelocity) rt += velocity.getZ() * velocity.getZ();
            averagingWindow[windowPointer++] = switch (unit){
                case METERS_PER_SECOND, BLOCKS_PER_SECOND -> Math.sqrt(rt) * WidgetUtils.getClientTickRate();
                case CENTIMETERS_PER_TICK -> Math.sqrt(rt) * 100;
            };
        } else {
            averagingWindow[windowPointer++] = 0d;
        }
        if (windowPointer >= averagingWindow.length) {
            windowPointer = 0;
        }
        double avg = 0;
        for (double v : averagingWindow) {
            avg += v;
        }
        avg /= averagingWindow.length;
        formatAndSetRenderText(literal(reduceDigits(avg) + unit.displayName));
    }

    private String reduceDigits(double v) {
        if (digitsAfterComma == 0) return String.valueOf((int) Math.floor(v));
        double f = Math.pow(10, digitsAfterComma);
        return String.valueOf(Math.floor(v * f) / f);
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.speed");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.speed.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.unit = (Unit) settings.optionById("unit").getValue();
        this.withXVelocity = (boolean) settings.optionById("with_x").getValue();
        this.withYVelocity = (boolean) settings.optionById("with_y").getValue();
        this.withZVelocity = (boolean) settings.optionById("with_z").getValue();
        this.digitsAfterComma = (int) settings.optionById("digits").getValue();
        this.windowPointer = 0;
        this.averagingWindow = new double[(int) settings.optionById("window_size").getValue()];
    }
}
