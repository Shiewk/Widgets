package de.shiewk.widgets.neoforge;

import de.shiewk.widgets.platform.PlatformAccessors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.debugchart.LocalSampleLogger;

public final class NeoPlatformAccessors implements PlatformAccessors {

    @Override
    public void addGuiElement(GuiGraphicsExtractor graphics, GuiElementRenderState element) {
        graphics.submitGuiElementRenderState(element);
    }

    @Override
    public ScreenRectangle peekScissor(GuiGraphicsExtractor graphics) {
        return graphics.peekScissorStack();
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
