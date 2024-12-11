package es.rcti.demoprinterplus.pruebabd;

import java.util.List;

public class RespuestaBuscarDominio {
    private boolean success;
    private List<DatosPersona> datos;
    private String mensaje;
    private String error;

    public static class DatosPersona {
        private String dni;
        private String nombre_completo;
        private String domicilio;
        private String municipalidad;

        public String getDni() {
            return dni;
        }

        public void setDni(String dni) {
            this.dni = dni;
        }

        public String getNombre_completo() {
            return nombre_completo;
        }

        public void setNombre_completo(String nombre_completo) {
            this.nombre_completo = nombre_completo;
        }

        public String getDomicilio() {
            return domicilio;
        }

        public void setDomicilio(String domicilio) {
            this.domicilio = domicilio;
        }

        public String getMunicipalidad() {
            return municipalidad;
        }

        public void setMunicipalidad(String municipalidad) {
            this.municipalidad = municipalidad;
        }
    }
}
