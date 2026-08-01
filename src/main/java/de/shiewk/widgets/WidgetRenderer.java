package de.shiewk.widgets;

import de.shiewk.widgets.screen.WidgetVisibilityToggle;
import de.shiewk.widgets.screen.WidgetConfigScreen;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public class WidgetRenderer {

    public static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(WidgetsMod.MOD_ID, "widgets-hud-layer");
    private static Minecraft client;
    public static int guiScale = 1;

    // Should be registered as a hud layer
	public void renderWidgets(GuiGraphicsExtractor drawContext) {
        if (client.options.hideGui) return;
        if (client.debugEntries.isOverlayVisible()) return;
        if (client.screen instanceof WidgetVisibilityToggle vt && !vt.shouldRenderWidgets()) return;
        final ProfilerFiller profiler = Profiler.get();
        profiler.push("widgets");
        final Font textRenderer = client.font;
        final long timeNano = Util.getNanos();
        final int windowWidth = drawContext.guiWidth();
        final int windowHeight = drawContext.guiHeight();

        final ObjectArrayList<ModWidget> enabled = WidgetManager.enabled;
        for (final ModWidget widget : enabled) {
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

    public void tickWidgets(Minecraft client) {
        WidgetRenderer.client = client;
        final ProfilerFiller profiler = Profiler.get();
        profiler.push("widgets");
        guiScale = client.getWindow().getGuiScale();

        final ObjectArrayList<ModWidget> enabled = WidgetManager.enabled;
        for (final ModWidget widget : enabled) {
            profiler.push(widget.getId().toString());
            widget.tick();
            profiler.pop();
        }

        profiler.pop();

        if (WidgetsMod.platform().getRegisteredSettingsKeyMapping().consumeClick()){
            client.setScreen(new WidgetConfigScreen(client.screen));
        }
    }

    public void refreshAllSettings() {
        for (ModWidget widget : WidgetManager.getAllWidgets()) {
            widget.onSettingsChanged();
        }
    }
}
