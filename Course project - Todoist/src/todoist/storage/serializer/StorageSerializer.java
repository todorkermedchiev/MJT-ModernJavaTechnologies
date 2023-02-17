package todoist.storage.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import todoist.storage.InMemoryStorage;
import todoist.storage.Storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;

public class StorageSerializer {
    private static final String DEFAULT_FILE_PATH = "resources/backup.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateConverter())
            .create();

    private final Path filePath;

    public StorageSerializer() {
        this.filePath = Path.of(DEFAULT_FILE_PATH);
    }

    public StorageSerializer(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("The file path cannot be null, empty it blank");
        }

        this.filePath = Path.of(path);
    }

    public void saveDataToFile(Storage storage) throws IOException {
        if (storage == null) {
            throw new IllegalArgumentException("Storage cannot be null.");
        }

        try (var bufferedWriter = Files.newBufferedWriter(filePath)) {
            bufferedWriter.write(GSON.toJson(storage));
            bufferedWriter.flush();
        }
    }

    public Storage readDataFromFile() throws IOException {
        try (var bufferedReader = Files.newBufferedReader(filePath)) {
            Storage newStorage = GSON.fromJson(bufferedReader, InMemoryStorage.class);
            return Objects.requireNonNullElseGet(newStorage, InMemoryStorage::new);
        }
    }
}
