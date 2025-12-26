package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

import static net.minecraft.text.Text.literal;
import static net.minecraft.text.Text.translatable;

public class PlayTimeWidget extends BasicTextWidget {

    private static final long startTime = System.nanoTime();

    private LabelStyle labelStyle = LabelStyle.NO_LABEL;

    public enum LabelStyle {
        NO_LABEL("none"),
        PLAYTIME("playtime"),
        PLAYED("played");

        public final String key;

        LabelStyle(String key) {
            this.key = key;
        }
    }

    public PlayTimeWidget(Identifier id) {
        super(id, List.of(
                new EnumWidgetSetting<>("labelstyle", translatable("widgets.widgets.playtime.labelStyle"), LabelStyle.class, LabelStyle.NO_LABEL, labelStyle -> translatable("widgets.widgets.playtime.labelStyle."+labelStyle.key))
        ));
    }

    @Override
    public void tickWidget() {
        long timePlayedMs = getPlayedMs();
        formatAndSetRenderText(switch (labelStyle){
            case NO_LABEL -> literal(msToTimeStr(timePlayedMs));
            case PLAYTIME -> literal(translatable("widgets.widgets.playtime.playtime", msToTimeStr(timePlayedMs)).getString());
            case PLAYED -> literal(translatable("widgets.widgets.playtime.played", msToTimeStr(timePlayedMs)).getString());
        });
    }

    private static long getPlayedMs() {
        return (System.nanoTime() - startTime) / 1000000;
    }

    private String msToTimeStr(long timePlayedMs) {
        long seconds = timePlayedMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return formatTimeNumber(hours) + ":" + formatTimeNumber(minutes % 60) + ":" + formatTimeNumber(seconds % 60);
    }

    private String formatTimeNumber(long l) {
        return l < 10 ? "0" + l : String.valueOf(l);
    }

    @Override
    public Text getName() {
        return translatable("widgets.widgets.playtime");
    }

    @Override
    public Text getDescription() {
        return translatable("widgets.widgets.playtime.description");
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        this.labelStyle = (LabelStyle) settings.optionById("labelstyle").getValue();
    }
}
