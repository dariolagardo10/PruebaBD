package es.rcti.demoprinterplus.pruebabd;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
public class RespuestaInfoLocalidad {
    @SerializedName("infoLocalidades")
    private List<InfoLocalidad> infoLocalidades;

    public List<InfoLocalidad> getInfoLocalidades() {
        return infoLocalidades != null ? infoLocalidades : new ArrayList<>();
    }

    public static class InfoLocalidad {
        // Ajuste para reflejar los nombres en mayúsculas que devuelve la API
        @SerializedName("LOCALIDAD")
        private String localidad;

        @SerializedName("CODIGO_POSTAL")
        private String codigoPostal;

        @SerializedName("DEPARTAMENTO")
        private String departamento;

        @SerializedName("PROVINCIA")
        private String provincia;

        @SerializedName("PAIS")
        private String pais;

        // Getters
        public String getLocalidad() {
            return localidad;
        }

        public String getCodigoPostal() {
            return codigoPostal;
        }

        public String getDepartamento() {
            return departamento;
        }

        public String getProvincia() {
            return provincia;
        }

        public String getPais() {
            return pais;
        }

        @Override
        public String toString() {
            return "InfoLocalidad{" +
                    "localidad='" + localidad + '\'' +
                    ", codigoPostal='" + codigoPostal + '\'' +
                    ", departamento='" + departamento + '\'' +
                    ", provincia='" + provincia + '\'' +
                    ", pais='" + pais + '\'' +
                    '}';
        }
    }
}

