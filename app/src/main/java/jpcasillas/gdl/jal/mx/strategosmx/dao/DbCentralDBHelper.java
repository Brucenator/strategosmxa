package jpcasillas.gdl.jal.mx.strategosmx.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jpcasillas.gdl.jal.mx.strategosmx.models.BoletasVO;
import jpcasillas.gdl.jal.mx.strategosmx.models.CensoVO;
import jpcasillas.gdl.jal.mx.strategosmx.models.CobranzaVO;
import jpcasillas.gdl.jal.mx.strategosmx.models.FotografiasCensoVO;
import jpcasillas.gdl.jal.mx.strategosmx.models.LecturasVO;
import jpcasillas.gdl.jal.mx.strategosmx.models.OrdenServicioVO;

public class DbCentralDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "dbcentral.db";
    private static final String TABLE_TREPCAMBOLETAS = "trepcamboletas";
    private static final String TABLE_TREPCAMLECTURAS = "trepcamlecturas";
    private static final String TABLE_TREPCAMCOBRANZAS = "trepcamcobranza";
    private static final String TABLE_TREPCAMORDENSERVICIO= "trepcamordenservicio";
    private static final String TABLE_TCENSO = "tcenso";
    private static final String TABLE_TREPCAMCENSO = "trepcamcenso";
    private static final String TABLE_TFOTOGRAFIASCENSO = "tfotografiascenso";
    private static final String TABLE_ORDEN_SERVICIO = "tordenservicio";

    public DbCentralDBHelper(final Context context) {
        //super(context, DATABASE_NAME, null, DATABASE_VERSION);
        super(context, context.getExternalFilesDir(null).getAbsolutePath() + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Comandos SQL
        ///data/data/<paquete>/databases/<nombre-de-la-bd>.db
        //File rutadb = new File(DB_PATH);
        //rutadb.mkdir();

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + DbCentralContract.BoletasEntry.TABLE_NAME + " ("
                + DbCentralContract.BoletasEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbCentralContract.BoletasEntry.MANZANA + " TEXT NOT NULL,"
                + DbCentralContract.BoletasEntry.FECHA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.BoletasEntry.HORA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.BoletasEntry.EJECUTIVO + " TEXT NOT NULL,"
                + DbCentralContract.BoletasEntry.LATITUD + " TEXT NOT NULL,"
                + DbCentralContract.BoletasEntry.LONGITUD + " TEXT NOT NULL,"
                + DbCentralContract.BoletasEntry.PROCESO + " TEXT NOT NULL,"
                + DbCentralContract.BoletasEntry.BIMESTRE + " TEXT NOT NULL,"
                + DbCentralContract.BoletasEntry.IMEI + " TEXT NOT NULL,"
                //+ DbCentralContract.BoletasEntry.ENVIADO + " TEXT NOT NULL,"
                + "UNIQUE (" + DbCentralContract.BoletasEntry.ID + "))");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + DbCentralContract.EjecutivosEntry.TABLE_NAME_EJE + " (" +
                DbCentralContract.EjecutivosEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DbCentralContract.EjecutivosEntry.EJECUTIVO + " TEXT NOT NULL," +
                DbCentralContract.EjecutivosEntry.CONSECUTIVO + " TEXT NOT NULL, "
                + "UNIQUE (" + DbCentralContract.EjecutivosEntry.ID + "))");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + DbCentralContract.LecturasEntry.TABLE_NAME + " ("
                + DbCentralContract.LecturasEntry.IDLECTURAS + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbCentralContract.LecturasEntry.MANZANA + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.FECHA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.HORA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.EJECUTIVO + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.LATITUD + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.LONGITUD + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.PROCESO + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.BIMESTRE + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.IMEI + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.LECTURA + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.RUTA + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.MEDIDOR + " TEXT NOT NULL,"
                + DbCentralContract.LecturasEntry.NOTASLECTURA + " TEXT NOT NULL,"
                + "UNIQUE (" + DbCentralContract.LecturasEntry.IDLECTURAS + "))");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + DbCentralContract.CobranzaEntry.TABLE_NAME + " ("
                + DbCentralContract.CobranzaEntry.IDCOBRANZA + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbCentralContract.CobranzaEntry.MANZANA + " TEXT NOT NULL,"
                + DbCentralContract.CobranzaEntry.FECHA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.CobranzaEntry.HORA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.CobranzaEntry.EJECUTIVO + " TEXT NOT NULL,"
                + DbCentralContract.CobranzaEntry.LATITUD + " TEXT NOT NULL,"
                + DbCentralContract.CobranzaEntry.LONGITUD + " TEXT NOT NULL,"
                + DbCentralContract.CobranzaEntry.PROCESO + " TEXT NOT NULL,"
                + DbCentralContract.CobranzaEntry.IMEI + " TEXT NOT NULL,"
                + DbCentralContract.CobranzaEntry.RUTA + " TEXT NOT NULL,"
                + DbCentralContract.CobranzaEntry.ENVIADO + " TEXT NOT NULL,"
                + DbCentralContract.CobranzaEntry.VUELTA + " TEXT NOT NULL,"
                + DbCentralContract.CobranzaEntry.OFICIO + " TEXT NOT NULL,"
                + "UNIQUE (" + DbCentralContract.CobranzaEntry.IDCOBRANZA + "))");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + DbCentralContract.CensoEntry.TABLE_REPCAMCENSO + " ("
                + DbCentralContract.CensoEntry.IDREPCAMCENSO + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbCentralContract.CensoEntry.CUENTA + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.CALLE + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.EXTERIOR + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.INTERIOR + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.COLONIA + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.CODIGOPOSTAL + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.USO + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.VIVIENDAS + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.LOCALES + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.DIAMTOMA + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.SERIEMEDIDOR + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.MARCAMEDIDOR + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.DIAMMEDIDOR + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.TOMAS + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.FECHA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.HORA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.EJECUTIVO + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.LATITUD + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.LONGITUD + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.IMEI + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.PROCESO + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.OBSERVACIONES + " TEXT NOT NULL,"
                + "UNIQUE (" + DbCentralContract.CensoEntry.IDREPCAMCENSO + "))");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + DbCentralContract.CensoEntry.TABLE_NAME_CENSO + " ("
                + DbCentralContract.CensoEntry.IDCENSO + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbCentralContract.CensoEntry.CUENTA + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.CALLE + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.EXTERIOR + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.INTERIOR + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.COLONIA + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.CODIGOPOSTAL + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.USO + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.VIVIENDAS + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.LOCALES + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.DIAMTOMA + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.SERIEMEDIDOR + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.MARCAMEDIDOR + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.DIAMMEDIDOR + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.TOMAS + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.IDREPCAMCENSO + " INTEGER NOT NULL,"
                /*+ DbCentralContract.CensoEntry.OBSERVACIONES + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.FECHA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.HORA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.EJECUTIVO + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.LATITUD + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.LONGITUD + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.IMEI + " TEXT NOT NULL,"
                + DbCentralContract.CensoEntry.PROCESO + " TEXT NOT NULL,"*/
                + "UNIQUE (" + DbCentralContract.CensoEntry.IDCENSO + "))");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + DbCentralContract.FotograciasCensoEntry.TABLE_NAME + " ("
                + DbCentralContract.FotograciasCensoEntry.IDFOTOGRAFIACENSO + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbCentralContract.FotograciasCensoEntry.IDREPCAMCENSO + " TEXT NOT NULL,"
                + DbCentralContract.FotograciasCensoEntry.FOTOGRAFIA + " TEXT NOT NULL,"
                + "UNIQUE (" + DbCentralContract.FotograciasCensoEntry.IDFOTOGRAFIACENSO + "))");

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + DbCentralContract.OrdenServicioEntry.TABLE_NAME + " ("
                + DbCentralContract.OrdenServicioEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbCentralContract.OrdenServicioEntry.MANZANA + " TEXT NOT NULL,"
                + DbCentralContract.OrdenServicioEntry.FECHA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.OrdenServicioEntry.HORA_REGISTRO + " TEXT NOT NULL,"
                + DbCentralContract.OrdenServicioEntry.EJECUTIVO + " TEXT NOT NULL,"
                + DbCentralContract.OrdenServicioEntry.LATITUD + " TEXT NOT NULL,"
                + DbCentralContract.OrdenServicioEntry.LONGITUD + " TEXT NOT NULL,"
                + DbCentralContract.OrdenServicioEntry.PROCESO + " TEXT NOT NULL,"
                + DbCentralContract.OrdenServicioEntry.BIMESTRE + " TEXT NOT NULL,"
                + DbCentralContract.OrdenServicioEntry.IMEI + " TEXT NOT NULL,"
                + DbCentralContract.OrdenServicioEntry.ORDENSERVICIO + " TEXT NOT NULL,"
                + DbCentralContract.OrdenServicioEntry.IDCLAVEORDEN + " TEXT NOT NULL,"
                + DbCentralContract.OrdenServicioEntry.VUELTA + " TEXT NOT NULL,"
                + "UNIQUE (" + DbCentralContract.OrdenServicioEntry.ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("tabla", "Updating table from " + oldVersion + " to " + newVersion);
        // Drop older table if existed
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICACIONES);
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_EJECUTIVOS);
        // Creating tables again
        onCreate(db);


        // If you need to add a column
        //if (newVersion > oldVersion) {
        //    db.execSQL("ALTER TABLE " + DbCentralContract.LecturasEntry.TABLE_NAME + " ADD COLUMN idnotaslectura TEXT");
            //db.execSQL("ALTER TABLE  " + NotificacionEntry.TABLE_NAME + " ADD COLUMN proceso TEXT");
            //db.execSQL("ALTER TABLE " + NotificacionEntry.TABLE_NAME + " ADD COLUMN lectura TEXT");
        //    db.execSQL("ALTER TABLE " + DbCentralContract.LecturasEntry.TABLE_NAME + " ADD COLUMN enviado TEXT");
        //}
    }

    public long saveBoletas(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                DbCentralContract.BoletasEntry.TABLE_NAME,
                null,
                values);
    }

    public long saveLecturas(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                DbCentralContract.LecturasEntry.TABLE_NAME,
                null,
                values);
    }

    public long saveEjecutivos(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                DbCentralContract.EjecutivosEntry.TABLE_NAME_EJE,
                null,
                values);
    }

    /*public void dropBoletas() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREPCAMBOLETAS);
        onCreate(db);
    }

    public void dropLecturas() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TREPCAMLECTURAS);
        onCreate(db);
    }*/

    public void deleteBoletas(Long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TREPCAMBOLETAS,"idboleta="+id,null);
        //db.execSQL("delete from " + TABLE_TREPCAMBOLETAS + " where idboleta='" + id + "'");
    }

    public void deleteLecturas(Long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TREPCAMLECTURAS,"idlecturas="+id,null);
        //db.execSQL("delete from " + TABLE_TREPCAMLECTURAS + " where idlecturas='" + id + "'");
    }

    /*public BoletasVO getBoletaPorId(int id) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_TREPCAMBOLETAS, new String[]{"id", "manzana", "fechaRegistro", "horaRegistro", "ejecutivo", "latitud", "longitud", "proceso", "bimestre", "imei"}, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        BoletasVO not = null;
        if (cursor != null) {
            cursor.moveToFirst();

            not.setIdBoletas(cursor.getLong(0));
            not.setManzana(cursor.getString(1));
            not.setFecharegistro(cursor.getString(2));
            not.setHoraregistro(cursor.getString(3));
            not.setEjecutivo(cursor.getString(4));
            not.setLatitud(Double.parseDouble(cursor.getString(5)));
            not.setLongitud(Double.parseDouble(cursor.getString(6)));
            not.setProceso(cursor.getInt(7));
            not.setBimestre(cursor.getString(8));
            not.setImei(cursor.getString(9));
        }
        return not;
    }

    public LecturasVO getLecturaPorId(int id) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_TREPCAMLECTURAS, new String[]{"id", "manzana", "fechaRegistro", "horaRegistro", "ejecutivo", "latitud", "longitud", "proceso", "bimestre", "imei","medidor","lectura","ruta"}, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        LecturasVO not = null;
        if (cursor != null) {
            cursor.moveToFirst();

            not.setId(cursor.getLong(0));
            not.setManzana(cursor.getString(1));
            not.setFecharegistro(cursor.getString(2));
            not.setHoraregistro(cursor.getString(3));
            not.setEjecutivo(cursor.getString(4));
            not.setLatitud(Double.parseDouble(cursor.getString(5)));
            not.setLongitud(Double.parseDouble(cursor.getString(6)));
            not.setProceso(cursor.getInt(7));
            not.setBimestre(cursor.getString(8));
            not.setImei(cursor.getString(9));
            not.setMedidor(cursor.getString(10));
            not.setLectura(cursor.getLong(11));
            not.setRuta(cursor.getString(12));
        }
        return not;
    }*/

    public List<BoletasVO> getBoletas() {

        List<BoletasVO> lista = new ArrayList<BoletasVO>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_TREPCAMBOLETAS;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                BoletasVO not = new BoletasVO();
                not.setIdBoletas(cursor.getLong(0));
                not.setManzana(cursor.getString(1));
                not.setFecharegistro(cursor.getString(2));
                not.setHoraregistro(cursor.getString(3));
                not.setEjecutivo(cursor.getString(4));
                not.setLatitud(Double.parseDouble(cursor.getString(5)));
                not.setLongitud(Double.parseDouble(cursor.getString(6)));
                not.setProceso(cursor.getInt(7));
                not.setBimestre(cursor.getString(8));
                not.setImei(cursor.getString(9));

                // Adding contact to list
                lista.add(not);
            } while (cursor.moveToNext());
        }
        // return contact list
        return lista;
    }

    public List<LecturasVO> getLecturas() {

        List<LecturasVO> lista = new ArrayList<LecturasVO>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_TREPCAMLECTURAS;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                LecturasVO not = new LecturasVO();
                not.setId(cursor.getLong(0));
                not.setManzana(cursor.getString(1));
                not.setFecharegistro(cursor.getString(2));
                not.setHoraregistro(cursor.getString(3));
                not.setEjecutivo(cursor.getString(4));
                not.setLatitud(Double.parseDouble(cursor.getString(5)));
                not.setLongitud(Double.parseDouble(cursor.getString(6)));
                not.setBimestre(cursor.getString(8));
                not.setImei(cursor.getString(9));
                not.setProceso(cursor.getInt(7));
                not.setLectura(cursor.getLong(10));
                not.setMedidor(cursor.getString(12));
                not.setRuta(cursor.getString(11));
                not.setIdnotalectura(cursor.getLong(13));
                // Adding contact to list
                lista.add(not);
            } while (cursor.moveToNext());
        }
        // return contact list
        return lista;
    }

    public long saveCobranza(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                DbCentralContract.CobranzaEntry.TABLE_NAME,
                null,
                values);
    }

    public List<CobranzaVO> getCobranzas() {

        List<CobranzaVO> lista = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_TREPCAMCOBRANZAS +" where enviado=0" ;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                CobranzaVO not = new CobranzaVO();
                not.setIdCobranza(cursor.getLong(0));
                not.setManzana(cursor.getString(1));
                not.setFecharegistro(cursor.getString(2));
                not.setHoraregistro(cursor.getString(3));
                not.setEjecutivo(cursor.getString(4));
                not.setLatitud(Double.parseDouble(cursor.getString(5)));
                not.setLongitud(Double.parseDouble(cursor.getString(6)));
                not.setProceso(cursor.getInt(7));
                not.setImei(cursor.getString(8));
                not.setRuta(cursor.getString(9));
                not.setEnviado(cursor.getInt(10));
                not.setVuelta(cursor.getInt(11));
                not.setOficio(cursor.getString(12));

                // Adding contact to list
                lista.add(not);
            } while (cursor.moveToNext());
        }
        // return contact list
        return lista;
    }

    public long UpdateCobranza(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.update(DbCentralContract.CobranzaEntry.TABLE_NAME,values,"idcobranza="+values.get(DbCentralContract.CobranzaEntry.IDCOBRANZA),null);
    }

    public long saveCenso(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                DbCentralContract.CensoEntry.TABLE_NAME_CENSO,
                null,
                values);
    }

    public long saveRepCamCenso(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                DbCentralContract.CensoEntry.TABLE_REPCAMCENSO,
                null,
                values);
    }

    public long saveFotografiasCenso(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                DbCentralContract.FotograciasCensoEntry.TABLE_NAME,
                null,
                values);
    }

    public List<CensoVO> getCenso() {

        List<CensoVO> lista = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_TCENSO ;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                CensoVO not = new CensoVO();
                not.setIdcenso(cursor.getLong(0));
                not.setCuenta(cursor.getString(1));
                not.setCalle(cursor.getString(2));
                not.setExterior(cursor.getString(3));
                not.setInterior(cursor.getString(4));
                not.setColonia(cursor.getString(5));
                not.setCodigopostal(cursor.getString(6));
                not.setUso(cursor.getInt(7));
                not.setViviendas(cursor.getInt(8));
                not.setLocales(cursor.getInt(9));
                not.setDiamtoma(cursor.getInt(10));
                not.setSeriemedidor(cursor.getString(11));
                not.setMarcamedidor(cursor.getString(12));
                not.setDiammedidor(cursor.getInt(13));
                not.setTomas(cursor.getInt(14));
                not.setIdrepcamcenso(cursor.getInt(15));
                /*not.setObservaciones(cursor.getString(15));
                not.setFecharegistro(cursor.getString(16));
                not.setHoraregistro(cursor.getString(17));
                not.setEjecutivo(cursor.getString(18));
                not.setLatitud(Double.parseDouble(cursor.getString(19)));
                not.setLongitud(Double.parseDouble(cursor.getString(20)));
                not.setProceso(cursor.getInt(21));
                not.setImei(cursor.getString(22));*/

                // Adding contact to list
                lista.add(not);
            } while (cursor.moveToNext());
        }
        // return contact list
        return lista;
    }

    public List<CensoVO> getReporteCampoCenso() {

        List<CensoVO> lista = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_TREPCAMCENSO ;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                CensoVO not = new CensoVO();
                not.setIdrepcamcenso(cursor.getInt(0));
                not.setCuenta(cursor.getString(1));
                not.setCalle(cursor.getString(2));
                not.setExterior(cursor.getString(3));
                not.setInterior(cursor.getString(4));
                not.setColonia(cursor.getString(5));
                not.setCodigopostal(cursor.getString(6));
                not.setUso(cursor.getInt(7));
                not.setViviendas(cursor.getInt(8));
                not.setLocales(cursor.getInt(9));
                not.setDiamtoma(cursor.getInt(10));
                not.setSeriemedidor(cursor.getString(11));
                not.setMarcamedidor(cursor.getString(12));
                not.setDiammedidor(cursor.getInt(13));
                not.setTomas(cursor.getInt(14));
                not.setFecharegistro(cursor.getString(15));
                not.setHoraregistro(cursor.getString(16));
                not.setEjecutivo(cursor.getString(17));
                not.setLatitud(Double.parseDouble(cursor.getString(18)));
                not.setLongitud(Double.parseDouble(cursor.getString(19)));
                not.setImei(cursor.getString(21));
                not.setProceso(cursor.getInt(20));
                not.setObservaciones(cursor.getString(22));

                // Adding contact to list
                lista.add(not);
            } while (cursor.moveToNext());
        }
        // return contact list
        return lista;
    }

    public List<FotografiasCensoVO> getFotografiasCenso() {

        List<FotografiasCensoVO> lista = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_TFOTOGRAFIASCENSO ;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                FotografiasCensoVO not = new FotografiasCensoVO();
                not.setIdfotografiacenso(cursor.getLong(0));
                not.setIdrepcamcenso(cursor.getLong(1));
                not.setFotogfrafia(cursor.getString(2));

                // Adding contact to list
                lista.add(not);
            } while (cursor.moveToNext());
        }
        // return contact list
        return lista;
    }

    public long saveOrdenServicio(ContentValues values) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(
                DbCentralContract.OrdenServicioEntry.TABLE_NAME,
                null,
                values);
    }

    public void deleteOrdenServicio(Long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TREPCAMORDENSERVICIO,"idordenservicio="+id,null);
        //db.execSQL("delete from " + TABLE_TREPCAMBOLETAS + " where idboleta='" + id + "'");
    }

    public List<OrdenServicioVO> getOrdenServicio() {

        List<OrdenServicioVO> lista = new ArrayList<OrdenServicioVO>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_TREPCAMORDENSERVICIO;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                OrdenServicioVO not = new OrdenServicioVO();
                not.setIdordenservicio(cursor.getLong(0));
                not.setManzana(cursor.getString(1));
                not.setFecharegistro(cursor.getString(2));
                not.setHoraregistro(cursor.getString(3));
                not.setEjecutivo(cursor.getString(4));
                not.setLatitud(Double.parseDouble(cursor.getString(5)));
                not.setLongitud(Double.parseDouble(cursor.getString(6)));
                not.setProceso(cursor.getInt(7));
                not.setBimestre(cursor.getString(8));
                not.setImei(cursor.getString(9));
                not.setOrdenservicio(cursor.getString(10));
                not.setIdclaveorden(cursor.getLong(11));
                not.setVuelta(cursor.getInt(12));

                // Adding contact to list
                lista.add(not);
            } while (cursor.moveToNext());
        }
        // return contact list
        return lista;
    }

}
