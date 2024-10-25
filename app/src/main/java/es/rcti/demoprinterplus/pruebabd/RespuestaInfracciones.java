package es.rcti.demoprinterplus.pruebabd;

import java.util.List;

public class RespuestaInfracciones {
    private List<Infraccion> infracciones;

    public List<Infraccion> getInfracciones() {
        return infracciones;
    }

    public static class Infraccion {
        private String id;
        private String descripcion;

        public String getId() { return id; }
        public String getDescripcion() { return descripcion; }
    }
}