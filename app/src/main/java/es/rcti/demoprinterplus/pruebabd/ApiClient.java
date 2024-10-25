package es.rcti.demoprinterplus.pruebabd;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.ConnectionPool;
import okhttp3.Cache;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import android.content.Context;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "https://systemposadas.com/";
    private static final boolean DESARROLLO = true; // Cambia esto a false para producción

    // Configuración de timeouts y caché
    private static final int TIMEOUT_CONNECT = 30;
    private static final int TIMEOUT_READ = 30;
    private static final int TIMEOUT_WRITE = 30;
    private static final int CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final int MAX_IDLE_CONNECTIONS = 5;
    private static final long KEEP_ALIVE_DURATION = 5; // minutos

    private static Retrofit retrofit = null;
    private static Context applicationContext;

    // Inicializar el contexto de la aplicación
    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = buildRetrofitClient(createDefaultOkHttpClient());
        }
        return retrofit;
    }

    public static Retrofit getClient(OkHttpClient customClient) {
        return buildRetrofitClient(customClient);
    }

    // Resetear el cliente (útil para cambios de configuración)
    public static void resetClient() {
        retrofit = null;
    }

    private static Retrofit buildRetrofitClient(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static OkHttpClient createDefaultOkHttpClient() {
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    // Pool de conexiones para reutilización
                    .connectionPool(new ConnectionPool(
                            MAX_IDLE_CONNECTIONS,
                            KEEP_ALIVE_DURATION,
                            TimeUnit.MINUTES
                    ));

            // Configurar caché si el contexto está disponible
            if (applicationContext != null) {
                File cacheDir = new File(applicationContext.getCacheDir(), "http-cache");
                Cache cache = new Cache(cacheDir, CACHE_SIZE);
                builder.cache(cache);
            }

            // Configuración SSL para desarrollo
            if (DESARROLLO) {
                configureSslForDevelopment(builder);
                Log.w(TAG, "⚠️ Usando configuración SSL insegura. NO USAR EN PRODUCCIÓN.");
            }

            // Agregar interceptor para logging en desarrollo
            if (DESARROLLO) {
                builder.addInterceptor(chain -> {
                    long startTime = System.currentTimeMillis();
                    okhttp3.Response response = chain.proceed(chain.request());
                    long endTime = System.currentTimeMillis();
                    Log.d(TAG, String.format("Request to %s completed in %d ms",
                            chain.request().url(), endTime - startTime));
                    return response;
                });
            }

            OkHttpClient client = builder.build();
            Log.d(TAG, "OkHttpClient configurado correctamente");
            return client;

        } catch (Exception e) {
            Log.e(TAG, "Error crítico al crear OkHttpClient: " + e.getMessage());
            throw new RuntimeException("Error al inicializar OkHttpClient", e);
        }
    }

    private static void configureSslForDevelopment(OkHttpClient.Builder builder) {
        try {
            TrustManager[] trustAllCerts = createTrustAllCerts();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true);

        } catch (Exception e) {
            Log.e(TAG, "Error al configurar SSL para desarrollo: " + e.getMessage());
            throw new RuntimeException("Error en configuración SSL", e);
        }
    }

    private static TrustManager[] createTrustAllCerts() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        // No implementado para desarrollo
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        // No implementado para desarrollo
                    }
                }
        };
    }

    // Método para verificar el estado de la conexión
    public static boolean isInitialized() {
        return retrofit != null;
    }
}