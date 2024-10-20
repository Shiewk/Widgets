package de.shiewk.widgets.client;

import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.WidgetSettings;
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

import static de.shiewk.widgets.WidgetUtils.translateToScreen;

public class WidgetRenderer implements HudRenderCallback, ClientTickEvents.StartTick, ClientLifecycleEvents.ClientStarted {

    private static MinecraftClient client;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
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
            final WidgetSettings settings = widget.getSettings();
            widget.render(
                    drawContext,
                    timeNano,
                    textRenderer,
                    (int) Math.round(Math.min(translateToScreen(settings.posX, windowWidth), windowWidth - (widget.width() * widget.getScaleFactor()))),
                    (int) Math.round(Math.min(translateToScreen(settings.posY, windowHeight), windowHeight - (widget.height() * widget.getScaleFactor())))
            );
            profiler.pop();
        }
        profiler.pop();
    }

    @Override
    public void onStartTick(MinecraftClient client) {
        final Profiler profiler = (WidgetRenderer.client = client).getProfiler();
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
}
