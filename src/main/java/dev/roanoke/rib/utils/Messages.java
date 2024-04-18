package dev.roanoke.rib.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.text.Text;
import dev.roanoke.rib.Rib;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Messages {
    private HashMap<String, String> messages = new HashMap<>();
    private String prefix = "";

    // bundledFilePath = /messages.json
    public Messages(Path filePath, String bundledFilePath) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

            HashMap<String, String> bundledMessages;
            try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(bundledFilePath), StandardCharsets.UTF_8)) {
                bundledMessages = gson.fromJson(reader, new TypeToken<HashMap<String, String>>(){}.getType());
            } catch (IOException e) {
                throw new RuntimeException("Failed to load bundled messages.json", e);
            }

            if (!Files.exists(filePath)) {
                // If the filePath does not exist, simply write the bundledMessages to the file
                Files.createDirectories(filePath.getParent());
                messages = bundledMessages;
                try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                    gson.toJson(bundledMessages, writer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // If the filePath exists, load its content and merge it with bundledMessages
                try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                    messages = gson.fromJson(reader, new TypeToken<HashMap<String, String>>(){}.getType());

                    // Check and merge missing keys from bundledMessages into messages
                    boolean hasChanges = false;
                    for (String key : bundledMessages.keySet()) {
                        if (!messages.containsKey(key)) {
                            messages.put(key, bundledMessages.get(key));
                            hasChanges = true;
                        }
                    }

                    // If there were missing keys, save the merged JSON back to the filePath
                    if (hasChanges) {
                        try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                            gson.toJson(messages, writer);
                        }
                    }
                } catch (IOException e) {
                    Rib.LOGGER.info("Failed to load or merge messages.json", e);
                    messages = new HashMap<>();
                }
            }
            prefix = getMessage("prefix");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directories for path " + filePath, e);
        }
    }

    public String getMessage(String key) {
        if (this.messages == null) {
            this.messages = new HashMap<String, String>();
        }
        String message = messages.getOrDefault(key, key);
        message = message.replace("{prefix}", this.prefix == null ? "" : this.prefix);
        return message;
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (String pKey: placeholders.keySet()) {
            message = message.replace(pKey, placeholders.get(pKey));
        }
        return message;
    }

    public Text getDisplayMessage(String key) {
        return getDisplayText(getMessage(key));
    }

    public Text getDisplayMessage(String key, Map<String, String> placeholders) {
        return getDisplayText(getMessage(key, placeholders));
    }

    public Text getDisplayText(String message) {
        if (Rib.adventure != null)  {
            return Rib.adventure.toNative(
                    MiniMessage.miniMessage().deserialize(message)
            );
        }
        return Text.literal("Error converting MiniMessage format");
    }
}