package yandex.com.mds.hw.network;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import yandex.com.mds.hw.utils.SerializationUtils;

public class ServiceGenerator {

    public static final String API_BASE_URL = "https://notesbackend-yufimtsev.rhcloud.com/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(SerializationUtils.GSON_SERVER));

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.baseUrl(API_BASE_URL).client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, String url) {
        Retrofit retrofit = builder.baseUrl(url).client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }
}