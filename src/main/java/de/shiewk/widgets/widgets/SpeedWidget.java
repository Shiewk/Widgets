package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.utils.WidgetUtils;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import static de.shiewk.widgets.utils.WidgetUtils.reduceDigits;

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
                        Component.translatable("widgets.widgets.speed.unit"),
                        Unit.class,
                        Unit.METERS_PER_SECOND,
                        unit -> Component.nullToEmpty(unit.displayName)
                ),
                new ToggleWidgetSetting("with_x", Component.translatable("widgets.widgets.speed.withX"), true),
                new ToggleWidgetSetting("with_y", Component.translatable("widgets.widgets.speed.withY"), false),
                new ToggleWidgetSetting("with_z", Component.translatable("widgets.widgets.speed.withZ"), true),
                new IntSliderWidgetSetting("digits", Component.translatable("widgets.widgets.speed.digits"), 0, 1, 3),
                new IntSliderWidgetSetting("window_size", Component.translatable("widgets.widgets.speed.windowSize"), 3, 10, 60)
        ));
    }

    private Unit unit = Unit.METERS_PER_SECOND;
    private boolean withXVelocity = true;
    private boolean withYVelocity = false;
    private boolean withZVelocity = true;
    private int digitsAfterComma = 1;
    private double[] averagingWindow = new double[10];
    private int windowPointer = 0;

    private Vec3 lastPos = new Vec3(0, 0, 0);

    @Override
    public void tickWidget() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Vec3 lastPos = this.lastPos;
            Vec3 newPos = this.lastPos = player.position();
            Vec3 velocity = lastPos.subtract(newPos);
            double rt = 0;
            if (withXVelocity) rt += velocity.x() * velocity.x();
            if (withYVelocity) rt += velocity.y() * velocity.y();
            if (withZVelocity) rt += velocity.z() * velocity.z();
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
        formatAndSetRenderText(reduceDigits(avg, digitsAfterComma) + unit.displayName);
    }

    @Override
    public Component getName() {
        return Component.translatable("widgets.widgets.speed");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("widgets.widgets.speed.description");
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
