package jpcasillas.gdl.jal.mx.strategosmx.models;

import android.content.ContentValues;

import java.io.Serializable;

import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralContract;
import jpcasillas.gdl.jal.mx.strategosmx.dao.NotificacionesContract;

public class LecturasVO implements Serializable {

    private long id;
    private String manzana;
    private String fecharegistro;
    private String horaregistro;
    private String ejecutivo;
    private double latitud;
    private double longitud;
    private String imei;
    private int uso;
    private String medidor;
    private int proceso = 101;
    private Long lectura;
    private String ruta;
    private String bimestre;
    private Long idnotalectura;

    public Long getIdnotalectura() {
        return idnotalectura;
    }

    public void setIdnotalectura(Long idnotalectura) {
        this.idnotalectura = idnotalectura;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getManzana() {
        return manzana;
    }

    public void setManzana(String manzana) {
        this.manzana = manzana;
    }

    public String getFecharegistro() {
        return fecharegistro;
    }

    public void setFecharegistro(String fecharegistro) {
        this.fecharegistro = fecharegistro;
    }

    public String getHoraregistro() {
        return horaregistro;
    }

    public void setHoraregistro(String horaregistro) {
        this.horaregistro = horaregistro;
    }

    public String getEjecutivo() {
        return ejecutivo;
    }

    public void setEjecutivo(String ejecutivo) {
        this.ejecutivo = ejecutivo;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DbCentralContract.LecturasEntry.IDLECTURAS, id);
        values.put(DbCentralContract.LecturasEntry.MANZANA, manzana);
        values.put(DbCentralContract.LecturasEntry.FECHA_REGISTRO, fecharegistro);
        values.put(DbCentralContract.LecturasEntry.HORA_REGISTRO, horaregistro);
        values.put(DbCentralContract.LecturasEntry.EJECUTIVO, ejecutivo);
        values.put(DbCentralContract.LecturasEntry.LATITUD, latitud);
        values.put(DbCentralContract.LecturasEntry.LONGITUD, longitud);
        values.put(DbCentralContract.LecturasEntry.MEDIDOR, medidor);
        values.put(DbCentralContract.LecturasEntry.PROCESO, proceso);
        values.put(DbCentralContract.LecturasEntry.LECTURA, lectura);
        values.put(DbCentralContract.LecturasEntry.RUTA, ruta);
        values.put(DbCentralContract.LecturasEntry.BIMESTRE, bimestre);
        values.put(DbCentralContract.LecturasEntry.IMEI, imei);
        return values;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public int getUso() {
        return uso;
    }

    public void setUso(int uso) {
        this.uso = uso;
    }

    public String getMedidor() {
        return medidor;
    }

    public void setMedidor(String medidor) {
        this.medidor = medidor;
    }

    public int getProceso() {
        return proceso;
    }

    public void setProceso(int proceso) {
        this.proceso = proceso;
    }

    public Long getLectura() {
        return lectura;
    }

    public void setLectura(Long lectura) {
        this.lectura = lectura;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getBimestre() {
        return bimestre;
    }

    public void setBimestre(String bimestre) {
        this.bimestre = bimestre;
    }

}
