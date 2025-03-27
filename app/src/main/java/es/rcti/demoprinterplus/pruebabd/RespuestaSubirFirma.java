
package es.rcti.demoprinterplus.pruebabd;
public class RespuestaSubirFirma {
    private boolean success;
    private String firmaUrl;
    private String firmaId;
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public String getFirmaUrl() {
        return firmaUrl;
    }

    public String getFirmaId() {
        return firmaId;
    }

    public String getError() {
        return error;
    }
}