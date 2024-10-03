package es.rcti.demoprinterplus.pruebabd;


import com.google.gson.annotations.SerializedName;

public class RespuestaLogin {

    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
