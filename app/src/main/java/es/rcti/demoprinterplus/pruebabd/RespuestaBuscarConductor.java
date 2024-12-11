package es.rcti.demoprinterplus.pruebabd;
public class RespuestaBuscarConductor {
    private boolean success;
    private String message;
    private ConductorData conductor; // Cambiado de 'data' a 'conductor' para que coincida con el JSON

    public static class ConductorData {
        private String apellido_nombre;
        private String dni;
        private String domicilio;
        private String razon_social;

        // Getters y Setters
        public String getApellidoNombre() { return apellido_nombre; }
        public String getDni() { return dni; }
        public String getDomicilio() { return domicilio; }
        public String getRazonSocial() { return razon_social; }
    }

    // Getters y Setters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public ConductorData getConductor() { return conductor; } // Cambiado de getData() a getConductor()
}
