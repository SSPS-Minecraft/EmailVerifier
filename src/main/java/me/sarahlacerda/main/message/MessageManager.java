package me.sarahlacerda.main.message;

import me.sarahlacerda.main.io.YmlDriver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import static java.text.MessageFormat.format;
import static me.sarahlacerda.main.io.YmlDriver.LANGUAGE_YML;
import static me.sarahlacerda.main.io.YmlDriver.ymlPath;
import static me.sarahlacerda.main.util.Logger.getLogger;

public class MessageManager {
    private final FileConfiguration languageFile;

    public MessageManager(YmlDriver ymlDriver) {
        this.languageFile = YamlConfiguration.loadConfiguration(ymlDriver.loadFile(LANGUAGE_YML));
    }

    public String getMessage(String messageReference) {
        if (containsMessage(getPreferredLanguage(), messageReference)) {
            return languageFile.get(ymlPath("messages", getPreferredLanguage(), messageReference)).toString();
        }

        getLogger()
                .warn(format("message reference \"{0}\" for language \"{1}\" selected in {2} could not be found! I will try to fallback to en-us",
                        messageReference,
                        getPreferredLanguage(),
                        LANGUAGE_YML));

        return getMessage("en-us", messageReference);
    }

    public String getMessage(String language, String messageReference) {
        return languageFile.get(ymlPath("messages", language), messageReference).toString();
    }

    public String getPreferredLanguage() {
        return languageFile.get("language").toString();
    }

    public boolean containsMessage(String language, String messageReference) {
        return languageFile.contains(ymlPath("messages", language, messageReference));
    }
}
