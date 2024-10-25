package es.rcti.demoprinterplus.pruebabd;

import java.util.List;

public class RespuestaSubirImagen {
    private boolean success;
    private String error;
    private List<String> imagenesUrls;

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public List<String> getImagenesUrls() {
        return imagenesUrls;
    }
}
