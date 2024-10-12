package es.rcti.demoprinterplus.pruebabd;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RespuestaTiposVehiculo {
    @SerializedName("tiposVehiculo")
    private List<TipoVehiculo> tiposVehiculo;

    public List<TipoVehiculo> getTiposVehiculo() {
        return tiposVehiculo;
    }

    public static class TipoVehiculo {
        @SerializedName("id")
        private int id;
        @SerializedName("descripcion")
        private String descripcion;
        @SerializedName("descripcionCorta")
        private String descripcionCorta;

        // Getters
        public int getId() { return id; }
        public String getDescripcion() { return descripcion; }
        public String getDescripcionCorta() { return descripcionCorta; }
    }
}