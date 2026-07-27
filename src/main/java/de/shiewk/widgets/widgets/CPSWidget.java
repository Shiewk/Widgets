package de.shiewk.widgets.widgets;

import de.shiewk.widgets.WidgetSettings;
import de.shiewk.widgets.color.GradientOptions;
import de.shiewk.widgets.widgets.settings.EnumWidgetSetting;
import de.shiewk.widgets.widgets.settings.GradientWidgetSetting;
import de.shiewk.widgets.widgets.settings.ToggleWidgetSetting;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public class CPSWidget extends BasicTextWidget {

    private static boolean countLeftClicks = true;
    private static boolean countMiddleClicks = true;
    private static boolean countRightClicks = true;

    // These contain the timestamp of the clicks, NOT the expiry time
    private static final LongArrayList leftClicks = new LongArrayList();
    private static final LongArrayList middleClicks = new LongArrayList();
    private static final LongArrayList rightClicks = new LongArrayList();

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
    private boolean showLabel = true;
    protected GradientOptions setTextColor;
    protected GradientOptions clickColor;

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
                new ToggleWidgetSetting("show_label", translatable("widgets.widgets.common.showLabel"), true),
                new EnumWidgetSetting<>("appearance", translatable("widgets.widgets.cps.appearance"), Appearance.class, Appearance.UNIFIED, appearance -> translatable("widgets.widgets.cps.appearance."+appearance.key)),
                new ToggleWidgetSetting("left", translatable("widgets.widgets.cps.left"), true),
                new ToggleWidgetSetting("middle", translatable("widgets.widgets.cps.middle"), false),
                new ToggleWidgetSetting("right", translatable("widgets.widgets.cps.right"), true),
                new GradientWidgetSetting("clickcolor", translatable("widgets.widgets.cps.clickcolor"), 0xff_ff_ff_ff)
        ));
    }

    public static void clickLeft() {
        if (countLeftClicks){
            leftClicks.add(Util.getMillis());
        }
    }

    public static void clickMiddle() {
        if (countMiddleClicks){
            middleClicks.add(Util.getMillis());
        }
    }

    public static void clickRight() {
        if (countRightClicks){
            rightClicks.add(Util.getMillis());
        }
    }

    @Override
    public void tickWidget() {
        this.textColor = isClicking() ? this.clickColor : this.setTextColor;
        int left = 0;
        int right = 0;
        int middle = 0;
        long mtime = Util.getMillis();
        if (countLeftClicks) {
            while (!leftClicks.isEmpty() && leftClicks.getFirst() + 1000 < mtime) {
                leftClicks.removeFirst();
            }
            left = leftClicks.size();
        }
        if (countRightClicks) {
            while (!rightClicks.isEmpty() && rightClicks.getFirst() + 1000 < mtime) {
                rightClicks.removeFirst();
            }
            right = rightClicks.size();
        }
        if (countMiddleClicks) {
            while (!middleClicks.isEmpty() && middleClicks.getFirst() + 1000 < mtime) {
                middleClicks.removeFirst();
            }
            middle = middleClicks.size();
        }
        if (showLabel){
            formatAndSetRenderText(switch (appearance){
                case UNIFIED -> literal((left + right + middle) + " CPS");
                case SPLIT_PIPE, SPLIT_SLASH -> literal(getClickText(left, middle, right) + " CPS");
            });
        } else {
            formatAndSetRenderText(switch (appearance){
                case UNIFIED -> literal(""+(left + right + middle));
                case SPLIT_PIPE, SPLIT_SLASH -> literal(getClickText(left, middle, right).toString());
            });
        }
    }

    private boolean isClicking() {
        MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
        return (countLeftClicks && mouseHandler.isLeftPressed())
                || (countRightClicks && mouseHandler.isRightPressed())
                || (countMiddleClicks && mouseHandler.isMiddlePressed());
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
        countLeftClicks = (boolean) settings.optionById("left").getValue();
        countMiddleClicks = (boolean) settings.optionById("middle").getValue();
        countRightClicks = (boolean) settings.optionById("right").getValue();
        appearance = (Appearance) settings.optionById("appearance").getValue();
        showLabel = (boolean) settings.optionById("show_label").getValue();
        setTextColor = (GradientOptions) settings.optionById("textcolor").getValue();
        clickColor = (GradientOptions) settings.optionById("clickcolor").getValue();
    }

    @Override
    public Component getName() {
        return translatable("widgets.widgets.cps");
    }

    @Override
    public Component getDescription() {
        return translatable("widgets.widgets.cps.description");
    }
}
