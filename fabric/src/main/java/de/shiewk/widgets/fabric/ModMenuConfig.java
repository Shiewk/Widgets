package de.shiewk.widgets.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.shiewk.widgets.client.screen.WidgetConfigScreen;

public final class ModMenuConfig implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return WidgetConfigScreen::new;
    }
    
}
