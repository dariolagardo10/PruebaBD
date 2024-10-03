package es.rcti.demoprinterplus.pruebabd;

import java.util.List;

public class RespuestaDepartamentos {
    private List<Departamento> departamentos;
    private String error;

    public List<Departamento> getDepartamentos() {
        return departamentos;
    }

    public String getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public static class Departamento {
        private String c_departamento;
        private String d_descrip;

        public String getC_departamento() {
            return c_departamento;
        }

        public String getD_descrip() {
            return d_descrip;
        }
    }
}