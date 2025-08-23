package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minecraft.text.Text.literal;

public class CPSWidget extends BasicTextWidget {

    public static class Click {
        public int ticks = 0;
        public final long expiresAt = Util.getMeasuringTimeMs() + 1000;

        @Deprecated(forRemoval = true, since = "1.2.1")
        public int tick(){
            return ticks++;
        }
    }

    private static boolean countLeftClicks = true;
    private static boolean countMiddleClicks = true;
    private static boolean countRightClicks = true;

    private static final ObjectArrayList<Click> leftClicks = new ObjectArrayList<>();
    private static final ObjectArrayList<Click> middleClicks = new ObjectArrayList<>();
    private static final ObjectArrayList<Click> rightClicks = new ObjectArrayList<>();

    public static int getCPSLeft() {
        return leftClicks.size();
    }
    public static int getCPSMiddle() {
        return middleClicks.size();
    }
    public static int getCPSRight() {
        return rightClicks.size();
    }

    private Appearance appearance = Appearance.UNIFIED;

    public enum Appearance {
        SPLIT_PIPE("pipe"),
        SPLIT_SLASH("slash"),
        UNIFIED("unified");

        public final String key;

        Appearance(String key) {
            this.key = key;
        }
    }

    public CPSWidget(Identifier id) {
        super(id, List.of(
                new EnumWidgetSetting<>("appearance", Text.translatable("widgets.widgets.cps.appearance"), Appearance.class, Appearance.UNIFIED, appearance -> Text.translatable("widgets.widgets.cps.appearance."+appearance.key)),
                new ToggleWidgetSetting("left", Text.translatable("widgets.widgets.cps.left"), true),
                new ToggleWidgetSetting("middle", Text.translatable("widgets.widgets.cps.middle"), false),
                new ToggleWidgetSetting("right", Text.translatable("widgets.widgets.cps.right"), true)
        ));
    }

    public static void clickLeft() {
        if (countLeftClicks){
            leftClicks.add(new Click());
        }
    }

    public static void clickMiddle() {
        if (countMiddleClicks){
            middleClicks.add(new Click());
        }
    }

    public static void clickRight() {
        if (countRightClicks){
            rightClicks.add(new Click());
        }
    }

    @Override
    public void tickWidget() {
        int left = 0;
        int right = 0;
        int middle = 0;
        long mtime = Util.getMeasuringTimeMs();
        if (countLeftClicks) {
            leftClicks.removeIf(click -> click.expiresAt <= mtime);
            left = leftClicks.size();
        }
        if (countRightClicks) {
            rightClicks.removeIf(click -> click.expiresAt <= mtime);
            right = rightClicks.size();
        }
        if (countMiddleClicks) {
            middleClicks.removeIf(click -> click.expiresAt <= mtime);
            middle = middleClicks.size();
        }
        formatAndSetRenderText(switch (appearance){
            case UNIFIED -> literal((left + right + middle) + " CPS");
            case SPLIT_PIPE, SPLIT_SLASH -> literal(getClickText(left, middle, right) + " CPS");
        });
    }

    private @NotNull StringBuilder getClickText(int left, int middle, int right) {
        StringBuilder sb = new StringBuilder();
        if (countLeftClicks){
            sb.append(left);
        }
        if (countMiddleClicks){
            if (!sb.isEmpty()){
                sb.append(appearance == Appearance.SPLIT_PIPE ? " | " : "/");
            }
            sb.append(middle);
        }
        if (countRightClicks){
            if (!sb.isEmpty()){
                sb.append(appearance == Appearance.SPLIT_PIPE ? " | " : "/");
            }
            sb.append(right);
        }
        return sb;
    }

    @Override
    public void onSettingsChanged(WidgetSettings settings) {
        super.onSettingsChanged(settings);
        countLeftClicks = ((ToggleWidgetSetting) settings.optionById("left")).getValue();
        countMiddleClicks = ((ToggleWidgetSetting) settings.optionById("middle")).getValue();
        countRightClicks = ((ToggleWidgetSetting) settings.optionById("right")).getValue();
        appearance = (Appearance) ((EnumWidgetSetting<?>) settings.optionById("appearance")).getValue();
    }

    @Override
    public Text getName() {
        return Text.translatable("widgets.widgets.cps");
}

    @Override
    public Text getDescription() {
        return Text.translatable("widgets.widgets.cps.description");
    }
}
