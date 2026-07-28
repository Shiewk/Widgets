package de.shiewk.widgets.neoforge;

import de.shiewk.widgets.WidgetsMod;
import de.shiewk.widgets.WidgetRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

@EventBusSubscriber(modid = WidgetsMod.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // this handles everything
        new NeoPlatformInterface();
    }

    @SubscribeEvent
    public static void onClientStarted(ClientStartedEvent event) {
        NeoPlatformInterface.getInstance().getWidgets().onClientStarted();
    }

    @SubscribeEvent
    public static void onPreClientTick(ClientTickEvent.Pre event){
        NeoPlatformInterface.getInstance().getWidgets().onClientStartTick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event){
        NeoPlatformInterface.getInstance().getWidgets().onEntityAttack(event.getTarget().getId());
    }

    @SubscribeEvent
    public static void onWorldSwitch(EntityJoinLevelEvent event){
        if (event.getEntity() == Minecraft.getInstance().player) {
            NeoPlatformInterface.getInstance().getWidgets().onWorldSwitch();
        }
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event){
        event.registerAboveAll(
                WidgetRenderer.LAYER_ID,
                (ctx, _) -> NeoPlatformInterface.getInstance().getWidgets().getRenderer().renderWidgets(ctx)
        );
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event){
        event.getDispatcher().register(
                Commands.literal("widgetsmod")
                        .executes(_ -> NeoPlatformInterface.getInstance().getWidgets().runSettingsCommand())
        );
    }

}
