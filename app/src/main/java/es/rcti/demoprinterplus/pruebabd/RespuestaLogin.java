package es.rcti.demoprinterplus.pruebabd;

import com.google.gson.annotations.SerializedName;

public class RespuestaLogin {
    @SerializedName("message")
    private String message;

    @SerializedName("nombre")
    private String nombre = "";

    @SerializedName("apellido")
    private String apellido = "";

    @SerializedName("legajo")
    private String legajo = "";

    @SerializedName("inspector_id")  // Nuevo campo
    private String inspectorId = "";

    @SerializedName("error")
    private String error;

    // Constructor por defecto
    public RespuestaLogin() {
    }

    // Getters y setters con validación
    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNombre() {
        return nombre != null ? nombre : "";
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido != null ? apellido : "";
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getLegajo() {
        return legajo != null ? legajo : "";
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }

    public String getError() {
        return error != null ? error : "";
    }

    public void setError(String error) {
        this.error = error;
    }
    public String getInspectorId() {
        return inspectorId != null ? inspectorId : "";
    }

    public void setInspectorId(String inspectorId) {
        this.inspectorId = inspectorId;
    }

    // Método auxiliar para verificar si hay error
    public boolean hasError() {
        return error != null && !error.isEmpty();
    }
}