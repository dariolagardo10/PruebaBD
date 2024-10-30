package es.rcti.demoprinterplus.pruebabd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private EditText editTextNewUsername, editTextNewPassword, editTextNewNombre, editTextNewApellido, editTextNewLegajo;
    private TextView textViewResult;
    private ProgressBar progressBar;
    private Button buttonLogin, buttonRegister, buttonShowRegister;
    private View layoutRegister;
    private OracleApiService apiService;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeUIComponents();
        setupListeners();
        apiService = ApiClient.getClient().create(OracleApiService.class);
    }

    private void initializeUIComponents() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextNewUsername = findViewById(R.id.editTextNewUsername);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextNewNombre = findViewById(R.id.editTextNewNombre);
        editTextNewApellido = findViewById(R.id.editTextNewApellido);
        editTextNewLegajo = findViewById(R.id.editTextNewLegajo);
        textViewResult = findViewById(R.id.textViewResult);
        progressBar = findViewById(R.id.progressBar);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonShowRegister = findViewById(R.id.buttonShowRegister);
        layoutRegister = findViewById(R.id.layoutRegister);
    }

    private void setupListeners() {
        buttonLogin.setOnClickListener(v -> verificarUsuario());
        buttonRegister.setOnClickListener(v -> registrarUsuario());
        buttonShowRegister.setOnClickListener(v -> toggleRegisterFields());
    }

    private void toggleRegisterFields() {
        if (layoutRegister.getVisibility() == View.GONE) {
            layoutRegister.setVisibility(View.VISIBLE);
            buttonShowRegister.setText("Cancelar registro");
        } else {
            layoutRegister.setVisibility(View.GONE);
            buttonShowRegister.setText("Registrarse");
            limpiarCamposRegistro();
        }
    }

    private void verificarUsuario() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor ingrese el nombre de usuario y la contraseña.");
            return;
        }

        mostrarProgreso(true);

        apiService.verificarUsuario("verificar", username, password).enqueue(new Callback<RespuestaLogin>() {
            @Override
            public void onResponse(Call<RespuestaLogin> call, Response<RespuestaLogin> response) {
                mostrarProgreso(false);

                if (response.isSuccessful() && response.body() != null) {
                    RespuestaLogin resultado = response.body();

                    if (resultado.getError() != null && !resultado.getError().isEmpty()) {
                        mostrarMensaje(resultado.getError());
                        Log.e(TAG, "Error del servidor: " + resultado.getError());
                        return;
                    }

                    if ("Usuario y contraseña correctos".equals(resultado.getMessage())) {
                        handleLoginExitoso(username, resultado);
                    } else {
                        mostrarMensaje(resultado.getMessage() != null ?
                                resultado.getMessage() : "Usuario o contraseña incorrectos");
                        Log.d(TAG, "Login fallido: " + resultado.getMessage());
                    }
                } else {
                    handleErrorRespuesta(response);
                }
            }

            @Override
            public void onFailure(Call<RespuestaLogin> call, Throwable t) {
                handleErrorConexion(t);
            }
        });
    }

    private void handleLoginExitoso(String username, RespuestaLogin resultado) {
        Log.d(TAG, "Login exitoso. Nombre: " + resultado.getNombre() +
                ", Apellido: " + resultado.getApellido() +
                ", Legajo: " + resultado.getLegajo() +
                ", Inspector ID: " + resultado.getInspectorId());

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("NOMBRE_INSPECTOR", resultado.getNombre());
        intent.putExtra("APELLIDO_INSPECTOR", resultado.getApellido());
        intent.putExtra("LEGAJO_INSPECTOR", resultado.getLegajo());
        intent.putExtra("INSPECTOR_ID", resultado.getInspectorId()); // Nuevo campo
        startActivity(intent);
        finish();
    }

    private void registrarUsuario() {
        String username = editTextNewUsername.getText().toString().trim();
        String password = editTextNewPassword.getText().toString().trim();
        String nombre = editTextNewNombre.getText().toString().trim();
        String apellido = editTextNewApellido.getText().toString().trim();
        String legajo = editTextNewLegajo.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || nombre.isEmpty() ||
                apellido.isEmpty() || legajo.isEmpty()) {
            mostrarMensaje("Todos los campos son obligatorios.");
            return;
        }

        mostrarProgreso(true);

        apiService.registrarUsuario("registrar", username, password, nombre, apellido, legajo)
                .enqueue(new Callback<RespuestaLogin>() {
                    @Override
                    public void onResponse(Call<RespuestaLogin> call, Response<RespuestaLogin> response) {
                        mostrarProgreso(false);

                        if (response.isSuccessful() && response.body() != null) {
                            RespuestaLogin resultado = response.body();

                            if (resultado.getError() != null && !resultado.getError().isEmpty()) {
                                mostrarMensaje(resultado.getError());
                                Log.e(TAG, "Error en registro: " + resultado.getError());
                                return;
                            }

                            if ("Usuario registrado correctamente".equals(resultado.getMessage())) {
                                mostrarMensaje("Usuario registrado correctamente");
                                Log.d(TAG, "Registro exitoso para: " + username + ", ID: " + resultado.getInspectorId());
                                limpiarCamposRegistro();
                                toggleRegisterFields();
                            } else {
                                mostrarMensaje(resultado.getMessage() != null ?
                                        resultado.getMessage() : "Error en el registro");
                                Log.d(TAG, "Registro fallido: " + resultado.getMessage());
                            }
                        } else {
                            handleErrorRespuesta(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<RespuestaLogin> call, Throwable t) {
                        handleErrorConexion(t);
                    }
                });
    }

    private void handleErrorRespuesta(Response<RespuestaLogin> response) {
        String errorMsg = "Error en la respuesta del servidor";
        if (response.errorBody() != null) {
            try {
                errorMsg += ": " + response.errorBody().string();
            } catch (Exception e) {
                Log.e(TAG, "Error al leer errorBody", e);
            }
        }
        mostrarMensaje(errorMsg);
        Log.e(TAG, errorMsg);
    }

    private void handleErrorConexion(Throwable t) {
        mostrarProgreso(false);
        String errorMsg = "Error en la conexión: " + t.getMessage();
        mostrarMensaje(errorMsg);
        Log.e(TAG, "Error de conexión", t);
    }

    private void mostrarMensaje(String mensaje) {
        if (textViewResult != null) {
            textViewResult.setText(mensaje);
        }
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void limpiarCamposRegistro() {
        editTextNewUsername.setText("");
        editTextNewPassword.setText("");
        editTextNewNombre.setText("");
        editTextNewApellido.setText("");
        editTextNewLegajo.setText("");
    }

    private void mostrarProgreso(boolean mostrar) {
        if (progressBar != null) {
            progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar referencias si es necesario
    }
}