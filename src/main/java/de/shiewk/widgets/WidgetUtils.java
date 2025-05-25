package de.shiewk.widgets;

import net.minecraft.client.MinecraftClient;

import java.util.function.BooleanSupplier;

public class WidgetUtils {

    public static final BooleanSupplier TRUE_SUPPLIER = () -> true;

    public static double translateToWidgetSettingsValue(double value, int max){
        return (value / max) * 100;
    }

    public static double translateToScreen(double value, int max){
        return value / 100d * max;
    }

    public static double computeEasing(double x) {
        return 1d - Math.pow(1d - x, 3.5d);
    }

    public static boolean isInSingleplayer(){
        return MinecraftClient.getInstance().isInSingleplayer();
    }

}
