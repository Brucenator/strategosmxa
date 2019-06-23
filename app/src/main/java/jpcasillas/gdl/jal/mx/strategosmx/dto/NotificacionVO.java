package jpcasillas.gdl.jal.mx.strategosmx.dto;

import android.content.ContentValues;


import java.io.Serializable;

import jpcasillas.gdl.jal.mx.strategosmx.dao.NotificacionesContract.NotificacionEntry;

public class NotificacionVO implements Serializable {

    private long id;
    private String manzana;
    private String fecharegistro;
    private String horaregistro;
    private long ejecutivo;
    private double latitud;
    private double longitud;
    private String imei;
    private int uso;
    private String medidor;
    private int proceso;
    private Long lectura;

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

    public long getEjecutivo() {
        return ejecutivo;
    }

    public void setEjecutivo(long ejecutivo) {
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
        values.put(NotificacionEntry.ID, id);
        values.put(NotificacionEntry.MANZANA, manzana);
        values.put(NotificacionEntry.FECHA_REGISTRO, fecharegistro);
        values.put(NotificacionEntry.HORA_REGISTRO, horaregistro);
        values.put(NotificacionEntry.EJECUTIVO, ejecutivo);
        values.put(NotificacionEntry.LATITUD, latitud);
        values.put(NotificacionEntry.LONGITUD, longitud);
        values.put(NotificacionEntry.MEDIDOR, medidor);
        values.put(NotificacionEntry.PROCESO, proceso);
        values.put(NotificacionEntry.LECTURA, lectura);
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

}
