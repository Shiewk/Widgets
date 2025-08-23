package de.shiewk.widgets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.shiewk.widgets.client.WidgetManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

import static de.shiewk.widgets.WidgetsMod.LOGGER;
import static de.shiewk.widgets.utils.WidgetUtils.translateToWidgetSettingsValue;

public class WidgetSettings {
    public double posX = 0; // posx * 100 = screen width
    public double posY = 0; // posy * 100 = screen height
    private boolean enabled = false;
    private final ObjectArrayList<WidgetSettingOption> customSettings;

    private WidgetSettings(JsonObject data, List<WidgetSettingOption> settings){
        customSettings = new ObjectArrayList<>(settings);
        if (data != null){
            final JsonElement enabled = data.get("enabled");
            this.enabled = enabled.isJsonPrimitive() && enabled.getAsJsonPrimitive().isBoolean() && enabled.getAsBoolean();
            final JsonElement x = data.get("x");
            this.posX = x.isJsonPrimitive() ? x.getAsJsonPrimitive().isNumber() ? x.getAsDouble() : 0 : 0;
            final JsonElement y = data.get("y");
            this.posY = y.isJsonPrimitive() ? y.getAsJsonPrimitive().isNumber() ? y.getAsDouble() : 0 : 0;
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

    public void setPosX(double v, int widgetWidth, int maxWidth) {
        posX = MathHelper.clamp(v, 0, 100 - translateToWidgetSettingsValue(widgetWidth, maxWidth));
    }
    public void setPosY(double v, int widgetHeight, int maxHeight) {
        posY = MathHelper.clamp(v, 0, 100 - translateToWidgetSettingsValue(widgetHeight, maxHeight));
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
        object.addProperty("x", posX);
        object.addProperty("y", posY);
        object.addProperty("enabled", enabled);
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
