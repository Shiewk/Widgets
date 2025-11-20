package de.shiewk.widgets.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import de.shiewk.widgets.ModWidget;
import de.shiewk.widgets.WidgetsMod;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class WidgetManager {

    static final ObjectArrayList<ModWidget> enabled = new ObjectArrayList<>(); // for performance
    private static final ObjectArrayList<ModWidget> allWidgets = new ObjectArrayList<>();

    public static void register(ModWidget widget){
        allWidgets.add(widget);
        if (widget.getSettings().isEnabled()){
            enable(widget);
        }
    }

    public static ObjectArrayList<ModWidget> getEnabledWidgets() {
        return new ObjectArrayList<>(enabled);
    }

    public static ObjectArrayList<ModWidget> getAllWidgets() {
        return new ObjectArrayList<>(allWidgets);
    }

    public static void enable(ModWidget widget) {
        enabled.add(widget);
    }

    public static void disable(ModWidget widget) {
        enabled.remove(widget);
    }

    static Function<Identifier, File> saveFileFactory = id -> new File(MinecraftClient.getInstance().runDirectory.getPath() + "/config/widgets/" + id.getNamespace() + "/" + id.getPath() + ".json");
    public static final Gson gson = new Gson();

    public static void saveWidgets(List<ModWidget> widgets) {
        for (ModWidget widget : widgets) {
            WidgetsMod.LOGGER.info("Saving widget {}", widget.getId());
            final File saveFile = saveFileFactory.apply(widget.getId());
            try {
                if (saveFile.getParentFile().isDirectory() || saveFile.getParentFile().mkdirs()){
                    if (saveFile.isFile() || saveFile.createNewFile()){
                        try (FileWriter fw = new FileWriter(saveFile)){
                            try (JsonWriter jw = new JsonWriter(fw)){
                                Streams.write(widget.getSettings().saveState(), jw);
                            }
                        }
                    } else {
                        WidgetsMod.LOGGER.error("Could not create file: {}", saveFile.getPath());
                    }
                } else {
                    WidgetsMod.LOGGER.error("Could not create directory: {}", saveFile.getParentFile().getPath());
                }
            } catch (IOException e){
                WidgetsMod.LOGGER.error("An I/O operation failed while saving widget {} ({})", widget, widget.getId());
                WidgetsMod.LOGGER.error(e.toString());
                for (StackTraceElement element : e.getStackTrace()) {
                    WidgetsMod.LOGGER.error(String.valueOf(element));
                }
            }
        }
    }

    public static @Nullable JsonObject loadWidget(Identifier id) {
        WidgetsMod.LOGGER.info("Loading widget data of {}", id);
        final File saveFile = saveFileFactory.apply(id);
        if (!saveFile.isFile()){
            return null;
        }
        try {
            try (FileReader fr = new FileReader(saveFile)){
                return gson.fromJson(fr, JsonObject.class);
            }
        } catch (IOException e){
            WidgetsMod.LOGGER.error("An I/O operation failed while loading widget {}", id);
            WidgetsMod.LOGGER.error(e.toString());
            for (StackTraceElement element : e.getStackTrace()) {
                WidgetsMod.LOGGER.error(String.valueOf(element));
            }
            return null;
        }
    }
}
