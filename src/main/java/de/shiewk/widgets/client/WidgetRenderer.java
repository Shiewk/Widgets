package de.shiewk.widgets.client;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.client.screen.EditWidgetPositionsScreen;
import de.shiewk.widgets.client.screen.WidgetConfigScreen;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;

public class WidgetRenderer implements ClientTickEvents.StartTick, ClientLifecycleEvents.ClientStarted, HudRenderCallback {

    private static MinecraftClient client;

    public void renderWidgets(DrawContext drawContext) {
        if (client.options.hudHidden) return;
        if (client.currentScreen instanceof EditWidgetPositionsScreen) return;
        final Profiler profiler = client.getProfiler();
        profiler.push("widgets");
        final TextRenderer textRenderer = client.textRenderer;
        final long timeNano = Util.getMeasuringTimeNano();
        final int windowWidth = drawContext.getScaledWindowWidth();
        final int windowHeight = drawContext.getScaledWindowHeight();

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
    public void onStartTick(MinecraftClient client) {
        WidgetRenderer.client = client;
        final Profiler profiler = client.getProfiler();
        profiler.push("widgets");

        final ObjectArrayList<ModWidget> enabled = WidgetManager.enabled;
        for (int i = 0, enabledSize = enabled.size(); i < enabledSize; i++) {
            final ModWidget widget = enabled.get(i);
            profiler.push(widget.getId().toString());
            widget.tick();
            profiler.pop();
        }

        profiler.pop();

        if (WidgetsModClient.configKeyBinding.wasPressed()){
            client.setScreen(new WidgetConfigScreen(client.currentScreen));
        }
    }

    @Override
    public void onClientStarted(MinecraftClient client) {
        for (ModWidget widget : WidgetManager.getAllWidgets()) {
            widget.onSettingsChanged(widget.getSettings());
        }
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        this.renderWidgets(drawContext);
    }
}
