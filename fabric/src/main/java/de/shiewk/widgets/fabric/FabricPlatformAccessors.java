package de.shiewk.widgets.fabric;

import de.shiewk.widgets.platform.PlatformAccessors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.debugchart.LocalSampleLogger;

public final class FabricPlatformAccessors implements PlatformAccessors {

    @Override
    public void addGuiElement(GuiGraphicsExtractor graphics, GuiElementRenderState element) {
        graphics.guiRenderState.addGuiElement(element);
    }

    @Override
    public ScreenRectangle peekScissor(GuiGraphicsExtractor graphics) {
        return graphics.scissorStack.peek();
    }

    @Override
    public PingDebugMonitor getPingMonitor(ClientPacketListener packetListener) {
        return packetListener.pingDebugMonitor;
    }

    @Override
    public LocalSampleLogger getPingLog(PingDebugMonitor pingMonitor) {
        return pingMonitor.delayTimer;
    }
}
