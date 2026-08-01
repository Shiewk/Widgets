package de.shiewk.widgets.neoforge;

import de.shiewk.widgets.WidgetsMod;
import de.shiewk.widgets.platform.PlatformAccessors;
import de.shiewk.widgets.platform.PlatformInterface;
import net.minecraft.client.KeyMapping;
import org.slf4j.Logger;

public final class NeoPlatformInterface implements PlatformInterface {

    private static NeoPlatformInterface instance;

    private final PlatformAccessors accessors = new NeoPlatformAccessors();
    private final WidgetsMod widgets;
    private final KeyMapping settingsMapping;

    public static void doInit(){
        if (instance == null){
            new NeoPlatformInterface();
        }
    }

    public static NeoPlatformInterface getInstance() {
        return instance;
    }

    private NeoPlatformInterface() {
        instance = this;
        this.widgets = new WidgetsMod(this);
        this.settingsMapping = this.widgets.createSettingsKeyMapping();
    }

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public Logger getLogger() {
        return WidgetsNeoforge.LOGGER;
    }

    public WidgetsMod getWidgets() {
        return widgets;
    }

    @Override
    public KeyMapping getRegisteredSettingsKeyMapping() {
        return settingsMapping;
    }

    @Override
    public PlatformAccessors accessors() {
        return accessors;
    }

}
