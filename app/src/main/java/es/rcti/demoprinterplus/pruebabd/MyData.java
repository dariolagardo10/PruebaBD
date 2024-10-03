package es.rcti.demoprinterplus.pruebabd;

import com.google.gson.annotations.SerializedName;

public class MyData {
    @SerializedName("ID")
    private String id;

    @SerializedName("USERNAME")
    private String username;

    @SerializedName("PASSWORD")
    private String password;

    @SerializedName("NOMBRE")
    private String nombre;

    @SerializedName("APELLIDO")
    private String apellido;

    @SerializedName("LEGAJO")
    private String legajo;

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getLegajo() {
        return legajo;
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }
}

