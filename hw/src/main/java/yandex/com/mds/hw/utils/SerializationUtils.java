package yandex.com.mds.hw.utils;

import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import yandex.com.mds.hw.models.Note;

public class SerializationUtils {
    public static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .registerTypeAdapter(String.class, new StringAdapter())
            .setDateFormat(TimeUtils.IsoDateFormat.toPattern())
            .create();

    public static final Gson GSON_SERVER = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .registerTypeAdapter(String.class, new StringAdapter())
            .setDateFormat(TimeUtils.IsoDateFormat.toPattern())
            .registerTypeAdapter(new TypeToken<Note>() {
            }.getType(), new NoteSerializer())
            .registerTypeAdapter(Note.class, new NoteDeserializer())
            .create();

    private static class NoteDeserializer implements JsonDeserializer<Note> {

        @Override
        public Note deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonElement element = json.getAsJsonObject().remove("color");
            String color = element.getAsString();
            Note record = GSON.fromJson(json, Note.class);
            try {
                record.setColor(Color.parseColor(color));
            } catch (IllegalArgumentException e) {
                record.setColor(-1);
            }
            return record;
        }
    }

    private static class NoteSerializer implements JsonSerializer<Note> {

        @Override
        public JsonElement serialize(Note src, Type typeOfSrc, JsonSerializationContext context) {
            JsonElement element = GSON.toJsonTree(src);
            element.getAsJsonObject().remove("color");
            element.getAsJsonObject().addProperty("color", "#" + Integer.toHexString(src.getColor()));
            element.getAsJsonObject().remove("serverId");
            element.getAsJsonObject().remove("ownerId");
            element.getAsJsonObject().remove("id");
            return element;
        }
    }

    private static class StringAdapter extends TypeAdapter<String> {
        public String read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return "";
            }
            return reader.nextString();
        }

        public void write(JsonWriter writer, String value) throws IOException {
            if (value == null) {
                writer.nullValue();
                return;
            }
            writer.value(value);
        }
    }

}
