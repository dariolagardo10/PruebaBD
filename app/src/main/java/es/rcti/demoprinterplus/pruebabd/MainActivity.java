package es.rcti.demoprinterplus.pruebabd;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    // Declaración de vistas
    private EditText etApellidoNombre, etDomicilio, etLocalidad, etCodPostal, etDepartamento,
            etProvincia, etPais, etLicencia, etMultaInfo,
            etNumeroDocumento, etDominio, etOtraMarca, etModeloVehiculo, etPropietario,
            etLugar, etDepartamentoMulta, etMunicipioMulta;
    private Spinner spinnerTipoDocumento, spinnerMarca, spinnerTipoVehiculo, spinnerInfraccion, spinnerExpedidaPor;
    private Button btnTomarFoto, btnConectarImprimir, btnInsertarConductor;
    private AutoCompleteTextView actvDepartamento, actvLocalidad;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        progressBar = findViewById(R.id.progressBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupSpinners();
        setupListeners();


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        apiService = ApiClient.getClient().create(OracleApiService.class);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposVehiculo);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoVehiculo.setAdapter(adapter);
    }

    private void setupInfraccionSpinner() {
        List<String> infracciones = new ArrayList<>();
        infracciones.add("Seleccione una infracción");
        infracciones.add("Exceso de velocidad");
        infracciones.add("Estacionamiento prohibido");
        infracciones.add("Semáforo en rojo");
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

        btnTomarFoto.setOnClickListener(v -> tomarFoto());
        btnConectarImprimir.setOnClickListener(v -> checkBluetoothPermissions());
        btnInsertarConductor.setOnClickListener(v -> insertarDatosConductor());


    }

    private void toggleVisibility(View view) {
        view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void tomarFoto() {
        // Implementar la lógica para tomar una foto
        Toast.makeText(this, "Funcionalidad de tomar foto aún no implementada", Toast.LENGTH_SHORT).show();
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
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            mostrarMensaje("No hay conexión con la impresora");
            return;
        }

        try {
            outputStream = bluetoothSocket.getOutputStream();

            // Inicializar la impresora
            outputStream.write(new byte[]{0x1B, 0x40}); // Reset
            outputStream.write(new byte[]{0x1B, 0x4D, 0x01}); // Fuente pequeña

            int lineWidth = 32; // Caracteres por línea para 57mm

            // Obtener la fecha y hora actual
            Calendar calendar = Calendar.getInstance();
            int dia = calendar.get(Calendar.DAY_OF_MONTH);
            int mes = calendar.get(Calendar.MONTH) + 1;
            int anio = calendar.get(Calendar.YEAR);
            int hora = calendar.get(Calendar.HOUR_OF_DAY);
            int minuto = calendar.get(Calendar.MINUTE);

            printPhoto(R.drawable.logos8);

            imprimirCampoEnLinea("SERIE A - 2024", "", lineWidth);
            imprimirCampoEnLinea(" ", "", lineWidth);


            // Imprimir contenido

            imprimirCampoEnLinea("Numero de Boleta", tvNumero.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Fecha", String.format("%02d/%02d/%04d", dia, mes, anio), lineWidth);
            imprimirCampoEnLinea("Hora", String.format("%02d:%02d", hora, minuto), lineWidth);

            // Sección Conductor
            imprimirCampoEnLinea("CONDUCTOR", "", lineWidth);
            imprimirCampoEnLinea("Apellido y Nombre", etApellidoNombre.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Domicilio", etDomicilio.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Localidad", etLocalidad.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Codigo Postal", etCodPostal.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Departamento", etDepartamento.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Provincia", etProvincia.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Pais", etPais.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Licencia", etLicencia.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Tipo Documento", spinnerTipoDocumento.getSelectedItem().toString(), lineWidth);
            imprimirCampoEnLinea("Numero Documento", etNumeroDocumento.getText().toString(), lineWidth);

            // Sección Vehículo
            imprimirCampoEnLinea("VEHICULO", "", lineWidth);
            imprimirCampoEnLinea("Dominio", etDominio.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Propietario", etPropietario.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Marca", spinnerMarca.getSelectedItem().toString(), lineWidth);
            if (spinnerMarca.getSelectedItem().toString().equals("Otra")) {
                imprimirCampoEnLinea("Otra Marca", etOtraMarca.getText().toString(), lineWidth);
            }
            imprimirCampoEnLinea("Modelo", etModeloVehiculo.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Tipo Vehículo", spinnerTipoVehiculo.getSelectedItem().toString(), lineWidth);

            // Sección Hecho
            imprimirCampoEnLinea("HECHO", "", lineWidth);
            imprimirCampoEnLinea("Infraccion", spinnerInfraccion.getSelectedItem().toString(), lineWidth);
            imprimirCampoEnLinea("Lugar", etLugar.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Departamento Multa", etDepartamentoMulta.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Municipio Multa", etMunicipioMulta.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Info Multa", etMultaInfo.getText().toString(), lineWidth);

            // Sección Especificaciones
            imprimirCampoEnLinea("ESPECIFICACIONES", "", lineWidth);
            imprimirCampoEnLinea("Equipo", etEquipo.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Marca Cinemometro", etMarcaCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Modelo Cinemometro", etModeloCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Serie Cinemometro", etSerieCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Codigo Aprobacion", etCodAprobacionCinemometro.getText().toString(), lineWidth);
            imprimirCampoEnLinea("Valor Cinemometro", etValorCinemometro.getText().toString(), lineWidth);

            imprimirCampoEnLinea("Expedido por", spinnerExpedidaPor.getSelectedItem().toString(), lineWidth);

            // Avanzar papel y cortar
            outputStream.write(new byte[]{0x0A, 0x0A, 0x0A, 0x0A});
            outputStream.write(new byte[]{0x1D, 0x56, 0x01}); // Corte parcial

            outputStream.flush();
            mostrarMensaje("Multa impresa con éxito");

        } catch (IOException e) {
            mostrarMensaje("Error al imprimir: " + e.getMessage());
        }
    }

    private void imprimirCampoEnLinea(String etiqueta, String valor, int lineWidth) throws IOException {
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
    }

    private String generarNumeroAleatorio() {
        // Genera un número aleatorio de 6 dígitos
        Random random = new Random();
        int numeroAleatorio = 100000 + random.nextInt(900000);
        return String.valueOf(numeroAleatorio);
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
                        Log.d("DEBUG", "Respuesta completa: " + respuesta.toString());

                        if (respuesta.getMessage() != null) {
                            mostrarMensaje(respuesta.getMessage());
                            Log.d("DEBUG", "Respuesta exitosa: " + respuesta.getMessage());

                            // Obtener el ID del acta insertada
                            String actaId = respuesta.getActaId();
                            if (actaId != null && !actaId.isEmpty()) {
                                Log.d("DEBUG", "ID del acta insertada: " + actaId);
                                limpiarCampos(); // Limpia los campos después de una inserción exitosa

                                // Insertar equipo de medición después de una inserción exitosa del acta
                                insertarEquipoMedicion(actaId);
                            } else {
                                Log.e("DEBUG", "No se pudo obtener el ID del acta insertada");
                                mostrarError("Error: No se pudo obtener el ID del acta");
                            }
                        } else if (respuesta.getError() != null) {
                            mostrarError("Error: " + respuesta.getError());
                            Log.e("DEBUG", "Error en la respuesta: " + respuesta.getError());
                        } else {
                            mostrarError("Respuesta inesperada del servidor");
                            Log.e("DEBUG", "Respuesta inesperada del servidor");
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                            mostrarError("Error del servidor: " + errorBody);
                            Log.e("DEBUG", "Error del servidor: " + errorBody);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("DEBUG", "Error al leer errorBody: " + e.getMessage());
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
    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                conectarImpresora();
            } else {
                mostrarMensaje("Permiso de Bluetooth denegado");
            }
        }
    }
}