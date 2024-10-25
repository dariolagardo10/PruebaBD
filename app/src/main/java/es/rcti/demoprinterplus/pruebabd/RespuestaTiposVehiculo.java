package es.rcti.demoprinterplus.pruebabd;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RespuestaTiposVehiculo {
    private List<TipoVehiculo> tiposVehiculo;
    private boolean success;
    private String error;

    public List<TipoVehiculo> getTiposVehiculo() {
        return tiposVehiculo;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public static class TipoVehiculo {
        private int id;
        private String descripcion;
        private String descripcionCorta;

        public int getId() {
            return id;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getDescripcionCorta() {
            return descripcionCorta;
        }
    }
}