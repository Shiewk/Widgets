package de.shiewk.widgets.utils;

import net.minecraft.client.MinecraftClient;
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

}
