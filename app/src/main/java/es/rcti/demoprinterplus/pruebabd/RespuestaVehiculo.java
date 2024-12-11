package es.rcti.demoprinterplus.pruebabd;

public class RespuestaVehiculo {
    private boolean success;
    private String error;
    private Vehiculo vehiculo;

    public static class Vehiculo {
        private String dominio;
        private String marca;
        private String modelo;
        private String tipoVehiculo;
        private String propietario;

        // Getters y Setters
        public String getDominio() { return dominio; }
        public void setDominio(String dominio) { this.dominio = dominio; }
        public String getMarca() { return marca; }
        public void setMarca(String marca) { this.marca = marca; }
        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }
        public String getTipoVehiculo() { return tipoVehiculo; }
        public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }
        public String getPropietario() { return propietario; }
        public void setPropietario(String propietario) { this.propietario = propietario; }
    }

    // Getters y Setters de la clase principal
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }
}