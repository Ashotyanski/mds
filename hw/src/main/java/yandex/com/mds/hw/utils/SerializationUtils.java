package yandex.com.mds.hw.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

public class SerializationUtils {
    public static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .setDateFormat(TimeUtils.dateFormat.toPattern())
            .create();

}
