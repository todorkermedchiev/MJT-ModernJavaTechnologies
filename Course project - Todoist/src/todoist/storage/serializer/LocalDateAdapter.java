package todoist.storage.serializer;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {
    private static final String DATE_FORMAT = "dd.MM.yyyy";

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDate localDate) throws IOException {
        System.out.println("write" + localDate);
        jsonWriter.value(localDate.toString());
    }

    @Override
    public LocalDate read(final JsonReader jsonReader) throws IOException {
        if (jsonReader.hasNext()) {
            System.out.println("read" + jsonReader.nextString());
            return LocalDate.parse(jsonReader.nextString());
        }
        return null;
    }
}
