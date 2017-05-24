package yandex.com.mds.hw.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

public class SerializationUtils {
    public static final int PARCEL_EMPTY_FIELD = -1;

    public static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .setDateFormat(TimeUtils.IsoDateFormat.toPattern())
            .create();

}
