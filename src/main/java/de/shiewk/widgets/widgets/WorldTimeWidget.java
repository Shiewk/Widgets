package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

import static net.minecraft.text.Text.*;

public class WorldTimeWidget extends BasicTextWidget {

    public WorldTimeWidget(Identifier id) {
        super(id, List.of(
                new ToggleWidgetSetting("show_day", translatable("widgets.widgets.worldtime.showDay"), true),
                new EnumWidgetSetting<>(
                        "time_format",
                        translatable("widgets.widgets.worldtime.timeFormat"),
                        ClockWidget.TimeOption.class,
                        ClockWidget.TimeOption.HOUR_24,
                        ClockWidget.TimeOption::getName
                )
        ));
    }

    protected boolean showDay;
    protected ClockWidget.TimeOption timeFormat;

    public Text getDayLabel(long day) {
        return translatable("widgets.widgets.worldtime.day", day);
    }

    public Text getTimeLabel(long hour, long minute) {
        return switch (timeFormat) {
            case NO_TIME -> empty();
            case HOUR_24 -> literal(hour + ":" + (minute < 10 ? "0" + minute : minute));
            case AM_PM -> {
                long displayHour = (hour % 12 == 0) ? 12 : hour % 12;
                String suffix = (hour < 12) ? " AM" : " PM";
                yield literal(displayHour + ":" + (minute < 10 ? "0" + minute : minute) + suffix);
            }
        };
    }

    @Override
    public void tickWidget() {
        final World world = MinecraftClient.getInstance().world;
        if (world == null) {
            formatAndSetRenderText("?");
        } else {
            long time = world.getTimeOfDay() + 6000;
            long day = time / 24000;
            long hour = time / 1000 % 24;
            long minute = (long) ((time % 1000) / 16.6666);
            if (showDay && timeFormat != ClockWidget.TimeOption.NO_TIME) {
                formatAndSetRenderText(translatable("widgets.widgets.worldtime.dayAndTime", getDayLabel(day), getTimeLabel(hour, minute)).getString());
            } else if (showDay) {
                formatAndSetRenderText(getDayLabel(day).getString());
            } else if (timeFormat != ClockWidget.TimeOption.NO_TIME) {
                formatAndSetRenderText(getTimeLabel(hour, minute).getString());
            } else {
                formatAndSetRenderText(getName());
            }
        }
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.showDay = (boolean) settings.optionById("show_day").getValue();
        this.timeFormat = (ClockWidget.TimeOption) settings.optionById("time_format").getValue();
    }

    @Override
    public Text getName() {
        return translatable("widgets.widgets.worldtime");
    }

    @Override
    public Text getDescription() {
        return translatable("widgets.widgets.worldtime.description");
    }

}
