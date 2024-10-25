package es.rcti.demoprinterplus.pruebabd;

import java.util.List;

public class RespuestaMarcas {
    private List<Marca> marcas;

    public List<Marca> getMarcas() {
        return marcas;
    }

    public static class Marca {
        private int id;
        private String descripcion;

        public int getId() {
            return id;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
