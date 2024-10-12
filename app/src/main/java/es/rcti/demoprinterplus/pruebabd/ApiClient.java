package es.rcti.demoprinterplus.pruebabd;
import android.util.Log;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final int TIMEOUT = 30; // segundos
    private static final String BASE_URL = "https://systemposadas.com/";
    private static Retrofit retrofit = null;
    private static final boolean DESARROLLO = true; // Cambia esto a false para producción

    public static Retrofit getClient() {
        if (retrofit == null) {
            try {
                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(TIMEOUT, TimeUnit.SECONDS);

                // Configuración SSL insegura (solo para desarrollo)
                if (DESARROLLO) {
                    TrustManager[] trustAllCerts = createTrustAllCerts();
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                    builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                            .hostnameVerifier((hostname, session) -> true);
                    Log.w(TAG, "Usando configuración SSL insegura. NO USAR EN PRODUCCIÓN.");
                }

                OkHttpClient okHttpClient = builder.build();

                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                Log.d(TAG, "Retrofit inicializado correctamente con URL base: " + BASE_URL);

            } catch (Exception e) {
                Log.e(TAG, "Error al inicializar Retrofit: " + e.getMessage());
                throw new RuntimeException("Error al inicializar Retrofit", e);
            }
        }
        return retrofit;
    }

    private static TrustManager[] createTrustAllCerts() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                }
        };
    }
}