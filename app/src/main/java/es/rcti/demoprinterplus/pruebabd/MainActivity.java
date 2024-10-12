package es.rcti.demoprinterplus.pruebabd;
import android.Manifest;
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
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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
    private String imagenBase64Temporal;

    private String actaIdActual;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentPhotoPath;

    private static final int REQUEST_IMAGE_PICK = 2;
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

    private OracleApiService apiService;

    private Button btnBuscarLocalidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        progressBar = findViewById(R.id.progressBar);
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            Log.e("UNCAUGHT", "Excepción no capturada: " + e.getMessage(), e);
            // Aquí puedes implementar lógica adicional, como enviar el error a un servicio de reporte de errores
        });
        setContentView(R.layout.activity_main);

        initializeViews();
        setupSpinners();
        setupListeners();

        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnTomarFoto.setOnClickListener(v -> mostrarOpcionesFoto());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        apiService = ApiClient.getClient().create(OracleApiService.class);
        spinnerTipoVehiculo = findViewById(R.id.spinnerTipoVehiculo);
        actvLocalidad = findViewById(R.id.actvLocalidad);
        ivPhotoPreview = findViewById(R.id.ivPhotoPreview);
        etCodPostal = findViewById(R.id.etCodPostal);
        actvDepartamento = findViewById(R.id.actvDepartamento);
        etProvincia = findViewById(R.id.etProvincia);
        etPais = findViewById(R.id.etPais);

        etLugar = findViewById(R.id.etLugar);
        etDepartamentoMulta = findViewById(R.id.etDepartamentoMulta);
        etMunicipioMulta = findViewById(R.id.etMunicipioMulta);

        btnBuscarLocalidad = findViewById(R.id.btnBuscarLocalidad);
        btnBuscarLocalidad.setOnClickListener(v -> {
            Log.d("MainActivity", "Botón de búsqueda presionado");
            String localidad = actvLocalidad.getText().toString().trim();
            if (!localidad.isEmpty()) {
                Log.d("MainActivity", "Iniciando búsqueda para localidad: " + localidad);
                buscarInfoLocalidad(localidad);
            } else {
                Log.d("MainActivity", "Campo de localidad vacío");
                Toast.makeText(this, "Por favor, ingrese una localidad", Toast.LENGTH_SHORT).show();
            }
        });

        // Llamar al método para cargar los tipos de vehículo
        cargarTiposVehiculo();
        setPosadasAsDefault();
    }
    private boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1) {
                    buscarInfoLocalidad(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void dispatchTakePictureIntent() {
        Log.d("CameraDebug", "Iniciando dispatchTakePictureIntent simplificado");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            Log.d("CameraDebug", "Actividad de cámara iniciada");
        } catch (ActivityNotFoundException e) {
            Log.e("CameraDebug", "No se encontró una aplicación de cámara", e);
            mostrarMensaje("No se encontró una aplicación de cámara");
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



    private void initializeViews() {
        spinnerTipoEquipo = findViewById(R.id.spinnerTipoEquipo);
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

        spinnerTipoDocumento = findViewById(R.id.spinnerTipoDocumento);
        spinnerMarca = findViewById(R.id.spinnerMarca);
        spinnerTipoVehiculo = findViewById(R.id.spinnerTipoVehiculo);
        spinnerInfraccion = findViewById(R.id.spinnerInfraccion);
        spinnerExpedidaPor = findViewById(R.id.spinnerExpedidaPor);

        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnConectarImprimir = findViewById(R.id.btnConectarImprimir);

        tvEstado = findViewById(R.id.tvEstado);
        tvConductor = findViewById(R.id.tvConductor);
        tvVehiculo = findViewById(R.id.tvVehiculo);
        tvHecho = findViewById(R.id.tvHecho);
        tvNumero = findViewById(R.id.tvNumero);
        tvEspecificaciones = findViewById(R.id.tvEspecificaciones);

        layoutConductor = findViewById(R.id.layoutConductor);
        layoutVehiculo = findViewById(R.id.layoutVehiculo);
        layoutHecho = findViewById(R.id.layoutHecho);
        layoutEspecificaciones = findViewById(R.id.layoutEspecificaciones);

        ivPhotoPreview = findViewById(R.id.ivPhotoPreview);

        btnInsertarConductor = findViewById(R.id.btnInsertarConductor);
    }

    private void setupSpinners() {
        setupTipoDocumentoSpinner();
        setupMarcaSpinner();
        setupTipoVehiculoSpinner();
        setupInfraccionSpinner();
        setupExpedidoPorSpinner();

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
        List<String> marcas = new ArrayList<>();
        marcas.add("Seleccione una marca");
        marcas.add("Toyota");
        marcas.add("Ford");
        marcas.add("Chevrolet");
        marcas.add("Volkswagen");
        marcas.add("Otra");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, marcas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMarca.setAdapter(adapter);
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
        List<String> infracciones = new ArrayList<>();
        infracciones.add("Seleccione una infracción");
        infracciones.add("Conducir en Estado de Ebriedad - Graduacion 0.10 a 0.50 gramos por litro de sangre");
        infracciones.add("Conducir en Estado de Ebriedad - Graduacion 0.51 a 1.00 gramos por litro de sangre");
        infracciones.add("Conducir en Estado de Ebriedad - Graduacion 1.01 a 1.50 gramos por litro de sangre");
        infracciones.add("Conducir en Estado de Ebriedad - Graduacion Superior a 1.50 gramos por litro de sangre");
        infracciones.add("Negarse a realizar el alcotest en ocasión de un operativo de control de tránsito");
        infracciones.add("No Completar el ciclo del test de alcoholemia por aire espirado luego de tres intentos");
        infracciones.add("Conducir bajo efectos de estupefacientes");
        infracciones.add("Negarse a realizar el test de consumo de estupefacientes en ocasión de un operativo de control de tránsito");
        infracciones.add("Conducir sin licencia y no estar habilitado para hacerlo");
        infracciones.add("Conducir sin licencia y no estar habilitado para ello por causa de minoridad");
        infracciones.add("Conducir con licencia adulterada en la que se evidencie violación a los requisitos exigidos por la normativa");
        infracciones.add("Conducir con licencia vencida");
        infracciones.add("Conducir con boleta de citación del inculpado vencida");
        infracciones.add("Conducir con licencia no correspondiente a la categoría del vehículo");
        infracciones.add("Girar a la Izquierda en Av. de Doble Mano, en lugares donde se encuentre expresamente prohibido");
        infracciones.add("Circular en sentido contrario al establecido");
        infracciones.add("Girar en U");
        infracciones.add("Participar u organizar, en la via pública, competencias o destrezas de velocidad en motos o vehículos");
        infracciones.add("No Respetar las Señales de los Semáforos");
        infracciones.add("No Respetar las Indicaciones de los agentes encargados del tránsito");
        infracciones.add("Estacionar sobre los delimitadores, rampas y en espacios exclusivos de estacionamiento");
        infracciones.add("No Detener la marcha ante el cartel indicador PARE");
        infracciones.add("No respetar controles rutinarios, vallas, conos de prevención");
        infracciones.add("Obstaculizar, dificultar o impedir, actos de inspección que ejecuten los agentes de tránsito");
        infracciones.add("Falta total o parcial de cualquiera de los dispositivos correspondientes a faros, luces reglamentarias");
        infracciones.add("Circular con luces antirreglamentarias");
        infracciones.add("Circular sin luces encendidas");
        infracciones.add("Circular con vehículos de transporte de carga sin condiciones de seguridad ni señalización");
        infracciones.add("Cruzar bocacalles a alta velocidad");
        infracciones.add("Darse a la Fuga");
        infracciones.add("Circular de manera contraria a las reglas de la buena conducción, realizando maniobras peligrosas");
        infracciones.add("Circular con cantidad de pasajeros que excedan la capacidad permitida para el vehículo objeto de la infracción");
        infracciones.add("Circular con menores de 10 años en el asiento delantero del vehículo");
        infracciones.add("Circular con menores de 4 años a bordo sin el correspondiente uso del sistema de retención infantil");
        infracciones.add("Circular con menores de 12 años a bordo de ciclomotores, motocicletas, motos, zootropos, y/o similares");
        infracciones.add("Circular marcha atrás en forma indebida contrarias a las reglas de la buena conducción");
        infracciones.add("Circular con motocicletas en lugares indebidos, veredas, ciclovías, u otro lugar expresamente prohibido");
        infracciones.add("Adulteración de las documentaciones exigibles para la circulación");
        infracciones.add("Negarse a exhibir la documentación del vehículo y/o licencia habilitante");
        infracciones.add("Circular con cédula de identificación del vehículo ilegible, deteriorada o en condiciones antirreglamentarias");
        infracciones.add("Circular sin cédula de identificación del vehículo");
        infracciones.add("Conducir sin la póliza de seguro obligatorio vigente, sea en formato físico o digital");
        infracciones.add("Circular sin la verificación técnica obligatoria vigente, o por encontrarse vencida");
        infracciones.add("Circular sin la verificación técnica obligatoria vigente a partir del término del plazo de gracia o vencida");
        infracciones.add("Circular sin chapa patente");
        infracciones.add("Circular con chapa patente con plaquetas adicionales y/o modificaciones, ilegibles o con partes borradas");
        infracciones.add("Conducir con permiso de circulación del RNPA, estando el mismo vencido");
        infracciones.add("Circular sin extintor de incendio o que el mismo esté con carga vencida");
        infracciones.add("Circular con vehículos que transportan explosivos y/o inflamables sin la debida autorización");
        infracciones.add("Circular transportando combustible de manera antirreglamentaria en recipientes o envases no autorizados");
        infracciones.add("Conducir con auriculares y/o dispositivos móviles, utilizados de forma manual");
        infracciones.add("Circular el conductor y/o los pasajeros que lo acompañen sin el cinturón de seguridad");
        infracciones.add("Estacionamiento o detención de vehículos en lugares reservados o de circulación exclusiva del servicio público");
        infracciones.add("Estacionar en ochavas u otros espacios reservados por razones de visibilidad y/o seguridad");
        infracciones.add("Estacionar en lugares prohibidos, sobre la vereda o entrada y salida de cocheras");
        infracciones.add("Estacionar dentro de la planta urbana vehículos pesados fuera del horario autorizado para tal fin");
        infracciones.add("Estacionar en lugares reservados para carga y descarga fuera de los horarios permitidos");
        infracciones.add("Estacionar o detenerse en doble fila");
        infracciones.add("Estacionar ciclomotores, motocicletas, motos, fuera del lugar reservado exclusivamente para estos");
        infracciones.add("Estacionar en sentido contrario a la circulación en calles y/o avenidas de la ciudad");
        infracciones.add("Estacionar o detenerse en vías multicarriles o avenidas, entorpeciendo la normal circulación");
        infracciones.add("No respetar la prioridad de paso de vehículos que se presentan sobre la derecha de bocacalles o cruces");
        infracciones.add("No ceder el paso a vehículos de bomberos, ambulancias, policías o de servicios públicos, en casos de urgencia");
        infracciones.add("Circular con vehículos de transporte de pasajeros o de carga sin habilitación extendida");
        infracciones.add("Ascender o descender pasajeros en bocacalles");
        infracciones.add("No conservar la derecha en encrucijadas, virajes, puentes, o alcantarillas");
        infracciones.add("Circular el conductor y/o acompañante sin casco reglamentario o utilizándolo de manera indebida");
        infracciones.add("Circular a velocidad inferior a la correspondiente a su carril");
        infracciones.add("Circular con vehículos con defensas delanteras y/o traseras, enganches sobresalientes, potentes peligrosos");
        infracciones.add("Circular con licencia de conducir de otra jurisdicción luego de 90 días");
        infracciones.add("Conducir sin licencia y acreditar la misma dentro del plazo dispuesto por el Juzgado");
        infracciones.add("Circular con caño de escape abierto y/o modificado, emisión de ruidos fuertes sobre límites permitidos");
        infracciones.add("No respetar las normas que regulan la circulación de peatones en sendas establecidas");
        infracciones.add("Colocación o uso de bocinas antirreglamentarias");
        infracciones.add("Interrumpir y/u obstruir innecesariamente las procesiones, desfiles cívico - militares o cortejos fúnebres");
        infracciones.add("Falta de luz en chapa patente");
        infracciones.add("Falta de limpiaparabrisas, parasol, paragolpe, balizas, cubierta neumática, apoyacabezas");
        infracciones.add("Falta de espejo retrovisor, parabrisas de cristal o vidrio inastillable");
        infracciones.add("Lavado de vehículos en playas, parques, paseos públicos, ríos, cuyas aguas desembocan en balnearios");
        infracciones.add("Toda infracción a ordenanzas o reglamentos y cuya sanción no está prevista en la ordenanza de penalidades");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, infracciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInfraccion.setAdapter(adapter);
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

    private void setupListeners() {

        etProvincia.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                cargarDepartamentos(etProvincia.getText().toString());
            }
        });

        actvDepartamento.setOnItemClickListener((parent, view, position, id) -> {
            String departamentoSeleccionado = (String) parent.getItemAtPosition(position);
            cargarLocalidades(etProvincia.getText().toString(), departamentoSeleccionado);
        });
        tvConductor.setOnClickListener(v -> toggleVisibility(layoutConductor));
        tvVehiculo.setOnClickListener(v -> toggleVisibility(layoutVehiculo));
        tvHecho.setOnClickListener(v -> toggleVisibility(layoutHecho));
        tvEspecificaciones.setOnClickListener(v -> toggleVisibility(layoutEspecificaciones));

        btnTomarFoto.setOnClickListener(v -> mostrarOpcionesFoto());
        btnConectarImprimir.setOnClickListener(v -> checkBluetoothPermissions());
        btnInsertarConductor.setOnClickListener(v -> insertarDatosConductor());


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

                if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
                    Log.d("CameraDebug", "Ruta de la foto: " + currentPhotoPath);
                    File imagenFile = new File(currentPhotoPath);
                    if (imagenFile.exists()) {
                        Log.d("CameraDebug", "Archivo de imagen existe");
                        procesarImagen(imagenFile);
                    } else {
                        Log.e("CameraDebug", "El archivo de imagen no existe en la ruta especificada");
                        mostrarError("El archivo de imagen no se encontró");
                    }
                } else {
                    Log.d("CameraDebug", "currentPhotoPath es null o vacío, intentando obtener imagen del Intent");
                    Bundle extras = data.getExtras();
                    if (extras != null && extras.containsKey("data")) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            Log.d("CameraDebug", "Imagen obtenida del Intent");
                            procesarBitmap(imageBitmap);
                        } else {
                            Log.e("CameraDebug", "No se pudo obtener la imagen del Intent");
                            mostrarError("No se pudo obtener la imagen");
                        }
                    } else {
                        Log.e("CameraDebug", "No se encontró la imagen en el Intent");
                        mostrarError("No se encontró la imagen");
                    }
                }
            }
        } else {
            Log.d("CameraDebug", "Resultado no OK o requestCode no reconocido");
            mostrarError("No se pudo capturar la imagen");
        }
    }


    private void procesarImagen(File imagenFile) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagenFile.getAbsolutePath());
            if (bitmap != null) {
                ivPhotoPreview.setImageBitmap(bitmap);
                imagenBase64Temporal = convertirImagenABase64(imagenFile);
                Log.d("DEBUG", "imagenBase64Temporal configurado. Longitud: " + (imagenBase64Temporal != null ? imagenBase64Temporal.length() : "null"));
                Log.d("CameraDebug", "Imagen procesada y convertida a base64");
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
            ivPhotoPreview.setImageBitmap(bitmap);
            imagenBase64Temporal = convertirBitmapABase64(bitmap);
            Log.d("CameraDebug", "Bitmap procesado y convertido a base64");
        } catch (Exception e) {
            Log.e("CameraDebug", "Error al procesar el bitmap", e);
            mostrarError("Error al procesar la imagen: " + e.getMessage());
        }
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

    private void subirImagen(String actaId, String imagenBase64) {
        Log.d("DEBUG", "Iniciando subirImagen. ActaId: " + actaId + ", longitud de imagen: " + imagenBase64.length());

        Call<RespuestaSubirImagen> call = apiService.subirImagen("subirImagen", actaId, imagenBase64);
        call.enqueue(new Callback<RespuestaSubirImagen>() {
            @Override
            public void onResponse(Call<RespuestaSubirImagen> call, Response<RespuestaSubirImagen> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RespuestaSubirImagen respuesta = response.body();
                    Log.d("DEBUG", "Respuesta de subirImagen: " + new Gson().toJson(respuesta));
                    if (respuesta.isSuccess()) {
                        Log.d("DEBUG", "Imagen subida exitosamente: " + respuesta.getImagenUrl());
                    } else {
                        Log.e("DEBUG", "Error al subir la imagen: " + respuesta.getError());
                    }
                } else {
                    Log.e("DEBUG", "Error en la respuesta del servidor: " + response.code());
                    try {
                        Log.e("DEBUG", "Cuerpo del error: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<RespuestaSubirImagen> call, Throwable t) {
                Log.e("DEBUG", "Error de conexión al subir imagen", t);
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
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
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
            imprimirCampoEnLinea(" ", "", lineWidth);

            Log.d("ImpresionMulta", "Imprimiendo número de boleta y fecha");
            imprimirCampoEnLinea("Numero de Boleta", tvNumero.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Fecha", String.format("%02d/%02d/%04d", dia, mes, anio), lineWidth);
            imprimirCampoEnLinea("Hora", String.format("%02d:%02d", hora, minuto), lineWidth);

            Log.d("ImpresionMulta", "Imprimiendo sección conductor");
            imprimirCampoEnLinea("CONDUCTOR", "", lineWidth);
            imprimirCampoEnLinea("Apellido y Nombre", etApellidoNombre.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Tipo Documento", spinnerTipoDocumento.getSelectedItem().toString(), lineWidth);
            imprimirCampoEnLinea("Numero Documento", etNumeroDocumento.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Domicilio", etDomicilio.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Localidad", actvLocalidad.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Codigo Postal", etCodPostal.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Departamento", actvDepartamento.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Provincia", etProvincia.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Pais", etPais.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Licencia", etLicencia.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Expedida por", spinnerExpedidaPor.getSelectedItem().toString(), lineWidth);
            imprimirCampoEnLinea("Clase", etClase.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Vencimiento", etVencimiento.getText().toString(), lineWidth);

            Log.d("ImpresionMulta", "Imprimiendo sección vehículo");
            imprimirCampoEnLinea("VEHICULO", "", lineWidth);
            imprimirCampoEnLinea("Dominio", etDominio.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Propietario", etPropietario.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Marca", spinnerMarca.getSelectedItem().toString(), lineWidth);
            if (spinnerMarca.getSelectedItem().toString().equals("Otra")) {
                imprimirCampoEnLinea("Otra Marca", etOtraMarca.getText().toString(), lineWidth);
            }
            imprimirCampoEnLinea("Modelo", etModeloVehiculo.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Tipo Vehiculo", spinnerTipoVehiculo.getSelectedItem().toString(), lineWidth);

            Log.d("ImpresionMulta", "Imprimiendo sección hecho");
            imprimirCampoEnLinea("HECHO", "", lineWidth);
            imprimirCampoEnLinea("Infraccion", spinnerInfraccion.getSelectedItem().toString(), lineWidth);
            imprimirCampoEnLinea("Lugar", etLugar.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Departamento Multa", etDepartamentoMulta.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Municipio Multa", etMunicipioMulta.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Info Multa", etMultaInfo.getText().toString(), lineWidth);

            Log.d("ImpresionMulta", "Imprimiendo sección especificaciones");
            imprimirCampoEnLinea("ESPECIFICACIONES", "", lineWidth);
            imprimirCampoEnLinea("Tipo Equipo", spinnerTipoEquipo.getSelectedItem().toString(), lineWidth);
            imprimirCampoEnLinea("Equipo", etEquipo.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Marca Cinemometro", etMarcaCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Modelo Cinemometro", etModeloCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Serie Cinemometro", etSerieCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Codigo Aprobacion", etCodAprobacionCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Valor Cinemometro", etValorCinemometro.getText().toString(), lineWidth);

            // Avanzar papel y cortar
            outputStream.write(new byte[]{0x0A, 0x0A, 0x0A, 0x0A});
            outputStream.write(new byte[]{0x1D, 0x56, 0x01}); // Corte parcial

            Log.d("ImpresionMulta", "Finalizando impresión");
            outputStream.flush();
            mostrarMensaje("Multa impresa con éxito");

        } catch (IOException e) {
            Log.e("ImpresionMulta", "Error al imprimir: " + e.getMessage(), e);
            mostrarMensaje("Error al imprimir: " + e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                Log.e("ImpresionMulta", "Error al cerrar OutputStream: " + e.getMessage(), e);
            }
        }
    }
    private void imprimirCampoEnLinea(String etiqueta, String valor, int lineWidth) {
        try {
            if (valor == null) {
                valor = "";
            }

            StringBuilder linea = new StringBuilder(etiqueta + ": " + valor);

            while (linea.length() > 0) {
                if (linea.length() <= lineWidth) {
                    outputStream.write((linea.toString() + "\n").getBytes());
                    break;
                } else {
                    String lineaParcial = linea.substring(0, lineWidth);
                    outputStream.write((lineaParcial + "\n").getBytes());
                    linea = new StringBuilder(linea.substring(lineWidth));
                }
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
        try {
            Log.d("DEBUG", "Iniciando insertarDatosConductor");

            // Obtención de valores
            String numero = generarNumeroAleatorio();
            String fecha = getCurrentDate();
            String hora = getCurrentTime();
            String dominio = etDominio != null ? etDominio.getText().toString() : "";
            String lugar = etLugar != null ? etLugar.getText().toString() : "";
            String infraccion = spinnerInfraccion != null ? spinnerInfraccion.getSelectedItem().toString() : "";
            String infractorDni = etNumeroDocumento != null ? etNumeroDocumento.getText().toString() : "";
            String infractorNombre = etApellidoNombre != null ? etApellidoNombre.getText().toString() : "";
            String infractorDomicilio = etDomicilio != null ? etDomicilio.getText().toString() : "";
            String infractorLocalidad = actvLocalidad != null ? actvLocalidad.getText().toString() : "";
            String infractorCp = etCodPostal != null ? etCodPostal.getText().toString() : "";
            String infractorProvincia = etProvincia != null ? etProvincia.getText().toString() : "";
            String infractorPais = etPais != null ? etPais.getText().toString() : "";
            String infractorLicencia = etLicencia != null ? etLicencia.getText().toString() : "";

            // Logging detallado de los valores obtenidos
            Log.d("DEBUG", "Valores obtenidos: " +
                    "\nnumero: " + numero +
                    "\nfecha: " + fecha +
                    "\nhora: " + hora +
                    "\ndominio: " + dominio +
                    "\nlugar: " + lugar +
                    "\ninfraccion: " + infraccion +
                    "\ninfractorDni: " + infractorDni +
                    "\ninfractorNombre: " + infractorNombre +
                    "\ninfractorDomicilio: " + infractorDomicilio +
                    "\ninfractorLocalidad: " + infractorLocalidad +
                    "\ninfractorCp: " + infractorCp +
                    "\ninfractorProvincia: " + infractorProvincia +
                    "\ninfractorPais: " + infractorPais +
                    "\ninfractorLicencia: " + infractorLicencia);

            // Validaciones expandidas
            if (infractorNombre.isEmpty()) {
                mostrarError("Por favor, complete el nombre del infractor");
                return;
            }
            if (infractorDni.isEmpty()) {
                mostrarError("Por favor, complete el número de documento del infractor");
                return;
            }
            if (dominio.isEmpty()) {
                mostrarError("Por favor, complete el dominio del vehículo");
                return;
            }
            if (lugar.isEmpty()) {
                mostrarError("Por favor, complete el lugar de la infracción");
                return;
            }
            if (infraccion.isEmpty() || infraccion.equals("Seleccione una infracción")) {
                mostrarError("Por favor, seleccione una infracción");
                return;
            }

            // Si todas las validaciones pasan, procedemos con la inserción
            Log.d("DEBUG", "Todas las validaciones pasaron, procediendo con la inserción");

            // Mostrar ProgressBar de forma segura
            runOnUiThread(() -> {
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });

            Call<RespuestaInsertarConductor> call = apiService.insertarConductor(
                    "insertarConductor",
                    numero, fecha, hora, dominio, lugar, infraccion,
                    infractorDni, infractorNombre, infractorDomicilio, infractorLocalidad,
                    infractorCp, infractorProvincia, infractorPais, infractorLicencia
            );

            call.enqueue(new Callback<RespuestaInsertarConductor>() {
                @Override
                public void onResponse(Call<RespuestaInsertarConductor> call, Response<RespuestaInsertarConductor> response) {
                    // Ocultar ProgressBar de forma segura
                    runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                    if (response.isSuccessful() && response.body() != null) {
                        RespuestaInsertarConductor respuesta = response.body();
                        if (respuesta.getMessage() != null) {
                            mostrarMensaje(respuesta.getMessage());
                            Log.d("DEBUG", "Respuesta exitosa: " + respuesta.getMessage());

                            // Obtener el ID del acta insertada
                            String actaId = respuesta.getActaId();
                            if (actaId != null && !actaId.isEmpty()) {
                                setActaIdActual(actaId); // Actualizar el ID del acta actual
                                Log.d("DEBUG", "ID del acta insertada: " + actaId);

                                // Subir la imagen si está disponible
                                if (imagenBase64Temporal != null && !imagenBase64Temporal.isEmpty()) {
                                    Log.d("DEBUG", "Llamando a subirImagen con actaId: " + actaId);
                                    subirImagen(actaId, imagenBase64Temporal);
                                } else {
                                    Log.d("DEBUG", "No hay imagen para subir");
                                    // Aquí podrías mostrar un mensaje al usuario si es necesario
                                    mostrarMensaje("No se ha capturado ninguna imagen para esta acta.");
                                }

                                // Insertar equipo de medición después de una inserción exitosa del acta
                                insertarEquipoMedicion(actaId);

                                limpiarCampos(); // Limpia los campos después de una inserción exitosa
                            } else {
                                Log.e("DEBUG", "No se pudo obtener el ID del acta insertada");
                                mostrarError("Error: No se pudo obtener el ID del acta");
                            }
                        } else if (respuesta.getError() != null) {
                            mostrarError("Error: " + respuesta.getError());
                            Log.e("DEBUG", "Error en la respuesta: " + respuesta.getError());
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                            Log.e("DEBUG", "Error en la respuesta del servidor: " + response.code() + ", Body: " + errorBody);
                            mostrarError("Error en la respuesta del servidor: " + response.code());
                        } catch (IOException e) {
                            Log.e("DEBUG", "Error al leer el cuerpo de la respuesta", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<RespuestaInsertarConductor> call, Throwable t) {
                    // Ocultar ProgressBar de forma segura
                    runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                    mostrarError("Error de conexión: " + t.getMessage());
                    Log.e("DEBUG", "Error de conexión: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e("DEBUG", "Excepción en insertarDatosConductor: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error inesperado: " + e.getMessage());
        }
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
        // Limpia todos los campos del formulario
        if (etApellidoNombre != null) etApellidoNombre.setText("");
        if (etNumeroDocumento != null) etNumeroDocumento.setText("");
        if (etDomicilio != null) etDomicilio.setText("");
        if (actvLocalidad != null) actvLocalidad.setText("");
        if (etCodPostal != null) etCodPostal.setText("");
        if (actvDepartamento != null) actvDepartamento.setText("");
        if (etProvincia != null) etProvincia.setText("");
        if (etPais != null) etPais.setText("");
        if (etLicencia != null) etLicencia.setText("");
        if (etDominio != null) etDominio.setText("");
        if (etLugar != null) etLugar.setText("");
        if (spinnerInfraccion != null) spinnerInfraccion.setSelection(0);
        setActaIdActual(null);

        // ... limpiar otros campos según sea necesario
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void insertarEquipoMedicion(String actaId) {
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
                return;
            }

            // Mostrar ProgressBar
            runOnUiThread(() -> {
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });

            Call<RespuestaInsertarEquipoMedicion> call = apiService.insertarEquipoMedicion(
                    "insertarEquipoMedicion",
                    actaId, tipo, equipo, marca, modelo,
                    numeroSerie, codigoAprobacion, valorMedido
            );

            call.enqueue(new Callback<RespuestaInsertarEquipoMedicion>() {
                @Override
                public void onResponse(Call<RespuestaInsertarEquipoMedicion> call, Response<RespuestaInsertarEquipoMedicion> response) {
                    // Ocultar ProgressBar
                    runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getMessage() != null) {
                            mostrarMensaje(response.body().getMessage());
                            Log.d("DEBUG", "Equipo de medición insertado: " + response.body().getMessage());
                            limpiarCamposEquipoMedicion();
                        } else if (response.body().getError() != null) {
                            mostrarError("Error al insertar equipo de medición: " + response.body().getError());
                            Log.e("DEBUG", "Error al insertar equipo de medición: " + response.body().getError());
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                            mostrarError("Error en la respuesta del servidor al insertar equipo de medición: " + errorBody);
                            Log.e("DEBUG", "Error en la respuesta del servidor al insertar equipo de medición: " + errorBody);
                            Log.e("DEBUG", "Código de respuesta: " + response.code());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("DEBUG", "Error al leer el cuerpo del error: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<RespuestaInsertarEquipoMedicion> call, Throwable t) {
                    // Ocultar ProgressBar
                    runOnUiThread(() -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                    mostrarError("Error de conexión al insertar equipo de medición: " + t.getMessage());
                    Log.e("DEBUG", "Error de conexión al insertar equipo de medición: " + t.getMessage());
                    t.printStackTrace();
                }
            });

        } catch (Exception e) {
            Log.e("DEBUG", "Excepción en insertarEquipoMedicion: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error inesperado: " + e.getMessage());
        }
    }


    private void limpiarCamposEquipoMedicion() {
        runOnUiThread(() -> {
            if (spinnerTipoEquipo != null) spinnerTipoEquipo.setSelection(0);
            if (etEquipo != null) etEquipo.setText("");
            if (etMarcaCinemometro != null) etMarcaCinemometro.setText("");
            if (etModeloCinemometro != null) etModeloCinemometro.setText("");
            if (etSerieCinemometro != null) etSerieCinemometro.setText("");
            if (etCodAprobacionCinemometro != null) etCodAprobacionCinemometro.setText("");
            if (etValorCinemometro != null) etValorCinemometro.setText("");
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
        Log.d("MainActivity", "Actualizando Spinner con " + tiposVehiculo.size() + " tipos de vehículo");
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean todosPermisosOtorgados = true;
            for (int resultado : grantResults) {
                if (resultado != PackageManager.PERMISSION_GRANTED) {
                    todosPermisosOtorgados = false;
                    break;
                }
            }
            if (todosPermisosOtorgados) {
                dispatchTakePictureIntent();
            } else {
                mostrarDialogoExplicativoPermisos();
            }
        }
    }

    // Modifica el método existente para usar la nueva verificación de permisos
    private void mostrarOpcionesFoto() {
        verificarYSolicitarPermisos();
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