package es.rcti.demoprinterplus.pruebabd;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import javax.net.ssl.*;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {


    // Declaración de vistas
    private boolean firmaEstaCargada = false;
    private ProgressDialog loadingDialog;
    private static final int REQUEST_GALLERY_PERMISSION = 201;

    private LinearLayout photoPreviewContainer;
    private static final int MAX_PHOTOS = 2;
    private ImageView[] photoPreviewViews = new ImageView[2];
    private List<String> imagenBase64List = new ArrayList<>();
    private int currentPhotoIndex = 0;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private String[] currentPhotoPaths = new String[2];

    private String imagenBase64Temporal;

    private String actaIdActual;

    private String currentPhotoPath;


    private EditText etClase;
    private EditText etVencimiento;
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final long MIN_TIME = 10000; // 10 segundos


    private static final float MIN_DISTANCE = 10; // 10 metros
    private EditText etApellidoNombre, etDomicilio, etLocalidad, etCodPostal, etDepartamento,
            etProvincia, etPais, etLicencia, etMultaInfo,
            etNumeroDocumento, etDominio, etOtraMarca, etModeloVehiculo, etPropietario,
            etLugar, etDepartamentoMulta, etMunicipioMulta;
    private Spinner spinnerTipoDocumento, spinnerMarca, spinnerTipoVehiculo, spinnerInfraccion, spinnerExpedidaPor;
    private Button btnTomarFoto, btnConectarImprimir, btnInsertarConductor;
    private AutoCompleteTextView actvDepartamento, actvLocalidad;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private TextView tvEstado, tvConductor, tvVehiculo, tvHecho, tvNumero, tvEspecificaciones;
    private LinearLayout layoutConductor, layoutVehiculo, layoutHecho, layoutEspecificaciones;
    private ImageView ivPhotoPreview;
    private ChipGroup chipGroupInfracciones;
    private List<String> infraccionesSeleccionadas = new ArrayList<>();
    private String inspectorId; // Nueva variable de clase
    private EditText
            etEquipo, etMarcaCinemometro, etModeloCinemometro, etSerieCinemometro,
            etCodAprobacionCinemometro, etValorCinemometro;
    private Spinner spinnerTipoEquipo;


    // Variables para la impresión Bluetooth
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;


    private OutputStream outputStream;
    private BluetoothAdapter bluetoothAdapter;
    private ProgressBar progressBar;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private RelativeLayout loadingOverlay;
    private OracleApiService apiService;
    private String nombreInspector;
    private String apellidoInspector;
    private String legajoInspector;

    private Button btnBuscarLocalidad;
    private Map<String, String> infraccionIdMap = new HashMap<>();
    private Bitmap firmaProcesada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        precargarFirma();

        // Configurar el manejador de excepciones no capturadas
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            Log.e("UNCAUGHT", "Excepción no capturada: " + e.getMessage(), e);
        });

        // Verificar ID del inspector
        inspectorId = getIntent().getStringExtra("INSPECTOR_ID");
        if (inspectorId == null || inspectorId.isEmpty()) {
            Log.e(TAG, "No se recibió el ID del inspector");
            Toast.makeText(this, "Error: No se pudo obtener la identificación del inspector", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Inicializar servicios y componentes básicos
        apiService = ApiClient.getClient().create(OracleApiService.class);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Inicializar vistas principales
        initializeViews();

        // Configurar componentes
        setupSpinners();
        setupListeners();
        obtenerDatosInspector();

        // Inicialización de componentes para fotos
        initializePhotoComponents();

        // Configurar componentes adicionales
        setupAdditionalComponents();
    }

    private byte[] comandoFirma = null; // Agregar esta variable en la clase


    private void precargarFirma() {
        if (firmaEstaCargada) {
            return;
        }

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Cargando firma...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        new AsyncTask<Void, Void, byte[]>() {
            // Método interno para convertir a blanco y negro
            private Bitmap convertirABlancoYNegro(Bitmap original) {
                int width = original.getWidth();
                int height = original.getHeight();
                Bitmap bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                int threshold = 128;
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int pixel = original.getPixel(x, y);
                        int red = Color.red(pixel);
                        int green = Color.green(pixel);
                        int blue = Color.blue(pixel);
                        int gray = (red + green + blue) / 3;
                        int bw = gray < threshold ? Color.BLACK : Color.WHITE;
                        bwBitmap.setPixel(x, y, bw);
                    }
                }
                return bwBitmap;
            }

            @Override
            protected byte[] doInBackground(Void... params) {
                try {
                    String urlFirma = obtenerUrlFirma(legajoInspector);
                    URL url = new URL(urlFirma);

                    // Configuración del SSL
                    TrustManager[] trustAllCerts = new TrustManager[]{
                            new X509TrustManager() {
                                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                            }
                    };

                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustAllCerts, new SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setConnectTimeout(30000);
                    connection.setReadTimeout(30000);
                    connection.setRequestProperty("Accept", "application/json");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        if (jsonResponse.getBoolean("success")) {
                            String base64Data = jsonResponse.getString("firma");
                            byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                            if (bitmap != null) {
                                int newWidth = 400;
                                int newHeight = (bitmap.getHeight() * newWidth) / bitmap.getWidth();
                                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                                Bitmap bwBitmap = convertirABlancoYNegro(resizedBitmap);
                                firmaProcesada = bwBitmap;

                                // Liberar memoria
                                bitmap.recycle();
                                resizedBitmap.recycle();

                                return Utils.decodeBitmap(bwBitmap);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("Firma", "Error precargando firma", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(byte[] command) {
                if (command != null) {
                    comandoFirma = command;
                    firmaEstaCargada = true;
                    Log.d("Firma", "Firma precargada exitosamente");
                } else {
                    Log.e("Firma", "No se pudo precargar la firma");
                }
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }
        }.execute();
    }



    private void initializePhotoComponents() {
        photoPreviewContainer = findViewById(R.id.photoPreviewContainer);
        for (int i = 0; i < 2; i++) {
            photoPreviewViews[i] = findViewById(getResources().getIdentifier("ivPhotoPreview" + (i + 1), "id", getPackageName()));
        }

        if (btnTomarFoto != null) {
            btnTomarFoto.setOnClickListener(v -> mostrarOpcionesFoto());
        }
    }

    private void setupAdditionalComponents() {
        // Configurar spinner de infracciones
        if (spinnerInfraccion != null) {
            spinnerInfraccion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String infraccionSeleccionada = parent.getItemAtPosition(position).toString();
                    if (position != 0 && !infraccionSeleccionada.equals("Cargando infracciones...")) {
                        agregarInfraccion(infraccionSeleccionada);
                        Log.d("DEBUG", "Infracción seleccionada: " + infraccionSeleccionada);
                        Log.d("DEBUG", "Infracciones actuales: " + infraccionesSeleccionadas);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        // Configurar botón de búsqueda de localidad
        if (btnBuscarLocalidad != null) {
            btnBuscarLocalidad.setOnClickListener(v -> {
                Log.d("MainActivity", "Botón de búsqueda presionado");
                if (actvLocalidad != null) {
                    String localidad = actvLocalidad.getText().toString().trim();
                    if (!localidad.isEmpty()) {
                        Log.d("MainActivity", "Iniciando búsqueda para localidad: " + localidad);
                        buscarInfoLocalidad(localidad);
                    } else {
                        Log.d("MainActivity", "Campo de localidad vacío");
                        Toast.makeText(this, "Por favor, ingrese una localidad", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Cargar datos iniciales
        cargarTiposVehiculo();
        setPosadasAsDefault();
    }

    // Modificación del método initializeViews para incluir todas las vistas necesarias
    private void initializeViews() {
        // Progress y Loading
        progressBar = findViewById(R.id.progressBar);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Botones
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnInsertarConductor = findViewById(R.id.btnInsertarConductor);
        btnBuscarLocalidad = findViewById(R.id.btnBuscarLocalidad);

        // Contenedores y grupos
        chipGroupInfracciones = findViewById(R.id.chipGroupInfracciones);
        layoutConductor = findViewById(R.id.layoutConductor);
        layoutVehiculo = findViewById(R.id.layoutVehiculo);
        layoutHecho = findViewById(R.id.layoutHecho);
        layoutEspecificaciones = findViewById(R.id.layoutEspecificaciones);

        // Spinners
        spinnerTipoEquipo = findViewById(R.id.spinnerTipoEquipo);
        spinnerTipoDocumento = findViewById(R.id.spinnerTipoDocumento);
        spinnerMarca = findViewById(R.id.spinnerMarca);
        spinnerTipoVehiculo = findViewById(R.id.spinnerTipoVehiculo);
        spinnerInfraccion = findViewById(R.id.spinnerInfraccion);
        spinnerExpedidaPor = findViewById(R.id.spinnerExpedidaPor);

        // EditText y AutoCompleteTextView
        etEquipo = findViewById(R.id.etEquipo);
        etMarcaCinemometro = findViewById(R.id.etMarcaCinemometro);
        etModeloCinemometro = findViewById(R.id.etModeloCinemometro);
        etSerieCinemometro = findViewById(R.id.etSerieCinemometro);
        etCodAprobacionCinemometro = findViewById(R.id.etCodAprobacionCinemometro);
        etValorCinemometro = findViewById(R.id.etValorCinemometro);

        actvDepartamento = findViewById(R.id.actvDepartamento);
        actvLocalidad = findViewById(R.id.actvLocalidad);
        etApellidoNombre = findViewById(R.id.etApellidoNombre);
        etDomicilio = findViewById(R.id.etDomicilio);
        etCodPostal = findViewById(R.id.etCodPostal);
        etClase = findViewById(R.id.etClase);
        etVencimiento = findViewById(R.id.etVencimiento);
        etProvincia = findViewById(R.id.etProvincia);
        etPais = findViewById(R.id.etPais);
        etLicencia = findViewById(R.id.etLicencia);
        etMultaInfo = findViewById(R.id.etMultaInfo);
        etNumeroDocumento = findViewById(R.id.etNumeroDocumento);
        etDominio = findViewById(R.id.etDominio);
        etOtraMarca = findViewById(R.id.etOtraMarca);
        etModeloVehiculo = findViewById(R.id.etModeloVehiculo);
        etPropietario = findViewById(R.id.etPropietario);
        etLugar = findViewById(R.id.etLugar);
        etDepartamentoMulta = findViewById(R.id.etDepartamentoMulta);
        etMunicipioMulta = findViewById(R.id.etMunicipioMulta);

        // TextViews
        tvEstado = findViewById(R.id.tvEstado);
        tvConductor = findViewById(R.id.tvConductor);
        tvVehiculo = findViewById(R.id.tvVehiculo);
        tvHecho = findViewById(R.id.tvHecho);
        tvNumero = findViewById(R.id.tvNumero);
        tvEspecificaciones = findViewById(R.id.tvEspecificaciones);
    }

    private void setupListeners() {
        // Listeners de botones principales
        if (btnInsertarConductor != null) {
            btnInsertarConductor.setOnClickListener(v -> {
                Log.d("DEBUG", "Botón Insertar Conductor presionado. Infracciones seleccionadas: " + infraccionesSeleccionadas);
                insertarDatosConductor();
            });
        } else {
            Log.e("MainActivity", "Error: btnInsertarConductor es null");
        }

        if (btnTomarFoto != null) {
            btnTomarFoto.setOnClickListener(v -> mostrarOpcionesFoto());
        } else {
            Log.e("MainActivity", "Error: btnTomarFoto es null");
        }

        // Listeners de secciones expandibles
        if (tvConductor != null)
            tvConductor.setOnClickListener(v -> toggleVisibility(layoutConductor));
        if (tvVehiculo != null)
            tvVehiculo.setOnClickListener(v -> toggleVisibility(layoutVehiculo));
        if (tvHecho != null) tvHecho.setOnClickListener(v -> toggleVisibility(layoutHecho));
        if (tvEspecificaciones != null)
            tvEspecificaciones.setOnClickListener(v -> toggleVisibility(layoutEspecificaciones));

        // Listener para provincia y departamento
        if (etProvincia != null) {
            etProvincia.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    cargarDepartamentos(etProvincia.getText().toString());
                }
            });
        }

        if (actvDepartamento != null) {
            actvDepartamento.setOnItemClickListener((parent, view, position, id) -> {
                String departamentoSeleccionado = (String) parent.getItemAtPosition(position);
                cargarLocalidades(etProvincia.getText().toString(), departamentoSeleccionado);
            });
        }

        // Configurar búsqueda de localidad si el botón existe
        if (btnBuscarLocalidad != null && actvLocalidad != null) {
            btnBuscarLocalidad.setOnClickListener(v -> {
                String localidad = actvLocalidad.getText().toString().trim();
                if (!localidad.isEmpty()) {
                    buscarInfoLocalidad(localidad);
                } else {
                    Toast.makeText(this, "Por favor, ingrese una localidad", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Método para mostrar/ocultar loading
    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void obtenerDatosInspector() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            nombreInspector = intent.getStringExtra("NOMBRE_INSPECTOR");
            apellidoInspector = intent.getStringExtra("APELLIDO_INSPECTOR");
            legajoInspector = intent.getStringExtra("LEGAJO_INSPECTOR");

            Log.d("MainActivity", "Datos del inspector recibidos - Nombre: " + nombreInspector +
                    ", Apellido: " + apellidoInspector +
                    ", Legajo: " + legajoInspector);
        } else {
            Log.w("MainActivity", "No se recibieron datos del inspector");
            // Puedes establecer valores por defecto o mostrar un mensaje
            nombreInspector = "";
            apellidoInspector = "";
            legajoInspector = "";
        }
    }

    private void agregarInfraccion(String infraccionDescripcion) {
        if (!infraccionesSeleccionadas.contains(infraccionDescripcion)) {
            String infraccionId = infraccionIdMap.get(infraccionDescripcion);
            if (infraccionId != null) {
                infraccionesSeleccionadas.add(infraccionDescripcion);
                Log.d("DEBUG", "Infracción agregada: " + infraccionDescripcion + " con ID: " + infraccionId);

                Chip chip = new Chip(this);
                chip.setText(infraccionDescripcion);
                chip.setCloseIconVisible(true);
                chip.setTag(infraccionId); // Guardamos el ID en el tag del Chip
                chip.setOnCloseIconClickListener(v -> {
                    chipGroupInfracciones.removeView(chip);
                    infraccionesSeleccionadas.remove(infraccionDescripcion);
                    Log.d("DEBUG", "Infracción removida: " + infraccionDescripcion + " con ID: " + infraccionId);
                });
                chipGroupInfracciones.addView(chip);
            } else {
                Log.e("DEBUG", "No se encontró ID para la infracción: " + infraccionDescripcion);
            }
        }
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Permiso de cámara (necesario para todas las versiones)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }

        // Permisos de almacenamiento según versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 y superior
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            // Android 12 y anterior
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_CAMERA_PERMISSION);
        } else {
            // Todos los permisos están concedidos, proceder con la captura de foto
            dispatchTakePictureIntent();
        }
    }

    private void checkCameraPermission() {
        try {
            Log.d("DEBUG", "Verificando permisos de cámara");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    // El usuario ya ha rechazado el permiso anteriormente, mostrar un diálogo explicativo
                    showExplanationDialog();
                } else {
                    // Primera vez que se solicita el permiso o el usuario seleccionó "No volver a preguntar"
                    requestCameraPermission();
                }
            } else {
                Log.d("DEBUG", "Permisos de cámara ya concedidos, llamando a tomarFoto()");
                tomarFoto();
            }
        } catch (Exception e) {
            Log.e("DEBUG", "Error en checkCameraPermission: " + e.getMessage(), e);
            mostrarMensaje("Error al verificar permisos de cámara");
        }
    }

    private void showExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permiso de cámara necesario")
                .setMessage("La aplicación necesita acceso a la cámara para tomar fotos. Por favor, conceda el permiso.")
                .setPositiveButton("OK", (dialog, which) -> requestCameraPermission())
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                    mostrarMensaje("La funcionalidad de fotos está deshabilitada");
                })
                .create().show();
    }


    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    private void setPosadasAsDefault() {
        // Establecer valores predeterminados
        etDepartamentoMulta.setText("Capital");
        etMunicipioMulta.setText("Posadas");

        // Hacer que los campos de departamento y municipio no sean editables
        etDepartamentoMulta.setEnabled(true);
        etMunicipioMulta.setEnabled(true);
    }

    private void setupLocalidadAutoComplete() {
        actvLocalidad.setThreshold(1);
        actvLocalidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1) {
                    buscarInfoLocalidad(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Log.d("CameraDebug", "Iniciando dispatchTakePictureIntent");

        // Verificar permiso de cámara para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
                return;
            }
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            // Crear el archivo para la imagen
            File photoFile = null;
            try {
                photoFile = createImageFile();
                currentPhotoPaths[currentPhotoIndex] = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                Log.e("CameraDebug", "Error creando archivo de imagen", ex);
                mostrarError("Error al crear archivo para la foto");
                return;
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);

                // Otorgar permisos URI temporales
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (ActivityNotFoundException e) {
            Log.e("CameraDebug", "No se encontró aplicación de cámara", e);
            mostrarMensaje("No se encontró una aplicación de cámara");
        } catch (Exception e) {
            Log.e("CameraDebug", "Error al iniciar la cámara: " + e.getMessage(), e);
            mostrarMensaje("Error al iniciar la cámara");
        }
    }

    private void listarAplicacionesCamara() {
        Log.d("CameraDebug", "Listando aplicaciones de cámara disponibles");
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo info : list) {
            Log.d("CameraDebug", "Aplicación de cámara encontrada: " + info.activityInfo.packageName);
        }

        if (list.isEmpty()) {
            Log.e("CameraDebug", "No se encontraron aplicaciones de cámara");
        }
    }

    private void mostrarDialogoSinCamara() {
        new AlertDialog.Builder(this)
                .setTitle("Cámara no disponible")
                .setMessage("No se encontró una aplicación de cámara. ¿Desea seleccionar una imagen de la galería?")
                .setPositiveButton("Abrir Galería", (dialog, which) -> abrirGaleria())
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void abrirGaleria() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
    }

    private void buscarInfoLocalidad(String localidad) {
        Log.d("MainActivity", "Entrando en buscarInfoLocalidad con: " + localidad);
        try {
            Call<RespuestaInfoLocalidad> call = apiService.buscarInfoLocalidad("buscarInfoLocalidad", localidad);
            Log.d("MainActivity", "Llamada a API creada");
            call.enqueue(new Callback<RespuestaInfoLocalidad>() {
                @Override
                public void onResponse(Call<RespuestaInfoLocalidad> call, Response<RespuestaInfoLocalidad> response) {

                    Log.d("MainActivity", "Respuesta JSON completa: " + new Gson().toJson(response.body()));
                    Log.d("MainActivity", "Respuesta recibida. Código: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        List<RespuestaInfoLocalidad.InfoLocalidad> infoLocalidades = response.body().getInfoLocalidades();
                        Log.d("MainActivity", "Número de localidades recibidas: " + (infoLocalidades != null ? infoLocalidades.size() : "null"));
                        if (infoLocalidades != null && !infoLocalidades.isEmpty()) {
                            RespuestaInfoLocalidad.InfoLocalidad infoLocalidad = infoLocalidades.get(0);
                            Log.d("MainActivity", "Información de localidad: " + infoLocalidad.toString());
                            actualizarSugerenciasLocalidad(infoLocalidades);
                        } else {
                            Log.d("MainActivity", "Lista de localidades vacía o nula");
                        }
                    } else {
                        Log.e("MainActivity", "Respuesta no exitosa: " + response.code());
                        if (response.errorBody() != null) {
                            try {
                                Log.e("MainActivity", "Error body: " + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<RespuestaInfoLocalidad> call, Throwable t) {
                    Log.e("MainActivity", "Error en la llamada a la API", t);
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Excepción en buscarInfoLocalidad", e);
        }
    }


    private void setupSpinners() {
        setupTipoDocumentoSpinner();
        setupMarcaSpinner();
        setupTipoVehiculoSpinner();
        setupInfraccionSpinner();
        setupExpedidoPorSpinner();
        setupEquipoMedicionSpinner();
        ArrayAdapter<CharSequence> adapterTipoEquipo = ArrayAdapter.createFromResource(this,
                R.array.tipos_equipo, android.R.layout.simple_spinner_item);
        adapterTipoEquipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoEquipo.setAdapter(adapterTipoEquipo);
    }

    private void setupTipoDocumentoSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tipo_documento_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDocumento.setAdapter(adapter);
    }

    private void setupMarcaSpinner() {
        // Inicializa el spinner con un elemento de carga
        ArrayAdapter<String> loadingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                Collections.singletonList("Cargando marcas..."));
        loadingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMarca.setAdapter(loadingAdapter);

        // Llama a la función para cargar las marcas
        cargarMarcas();
    }


    private void actualizarSpinnerMarcas(List<RespuestaMarcas.Marca> marcas) {
        List<String> descripcionesMarcas = new ArrayList<>();
        descripcionesMarcas.add("Seleccione una marca");
        for (RespuestaMarcas.Marca marca : marcas) {
            descripcionesMarcas.add(marca.getDescripcion());
        }
        descripcionesMarcas.add("Otra");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, descripcionesMarcas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMarca.setAdapter(adapter);
    }

    private void cargarMarcas() {
        Call<RespuestaMarcas> call = apiService.obtenerMarcas("obtenerMarcas");
        call.enqueue(new Callback<RespuestaMarcas>() {
            @Override
            public void onResponse(Call<RespuestaMarcas> call, Response<RespuestaMarcas> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RespuestaMarcas.Marca> marcas = response.body().getMarcas();
                    actualizarSpinnerMarcas(marcas);
                } else {
                    Log.e("API", "Error al obtener marcas: " + response.message());
                    mostrarError("Error al cargar las marcas de vehículos");
                }
            }

            @Override
            public void onFailure(Call<RespuestaMarcas> call, Throwable t) {
                Log.e("API", "Error de red al obtener marcas", t);
                mostrarError("Error de conexión al cargar las marcas");
            }
        });
    }

    private void setupTipoVehiculoSpinner() {
        List<String> tiposVehiculo = new ArrayList<>();
        tiposVehiculo.add("Seleccione un tipo de vehículo");
        tiposVehiculo.add("Automóvil");
        tiposVehiculo.add("Motocicleta");
        tiposVehiculo.add("Camioneta");
        tiposVehiculo.add("Camión");
        tiposVehiculo.add("Minivan");
        tiposVehiculo.add("Colectivo");
        tiposVehiculo.add("Omnibus");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposVehiculo);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoVehiculo.setAdapter(adapter);
    }

    private void setupInfraccionSpinner() {
        // Inicialmente muestra "Cargando infracciones..."
        ArrayAdapter<String> loadingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                Collections.singletonList("Cargando infracciones..."));
        loadingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInfraccion.setAdapter(loadingAdapter);

        // Llama a la API para obtener las infracciones
        Call<RespuestaInfracciones> call = apiService.obtenerInfracciones("obtenerInfracciones");
        call.enqueue(new Callback<RespuestaInfracciones>() {
            @Override
            public void onResponse(Call<RespuestaInfracciones> call, Response<RespuestaInfracciones> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RespuestaInfracciones.Infraccion> infracciones = response.body().getInfracciones();
                    if (infracciones != null && !infracciones.isEmpty()) {
                        actualizarSpinnerInfracciones(infracciones);
                    } else {
                        mostrarError("No se encontraron infracciones.");
                    }
                } else {
                    Log.e("API", "Error al obtener infracciones: " + response.message());
                    mostrarError("Error al cargar las infracciones");
                }
            }

            @Override
            public void onFailure(Call<RespuestaInfracciones> call, Throwable t) {
                Log.e("API", "Error de red al obtener infracciones", t);
                mostrarError("Error de conexión al cargar las infracciones");
            }
        });
    }

    private void actualizarSpinnerInfracciones(List<RespuestaInfracciones.Infraccion> infracciones) {
        List<String> descripcionesInfracciones = new ArrayList<>();
        descripcionesInfracciones.add("Seleccione una infracción");
        infraccionIdMap.clear();
        for (RespuestaInfracciones.Infraccion infraccion : infracciones) {
            descripcionesInfracciones.add(infraccion.getDescripcion());
            infraccionIdMap.put(infraccion.getDescripcion(), infraccion.getId());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, descripcionesInfracciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInfraccion.setAdapter(adapter);

        // Limpiar infracciones seleccionadas y chips
        //infraccionesSeleccionadas.clear();
        // chipGroupInfracciones.removeAllViews();

        Log.d("DEBUG", "Infracciones cargadas: " + descripcionesInfracciones);
    }


    private void setupExpedidoPorSpinner() {
        List<String> expedidores = new ArrayList<>();
        expedidores.add("Seleccione un expedidor");
        expedidores.add("Posadas");
        expedidores.add("Otras ciudades..."); // Añade más ciudades según sea necesario
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, expedidores);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpedidaPor.setAdapter(adapter);
    }

    private void toggleVisibility(View view) {
        view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void tomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.d("MainActivity", "Archivo de imagen creado: " + photoFile.getAbsolutePath());
            } catch (IOException ex) {
                Log.e("MainActivity", "Error al crear el archivo de imagen", ex);
                mostrarMensaje("Error al crear el archivo de imagen: " + ex.getMessage());
                return;
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "es.rcti.demoprinterplus.pruebabd.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.d("MainActivity", "Iniciando captura de imagen con URI: " + photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Log.e("MainActivity", "No se pudo crear el archivo para la foto");
                mostrarMensaje("No se pudo crear el archivo para la foto");
            }
        } else {
            Log.e("MainActivity", "No se encontró una aplicación de cámara");
            mostrarMensaje("No se encontró una aplicación de cámara");
        }
    }

    private static final int REQUEST_PERMISSIONS = 1001;

    private void verificarYSolicitarPermisos() {
        List<String> permisosNecesarios = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisosNecesarios.add(Manifest.permission.CAMERA);
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permisosNecesarios.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!permisosNecesarios.isEmpty()) {
            ActivityCompat.requestPermissions(this, permisosNecesarios.toArray(new String[0]), REQUEST_PERMISSIONS);
        } else {
            listarAplicacionesCamara();
            dispatchTakePictureIntent();
        }
    }

    private void mostrarDialogoExplicativoPermisos() {
        new AlertDialog.Builder(this)
                .setTitle("Permisos necesarios")
                .setMessage("Esta aplicación necesita acceso a la cámara y al almacenamiento para funcionar correctamente. Por favor, conceda estos permisos en la configuración de la aplicación.")
                .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        Log.d("CameraDebug", "Archivo de imagen creado en: " + currentPhotoPath);
        return image;
    }

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("CameraDebug", "onActivityResult llamado - RequestCode: " + requestCode + ", ResultCode: " + resultCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Log.d("CameraDebug", "Imagen capturada con éxito");

                if (currentPhotoPaths[currentPhotoIndex] != null && !currentPhotoPaths[currentPhotoIndex].isEmpty()) {
                    Log.d("CameraDebug", "Ruta de la foto: " + currentPhotoPaths[currentPhotoIndex]);
                    File imagenFile = new File(currentPhotoPaths[currentPhotoIndex]);
                    if (imagenFile.exists()) {
                        Log.d("CameraDebug", "Archivo de imagen existe");
                        procesarImagen(imagenFile);
                        return; // Salimos después de procesar la imagen del archivo
                    } else {
                        Log.e("CameraDebug", "El archivo de imagen no existe en la ruta especificada");
                    }
                }

                // Si llegamos aquí, intentamos obtener la miniatura del Intent
                if (data != null && data.getExtras() != null && data.getExtras().containsKey("data")) {
                    Log.d("CameraDebug", "Intentando obtener miniatura del Intent");
                    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                    if (imageBitmap != null) {
                        Log.d("CameraDebug", "Miniatura obtenida del Intent");
                        procesarBitmap(imageBitmap);
                    } else {
                        Log.d("CameraDebug", "La miniatura es null, intentando recuperar por última ruta conocida");
                        if (currentPhotoPaths[currentPhotoIndex] != null) {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPaths[currentPhotoIndex]);
                                if (bitmap != null) {
                                    Log.d("CameraDebug", "Imagen recuperada de la ruta del archivo");
                                    procesarBitmap(bitmap);
                                } else {
                                    Log.e("CameraDebug", "No se pudo decodificar la imagen del archivo");
                                    mostrarError("No se pudo procesar la imagen");
                                }
                            } catch (Exception e) {
                                Log.e("CameraDebug", "Error al procesar el archivo de imagen", e);
                                mostrarError("Error al procesar la imagen");
                            }
                        } else {
                            Log.e("CameraDebug", "No hay datos de imagen disponibles");
                            mostrarError("No se pudo obtener la imagen");
                        }
                    }
                } else {
                    Log.d("CameraDebug", "El Intent es null, intentando recuperar por última ruta conocida");
                    if (currentPhotoPaths[currentPhotoIndex] != null) {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPaths[currentPhotoIndex]);
                            if (bitmap != null) {
                                Log.d("CameraDebug", "Imagen recuperada de la ruta del archivo");
                                procesarBitmap(bitmap);
                            } else {
                                Log.e("CameraDebug", "No se pudo decodificar la imagen del archivo");
                                mostrarError("No se pudo procesar la imagen");
                            }
                        } catch (Exception e) {
                            Log.e("CameraDebug", "Error al procesar el archivo de imagen", e);
                            mostrarError("Error al procesar la imagen");
                        }
                    } else {
                        Log.e("CameraDebug", "No hay datos de imagen disponibles");
                        mostrarError("No se pudo obtener la imagen");
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null && data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        procesarBitmap(bitmap);
                    } catch (IOException e) {
                        Log.e("CameraDebug", "Error al procesar la imagen seleccionada", e);
                        mostrarError("Error al procesar la imagen seleccionada");
                    }
                } else {
                    Log.e("CameraDebug", "No se pudo obtener la imagen seleccionada");
                    mostrarError("No se pudo obtener la imagen seleccionada");
                }
            }
        } else {
            Log.d("CameraDebug", "Resultado no OK o requestCode no reconocido");
            mostrarError("No se pudo capturar o seleccionar la imagen");
        }
    }

    private void procesarImagen(File imagenFile) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagenFile.getAbsolutePath());
            if (bitmap != null) {
                photoPreviewViews[currentPhotoIndex].setImageBitmap(bitmap);
                photoPreviewViews[currentPhotoIndex].setVisibility(View.VISIBLE);

                String base64Image = convertirImagenABase64(imagenFile);
                imagenBase64List.add(base64Image);

                currentPhotoIndex++;
                actualizarBotonTomarFoto();

                Log.d("DEBUG", "Imagen procesada y convertida a base64. Total de imágenes: " + imagenBase64List.size());
            } else {
                Log.e("CameraDebug", "No se pudo decodificar el bitmap desde el archivo");
                mostrarError("No se pudo procesar la imagen");
            }
        } catch (Exception e) {
            Log.e("CameraDebug", "Error al procesar la imagen", e);
            mostrarError("Error al procesar la imagen: " + e.getMessage());
        }
    }

    private void procesarBitmap(Bitmap bitmap) {
        try {
            photoPreviewViews[currentPhotoIndex].setImageBitmap(bitmap);
            photoPreviewViews[currentPhotoIndex].setVisibility(View.VISIBLE);

            String base64Image = convertirBitmapABase64(bitmap);
            imagenBase64List.add(base64Image);

            currentPhotoIndex++;
            actualizarBotonTomarFoto();

            Log.d("CameraDebug", "Bitmap procesado y convertido a base64. Total de imágenes: " + imagenBase64List.size());
        } catch (Exception e) {
            Log.e("CameraDebug", "Error al procesar el bitmap", e);
            mostrarError("Error al procesar la imagen: " + e.getMessage());
        }
    }

    private void actualizarBotonTomarFoto() {
        btnTomarFoto.setText("Agregar Foto (" + currentPhotoIndex + "/2)");
    }

    private void debugCameraAvailability() {
        Log.d("CameraDebug", "Iniciando verificación de disponibilidad de cámara");
        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Log.d("CameraDebug", "El dispositivo tiene una cámara");
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            List<ResolveInfo> activities = pm.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
            Log.d("CameraDebug", "Número de actividades que pueden manejar la intención de cámara: " + activities.size());
            for (ResolveInfo info : activities) {
                Log.d("CameraDebug", "Actividad de cámara encontrada: " + info.activityInfo.packageName + "/" + info.activityInfo.name);
            }
        } else {
            Log.e("CameraDebug", "El dispositivo no tiene una cámara");
        }

        // Verificar permisos de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d("CameraDebug", "Permiso de cámara concedido");
        } else {
            Log.e("CameraDebug", "Permiso de cámara NO concedido");
        }

        // Verificar permisos de almacenamiento
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d("CameraDebug", "Permiso de almacenamiento concedido");
        } else {
            Log.e("CameraDebug", "Permiso de almacenamiento NO concedido");
        }
    }

    private void subirImagenDesdeUri(String actaId, Uri imageUri) {
        // Implementa la lógica para subir la imagen seleccionada de la galería
        // Puedes convertir el Uri a un File si es necesario
        // O adaptar tu método subirImagen para que acepte un Uri en lugar de un File
    }

    private String obtenerActaIdActual() {
        if (actaIdActual == null || actaIdActual.isEmpty()) {
            // Si no hay un ID de acta actual, genera uno nuevo
            actaIdActual = generarNumeroAleatorio();
            Log.d("MainActivity", "Generando nuevo ID de acta: " + actaIdActual);
        } else {
            Log.d("MainActivity", "Usando ID de acta existente: " + actaIdActual);
        }
        return actaIdActual;
    }

    private void subirImagen(String actaId, List<String> imagenBase64List) {
        Log.d("DEBUG", "Iniciando subirImagen. ActaId: " + actaId + ", Número de imágenes: " + imagenBase64List.size());

        Call<RespuestaSubirImagen> call = apiService.subirImagen("subirImagen", actaId, imagenBase64List);
        call.enqueue(new Callback<RespuestaSubirImagen>() {
            @Override
            public void onResponse(Call<RespuestaSubirImagen> call, Response<RespuestaSubirImagen> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RespuestaSubirImagen respuesta = response.body();
                    Log.d("DEBUG", "Respuesta de subirImagen: " + new Gson().toJson(respuesta));
                    if (respuesta.isSuccess()) {
                        Log.d("DEBUG", "Imágenes subidas exitosamente");
                        mostrarMensaje("Imágenes subidas con éxito");
                        if (respuesta.getImagenesUrls() != null) {
                            for (String url : respuesta.getImagenesUrls()) {
                                Log.d("DEBUG", "URL de imagen subida: " + url);
                            }
                        }
                    } else {
                        Log.e("DEBUG", "Error al subir imágenes: " + respuesta.getError());
                        mostrarError("Error al subir imágenes: " + respuesta.getError());
                    }
                } else {
                    Log.e("DEBUG", "Error en la respuesta del servidor: " + response.code());
                    try {
                        Log.e("DEBUG", "Cuerpo del error: " + response.errorBody().string());
                        mostrarError("Error en la respuesta del servidor: " + response.code());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<RespuestaSubirImagen> call, Throwable t) {
                Log.e("DEBUG", "Error de conexión al subir imágenes", t);
                mostrarError("Error de conexión al subir imágenes: " + t.getMessage());
            }
        });
    }

    private String convertirImagenABase64(File imagenFile) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagenFile.getAbsolutePath());
            return convertirBitmapABase64(bitmap);
        } catch (Exception e) {
            Log.e("CameraDebug", "Error al convertir imagen a base64", e);
            return null;
        }
    }

    private String convertirBitmapABase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_PERMISSION);
            } else {
                conectarImpresora();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                        REQUEST_BLUETOOTH_PERMISSION);
            } else {
                conectarImpresora();
            }
        }
    }

    private void conectarImpresora() {
        if (bluetoothAdapter == null) {
            mostrarMensaje("Bluetooth no disponible en este dispositivo");
            tvEstado.setText("Estado: Bluetooth no disponible");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            mostrarMensaje("Por favor, active el Bluetooth");
            tvEstado.setText("Estado: Bluetooth desactivado");
            return;
        }

        buscarYConectarDispositivo();
    }

    private void buscarYConectarDispositivo() {
        try {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.isEmpty()) {
                mostrarMensaje("No se encontraron dispositivos emparejados");
                tvEstado.setText("Estado: No hay dispositivos Bluetooth emparejados");
                return;
            }

            for (BluetoothDevice device : pairedDevices) {
                try {
                    bluetoothDevice = device;
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    bluetoothSocket.connect();
                    mostrarMensaje("Conectado a: " + device.getName());
                    tvEstado.setText("Estado: Conectado a " + device.getName());
                    imprimirMulta();
                    return;
                } catch (IOException e) {
                    mostrarMensaje("Error al conectar con " + device.getName() + ": " + e.getMessage());
                }
            }
            mostrarMensaje("No se pudo conectar a ninguna impresora");
            tvEstado.setText("Estado: No se pudo conectar a ninguna impresora");
        } catch (SecurityException e) {
            mostrarMensaje("Error de seguridad: " + e.getMessage());
            tvEstado.setText("Estado: Error de seguridad en Bluetooth");
        }
    }

    public void printPhoto(int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), img);
            if (bmp != null) {
                byte[] command = Utils.decodeBitmap(bmp);
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(command);

                // Imprimir el texto al lado de la imagen
                outputStream.write(PrinterCommands.ESC_ALIGN_LEFT); // Alinear a la izquierda
                String texto = "";
                outputStream.write(texto.getBytes());
                printNewLine(); // Asegurarse de avanzar a la siguiente línea
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    public void printText(byte[] data) {
        try {
            if (outputStream != null) {
                outputStream.write(data);
            }
        } catch (IOException e) {
            mostrarMensaje("Error al enviar datos a la impresora: " + e.getMessage());
        }
    }

    private void printNewLine() {
        try {
            if (outputStream != null) {
                outputStream.write(PrinterCommands.FEED_LINE);
                outputStream.flush(); // Aseguramos que se envíe inmediatamente
            } else {
                Log.e("PrinterError", "OutputStream es null");
            }
        } catch (IOException e) {
            Log.e("PrinterError", "Error al imprimir nueva línea: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje("Error al avanzar el papel: " + e.getMessage());
        }
    }


    private void imprimirMulta() {
        Log.d("ImpresionMulta", "Iniciando impresión de multa");
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            mostrarMensaje("No hay conexión con la impresora");
            return;
        }

        try {
            outputStream = bluetoothSocket.getOutputStream();
            Log.d("ImpresionMulta", "OutputStream obtenido");

            // Inicializar la impresora
            outputStream.write(new byte[]{0x1B, 0x40}); // Reset
            outputStream.write(new byte[]{0x1B, 0x4D, 0x01}); // Fuente pequeña
            Log.d("ImpresionMulta", "Impresora inicializada");

            int lineWidth = 32; // Caracteres por línea para 57mm

            // Obtener la fecha y hora actual
            Calendar calendar = Calendar.getInstance();
            int dia = calendar.get(Calendar.DAY_OF_MONTH);
            int mes = calendar.get(Calendar.MONTH) + 1;
            int anio = calendar.get(Calendar.YEAR);
            int hora = calendar.get(Calendar.HOUR_OF_DAY);
            int minuto = calendar.get(Calendar.MINUTE);

            // Imprimir la imagen primero
            Log.d("ImpresionMulta", "Imprimiendo imagen");
            printPhoto(R.drawable.logos8);

            Log.d("ImpresionMulta", "Imprimiendo encabezado");
            imprimirCampoEnLinea("SERIE A - 2024", "", lineWidth);
            imprimirCampoEnLinea("", "", lineWidth);

            Log.d("ImpresionMulta", "Imprimiendo numero de Acta y fecha");
            printNewLine();
            imprimirCampoEnLinea("Numero de Acta: ", obtenerActaIdActual(), lineWidth);
            imprimirCampoEnLinea("Fecha: ", String.format("%02d/%02d/%04d", dia, mes, anio), lineWidth);
            imprimirCampoEnLinea("Hora: ", String.format("%02d:%02d", hora, minuto), lineWidth);
            printNewLine();

            Log.d("ImpresionMulta", "Imprimiendo sección conductor");
            printNewLine();
            imprimirCampoEnLinea("CONDUCTOR", "", lineWidth);
            printNewLine();
            imprimirCampoEnLinea("Apellido y Nombre: ", etApellidoNombre.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Tipo Documento: ", spinnerTipoDocumento.getSelectedItem().toString(), lineWidth);
            imprimirCampoEnLinea("Numero Documento: ", etNumeroDocumento.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Domicilio: ", etDomicilio.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Localidad: ", actvLocalidad.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Codigo Postal: ", etCodPostal.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Departamento: ", actvDepartamento.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Provincia: ", etProvincia.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Pais: ", etPais.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Licencia: ", etLicencia.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Expedida por: ", spinnerExpedidaPor.getSelectedItem().toString(), lineWidth);
            imprimirCampoEnLinea("Clase: ", etClase.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Vencimiento: ", etVencimiento.getText().toString(), lineWidth);
            printNewLine();

            Log.d("ImpresionMulta", "Imprimiendo sección vehículo");
            printNewLine();
            imprimirCampoEnLinea("VEHICULO", "", lineWidth);
            printNewLine();
            imprimirCampoEnLinea("Dominio: ", etDominio.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Propietario: ", etPropietario.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Marca: ", spinnerMarca.getSelectedItem().toString(), lineWidth);
            if (spinnerMarca.getSelectedItem().toString().equals("Otra")) {
                imprimirCampoEnLinea("Otra Marca: ", etOtraMarca.getText().toString(), lineWidth);
            }
            imprimirCampoEnLinea("Modelo: ", etModeloVehiculo.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Tipo Vehiculo: ", spinnerTipoVehiculo.getSelectedItem().toString(), lineWidth);
            printNewLine();

            Log.d("ImpresionMulta", "Imprimiendo sección hecho");
            printNewLine();
            imprimirCampoEnLinea("HECHO", "", lineWidth);
            printNewLine();
            imprimirCampoEnLinea("Infracciones:", "", lineWidth);
            for (String infraccion : infraccionesSeleccionadas) {
                imprimirCampoEnLinea("- ", infraccion, lineWidth);
            }
            imprimirCampoEnLinea("Lugar: ", etLugar.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Departamento Multa: ", etDepartamentoMulta.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Municipio Multa: ", etMunicipioMulta.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Info Multa: ", etMultaInfo.getText().toString(), lineWidth);
            printNewLine();

            Log.d("ImpresionMulta", "Imprimiendo sección especificaciones");
            printNewLine();
            imprimirCampoEnLinea("ESPECIFICACIONES", "", lineWidth);
            printNewLine();

            // Obtener el tipo de equipo seleccionado
            String tipoEquipo = spinnerTipoEquipo.getSelectedItem().toString();
            imprimirCampoEnLinea("Tipo Equipo: ", tipoEquipo, lineWidth);

            // Ajustar la impresión según el tipo de equipo
            if (tipoEquipo.equals("Etilometro")) {
                imprimirCampoEnLinea("Equipo: ", "Etilometro con dispositivo impresor", lineWidth);
                imprimirCampoEnLinea("Marca: ", etMarcaCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Modelo: ", etModeloCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Serie: ", etSerieCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Codigo Aprobacion: ", etCodAprobacionCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Valor: ", etValorCinemometro.getText().toString(), lineWidth);
            } else if (tipoEquipo.equals("Decibelimetro")) {
                imprimirCampoEnLinea("Equipo: ", "Decibelimetro", lineWidth);
                imprimirCampoEnLinea("Marca: ", etMarcaCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Modelo: ", etModeloCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Serie: ", etSerieCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Codigo Aprobacion: ", etCodAprobacionCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Valor: ", etValorCinemometro.getText().toString() + " dB", lineWidth);
            } else {
                imprimirCampoEnLinea("Equipo: ", etEquipo.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Marca: ", etMarcaCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Modelo: ", etModeloCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Serie: ", etSerieCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Codigo Aprobacion: ", etCodAprobacionCinemometro.getText().toString(), lineWidth);
                imprimirCampoEnLinea("Valor: ", etValorCinemometro.getText().toString(), lineWidth);
            }
            printNewLine();

            String urlFirma = obtenerUrlFirma(legajoInspector);
            Log.d("Firma", "URL de la firma: " + urlFirma);
            verificarExistenciaFirma(urlFirma);
            Log.d("Firma", "Datos del Inspector:");
            Log.d("Firma", "Nombre: " + nombreInspector);
            Log.d("Firma", "Apellido: " + apellidoInspector);
            Log.d("Firma", "Legajo: " + legajoInspector);

            Log.d("Firma", "URL de la firma: " + urlFirma);
            verificarExistenciaFirma(urlFirma);
            Log.d("Firma", "URL de la firma: " + urlFirma);
            Log.d("ImpresionMulta", "Imprimiendo sección datos del inspector");
            printNewLine();
            imprimirCampoEnLinea("DATOS DEL INSPECTOR", "", lineWidth);
            printNewLine();
            imprimirCampoEnLinea("Inspector: ", nombreInspector + " " + apellidoInspector, lineWidth);
            imprimirCampoEnLinea("Legajo: ", legajoInspector, lineWidth);
            printNewLine();



            // Después de imprimir los datos del inspector
            outputStream.write(PrinterCommands.FEED_LINE);
            outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
            imprimirCampoEnLinea("FIRMA DEL INSPECTOR", "", lineWidth);
            outputStream.write(PrinterCommands.FEED_LINE);

// Imprimir la firma
            Log.d("Firma", "¿OutputStream es null? " + (outputStream == null));
            imprimirFirma(urlFirma, lineWidth);

            outputStream.write(PrinterCommands.ESC_ALIGN_LEFT); // Volver a alineación izquierda
            outputStream.write(PrinterCommands.FEED_LINE);

            imprimirCampoEnLinea("LA PRESENTE SERA ELEVADA PARA SU INTERVENCION A LA AUTORIDAD DE JUZGAMIENTO DEL TRIBUNAL MUNICIPAL DE FALTAS, CON DOMICILIO EN: SAN MARTIN 1555 POSADAS, DONDE PODRA OFRECER DESCARGO Y EJERCER SU DERECHO DE DEFENSA EN LOS TERMINOS DE LOS ARTS.69,70 Y 71 DE LA LEY 24.449.", "", lineWidth);
            outputStream.write(new byte[]{0x0A, 0x0A, 0x0A, 0x0A});
            outputStream.write(new byte[]{0x1D, 0x56, 0x01}); // Corte parcial

            Log.d("ImpresionMulta", "Finalizando impresion");
            outputStream.flush();
            mostrarMensaje("Multa impresa con éxito");

        } catch (IOException e) {
            Log.e("ImpresionMulta", "Error al imprimir: " + e.getMessage(), e);
            mostrarMensaje("Error al imprimir: " + e.getMessage());
        }
    }

    private String obtenerUrlFirma(String legajo) {
        return "https://systemposadas.com/test_firma.php?legajo=" + legajo;
    }


    private void imprimirFirma(String urlFirma, int lineWidth) {
        // Primero intentar usar el comando precargado
        if (comandoFirma != null) {


            try {
                if (comandoFirma != null) {
                    outputStream.write(PrinterCommands.ESC_INIT);
                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    outputStream.write(comandoFirma);
                    outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    outputStream.write(PrinterCommands.FEED_LINE);
                    outputStream.flush();
                    Log.d("Firma", "Firma impresa usando comando precargado");
                    return;
                }
            } catch (Exception e) {
                Log.e("Firma", "Error usando comando precargado", e);
            }
        }

        // Si no hay comando precargado pero hay firma procesada
        if (firmaProcesada != null) {
            try {
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                byte[] command = Utils.decodeBitmap(firmaProcesada);
                outputStream.write(command);
                outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                outputStream.write(PrinterCommands.FEED_LINE);
                outputStream.flush();

                // Guardar el comando para futuras impresiones
                comandoFirma = command;
                Log.d("Firma", "Firma impresa usando bitmap precargado");
                return;
            } catch (Exception e) {
                Log.e("Firma", "Error usando firma precargada", e);
            }
        }
        // Método original como fallback
        new AsyncTask<String, String, Bitmap>() {
            private Bitmap convertirABlancoYNegro(Bitmap original) {
                int width = original.getWidth();
                int height = original.getHeight();
                Bitmap bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                // Aumentar el umbral para hacer la imagen más limpia
                int threshold = 160; // Aumentado de 128 a 160 para ser más estricto

                // Matriz para ayudar a eliminar ruido
                int[][] pixels = new int[width][height];

                // Primera pasada - obtener valores
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int pixel = original.getPixel(x, y);
                        int red = Color.red(pixel);
                        int green = Color.green(pixel);
                        int blue = Color.blue(pixel);
                        int gray = (red + green + blue) / 3;
                        pixels[x][y] = gray;
                    }
                }

                // Segunda pasada - aplicar umbral y eliminar ruido
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        // Verificar píxeles vecinos para eliminar ruido
                        if (x > 0 && x < width-1 && y > 0 && y < height-1) {
                            int count = 0;
                            for (int i = -1; i <= 1; i++) {
                                for (int j = -1; j <= 1; j++) {
                                    if (pixels[x+i][y+j] < threshold) count++;
                                }
                            }
                            // Si la mayoría de los vecinos son negros, este pixel será negro
                            bwBitmap.setPixel(x, y, (count > 4) ? Color.BLACK : Color.WHITE);
                        } else {
                            // Para bordes, usar umbral simple
                            bwBitmap.setPixel(x, y, pixels[x][y] < threshold ? Color.BLACK : Color.WHITE);
                        }
                    }
                }

                return bwBitmap;
            }

            @Override
            protected void onPreExecute() {
                try {
                    outputStream.write("\n".getBytes());
                    outputStream.flush();
                } catch (IOException e) {
                    Log.e("Firma", "Error al escribir estado inicial", e);
                }
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                HttpURLConnection connection = null;
                try {
                    TrustManager[] trustAllCerts = new TrustManager[]{
                            new X509TrustManager() {
                                public X509Certificate[] getAcceptedIssuers() {
                                    return new X509Certificate[0];
                                }
                                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                            }
                    };

                    SSLContext sc = SSLContext.getInstance("TLS");
                    sc.init(null, trustAllCerts, new SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

                    URL url = new URL(params[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setConnectTimeout(30000);
                    connection.setReadTimeout(30000);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Cache-Control", "no-cache");

                    connection.connect();
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        if (jsonResponse.getBoolean("success")) {
                            String base64Data = jsonResponse.getString("firma");
                            byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
                            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        }
                    }
                } catch (Exception e) {
                    Log.e("Firma", "Error descargando firma", e);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                try {
                    if (bitmap != null) {
                        // Aumentar el tamaño de la firma
                        int newWidth = 400;
                        int newHeight = (bitmap.getHeight() * newWidth) / bitmap.getWidth();
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                        // Convertir a B/N
                        Bitmap bwBitmap = convertirABlancoYNegro(resizedBitmap);

                        // Generar comando de impresión
                        byte[] command = Utils.decodeBitmap(bwBitmap);

                        // Imprimir
                        outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                        outputStream.write(command);
                        outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                        outputStream.write(PrinterCommands.FEED_LINE);

                        // Almacenar para uso futuro
                        firmaProcesada = bwBitmap;
                        comandoFirma = command;

                        bitmap.recycle();
                        resizedBitmap.recycle();

                        Log.d("Firma", "Firma impresa y almacenada desde fallback");
                    } else {
                        Log.e("Firma", "Error: bitmap nulo en fallback");
                    }
                    outputStream.flush();
                } catch (Exception e) {
                    Log.e("Firma", "Error al procesar imagen en fallback", e);
                }
            }
        }.execute(urlFirma);
    }


    private byte[] readStreamWithLog(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        int total = 0;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
            total += bytesRead;
            Log.d("Firma-Posnet", "Leyendo stream: " + total + " bytes");
        }
        return baos.toByteArray();
    }
    private String getStackTraceString(Exception e) {
        if (e == null) return "No stack trace";

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString().split("\n")[0]; // Solo primera línea del stack
    }
    private String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length && i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }
    private byte[] convertInputStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    public class DownloadFirmaTask extends AsyncTask<String, String, Bitmap> {
        private static final String TAG = "Firma";
        private OutputStream outputStream;

        public DownloadFirmaTask(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        protected void onPreExecute() {
            try {
                outputStream.write("\n=== Iniciando descarga de firma ===\n".getBytes());
                outputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error al escribir estado inicial", e);
            }
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            HttpURLConnection connection = null;
            try {
                String urlStr = params[0];
                publishProgress("Conectando a: " + urlStr);

                URL url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);

                publishProgress("Iniciando conexión...");
                connection.connect();

                int responseCode = connection.getResponseCode();
                publishProgress("Código de respuesta: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Leer la respuesta JSON
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());

                    if (jsonResponse.getBoolean("success")) {
                        String base64Data = jsonResponse.getString("firma");
                        publishProgress("Firma Base64 recibida, longitud: " + base64Data.length());

                        // Decodificar Base64 a bytes
                        byte[] imageBytes = Base64.decode(base64Data, Base64.DEFAULT);
                        publishProgress("Bytes decodificados: " + imageBytes.length);

                        // Convertir bytes a Bitmap
                        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    } else {
                        String error = jsonResponse.optString("error", "Error desconocido");
                        publishProgress("Error en respuesta: " + error);
                    }
                } else {
                    publishProgress("Error HTTP: " + responseCode);
                    try {
                        InputStream errorStream = connection.getErrorStream();
                        if (errorStream != null) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                            StringBuilder error = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                error.append(line);
                            }
                            publishProgress("Error detallado: " + error.toString());
                        }
                    } catch (Exception e) {
                        publishProgress("No se pudo leer mensaje de error");
                    }
                }
            } catch (Exception e) {
                publishProgress("Error: " + e.getMessage());
                Log.e(TAG, "Error descargando firma", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            try {
                outputStream.write((values[0] + "\n").getBytes());
                outputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error al escribir progreso", e);
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            try {
                if (bitmap != null) {
                    outputStream.write("Procesando imagen...\n".getBytes());

                    // Redimensionar si es necesario
                    int newWidth = 200;
                    int newHeight = (bitmap.getHeight() * newWidth) / bitmap.getWidth();
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                    // Convertir a B/N
                    Bitmap bwBitmap = convertirABlancoYNegro(resizedBitmap);

                    // Imprimir
                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    byte[] command = Utils.decodeBitmap(bwBitmap);
                    outputStream.write(command);
                    outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    outputStream.write(PrinterCommands.FEED_LINE);

                    bitmap.recycle();
                    resizedBitmap.recycle();
                    bwBitmap.recycle();

                    outputStream.write("Firma impresa exitosamente\n".getBytes());
                } else {
                    outputStream.write("Error: No se pudo procesar la imagen\n".getBytes());
                }
                outputStream.flush();
            } catch (Exception e) {
                Log.e(TAG, "Error al procesar imagen", e);
                try {
                    outputStream.write(("Error final: " + e.getMessage() + "\n").getBytes());
                    outputStream.flush();
                } catch (IOException ioe) {
                    Log.e(TAG, "Error al escribir error final", ioe);
                }
            }
        }

        private Bitmap convertirABlancoYNegro(Bitmap original) {
            int width = original.getWidth();
            int height = original.getHeight();
            Bitmap bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            int threshold = 128;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = original.getPixel(x, y);
                    int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                    int bw = gray < threshold ? Color.BLACK : Color.WHITE;
                    bwBitmap.setPixel(x, y, bw);
                }
            }
            return bwBitmap;
        }
    }
    private void verificarDatosFirma() {
        String urlFirma = obtenerUrlFirma(legajoInspector);
        Log.d("Firma", "=== Verificación de Firma ===");
        Log.d("Firma", "Datos del Inspector:");
        Log.d("Firma", "Nombre: " + nombreInspector);
        Log.d("Firma", "Apellido: " + apellidoInspector);
        Log.d("Firma", "Legajo: " + legajoInspector);
        Log.d("Firma", "URL de la firma: " + urlFirma);
        verificarExistenciaFirma(urlFirma);
    }
    private void setupEquipoMedicionSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tipos_equipo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoEquipo.setAdapter(adapter);

        spinnerTipoEquipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccion = parent.getItemAtPosition(position).toString();
                Log.d("EquipoMedicion", "Equipo seleccionado: " + seleccion);
                autocompletarEquipoMedicion(seleccion);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
               // limpiarCamposEquipoMedicion();
            }
        });
    }
    private void verificarExistenciaFirma(String urlFirma) {
        new Thread(() -> {
            try {
                URL url = new URL(urlFirma);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                Log.d("Firma", "Verificación de firma - URL: " + urlFirma);
                Log.d("Firma", "Código de respuesta: " + responseCode);
                if (responseCode == 200) {
                    Log.d("Firma", "La imagen de la firma existe");
                } else {
                    Log.d("Firma", "La imagen de la firma NO existe");
                }
            } catch (Exception e) {
                Log.e("Firma", "Error al verificar firma: " + e.getMessage());
            }
        }).start();
    }
    private void autocompletarEquipoMedicion(String tipoEquipo) {
        Log.d("EquipoMedicion", "Autocompleting para: " + tipoEquipo);
        runOnUiThread(() -> {
            switch (tipoEquipo) {
                case "Etilometro":
                    Toast.makeText(this, "Seleccionado: Etilometro", Toast.LENGTH_SHORT).show();
                    if (etEquipo != null) etEquipo.setText("Etilometro con dispositivo impresor");
                    if (etMarcaCinemometro != null) etMarcaCinemometro.setText("ACS");
                    if (etModeloCinemometro != null) etModeloCinemometro.setText("SAFIR EVOLUTION");
                    if (etCodAprobacionCinemometro != null) etCodAprobacionCinemometro.setText("ET 10-2253");
                    if (etValorCinemometro != null) etValorCinemometro.setText("");
                    mostrarDialogoSeleccionModelo();
                    break;

                case "Decibelimetro":
                    Toast.makeText(this, "Seleccionado: Decibelimetro", Toast.LENGTH_SHORT).show();
                    if (etEquipo != null) etEquipo.setText("Decibelimetro");
                    if (etMarcaCinemometro != null) etMarcaCinemometro.setText("Uni-T");
                    if (etModeloCinemometro != null) etModeloCinemometro.setText("UT353");
                    if (etSerieCinemometro != null) etSerieCinemometro.setText("C203115656 (96697)");
                    if (etCodAprobacionCinemometro != null) etCodAprobacionCinemometro.setText("C12122301");
                    if (etValorCinemometro != null) etValorCinemometro.setText("");
                    break;


                case "Seleccione un tipo":
                default:
                   // limpiarCamposEquipoMedicion();
                    break;
            }
        });
    }


    private void mostrarDialogoSeleccionModelo() {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Seleccione el modelo específico");

            final String[] modelos = getResources().getStringArray(R.array.etilometros);

            builder.setItems(modelos, (dialog, which) -> {
                String modeloSeleccionado = modelos[which];
                Log.d("EquipoMedicion", "Modelo seleccionado: " + modeloSeleccionado);

                // Separar el modelo y el número de serie
                String[] partes = modeloSeleccionado.split(" - ");
                if (partes.length == 2) {
                    if (etModeloCinemometro != null) etModeloCinemometro.setText(partes[0]);
                    if (etSerieCinemometro != null) etSerieCinemometro.setText(partes[1]);

                    // Asignar precintos según el modelo seleccionado
                    switch (partes[1]) {
                        case "SESAH1R016002624":
                            if (etCodAprobacionCinemometro != null)
                                etCodAprobacionCinemometro.setText("B94097 (A) / B94098 (A) / B94099 (A)(Impresora)");
                            break;
                        case "SESAH1Q111001627":
                            if (etCodAprobacionCinemometro != null)
                                etCodAprobacionCinemometro.setText("R7217 (A) / R7218 (A) / R7219 (A)(Impresora)");
                            break;
                        case "SESAH1P319001342":
                            if (etCodAprobacionCinemometro != null)
                                etCodAprobacionCinemometro.setText("R7214 (A) / R7215 (A) / R7216 (A)(Impresora)");
                            break;
                        case "SESAH1T206003896":
                            if (etCodAprobacionCinemometro != null)
                                etCodAprobacionCinemometro.setText("B94100 (A) / B94101 (A) / B94102 (A)(Impresora)");
                            break;
                    }

                    Toast.makeText(MainActivity.this,
                            "Modelo seleccionado: " + partes[0],
                            Toast.LENGTH_SHORT).show();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }
    private void mostrarDialogoSeleccionEquipo(String tipoArray) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Seleccione el Equipo");

            // Obtener el array correspondiente según el tipo
            int arrayId = getResources().getIdentifier(tipoArray, "array", getPackageName());
            final String[] equipos = getResources().getStringArray(arrayId);

            builder.setItems(equipos, (dialog, which) -> {
                // Extraer información del equipo seleccionado
                String equipoSeleccionado = equipos[which];
                String[] partes = equipoSeleccionado.split(" - ");

                if (etModeloCinemometro != null) {
                    etModeloCinemometro.setText(partes[0]);
                }
                if (etSerieCinemometro != null) {
                    etSerieCinemometro.setText(partes[1]);
                }

                // Cargar precintos correspondientes si es un etilómetro
                if (tipoArray.equals("etilometros")) {
                    int precintosArrayId = getResources().getIdentifier(
                            "precintos_etilometro_" + (which + 1),
                            "array",
                            getPackageName()
                    );
                    String[] precintos = getResources().getStringArray(precintosArrayId);
                    StringBuilder precintosStr = new StringBuilder();
                    for (String precinto : precintos) {
                        if (precintosStr.length() > 0) precintosStr.append(" / ");
                        precintosStr.append(precinto);
                    }
                    if (etCodAprobacionCinemometro != null) {
                        etCodAprobacionCinemometro.setText(precintosStr.toString());
                    }
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }
    private void imprimirCampoEnLinea(String etiqueta, String valor, int lineWidth) {
        try {
            String texto = etiqueta + valor;
            // Dividimos el texto en palabras
            String[] palabras = texto.split(" ");
            StringBuilder lineaActual = new StringBuilder();
            List<String> palabrasEnLinea = new ArrayList<>();

            for (String palabra : palabras) {
                if (lineaActual.length() + palabra.length() + 1 <= lineWidth) {
                    if (lineaActual.length() > 0) {
                        lineaActual.append(" ");
                    }
                    lineaActual.append(palabra);
                    palabrasEnLinea.add(palabra);
                } else {
                    if (!palabrasEnLinea.isEmpty()) {
                        // Justificar la línea actual
                        int espaciosNecesarios = lineWidth - (lineaActual.length() - (palabrasEnLinea.size() - 1));
                        int espaciosExtra = palabrasEnLinea.size() > 1 ? espaciosNecesarios - (palabrasEnLinea.size() - 1) : 0;

                        StringBuilder lineaJustificada = new StringBuilder();
                        for (int i = 0; i < palabrasEnLinea.size(); i++) {
                            lineaJustificada.append(palabrasEnLinea.get(i));
                            if (i < palabrasEnLinea.size() - 1) {
                                int espaciosAdicionales = espaciosExtra / (palabrasEnLinea.size() - 1);
                                if (i < espaciosExtra % (palabrasEnLinea.size() - 1)) {
                                    espaciosAdicionales++;
                                }
                                // Reemplazamos String.repeat() con un loop
                                for (int j = 0; j < espaciosAdicionales + 1; j++) {
                                    lineaJustificada.append(" ");
                                }
                            }
                        }
                        outputStream.write((lineaJustificada.toString() + "\n").getBytes());
                    }
                    // Comenzar nueva línea
                    lineaActual = new StringBuilder(palabra);
                    palabrasEnLinea.clear();
                    palabrasEnLinea.add(palabra);
                }
            }

            // Imprimir la última línea (sin justificar)
            if (lineaActual.length() > 0) {
                outputStream.write((lineaActual.toString() + "\n").getBytes());
            }
        } catch (IOException e) {
            Log.e("ImpresionMulta", "Error al imprimir campo: " + etiqueta, e);
            throw new RuntimeException("Error al imprimir campo: " + etiqueta, e);
        }
    }
    // Método para generar un nuevo ID de acta (ya existente en tu código)
    private String generarNumeroAleatorio() {
        Random random = new Random();
        int numeroAleatorio = 100000 + random.nextInt(900000);
        return String.valueOf(numeroAleatorio);
    }

    // Método para establecer el ID del acta actual (a llamar después de insertar un acta)
    private void setActaIdActual(String id) {
        this.actaIdActual = id;
    }
    private void insertarDatosConductor() {
        if (infraccionesSeleccionadas.isEmpty()) {
            mostrarError("Debe seleccionar al menos una infracción");
            return;
        }

        try {
            Log.d("DEBUG", "Iniciando insertarDatosConductor");

            String inspectorId = getIntent().getStringExtra("INSPECTOR_ID");
            if (inspectorId == null || inspectorId.isEmpty()) {
                mostrarError("No se pudo obtener la identificación del inspector");
                return;
            }

            // Verificar si el Bluetooth está disponible y activado antes de continuar
            if (bluetoothAdapter == null) {
                mostrarError("Bluetooth no disponible en este dispositivo. No se podrá imprimir.");
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                mostrarError("Por favor, active el Bluetooth antes de continuar");
                return;
            }

            // Mostrar loading overlay
            showLoading(true);
            btnInsertarConductor.setEnabled(false);

            // Preparar datos
            JSONArray infraccionesJson = new JSONArray();
            for (String infraccion : infraccionesSeleccionadas) {
                String infraccionId = infraccionIdMap.get(infraccion);
                if (infraccionId != null) {
                    infraccionesJson.put(infraccionId);
                }
            }

            // Obtener y validar datos del formulario
            String numero = generarNumeroAleatorio();
            tvNumero.setText(numero);
            String fecha = getCurrentDate();
            String hora = getCurrentTime();
            String dominio = etDominio.getText().toString().trim();
            String lugar = etLugar.getText().toString().trim();
            String infractorDni = etNumeroDocumento.getText().toString().trim();
            String infractorNombre = etApellidoNombre.getText().toString().trim();
            String infractorDomicilio = etDomicilio.getText().toString().trim();
            String infractorLocalidad = actvLocalidad.getText().toString().trim();
            String infractorCp = etCodPostal.getText().toString().trim();
            String infractorProvincia = etProvincia.getText().toString().trim();
            String infractorPais = etPais.getText().toString().trim();
            String infractorLicencia = etLicencia.getText().toString().trim();
            String tipoVehiculo = spinnerTipoVehiculo.getSelectedItem().toString();
            String marcaVehiculo = spinnerMarca.getSelectedItem().toString();
            String propietario = etPropietario.getText().toString().trim();
            String modeloVehiculo = etModeloVehiculo.getText().toString().trim();
            String departamento = etDepartamentoMulta.getText().toString().trim();
            String municipio = etMunicipioMulta.getText().toString().trim();
            String observaciones = etMultaInfo.getText().toString().trim();

            // Validación de campos requeridos
            if (TextUtils.isEmpty(infractorNombre) || TextUtils.isEmpty(infractorDni) ||
                    TextUtils.isEmpty(dominio) || TextUtils.isEmpty(lugar) ||
                    TextUtils.isEmpty(propietario) || TextUtils.isEmpty(modeloVehiculo) ||
                    TextUtils.isEmpty(departamento) || TextUtils.isEmpty(municipio)) {
                showLoading(false);
                btnInsertarConductor.setEnabled(true);
                mostrarError("Por favor, complete todos los campos obligatorios");
                return;
            }
            // Manejar caso de "Otra" marca
            if (marcaVehiculo.equals("Otra")) {
                marcaVehiculo = etOtraMarca.getText().toString().trim();
                if (marcaVehiculo.isEmpty()) {
                    showLoading(false);
                    btnInsertarConductor.setEnabled(true);
                    mostrarError("Por favor, ingrese la marca del vehículo");
                    return;
                }
            }

            // Guardar datos para uso en callbacks
            final List<String> imagenesPendientes = new ArrayList<>(imagenBase64List);
            final boolean tieneEquipo = !TextUtils.isEmpty(etEquipo.getText().toString().trim());

            // Llamada a la API
            Call<RespuestaInsertarConductor> call = apiService.insertarConductor(
                    "insertarConductor",
                    numero, fecha, hora, dominio, lugar,
                    infraccionesJson.toString(),
                    infractorDni, infractorNombre, infractorDomicilio, infractorLocalidad,
                    infractorCp, infractorProvincia, infractorPais, infractorLicencia,
                    tipoVehiculo,
                    inspectorId,
                    marcaVehiculo,
                    propietario,
                    modeloVehiculo,
                    departamento,
                    municipio,
                    observaciones
            );

            call.enqueue(new Callback<RespuestaInsertarConductor>() {
                @Override
                public void onResponse(Call<RespuestaInsertarConductor> call, Response<RespuestaInsertarConductor> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        RespuestaInsertarConductor respuesta = response.body();
                        if (respuesta.getMessage() != null) {
                            String actaId = respuesta.getActaId();
                            if (actaId != null && !actaId.isEmpty()) {
                                setActaIdActual(actaId);

                                // Contador de operaciones pendientes
                                AtomicInteger operacionesPendientes = new AtomicInteger(0);

                                // Contamos las operaciones que necesitamos realizar
                                if (!imagenBase64List.isEmpty()) {
                                    operacionesPendientes.incrementAndGet();
                                }
                                // Verificamos si hay datos de equipo de medición
                                if (!etEquipo.getText().toString().trim().isEmpty()) {
                                    operacionesPendientes.incrementAndGet();
                                }

                                // Si no hay operaciones adicionales, procedemos a imprimir
                                if (operacionesPendientes.get() == 0) {
                                    finalizarProcesoEImprimir(true);
                                    return;
                                }
                                // Si hay imágenes, las subimos
                                if (!imagenBase64List.isEmpty()) {
                                    Call<RespuestaSubirImagen> callImagen = apiService.subirImagen(
                                            "subirImagen", actaId, imagenBase64List);
                                    callImagen.enqueue(new Callback<RespuestaSubirImagen>() {
                                        @Override
                                        public void onResponse(Call<RespuestaSubirImagen> call,
                                                               Response<RespuestaSubirImagen> response) {
                                            if (operacionesPendientes.decrementAndGet() == 0) {
                                                finalizarProcesoEImprimir(true);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<RespuestaSubirImagen> call, Throwable t) {
                                            mostrarError("Error al subir imágenes: " + t.getMessage());
                                            if (operacionesPendientes.decrementAndGet() == 0) {
                                                finalizarProcesoEImprimir(false);
                                            }
                                        }
                                    });
                                }

                                // Si hay datos de equipo, los insertamos
                                if (!etEquipo.getText().toString().trim().isEmpty()) {
                                    insertarEquipoMedicion(actaId, new Callback<RespuestaInsertarEquipoMedicion>() {
                                        @Override
                                        public void onResponse(Call<RespuestaInsertarEquipoMedicion> call,
                                                               Response<RespuestaInsertarEquipoMedicion> response) {
                                            if (operacionesPendientes.decrementAndGet() == 0) {
                                                finalizarProcesoEImprimir(true);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<RespuestaInsertarEquipoMedicion> call, Throwable t) {
                                            mostrarError("Error al guardar equipo: " + t.getMessage());
                                            if (operacionesPendientes.decrementAndGet() == 0) {
                                                finalizarProcesoEImprimir(false);
                                            }
                                        }
                                    });
                                }
                            } else {
                                finalizarProcesoEImprimir(false);
                                Log.e("DEBUG", "No se pudo obtener el ID del acta insertada");
                            }
                        } else if (respuesta.getError() != null) {
                            finalizarProcesoEImprimir(false);
                            mostrarError("Error: " + respuesta.getError());
                        }
                    } else {
                        finalizarProcesoEImprimir(false);
                        try {
                            String errorBody = response.errorBody() != null ?
                                    response.errorBody().string() : "Error desconocido";
                            Log.e("DEBUG", "Error en la respuesta del servidor: " +
                                    response.code() + ", Body: " + errorBody);
                            mostrarError("Error en la respuesta del servidor: " + response.code());
                        } catch (IOException e) {
                            Log.e("DEBUG", "Error al leer el cuerpo de la respuesta", e);
                        }
                    }
                }
                @Override
                public void onFailure(Call<RespuestaInsertarConductor> call, Throwable t) {
                    finalizarProcesoEImprimir(false);
                    mostrarError("Error de conexión: " + t.getMessage());
                    Log.e("DEBUG", "Error de conexión: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            finalizarProcesoEImprimir(false);
            Log.e("DEBUG", "Excepción en insertarDatosConductor: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error inesperado: " + e.getMessage());
        }
    }


    // Modifica el método finalizarProcesoEImprimir
    private void finalizarProcesoEImprimir(boolean exito) {
        runOnUiThread(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.GONE);
                btnInsertarConductor.setEnabled(true);
            }
            if (exito) {
                mostrarMensaje("Datos guardados exitosamente");

                // Verificar si la firma está cargada antes de imprimir
                if (!firmaEstaCargada) {
                    // Mostrar diálogo de carga
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Cargando firma");
                    builder.setMessage("Por favor espere mientras se carga la firma...");
                    builder.setCancelable(false);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    // Intentar cargar la firma
                    precargarFirma();

                    // Verificar periódicamente si la firma está lista
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (firmaEstaCargada) {
                                dialog.dismiss();
                                if (verificarBluetoothYConectar()) {
                                    imprimirMulta();
                                    limpiarCampos();
                                }
                            } else {
                                // Intentar nuevamente en 500ms
                                new Handler().postDelayed(this, 500);
                            }
                        }
                    }, 500);
                } else {
                    // La firma ya está cargada, imprimir directamente
                    if (verificarBluetoothYConectar()) {
                        imprimirMulta();
                        limpiarCampos();
                    }
                }
            } else {
                mostrarError("Ocurrió un error al guardar los datos. Puede intentar nuevamente.");
            }
        });
    }
    // Método auxiliar para verificar Bluetooth
    private boolean verificarBluetoothYConectar() {
        if (bluetoothAdapter == null) {
            mostrarError("Bluetooth no disponible en este dispositivo");
            return false;
        }

        if (!bluetoothAdapter.isEnabled()) {
            mostrarError("Por favor, active el Bluetooth para imprimir");
            return false;
        }

        try {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.isEmpty()) {
                mostrarError("No se encontraron dispositivos emparejados");
                return false;
            }

            for (BluetoothDevice device : pairedDevices) {
                try {
                    bluetoothDevice = device;
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    bluetoothSocket.connect();
                    mostrarMensaje("Conectado a: " + device.getName());
                    return true;
                } catch (IOException e) {
                    Log.e("Bluetooth", "Error al conectar con " + device.getName() + ": " + e.getMessage());
                    continue;
                }
            }

            mostrarError("No se pudo conectar a ninguna impresora");
            return false;

        } catch (SecurityException e) {
            mostrarError("Error de seguridad al acceder al Bluetooth: " + e.getMessage());
            return false;
        }
    }



    // Métodos auxiliares para manejo de errores
    private void handleErrorResponse(Response<?> response, String prefix) throws IOException {
        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
        String message = String.format("%s: %d, %s", prefix, response.code(), errorBody);
        Log.e("DEBUG", message);
        mostrarError(message);
    }

    private void handleError(String prefix, Throwable t) {
        String message = t != null ? String.format("%s: %s", prefix, t.getMessage()) : prefix;
        Log.e("DEBUG", message, t);
        mostrarError(message);
    }
    private void handleErrorMessage(String prefix, String errorMessage) {
        String message = String.format("%s: %s", prefix, errorMessage);
        Log.e("DEBUG", message);
        mostrarError(message);
    }


    private void handleException(String operation, Exception e) {
        String message = String.format("Error en %s: %s", operation, e.getMessage());
        Log.e("DEBUG", message, e);
        mostrarError(message);
    }


    private void subirImagenes(String actaId, AtomicInteger operacionesPendientes) {
        Call<RespuestaSubirImagen> callImagen = apiService.subirImagen("subirImagen", actaId, imagenBase64List);
        callImagen.enqueue(new Callback<RespuestaSubirImagen>() {
            @Override
            public void onResponse(Call<RespuestaSubirImagen> call, Response<RespuestaSubirImagen> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("DEBUG", "Imágenes subidas exitosamente");
                        if (operacionesPendientes.decrementAndGet() == 0) {
                            finalizarProceso(true);
                        }
                    } else {
                        handleErrorResponse(response, "Error al subir imágenes");
                        if (operacionesPendientes.decrementAndGet() == 0) {
                            finalizarProceso(false);
                        }
                    }
                } catch (IOException e) {
                    handleException("subir imágenes", e);
                    if (operacionesPendientes.decrementAndGet() == 0) {
                        finalizarProceso(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<RespuestaSubirImagen> call, Throwable t) {
                handleError("Error al subir imágenes", t);
                if (operacionesPendientes.decrementAndGet() == 0) {
                    finalizarProceso(false);
                }
            }
        });
    }

    // Método auxiliar para finalizar el proceso
    private void finalizarProceso(boolean exito) {
        runOnUiThread(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.GONE);
                btnInsertarConductor.setEnabled(true);
            }
            if (exito) {
                mostrarMensaje("Datos guardados exitosamente");
                limpiarCampos();
            }
        });
    }

    private void mostrarMensaje(final String mensaje) {
        try {
            runOnUiThread(() -> {
                try {
                    if (!isFinishing()) {
                        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e("DEBUG", "Error al mostrar Toast: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            Log.e("DEBUG", "Error crítico al mostrar mensaje: " + e.getMessage(), e);
        }
    }

    private void mostrarError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e("DEBUG", error);
    }
    private void limpiarCampos() {
        runOnUiThread(() -> {
            // Limpiar imágenes
            imagenBase64List.clear();
            for (ImageView view : photoPreviewViews) {
                view.setImageBitmap(null);
                view.setVisibility(View.GONE);
            }
            currentPhotoIndex = 0;
            actualizarBotonTomarFoto();

            // Limpiar campos del conductor
            if (etApellidoNombre != null) etApellidoNombre.setText("");
            if (etNumeroDocumento != null) etNumeroDocumento.setText("");
            if (etDomicilio != null) etDomicilio.setText("");
            if (actvLocalidad != null) actvLocalidad.setText("");
            if (etCodPostal != null) etCodPostal.setText("");
            if (actvDepartamento != null) actvDepartamento.setText("");
            if (etProvincia != null) etProvincia.setText("");
            if (etPais != null) etPais.setText("");
            if (etLicencia != null) etLicencia.setText("");
            if (etClase != null) etClase.setText("");
            if (etVencimiento != null) etVencimiento.setText("");

            // Limpiar campos del vehículo
            if (etDominio != null) etDominio.setText("");
            if (etOtraMarca != null) etOtraMarca.setText("");
            if (etModeloVehiculo != null) etModeloVehiculo.setText("");
            if (etPropietario != null) etPropietario.setText("");

            // Limpiar campos del hecho
            if (etLugar != null) etLugar.setText("");
            if (etDepartamentoMulta != null) etDepartamentoMulta.setText("Capital");
            if (etMunicipioMulta != null) etMunicipioMulta.setText("Posadas");
            if (etMultaInfo != null) etMultaInfo.setText("");

            // Limpiar equipo de medición
            limpiarCamposEquipoMedicion();

            // Restablecer spinners
            if (spinnerTipoDocumento != null) spinnerTipoDocumento.setSelection(0);
            if (spinnerMarca != null) spinnerMarca.setSelection(0);
            if (spinnerTipoVehiculo != null) spinnerTipoVehiculo.setSelection(0);
            if (spinnerInfraccion != null) spinnerInfraccion.setSelection(0);
            if (spinnerExpedidaPor != null) spinnerExpedidaPor.setSelection(0);

            // Limpiar infracciones seleccionadas
            infraccionesSeleccionadas.clear();
            if (chipGroupInfracciones != null) {
                chipGroupInfracciones.removeAllViews();
            }

            // Reset del número de acta
            actaIdActual = null;
            if (tvNumero != null) tvNumero.setText("");
        });
    }
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
    private void insertarEquipoMedicion(String actaId, Callback<RespuestaInsertarEquipoMedicion> callback) {
        try {
            Log.d("DEBUG", "Iniciando insertarEquipoMedicion con Acta ID: " + actaId);

            String tipo = spinnerTipoEquipo.getSelectedItem().toString();
            String equipo = etEquipo.getText().toString().trim();
            String marca = etMarcaCinemometro.getText().toString().trim();
            String modelo = etModeloCinemometro.getText().toString().trim();
            String numeroSerie = etSerieCinemometro.getText().toString().trim();
            String codigoAprobacion = etCodAprobacionCinemometro.getText().toString().trim();
            String valorMedido = etValorCinemometro.getText().toString().trim();

            Log.d("DEBUG", "Datos del equipo de medición: " +
                    "\nActa ID: " + actaId +
                    "\nTipo: " + tipo +
                    "\nEquipo: " + equipo +
                    "\nMarca: " + marca +
                    "\nModelo: " + modelo +
                    "\nNúmero de Serie: " + numeroSerie +
                    "\nCódigo de Aprobación: " + codigoAprobacion +
                    "\nValor Medido: " + valorMedido);

            // Validación mejorada
            if (tipo.equals("Seleccione un tipo") || equipo.isEmpty() || marca.isEmpty() || modelo.isEmpty() ||
                    numeroSerie.isEmpty() || codigoAprobacion.isEmpty() || valorMedido.isEmpty()) {
                mostrarError("Por favor, complete todos los campos del equipo de medición");
                if (callback != null) {
                    callback.onFailure(null, new Exception("Campos incompletos"));
                }
                return;
            }

            Call<RespuestaInsertarEquipoMedicion> call = apiService.insertarEquipoMedicion(
                    "insertarEquipoMedicion",
                    actaId, tipo, equipo, marca, modelo,
                    numeroSerie, codigoAprobacion, valorMedido
            );

            call.enqueue(new Callback<RespuestaInsertarEquipoMedicion>() {
                @Override
                public void onResponse(Call<RespuestaInsertarEquipoMedicion> call, Response<RespuestaInsertarEquipoMedicion> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getMessage() != null) {
                            Log.d("DEBUG", "Equipo de medición insertado: " + response.body().getMessage());
                           // limpiarCamposEquipoMedicion();
                            if (callback != null) {
                                callback.onResponse(call, response);
                            }
                        } else if (response.body().getError() != null) {
                            String error = "Error al insertar equipo de medición: " + response.body().getError();
                            Log.e("DEBUG", error);
                            mostrarError(error);
                            if (callback != null) {
                                callback.onFailure(call, new Exception(error));
                            }
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                            Log.e("DEBUG", "Error en la respuesta del servidor al insertar equipo de medición: " + errorBody);
                            Log.e("DEBUG", "Código de respuesta: " + response.code());
                            mostrarError("Error en la respuesta del servidor al insertar equipo de medición");
                            if (callback != null) {
                                callback.onFailure(call, new Exception(errorBody));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("DEBUG", "Error al leer el cuerpo del error: " + e.getMessage());
                            if (callback != null) {
                                callback.onFailure(call, e);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<RespuestaInsertarEquipoMedicion> call, Throwable t) {
                    String error = "Error de conexión al insertar equipo de medición: " + t.getMessage();
                    Log.e("DEBUG", error);
                    mostrarError(error);
                    t.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(call, t);
                    }
                }
            });

        } catch (Exception e) {
            Log.e("DEBUG", "Excepción en insertarEquipoMedicion: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error inesperado: " + e.getMessage());
            if (callback != null) {
                callback.onFailure(null, e);
            }
        }
    }

    // Asegurémonos de que el método de limpieza también haga los campos editables
    private void limpiarCamposEquipoMedicion() {
        runOnUiThread(() -> {
            if (spinnerTipoEquipo != null) spinnerTipoEquipo.setSelection(0);
            if (etEquipo != null) {
                etEquipo.setText("");
                etEquipo.setEnabled(true);
            }
            if (etMarcaCinemometro != null) {
                etMarcaCinemometro.setText("");
                etMarcaCinemometro.setEnabled(true);
            }
            if (etModeloCinemometro != null) {
                etModeloCinemometro.setText("");
                etModeloCinemometro.setEnabled(true);
            }
            if (etSerieCinemometro != null) {
                etSerieCinemometro.setText("");
                etSerieCinemometro.setEnabled(true);
            }
            if (etCodAprobacionCinemometro != null) {
                etCodAprobacionCinemometro.setText("");
                etCodAprobacionCinemometro.setEnabled(true);
            }
            if (etValorCinemometro != null) {
                etValorCinemometro.setText("");
                etValorCinemometro.setEnabled(true);
            }
        });
    }
    private void cargarTiposVehiculo() {
        Call<RespuestaTiposVehiculo> call = apiService.obtenerTiposVehiculo("obtenerTiposVehiculo");
        call.enqueue(new Callback<RespuestaTiposVehiculo>() {
            @Override
            public void onResponse(Call<RespuestaTiposVehiculo> call, Response<RespuestaTiposVehiculo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RespuestaTiposVehiculo.TipoVehiculo> tiposVehiculo = response.body().getTiposVehiculo();
                    // Aquí manejaremos la actualización del Spinner
                    actualizarSpinnerTiposVehiculo(tiposVehiculo);
                } else {
                    // Manejar error
                    Log.e("API", "Error al obtener tipos de vehículo: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RespuestaTiposVehiculo> call, Throwable t) {
                // Manejar error de red
                Log.e("API", "Error de red al obtener tipos de vehículo", t);
            }
        });
    }
    private void actualizarSugerenciasLocalidad(List<RespuestaInfoLocalidad.InfoLocalidad> infoLocalidades) {
        Log.d("MainActivity", "Actualizando sugerencias con " + infoLocalidades.size() + " localidades");
        runOnUiThread(() -> {
            if (!infoLocalidades.isEmpty()) {
                RespuestaInfoLocalidad.InfoLocalidad info = infoLocalidades.get(0);
                etCodPostal.setText(info.getCodigoPostal());
                actvDepartamento.setText(info.getDepartamento());
                etProvincia.setText(info.getProvincia());
                etPais.setText(info.getPais());
                Log.d("MainActivity", "Campos actualizados con la información de: " + info.getLocalidad());
            } else {
                Log.d("MainActivity", "No se encontraron localidades para actualizar");
            }
        });
    }

    private void actualizarSpinnerTiposVehiculo(List<RespuestaTiposVehiculo.TipoVehiculo> tiposVehiculo) {
        List<String> descripcionesTiposVehiculo = new ArrayList<>();
        for (RespuestaTiposVehiculo.TipoVehiculo tipo : tiposVehiculo) {
            descripcionesTiposVehiculo.add(tipo.getDescripcion());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, descripcionesTiposVehiculo);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoVehiculo.setAdapter(adapter);
    }

    private void cargarDepartamentos(String provincia) {
        Call<RespuestaDepartamentos> call = apiService.obtenerDepartamentos("obtenerDepartamentos", provincia);
        //Call<RespuestaDepartamentos> call = apiService.obtenerDepartamentos("obtener_departamentos", provincia);
        call.enqueue(new Callback<RespuestaDepartamentos>() {
            @Override
            public void onResponse(Call<RespuestaDepartamentos> call, Response<RespuestaDepartamentos> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<RespuestaDepartamentos.Departamento> departamentos = response.body().getDepartamentos();
                    List<String> nombresDepartamentos = new ArrayList<>();
                    for (RespuestaDepartamentos.Departamento departamento : departamentos) {
                        nombresDepartamentos.add(departamento.getD_descrip());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_dropdown_item_1line, nombresDepartamentos);
                    actvDepartamento.setAdapter(adapter);
                } else {
                    Toast.makeText(MainActivity.this, "Error al cargar departamentos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaDepartamentos> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarLocalidades(String provincia, String departamento) {
        Call<RespuestaLocalidades> call = apiService.obtenerLocalidades("obtenerLocalidades", provincia, departamento);
        // Call<RespuestaLocalidades> call = apiService.obtenerLocalidades("obtener_localidades", provincia, departamento);
        call.enqueue(new Callback<RespuestaLocalidades>() {
            @Override
            public void onResponse(Call<RespuestaLocalidades> call, Response<RespuestaLocalidades> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<RespuestaLocalidades.Localidad> localidades = response.body().getLocalidades();
                    List<String> nombresLocalidades = new ArrayList<>();
                    for (RespuestaLocalidades.Localidad localidad : localidades) {
                        nombresLocalidades.add(localidad.getD_descrip());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_dropdown_item_1line, nombresLocalidades);
                    actvLocalidad.setAdapter(adapter);
                } else {
                    Toast.makeText(MainActivity.this, "Error al cargar localidades", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaLocalidades> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean allPermissionsGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            switch (requestCode) {
                case REQUEST_CAMERA_PERMISSION:
                    dispatchTakePictureIntent();
                    break;
                case REQUEST_GALLERY_PERMISSION:
                    abrirGaleria();
                    break;
            }
        } else {
            // Si los permisos fueron denegados
            boolean shouldShowRationale = false;
            for (String permission : permissions) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    shouldShowRationale = true;
                    break;
                }
            }

            if (shouldShowRationale) {
                mostrarDialogoExplicativo();
            } else {
                // El usuario seleccionó "No volver a preguntar"
                new AlertDialog.Builder(this)
                        .setTitle("Permisos necesarios")
                        .setMessage("Esta aplicación necesita permisos para funcionar correctamente. Por favor, habilítalos en la configuración de la aplicación.")
                        .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        }
    }
    // Modifica el método existente para usar la nueva verificación de permisos
    private void mostrarOpcionesFoto() {
        if (currentPhotoIndex >= 2) {
            Toast.makeText(this, "Ya has tomado el máximo de 2 fotos", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar imagen")
                .setItems(new CharSequence[]{"Tomar foto", "Seleccionar de galería"}, (dialog, which) -> {
                    if (which == 0) {
                        // Verificar permisos para la cámara
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // Android 13 y superior
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                                    PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.CAMERA},
                                        REQUEST_CAMERA_PERMISSION);
                            } else {
                                dispatchTakePictureIntent();
                            }
                        } else {
                            // Android 12 y anterior
                            checkAndRequestPermissions();
                        }
                    } else {
                        // Verificar permisos para la galería
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                                        REQUEST_GALLERY_PERMISSION);
                            } else {
                                abrirGaleria();
                            }
                        } else {
                            if (ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        REQUEST_GALLERY_PERMISSION);
                            } else {
                                abrirGaleria();
                            }
                        }
                    }
                });
        builder.show();
    }
    private void mostrarDialogoExplicativo() {
        new AlertDialog.Builder(this)
                .setTitle("Permisos necesarios")
                .setMessage("Esta aplicación necesita acceso a la cámara y al almacenamiento para tomar y guardar fotos. Por favor, conceda estos permisos para continuar.")
                .setPositiveButton("Solicitar permisos", (dialog, which) -> {
                    requestPermissions(new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, REQUEST_CAMERA_PERMISSION);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


    private void mostrarDialogoOpciones() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar imagen")
                .setItems(new CharSequence[]{"Tomar foto", "Seleccionar de galería"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            dispatchTakePictureIntent();
                        } else {
                            abrirGaleria();
                        }
                    }
                });
        builder.show();
    }


}