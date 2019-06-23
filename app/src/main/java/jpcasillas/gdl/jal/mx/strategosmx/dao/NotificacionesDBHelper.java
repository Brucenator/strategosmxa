package jpcasillas.gdl.jal.mx.strategosmx.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.nfc.Tag;
import android.util.Log;

import jpcasillas.gdl.jal.mx.strategosmx.dao.NotificacionesContract.NotificacionEntry;
import jpcasillas.gdl.jal.mx.strategosmx.dto.NotificacionVO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class NotificacionesDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "wfactiv.db";
    private static final String TABLE_NOTIFICACIONES = "notificaciones";
    private static final String TABLE_EJECUTIVOS = "ejecutivos";
    //private static final String DB_PATH = Environment.getExternalStorageDirectory()+"/notificaciones/";
    //private static final String DB_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/notificaciones/";

    public NotificacionesDBHelper(final Context context) {
        //super(context, DATABASE_NAME, null, DATABASE_VERSION);
        super(context, context.getExternalFilesDir(null).getAbsolutePath() + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Comandos SQL
        ///data/data/<paquete>/databases/<nombre-de-la-bd>.db
        //File rutadb = new File(DB_PATH);
        //rutadb.mkdir();

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + NotificacionEntry.TABLE_NAME + " ("
                + NotificacionEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NotificacionEntry.MANZANA + " TEXT NOT NULL,"
                + NotificacionEntry.FECHA_REGISTRO + " TEXT NOT NULL,"
                + NotificacionEntry.HORA_REGISTRO + " TEXT NOT NULL,"
                + NotificacionEntry.EJECUTIVO + " TEXT NOT NULL,"
                + NotificacionEntry.LATITUD + " TEXT NOT NULL,"
                + NotificacionEntry.LONGITUD + " TEXT NOT NULL,"
                + NotificacionEntry.MEDIDOR + " TEXT NOT NULL,"
                + NotificacionEntry.PROCESO + " TEXT NOT NULL,"
                + "UNIQUE (" + NotificacionEntry.ID + "))");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + NotificacionEntry.EJECUTIVO + " (" +
                NotificacionEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                NotificacionEntry.EJECUTIVO + " TEXT NOT NULL," +
                NotificacionEntry.CONSECUTIVO + " TEXT NOT NULL, "
                + "UNIQUE (" + NotificacionEntry.ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("tabla", "Updating table from " + oldVersion + " to " + newVersion);
        // Drop older table if existed
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICACIONES);
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_EJECUTIVOS);
        // Creating tables again
        //onCreate(db);


        // If you need to add a column
        //if (newVersion > oldVersion) {
        //    db.execSQL("ALTER TABLE " + NotificacionEntry.TABLE_NAME + " ADD COLUMN uso TEXT");
        //    db.execSQL("ALTER TABLE  " + NotificacionEntry.TABLE_NAME + " ADD COLUMN proceso TEXT");
        //    db.execSQL("ALTER TABLE " + NotificacionEntry.TABLE_NAME + " ADD COLUMN lectura TEXT");
        //    db.execSQL("ALTER TABLE " + NotificacionEntry.TABLE_NAME + " ADD COLUMN medidor TEXT");
        //}
    }

    public long saveNotificacion(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                NotificacionEntry.TABLE_NAME,
                null,
                values);
    }

    public long saveEjecutivos(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                NotificacionEntry.TABLE_NAME_EJE,
                null,
                values);
    }

    public void limpiaNotificaciones() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICACIONES);
        onCreate(db);
    }

    public void delete(Long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NOTIFICACIONES + " where id='" + id + "'");
    }

    public NotificacionVO getNotificacionPorId(int id) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_NOTIFICACIONES, new String[]{"id", "manzana", "fechaRegistro", "horaRegistro", "ejecutivo", "latitud", "longitud", "medidor", "proceso", "uso", "lectura"}, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        NotificacionVO not = null;
        if (cursor != null) {
            cursor.moveToFirst();

            not.setId(cursor.getLong(0));
            not.setManzana(cursor.getString(1));
            not.setFecharegistro(cursor.getString(2));
            not.setHoraregistro(cursor.getString(3));
            not.setEjecutivo(Long.parseLong(cursor.getString(4)));
            not.setLatitud(Double.parseDouble(cursor.getString(5)));
            not.setLongitud(Double.parseDouble(cursor.getString(6)));
            not.setMedidor(cursor.getString(7));
            not.setProceso(cursor.getInt(8));
            not.setUso(cursor.getInt(9));
            not.setLectura(cursor.getLong(10));
        }
        return not;
    }

    public List<NotificacionVO> getNotificaciones() {

        List<NotificacionVO> lista = new ArrayList<NotificacionVO>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NOTIFICACIONES;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                NotificacionVO not = new NotificacionVO();
                not.setId(cursor.getLong(0));
                not.setManzana(cursor.getString(1));
                not.setFecharegistro(cursor.getString(2));
                not.setHoraregistro(cursor.getString(3));
                not.setEjecutivo(Long.parseLong(cursor.getString(4)));
                not.setLatitud(Double.parseDouble(cursor.getString(5)));
                not.setLongitud(Double.parseDouble(cursor.getString(6)));

                // Adding contact to list
                lista.add(not);
            } while (cursor.moveToNext());
        }
        // return contact list
        return lista;
    }

}
