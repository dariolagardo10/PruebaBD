package es.rcti.demoprinterplus.pruebabd;

import java.util.List;

public class RespuestaSincronizacion {
    private boolean success;
    private String error;
    private List<ResultadoSincronizacion> resultados;

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public List<ResultadoSincronizacion> getResultados() {
        return resultados;
    }

    public static class ResultadoSincronizacion {
        private long localId;
        private long serverId;
        private String tabla;
        private String status;

        public long getLocalId() {
            return localId;
        }

        public long getServerId() {
            return serverId;
        }

        public String getTabla() {
            return tabla;
        }

        public String getStatus() {
            return status;
        }
    }
}