package de.shiewk.widgets.platform;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.debugchart.LocalSampleLogger;

public interface PlatformAccessors {

    void addGuiElement(GuiGraphicsExtractor graphics, GuiElementRenderState element);

    ScreenRectangle peekScissor(GuiGraphicsExtractor graphics);

    PingDebugMonitor getPingMonitor(ClientPacketListener packetListener);

    LocalSampleLogger getPingLog(PingDebugMonitor pingMonitor);

}
