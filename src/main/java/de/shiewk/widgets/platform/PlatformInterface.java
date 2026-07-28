package de.shiewk.widgets.platform;

import net.minecraft.client.KeyMapping;
import org.slf4j.Logger;

public interface PlatformInterface {

    String getPlatformName();

    Logger getLogger();

    KeyMapping getRegisteredSettingsKeyMapping();

    PlatformAccessors accessors();

}
