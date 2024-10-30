package es.rcti.demoprinterplus.pruebabd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "InfraccionesDB";
    private static final int DATABASE_VERSION = 1;

    // Nombres de tablas
    public static final String TABLE_ACTAS = "actas";
    public static final String TABLE_INFRACCIONES = "infracciones";
    public static final String TABLE_IMAGENES = "imagenes";
    public static final String TABLE_EQUIPOS = "equipos";
    public static final String TABLE_SYNC_QUEUE = "sync_queue";

    // Columnas comunes
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_SYNC_STATUS = "sync_status";

    // Columnas de sincronización
    public static final String COLUMN_TABLE_NAME = "table_name";
    public static final String COLUMN_RECORD_ID = "record_id";
    public static final String COLUMN_SYNC_ACTION = "sync_action"; // Cambiado de action a sync_action
    public static final String COLUMN_DATA = "data";

    // Columnas específicas para Actas
    public static final String COLUMN_NUMERO = "numero";
    public static final String COLUMN_FECHA = "fecha";
    public static final String COLUMN_HORA = "hora";
    public static final String COLUMN_DOMINIO = "dominio";
    public static final String COLUMN_LUGAR = "lugar";
    public static final String COLUMN_INFRACTOR_DNI = "infractor_dni";
    public static final String COLUMN_INFRACTOR_NOMBRE = "infractor_nombre";
    public static final String COLUMN_INFRACTOR_DOMICILIO = "infractor_domicilio";
    public static final String COLUMN_INFRACTOR_LOCALIDAD = "infractor_localidad";
    public static final String COLUMN_INFRACTOR_CP = "infractor_cp";
    public static final String COLUMN_INFRACTOR_PROVINCIA = "infractor_provincia";
    public static final String COLUMN_INFRACTOR_PAIS = "infractor_pais";
    public static final String COLUMN_INFRACTOR_LICENCIA = "infractor_licencia";
    public static final String COLUMN_TIPO_VEHICULO = "tipo_vehiculo";

    // Columnas para Infracciones
    public static final String COLUMN_ACTA_ID = "acta_id";
    public static final String COLUMN_INFRACCION_ID = "infraccion_id";
    public static final String COLUMN_DESCRIPCION = "descripcion";

    // Columnas para Imágenes
    public static final String COLUMN_IMAGEN_BASE64 = "imagen_base64";
    public static final String COLUMN_RUTA_LOCAL = "ruta_local";

    // Columnas para Equipos
    public static final String COLUMN_TIPO = "tipo";
    public static final String COLUMN_EQUIPO = "equipo";
    public static final String COLUMN_MARCA = "marca";
    public static final String COLUMN_MODELO = "modelo";
    public static final String COLUMN_NUMERO_SERIE = "numero_serie";
    public static final String COLUMN_CODIGO_APROBACION = "codigo_aprobacion";
    public static final String COLUMN_VALOR_MEDIDO = "valor_medido";

    // SQL de creación de tablas
    private static final String CREATE_TABLE_ACTAS = "CREATE TABLE " + TABLE_ACTAS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NUMERO + " TEXT,"
            + COLUMN_FECHA + " TEXT,"
            + COLUMN_HORA + " TEXT,"
            + COLUMN_DOMINIO + " TEXT,"
            + COLUMN_LUGAR + " TEXT,"
            + COLUMN_INFRACTOR_DNI + " TEXT,"
            + COLUMN_INFRACTOR_NOMBRE + " TEXT,"
            + COLUMN_INFRACTOR_DOMICILIO + " TEXT,"
            + COLUMN_INFRACTOR_LOCALIDAD + " TEXT,"
            + COLUMN_INFRACTOR_CP + " TEXT,"
            + COLUMN_INFRACTOR_PROVINCIA + " TEXT,"
            + COLUMN_INFRACTOR_PAIS + " TEXT,"
            + COLUMN_INFRACTOR_LICENCIA + " TEXT,"
            + COLUMN_TIPO_VEHICULO + " TEXT,"
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0"
            + ")";
    private static final String CREATE_TABLE_INFRACCIONES = "CREATE TABLE " + TABLE_INFRACCIONES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ACTA_ID + " INTEGER,"
            + COLUMN_INFRACCION_ID + " TEXT,"
            + COLUMN_DESCRIPCION + " TEXT,"
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + COLUMN_ACTA_ID + ") REFERENCES " + TABLE_ACTAS + "(" + COLUMN_ID + ")"
            + ")";

    private static final String CREATE_TABLE_IMAGENES = "CREATE TABLE " + TABLE_IMAGENES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ACTA_ID + " INTEGER,"
            + COLUMN_IMAGEN_BASE64 + " TEXT,"
            + COLUMN_RUTA_LOCAL + " TEXT,"
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + COLUMN_ACTA_ID + ") REFERENCES " + TABLE_ACTAS + "(" + COLUMN_ID + ")"
            + ")";

    private static final String CREATE_TABLE_EQUIPOS = "CREATE TABLE " + TABLE_EQUIPOS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ACTA_ID + " INTEGER,"
            + COLUMN_TIPO + " TEXT,"
            + COLUMN_EQUIPO + " TEXT,"
            + COLUMN_MARCA + " TEXT,"
            + COLUMN_MODELO + " TEXT,"
            + COLUMN_NUMERO_SERIE + " TEXT,"
            + COLUMN_CODIGO_APROBACION + " TEXT,"
            + COLUMN_VALOR_MEDIDO + " TEXT,"
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + COLUMN_ACTA_ID + ") REFERENCES " + TABLE_ACTAS + "(" + COLUMN_ID + ")"
            + ")";

    private static final String CREATE_TABLE_SYNC_QUEUE =
            "CREATE TABLE " + TABLE_SYNC_QUEUE + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "table_name TEXT NOT NULL, " +
                    "record_id INTEGER NOT NULL, " +
                    "sync_action TEXT NOT NULL, " +  // Cambiado de action a sync_action
                    "data TEXT, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_ACTAS);
            db.execSQL(CREATE_TABLE_INFRACCIONES);
            db.execSQL(CREATE_TABLE_IMAGENES);
            db.execSQL(CREATE_TABLE_EQUIPOS);
            db.execSQL(CREATE_TABLE_SYNC_QUEUE);

            // Crear índices para mejorar el rendimiento
            db.execSQL("CREATE INDEX idx_actas_sync ON " + TABLE_ACTAS + "(" + COLUMN_SYNC_STATUS + ")");
            db.execSQL("CREATE INDEX idx_infracciones_acta ON " + TABLE_INFRACCIONES + "(" + COLUMN_ACTA_ID + ")");
            db.execSQL("CREATE INDEX idx_imagenes_acta ON " + TABLE_IMAGENES + "(" + COLUMN_ACTA_ID + ")");
            db.execSQL("CREATE INDEX idx_equipos_acta ON " + TABLE_EQUIPOS + "(" + COLUMN_ACTA_ID + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error creating tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.beginTransaction();

            // Eliminar tablas en orden inverso a las dependencias
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYNC_QUEUE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIPOS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGENES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFRACCIONES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTAS);

            onCreate(db);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    // Métodos CRUD para Actas
    public long insertarActa(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        db.beginTransaction();
        try {
            values.put(COLUMN_SYNC_STATUS, 0); // 0 = no sincronizado
            id = db.insert(TABLE_ACTAS, null, values);
            if (id != -1) {
                ContentValues syncValues = new ContentValues();
                syncValues.put(COLUMN_TABLE_NAME, TABLE_ACTAS);
                syncValues.put(COLUMN_RECORD_ID, id);
                syncValues.put(COLUMN_SYNC_ACTION, "INSERT");  // Cambiado de ACTION a SYNC_ACTION
                syncValues.put(COLUMN_DATA, values.toString());
                db.insert(TABLE_SYNC_QUEUE, null, syncValues);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting acta: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        return id;
    }

    public void insertarInfracciones(long actaId, List<String> infraccionIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String infraccionId : infraccionIds) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_ACTA_ID, actaId);
                values.put(COLUMN_INFRACCION_ID, infraccionId);
                values.put(COLUMN_SYNC_STATUS, 0);

                long id = db.insert(TABLE_INFRACCIONES, null, values);
                if (id != -1) {
                    ContentValues syncValues = new ContentValues();
                    syncValues.put(COLUMN_TABLE_NAME, TABLE_INFRACCIONES);
                    syncValues.put(COLUMN_RECORD_ID, id);
                    syncValues.put(COLUMN_SYNC_ACTION, "INSERT");  // Cambiado de ACTION a SYNC_ACTION
                    syncValues.put(COLUMN_DATA, values.toString());
                    db.insert(TABLE_SYNC_QUEUE, null, syncValues);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error inserting infracciones: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public void insertarImagenes(long actaId, List<String> imagenesBase64) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String imagenBase64 : imagenesBase64) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_ACTA_ID, actaId);
                values.put(COLUMN_IMAGEN_BASE64, imagenBase64);
                values.put(COLUMN_SYNC_STATUS, 0);

                long id = db.insert(TABLE_IMAGENES, null, values);
                if (id != -1) {
                    ContentValues syncValues = new ContentValues();
                    syncValues.put(COLUMN_TABLE_NAME, TABLE_IMAGENES);
                    syncValues.put(COLUMN_RECORD_ID, id);
                    syncValues.put(COLUMN_SYNC_ACTION, "INSERT");  // Cambiado de ACTION a SYNC_ACTION
                    syncValues.put(COLUMN_DATA, values.toString());
                    db.insert(TABLE_SYNC_QUEUE, null, syncValues);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error inserting images: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public void insertarEquipo(long actaId, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            values.put(COLUMN_ACTA_ID, actaId);
            values.put(COLUMN_SYNC_STATUS, 0);

            long id = db.insert(TABLE_EQUIPOS, null, values);
            if (id != -1) {
                ContentValues syncValues = new ContentValues();
                syncValues.put(COLUMN_TABLE_NAME, TABLE_EQUIPOS);
                syncValues.put(COLUMN_RECORD_ID, id);
                syncValues.put(COLUMN_SYNC_ACTION, "INSERT");  // Cambiado de ACTION a SYNC_ACTION
                syncValues.put(COLUMN_DATA, values.toString());
                db.insert(TABLE_SYNC_QUEUE, null, syncValues);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting equipo: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public List<SyncRecord> getUnsynedRecords() {
        List<SyncRecord> unsynedRecords = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_SYNC_QUEUE + " ORDER BY " + COLUMN_CREATED_AT;
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SyncRecord record = new SyncRecord();
                    int idIndex = cursor.getColumnIndex(COLUMN_ID);
                    int tableNameIndex = cursor.getColumnIndex(COLUMN_TABLE_NAME);
                    int recordIdIndex = cursor.getColumnIndex(COLUMN_RECORD_ID);
                    int actionIndex = cursor.getColumnIndex(COLUMN_SYNC_ACTION);  // Cambiado
                    int dataIndex = cursor.getColumnIndex(COLUMN_DATA);

                    if (idIndex != -1) record.setId(cursor.getLong(idIndex));
                    if (tableNameIndex != -1) record.setTableName(cursor.getString(tableNameIndex));
                    if (recordIdIndex != -1) record.setRecordId(cursor.getLong(recordIdIndex));
                    if (actionIndex != -1) record.setAction(cursor.getString(actionIndex));
                    if (dataIndex != -1) record.setData(cursor.getString(dataIndex));

                    unsynedRecords.add(record);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting unsynced records: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return unsynedRecords;
    }

    public void markAsSynced(String tableName, long recordId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_SYNC_STATUS, 1);

            db.update(tableName, values, COLUMN_ID + "=?",
                    new String[]{String.valueOf(recordId)});

            db.delete(TABLE_SYNC_QUEUE,
                    COLUMN_TABLE_NAME + "=? AND " + COLUMN_RECORD_ID + "=?",
                    new String[]{tableName, String.valueOf(recordId)});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error marking record as synced: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getAllActas() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ACTAS, null, null, null, null, null,
                COLUMN_CREATED_AT + " DESC");
    }

    public Cursor getActaById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ACTAS, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
    }

    public List<String> getInfraccionesForActa(long actaId) {
        List<String> infracciones = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_INFRACCIONES,
                    new String[]{COLUMN_INFRACCION_ID},
                    COLUMN_ACTA_ID + "=?",
                    new String[]{String.valueOf(actaId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int infraccionIdIndex = cursor.getColumnIndex(COLUMN_INFRACCION_ID);
                if (infraccionIdIndex != -1) {
                    do {
                        infracciones.add(cursor.getString(infraccionIdIndex));
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting infracciones: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return infracciones;
    }
    public List<String> getImagenesForActa(long actaId) {
        List<String> imagenes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_IMAGENES,
                    new String[]{COLUMN_IMAGEN_BASE64},
                    COLUMN_ACTA_ID + "=?",
                    new String[]{String.valueOf(actaId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int imagenIndex = cursor.getColumnIndex(COLUMN_IMAGEN_BASE64);
                if (imagenIndex != -1) {
                    do {
                        imagenes.add(cursor.getString(imagenIndex));
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting images: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return imagenes;
    }

    public Cursor getEquipoForActa(long actaId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EQUIPOS, null,
                COLUMN_ACTA_ID + "=?",
                new String[]{String.valueOf(actaId)},
                null, null, null);
    }

    public boolean deleteActa(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Primero eliminar registros relacionados
            db.delete(TABLE_EQUIPOS, COLUMN_ACTA_ID + "=?",
                    new String[]{String.valueOf(id)});
            db.delete(TABLE_IMAGENES, COLUMN_ACTA_ID + "=?",
                    new String[]{String.valueOf(id)});
            db.delete(TABLE_INFRACCIONES, COLUMN_ACTA_ID + "=?",
                    new String[]{String.valueOf(id)});

            // Luego eliminar el acta
            int result = db.delete(TABLE_ACTAS, COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)});

            if (result > 0) {
                db.setTransactionSuccessful();
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting acta: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public int getUnsynedCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SYNC_QUEUE, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Clase interna para los registros de sincronización
    public static class SyncRecord {
        private long id;
        private String tableName;
        private long recordId;
        private String action;  // Mantenemos el nombre action aquí ya que es interno a la clase
        private String data;

        // Getters y setters
        public long getId() { return id; }
        public void setId(long id) { this.id = id; }

        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }

        public long getRecordId() { return recordId; }
        public void setRecordId(long recordId) { this.recordId = recordId; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }

        @Override
        public String toString() {
            return "SyncRecord{" +
                    "id=" + id +
                    ", tableName='" + tableName + '\'' +
                    ", recordId=" + recordId +
                    ", action='" + action + '\'' +
                    ", data='" + data + '\'' +
                    '}';
        }
    }
}