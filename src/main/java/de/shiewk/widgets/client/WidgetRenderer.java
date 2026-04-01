package de.shiewk.widgets.client;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.WidgetsMod;
import de.shiewk.widgets.client.screen.WidgetVisibilityToggle;
import de.shiewk.widgets.client.screen.WidgetConfigScreen;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.NonNull;

public class WidgetRenderer implements ClientTickEvents.StartTick, ClientLifecycleEvents.ClientStarted {

    public static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "widgets-hud-layer");
    private static Minecraft client;
    public static int guiScale = 1;

    public WidgetRenderer(){
        HudElementRegistry.addLast(
                LAYER_ID,
                this::renderWidgets
        );
    }

    public void renderWidgets(GuiGraphicsExtractor drawContext, DeltaTracker tickCounter) {
        if (client.options.hideGui) return;
        if (client.screen instanceof WidgetVisibilityToggle vt && !vt.shouldRenderWidgets()) return;
        final ProfilerFiller profiler = Profiler.get();
        profiler.push("widgets");
        final Font textRenderer = client.font;
        final long timeNano = Util.getNanos();
        final int windowWidth = drawContext.guiWidth();
        final int windowHeight = drawContext.guiHeight();

        final ObjectArrayList<ModWidget> enabled = WidgetManager.enabled;
        for (int i = 0, enabledSize = enabled.size(); i < enabledSize; i++) {
            final ModWidget widget = enabled.get(i);
            profiler.push(widget.getId().toString());
            widget.render(
                    drawContext,
                    timeNano,
                    textRenderer,
                    widget.getX(windowWidth),
                    widget.getY(windowHeight)
            );
            profiler.pop();
        }
        profiler.pop();
    }

    @Override
    public void onStartTick(Minecraft client) {
        WidgetRenderer.client = client;
        final ProfilerFiller profiler = Profiler.get();
        profiler.push("widgets");
        guiScale = client.getWindow().getGuiScale();

        final ObjectArrayList<ModWidget> enabled = WidgetManager.enabled;
        for (int i = 0, enabledSize = enabled.size(); i < enabledSize; i++) {
            final ModWidget widget = enabled.get(i);
            profiler.push(widget.getId().toString());
            widget.tick();
            profiler.pop();
        }

        profiler.pop();

        if (WidgetsModClient.configKeyBinding.consumeClick()){
            client.setScreen(new WidgetConfigScreen(client.screen));
        }
    }

    @Override
    public void onClientStarted(@NonNull Minecraft client) {
        for (ModWidget widget : WidgetManager.getAllWidgets()) {
            widget.onSettingsChanged();
        }
    }
}
