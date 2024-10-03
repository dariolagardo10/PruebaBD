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
    private EditText editTextNewNombre, editTextNewApellido, editTextNewLegajo;
    private TextView textViewResult;
    private ProgressBar progressBar;
    private Button buttonLogin, buttonRegister, buttonShowRegister;
    private View layoutRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeUIComponents();
        setupListeners();
    }

    private void initializeUIComponents() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
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
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor ingrese el nombre de usuario y la contraseña.");
            return;
        }

        mostrarProgreso(true);

        OracleApiService apiService = ApiClient.getClient().create(OracleApiService.class);
        apiService.verificarUsuario("verificar", username, password).enqueue(new Callback<RespuestaLogin>() {
            @Override
            public void onResponse(Call<RespuestaLogin> call, Response<RespuestaLogin> response) {
                mostrarProgreso(false);
                if (response.isSuccessful() && response.body() != null) {
                    RespuestaLogin resultado = response.body();
                    if ("Usuario y contraseña correctos".equals(resultado.getMessage())) {
                        onLoginSuccess(username);
                    } else {
                        mostrarMensaje("Usuario o contraseña incorrectos");
                    }
                } else {
                    mostrarMensaje("Error en la respuesta del servidor");
                }
            }

            @Override
            public void onFailure(Call<RespuestaLogin> call, Throwable t) {
                mostrarProgreso(false);
                mostrarMensaje("Error en la conexión: " + t.getMessage());
                Log.e("API Error", t.getMessage());
            }
        });
    }

    private void registrarUsuario() {
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        String nombre = editTextNewNombre.getText().toString();
        String apellido = editTextNewApellido.getText().toString();
        String legajo = editTextNewLegajo.getText().toString();

        if (username.isEmpty() || password.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || legajo.isEmpty()) {
            mostrarMensaje("Todos los campos son obligatorios.");
            return;
        }

        mostrarProgreso(true);

        OracleApiService apiService = ApiClient.getClient().create(OracleApiService.class);
        apiService.registrarUsuario("registrar", username, password, nombre, apellido, legajo).enqueue(new Callback<RespuestaLogin>() {
            @Override
            public void onResponse(Call<RespuestaLogin> call, Response<RespuestaLogin> response) {
                mostrarProgreso(false);
                if (response.isSuccessful() && response.body() != null) {
                    mostrarMensaje("Usuario registrado correctamente");
                    limpiarCamposRegistro();
                    toggleRegisterFields(); // Ocultar campos de registro después de un registro exitoso
                } else {
                    mostrarMensaje("Error en el registro.");
                }
            }

            @Override
            public void onFailure(Call<RespuestaLogin> call, Throwable t) {
                mostrarProgreso(false);
                mostrarMensaje("Error en la conexión: " + t.getMessage());
                Log.e("API Error", t.getMessage());
            }
        });
    }

    private void onLoginSuccess(String username) {
        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish();
    }

    private void mostrarMensaje(String mensaje) {
        textViewResult.setText(mensaje);
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void limpiarCamposRegistro() {
        editTextNewNombre.setText("");
        editTextNewApellido.setText("");
        editTextNewLegajo.setText("");
    }

    private void mostrarProgreso(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }
}