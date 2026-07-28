package de.shiewk.widgets.fabric;

import net.fabricmc.api.ClientModInitializer;

public final class WidgetsFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // This handles everything
        new FabricPlatformInterface();
    }

}
