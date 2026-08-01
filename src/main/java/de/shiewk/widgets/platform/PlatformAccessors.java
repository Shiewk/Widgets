package de.shiewk.widgets.platform;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import net.minecraft.util.debugchart.LocalSampleLogger;

public interface PlatformAccessors {

    void addGuiElement(GuiGraphics graphics, GuiElementRenderState element);

    ScreenRectangle peekScissor(GuiGraphics graphics);

    PingDebugMonitor getPingMonitor(ClientPacketListener packetListener);

    LocalSampleLogger getPingLog(PingDebugMonitor pingMonitor);

}
