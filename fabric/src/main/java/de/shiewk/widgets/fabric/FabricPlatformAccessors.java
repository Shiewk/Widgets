package de.shiewk.widgets.fabric;

import de.shiewk.widgets.platform.PlatformAccessors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import net.minecraft.util.debugchart.LocalSampleLogger;

public final class FabricPlatformAccessors implements PlatformAccessors {

    @Override
    public void addGuiElement(GuiGraphics graphics, GuiElementRenderState element) {
        graphics.guiRenderState.submitGuiElement(element);
    }

    @Override
    public ScreenRectangle peekScissor(GuiGraphics graphics) {
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
