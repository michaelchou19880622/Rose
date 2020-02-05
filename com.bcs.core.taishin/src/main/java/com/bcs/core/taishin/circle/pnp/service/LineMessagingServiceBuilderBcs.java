package com.bcs.core.taishin.circle.pnp.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public class LineMessagingServiceBuilderBcs {
    public static final String DEFAULT_API_END_POINT = "https://api.line.me/";
    public static final long DEFAULT_CONNECT_TIMEOUT = 10_000;
    public static final long DEFAULT_READ_TIMEOUT = 10_000;
    public static final long DEFAULT_WRITE_TIMEOUT = 10_000;

    private OkHttpClient.Builder okHttpClientBuilder;
    private Retrofit.Builder retrofitBuilder;

    public LineMessagingService build(LineMessagingServiceBuilder builder, boolean withProxy, String proxyHost) throws Exception {

        Field f = LineMessagingServiceBuilder.class.getDeclaredField("okHttpClientBuilder");
        f.setAccessible(true);
        Object okHttpClientBuilderObj = f.get(builder);
        if (okHttpClientBuilderObj != null) {
            okHttpClientBuilder = (Builder) okHttpClientBuilderObj;
        }

        okHttpClientBuilder.connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.MILLISECONDS);

        if (withProxy) {
            okHttpClientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, 80)));
        }

        final OkHttpClient okHttpClient = okHttpClientBuilder.build();

        if (retrofitBuilder == null) {
            retrofitBuilder = createDefaultRetrofitBuilder();
        }

        retrofitBuilder.client(okHttpClient);
        retrofitBuilder.baseUrl(DEFAULT_API_END_POINT);
        final Retrofit retrofit = retrofitBuilder.build();

        return retrofit.create(LineMessagingService.class);
    }

    private static Retrofit.Builder createDefaultRetrofitBuilder() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Register JSR-310(java.time.temporal.*) module and read number as millsec.
        objectMapper.registerModule(new JavaTimeModule()).configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        return new Retrofit.Builder().addConverterFactory(JacksonConverterFactory.create(objectMapper));
    }
}
