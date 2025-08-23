package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.IntSliderWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static net.minecraft.text.Text.literal;

public class ClockWidget extends BasicTextWidget {

    public enum TimeOption {
        NO_TIME("none"),
        HOUR_24("24hour"),
        AM_PM("am_pm");

        public final String key;

        TimeOption(String key) {
            this.key = key;
        }
    }

    public enum DateOption {
        NO_DATE(null),
        FULL_MONTH_DAY("MMMM dd"),
        DAY_FULL_MONTH("dd. MMMM"),
        ABB_MONTH_DAY("MMM dd"),
        DAY_ABB_MONTH("dd. MMM"),
        MONTH_DAY("MM/dd"),
        MONTH_DAY_2("dd.MM"),
        FULL_MONTH_DAY_YEAR("MMMM dd, y"),
        ABB_MONTH_DAY_YEAR("MMM dd, y"),
        DAY_FULL_MONTH_YEAR("dd. MMMM y"),
        DAY_ABB_MONTH_YEAR("dd. MMM y"),
        MONTH_DAY_YEAR("MM/dd/y"),
        MONTH_DAY_YEAR_2("dd.MM.y");

        public final String format;

        DateOption(String format) {
            this.format = format;
        }

        public Text getName(){
            return this.format == null ? Text.translatable("widgets.widgets.clock.dateFormat.none") : Text.of(new SimpleDateFormat(format).format(Date.from(Instant.now())));
        }
    }

    public enum WeekOption {
        NO_DAY_OF_WEEK(null),
        ABBREVIATED("E"),
        FULL("EEEE");

        public final String format;

        WeekOption(String format) {
            this.format = format;
        }

        public Text getName(){
            return this.format == null ? Text.translatable("widgets.widgets.clock.weekFormat.none") : Text.of(new SimpleDateFormat(format).format(Date.from(Instant.now())));
        }
    }
    public ClockWidget(Identifier id) {
        super(id, List.of(
                new EnumWidgetSetting<>("hour_format",
                        Text.translatable("widgets.widgets.clock.hourFormat"),
                        TimeOption.class,
                        TimeOption.HOUR_24,
                        timeOption -> Text.translatable("widgets.widgets.clock.hourFormat."+timeOption.key)),
                new ToggleWidgetSetting("show_seconds",
                        Text.translatable("widgets.widgets.clock.showSeconds"),
                        true),
                new EnumWidgetSetting<>("date_format",
                        Text.translatable("widgets.widgets.clock.dateFormat"),
                        DateOption.class,
                        DateOption.NO_DATE,
                        DateOption::getName),
                new EnumWidgetSetting<>("week_format",
                        Text.translatable("widgets.widgets.clock.weekFormat"),
                        WeekOption.class,
                        WeekOption.NO_DAY_OF_WEEK,
                        WeekOption::getName)
        ));
        ((IntSliderWidgetSetting) getSettings().optionById("width")).setMaxValue(80*5);
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss aa EEEE");

    @Override
    public void tickWidget() {
        formatAndSetRenderText(literal(dateFormat.format(Date.from(Instant.now()))));
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        String datePattern = "HH:mm:ss";
        TimeOption timeOption = (TimeOption) ((EnumWidgetSetting<?>) settings.optionById("hour_format")).getValue();
        DateOption dateOption = (DateOption) ((EnumWidgetSetting<?>) settings.optionById("date_format")).getValue();
        WeekOption weekOption = (WeekOption) ((EnumWidgetSetting<?>) settings.optionById("week_format")).getValue();
        boolean showSeconds = ((ToggleWidgetSetting) settings.optionById("show_seconds")).getValue();
        if (timeOption == TimeOption.HOUR_24){
            datePattern = showSeconds ? "HH:mm:ss" : "HH:mm";
        } else if (timeOption == TimeOption.AM_PM){
            datePattern = showSeconds ? "hh:mm:ss aa" : "hh:mm aa";
        } else if (timeOption == TimeOption.NO_TIME){
            datePattern = "";
        }
        if (dateOption.format != null){
            if (datePattern.isEmpty()) {
                datePattern = dateOption.format + datePattern;
            } else {
                datePattern = dateOption.format + " " + datePattern;
            }
        }
        if (weekOption.format != null){
            if (datePattern.isEmpty()){
                datePattern = weekOption.format + datePattern;
            } else {
                datePattern = weekOption.format + ", " + datePattern;
            }
        }
        dateFormat = new SimpleDateFormat(datePattern);
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.clock");
    }

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.clock.description");
    }
}
