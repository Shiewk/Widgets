package de.shiewk.widgets.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.tick.TickManager;

import java.util.function.BooleanSupplier;

public class WidgetUtils {

    public static final BooleanSupplier TRUE_SUPPLIER = () -> true;

    public static double computeEasing(double x) {
        return 1d - Math.pow(1d - x, 3.5d);
    }

    public static boolean isInSingleplayer(){
        return MinecraftClient.getInstance().isInSingleplayer();
    }

    public static float getClientTickRate(){
        float tickRate = 20f;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            TickManager tickManager = client.world.getTickManager();
            if (!tickManager.isFrozen()){
                tickRate = Math.min(tickManager.getTickRate(), 20);
            }
        }
        return tickRate;
    }

    public static void playSound(SoundEvent ev){
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(ev, 1f, 1f));
    }

    public static int fadeColor(int color1, int color2, double delta) {
        int alpha = (int) MathHelper.lerp(delta, (color1 >> 24) & 0xff, (color2 >> 24) & 0xff);
        int red = (int) MathHelper.lerp(delta, (color1 >> 16) & 0xff, (color2 >> 16) & 0xff);
        int green = (int) MathHelper.lerp(delta, (color1 >> 8) & 0xff, (color2 >> 8) & 0xff);
        int blue = (int) MathHelper.lerp(delta, color1 & 0xff, color2 & 0xff);
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

}
