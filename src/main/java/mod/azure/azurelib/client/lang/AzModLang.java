package mod.azure.azurelib.client.lang;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import mod.azure.azurelib.AzureLib;

/**
 * Loads every mod's {@code assets/<namespace>/lang/en_US.lang} into Beta's translation table.
 * <p>
 * Beta only reads its own built-in {@code /lang/en_US.lang}, so without this, mod items show their raw translation key.
 * Called by {@code LanguageMixin_AzModLang} after vanilla's files are loaded, so mods can also override vanilla
 * strings. Any mod can ship lang files this way; it doesn't need to depend on AzureLib's API, only have it installed.
 * <p>
 * Beta only has English, so only {@code en_US.lang} (or {@code en_us.lang}) is read. The format is the vanilla one:
 * {@code key=value} lines, e.g. {@code item.yourmodid.your_item.name=Your Item}.
 */
public final class AzModLang {

    private static final String[] FILE_NAMES = { "en_US.lang", "en_us.lang" };

    private AzModLang() {
        throw new UnsupportedOperationException();
    }

    public static void loadInto(Properties translations) {
        int files = 0;
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            for (Path root : mod.getRootPaths()) {
                Path assets = root.resolve("assets");
                if (!Files.isDirectory(assets)) {
                    continue;
                }
                try (DirectoryStream<Path> namespaces = Files.newDirectoryStream(assets)) {
                    for (Path namespace : namespaces) {
                        files += loadNamespace(namespace, translations, mod);
                    }
                } catch (IOException e) {
                    AzureLib.LOGGER.error("Couldn't list lang files of mod {}", mod.getMetadata().getId(), e);
                }
            }
        }
        if (files > 0) {
            AzureLib.LOGGER.info("Loaded {} mod lang file(s)", files);
        }
    }

    private static int loadNamespace(Path namespace, Properties translations, ModContainer mod) {
        for (String fileName : FILE_NAMES) {
            Path file = namespace.resolve("lang").resolve(fileName);
            if (!Files.isRegularFile(file)) {
                continue;
            }
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                translations.load(reader);
                return 1;
            } catch (IOException | IllegalArgumentException e) {
                AzureLib.LOGGER.error("Couldn't load {} from mod {}", file, mod.getMetadata().getId(), e);
                return 0;
            }
        }
        return 0;
    }
}
