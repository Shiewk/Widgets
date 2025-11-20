package de.shiewk.widgets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import de.shiewk.widgets.client.WidgetManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

import static de.shiewk.widgets.WidgetsMod.LOGGER;
import static de.shiewk.widgets.client.WidgetManager.gson;

public class WidgetSettings {
    public Anchor anchor = Anchor.TOP_LEFT;
    public int offsetX = 0;
    public int offsetY = 0;
    private boolean enabled = false;
    private final ObjectArrayList<WidgetSettingOption> customSettings;

    private WidgetSettings(JsonObject data, List<WidgetSettingOption> settings){
        customSettings = new ObjectArrayList<>(settings);
        if (data != null){
            try {
                this.enabled = Objects.requireNonNullElse(gson.fromJson(data.get("enabled"), Boolean.class), false);
                this.anchor = gson.fromJson(data.get("anchor"), Anchor.class);
                this.offsetX = Objects.requireNonNullElse(gson.fromJson(data.get("ox"), Integer.class), 0);
                this.offsetY = Objects.requireNonNullElse(gson.fromJson(data.get("oy"), Integer.class), 0);
            } catch (JsonSyntaxException | NullPointerException e) {
                LOGGER.info("Failed to load widget positioning:", e);
            } finally {
                if (anchor == null){
                    anchor = Anchor.TOP_LEFT;
                }
            }

            final JsonElement s = data.get("settings");
            if (s != null && s.isJsonObject()){
                final JsonObject savedSettings = s.getAsJsonObject();
                for (WidgetSettingOption setting : this.customSettings) {
                    final String settingId = setting.getId();
                    if (savedSettings.has(settingId)){
                        try {
                            setting.loadState(savedSettings.get(settingId));
                        } catch (Throwable e){
                            LOGGER.error("Could not load setting '{}' from element {}:", settingId, savedSettings.get(settingId));
                            LOGGER.error(e.toString());
                            for (StackTraceElement element : e.getStackTrace()) {
                                LOGGER.error(element.toString());
                            }
                        }
                    }
                }
            }
        }
    }
    public static WidgetSettings ofId(Identifier id, List<WidgetSettingOption> customSettings){
        final JsonObject data = WidgetManager.loadWidget(id);
        return new WidgetSettings(data, customSettings);
    }

    public void setPos(Anchor anchor, int offsetX, int offsetY){
        this.anchor = anchor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public boolean isEnabled(){
        return enabled;
    }

    public void setEnabled(ModWidget widget, boolean enabled){
        this.enabled = enabled;
        if (enabled){
            WidgetManager.enable(widget);
        } else {
            WidgetManager.disable(widget);
        }
    }

    public void toggleEnabled(ModWidget widget){
        setEnabled(widget, !enabled);
    }

    public final JsonObject saveState(){
        JsonObject object = new JsonObject();
        object.add("anchor", gson.toJsonTree(this.anchor));
        object.add("enabled", gson.toJsonTree(this.enabled));
        object.add("ox", gson.toJsonTree(this.offsetX));
        object.add("oy", gson.toJsonTree(this.offsetY));

        JsonObject customSettings = new JsonObject();
        for (WidgetSettingOption customSetting : this.customSettings) {
            customSettings.add(customSetting.getId(), customSetting.saveState());
        }
        object.add("settings", customSettings);

        return object;
    }

    public WidgetSettingOption optionById(String id){
        for (WidgetSettingOption customSetting : customSettings) {
            if (customSetting.getId().equals(id)){
                return customSetting;
            }
        }
        return null;
    }

    public ObjectArrayList<WidgetSettingOption> getCustomSettings() {
        return customSettings.clone();
    }
}
