package es.rcti.demoprinterplus.pruebabd;

import java.util.List;

public class RespuestaLocalidades {
    private List<Localidad> localidades;
    private String error;

    public List<Localidad> getLocalidades() {
        return localidades;
    }

    public String getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public static class Localidad {
        private String c_localidad;
        private String d_descrip;
        private String c_postal;

        public String getC_localidad() {
            return c_localidad;
        }

        public String getD_descrip() {
            return d_descrip;
        }

        public String getC_postal() {
            return c_postal;
        }
    }
}
