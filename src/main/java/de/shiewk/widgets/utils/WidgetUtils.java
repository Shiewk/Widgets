package de.shiewk.widgets.utils;

import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.TickRateManager;

public class WidgetUtils {

    public static final BooleanSupplier TRUE_SUPPLIER = () -> true;

    public static double computeEasing(double x) {
        return 1d - Math.pow(1d - x, 3.5d);
    }

    public static boolean isInSingleplayer(){
        return Minecraft.getInstance().isLocalServer();
    }

    public static float getClientTickRate(){
        float tickRate = 20f;
        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            TickRateManager tickManager = client.level.tickRateManager();
            if (!tickManager.isFrozen()){
                tickRate = Math.min(tickManager.tickrate(), 20);
            }
        }
        return tickRate;
    }

    public static void playSound(SoundEvent ev){
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ev, 1f, 1f));
    }

    public static int fadeColor(int color1, int color2, double delta) {
        int alpha = (int) Mth.lerp(delta, (color1 >> 24) & 0xff, (color2 >> 24) & 0xff);
        int red = (int) Mth.lerp(delta, (color1 >> 16) & 0xff, (color2 >> 16) & 0xff);
        int green = (int) Mth.lerp(delta, (color1 >> 8) & 0xff, (color2 >> 8) & 0xff);
        int blue = (int) Mth.lerp(delta, color1 & 0xff, color2 & 0xff);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static String colorARGBToHexRGBA(int color) {
        int a = color >> 24 & 0xff;
        int r = color >> 16 & 0xff;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;
        return toHexSingle(r) + toHexSingle(g) + toHexSingle(b) + toHexSingle(a);
    }

    private static String toHexSingle(int comp){
        String s = Integer.toHexString(comp);
        return "0".repeat(2 - s.length()) + s;
    }

    public static String reduceDigits(double v, int digits) {
        if (digits == 0) return String.valueOf((int) Math.floor(v));
        double f = Math.pow(10, digits);
        return String.valueOf(Math.floor(v * f) / f);
    }

}
