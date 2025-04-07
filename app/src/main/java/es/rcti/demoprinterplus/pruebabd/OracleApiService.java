package es.rcti.demoprinterplus.pruebabd;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface OracleApiService {


        @FormUrlEncoded
        @POST("/oracle_api.php")
        Call<RespuestaLogin> verificarUsuario(
                @Field("accion") String accion,
                @Field("username") String username,
                @Field("password") String password
        );

        @FormUrlEncoded
        @POST("/oracle_api.php")
        Call<RespuestaLogin> registrarUsuario(
                @Field("accion") String accion,
                @Field("username") String username,
                @Field("password") String password,
                @Field("nombre") String nombre,
                @Field("apellido") String apellido,
                @Field("legajo") String legajo
        );



    @POST("Conductor_Api.php")
    Call<RespuestaSincronizacion> sincronizarDatos(
            @Field("accion") String accion,
            @Field("registros") String registrosJson
    );

    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaBuscarConductor> buscarConductorPorDNI(
            @Field("accion") String accion,
            @Field("dni") String dni
    );

    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaVehiculo> buscarVehiculo(
            @Field("accion") String accion,  // Cambiar "action" por "accion"
            @Field("dominio") String dominio
    );
    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaInsertarConductor> insertarConductor(
            @Field("accion") String accion,
            @Field("numero") String numero,
            @Field("fecha") String fecha,
            @Field("hora") String hora,
            @Field("dominio") String dominio,
            @Field("lugar") String lugar,
            @Field("infracciones") String infracciones,
            @Field("infractor_dni") String infractorDni,
            @Field("infractor_nombre") String infractorNombre,
            @Field("infractor_domicilio") String infractorDomicilio,
            @Field("infractor_localidad") String infractorLocalidad,
            @Field("infractor_cp") String infractorCp,
            @Field("infractor_provincia") String infractorProvincia,
            @Field("infractor_pais") String infractorPais,
            @Field("infractor_licencia") String infractorLicencia,
            @Field("tipo_vehiculo") String tipoVehiculo,
            @Field("inspector_id") String inspectorId,
            @Field("marca_vehiculo") String marcaVehiculo,
            @Field("propietario") String propietario,
            @Field("modelo_vehiculo") String modeloVehiculo,
            @Field("departamento") String departamento,
            @Field("municipio") String municipio,
            @Field("observaciones") String observaciones,
            @Field("tipo_acta") String tipoActa  // Nuevo parámetro
    );
    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaDepartamentos> obtenerDepartamentos(
            @Field("accion") String accion,
            @Field("provincia") String provincia
    );

    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaLocalidades> obtenerLocalidades(
            @Field("accion") String accion,
            @Field("provincia") String provincia,
            @Field("departamento") String departamento
    );


    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaInsertarEquipoMedicion> insertarEquipoMedicion(
            @Field("accion") String accion,
            @Field("dt_acta_id") String dtActaId,
            @Field("tipo") String tipo,
            @Field("equipo") String equipo,
            @Field("marca") String marca,
            @Field("modelo") String modelo,
            @Field("numero_serie") String numeroSerie,
            @Field("codigo_aprobacion") String codigoAprobacion,
            @Field("valor_medido") String valorMedido
    );

    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaVerificarActa> verificarActa(
            @Field("accion") String accion,
            @Field("dt_acta_id") String dtActaId
    );


    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaInfoLocalidad> buscarInfoLocalidad(@Field("accion") String accion, @Field("localidad") String localidad);

    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaSubirImagen> subirImagen(
            @Field("accion") String accion,
            @Field("actaId") String actaId,
            @Field("imagenes[]") List<String> imagenesBase64
    );
    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaTiposVehiculo> obtenerTiposVehiculo(@Field("accion") String accion);

    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaSubirFirma> subirFirmaInfractor(
            @Field("accion") String accion,
            @Field("actaId") String actaId,
            @Field("firma") String firmaBase64
    );

    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaMarcas> obtenerMarcas(@Field("accion") String accion);

    @FormUrlEncoded
    @POST("Conductor_Api.php")
    Call<RespuestaInfracciones> obtenerInfracciones(@Field("accion") String accion);
}
