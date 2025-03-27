package es.rcti.demoprinterplus.pruebabd; // Ajusta esto a tu paquete

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SignatureActivity extends AppCompatActivity {
    private SignatureView signatureView;
    private Button btnClear;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Crear el layout programáticamente
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        mainLayout.setPadding(32, 32, 32, 32);

        // Configurar SignatureView
        signatureView = new SignatureView(this);
        LinearLayout.LayoutParams signatureParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f);
        signatureView.setLayoutParams(signatureParams);
        signatureView.setBackgroundColor(Color.WHITE);

        // Crear layout para botones
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonLayout.setGravity(Gravity.CENTER);
        buttonLayout.setPadding(0, 32, 0, 0);

        // Configurar botones
        btnClear = new Button(this);
        btnClear.setText("Borrar");
        btnClear.setEnabled(false);

        btnSave = new Button(this);
        btnSave.setText("Guardar");
        btnSave.setEnabled(false);

        // Añadir botones al layout
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(16, 0, 16, 0);

        buttonLayout.addView(btnClear, buttonParams);
        buttonLayout.addView(btnSave, buttonParams);

        // Añadir vistas al layout principal
        mainLayout.addView(signatureView);
        mainLayout.addView(buttonLayout);

        setContentView(mainLayout);

        // Configurar listeners
        setupListeners();
    }

    private void setupListeners() {
        signatureView.setOnSignatureListener(new SignatureView.OnSignatureListener() {
            @Override
            public void onSignatureDrawn(boolean hasSignature) {
                btnClear.setEnabled(hasSignature);
                btnSave.setEnabled(hasSignature);
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signatureView.clear();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap signature = signatureView.getBitmap();
                if (signature != null) {
                    saveBitmapAndFinish(signature);
                }
            }
        });
    }

    private void saveBitmapAndFinish(Bitmap bitmap) {
        try {
            // Guardar temporalmente la imagen
            String fileName = "signature_" + System.currentTimeMillis() + ".png";
            File file = new File(getCacheDir(), fileName);

            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            // Crear URI usando FileProvider
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            // Devolver el resultado
            Intent resultIntent = new Intent();
            resultIntent.putExtra("signature_uri", uri.toString());
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la firma", Toast.LENGTH_SHORT).show();
        }
    }
}