package jpcasillas.gdl.jal.mx.strategosmx.models;

import android.content.ContentValues;

import java.io.Serializable;

import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralContract;

public class CobranzaVO implements Serializable {

    private long idcobranza;
    private String manzana;
    private String fecharegistro;
    private String horaregistro;
    private String ejecutivo;
    private double latitud;
    private double longitud;
    private String imei;
    private int proceso = 102;
    private String oficio;
    private String ruta;
    private int vuelta;
    private int enviado;

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public int getProceso() {
        return proceso;
    }

    public void setProceso(int proceso) {
        this.proceso = proceso;
    }

    public String getOficio() {
        return oficio;
    }

    public void setOficio(String oficio) {
        this.oficio = oficio;
    }

    public long getIdCobranza() {
        return idcobranza;
    }

    public void setIdCobranza(long idcobranza) {
        this.idcobranza = idcobranza;
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

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public int getVuelta() {
        return vuelta;
    }

    public void setVuelta(int vuelta) {
        this.vuelta = vuelta;
    }

    public int getEnviado() {
        return enviado;
    }

    public void setEnviado(int enviado) {
        this.enviado = enviado;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DbCentralContract.CobranzaEntry.IDCOBRANZA, idcobranza);
        values.put(DbCentralContract.CobranzaEntry.MANZANA, manzana);
        values.put(DbCentralContract.CobranzaEntry.FECHA_REGISTRO, fecharegistro);
        values.put(DbCentralContract.CobranzaEntry.HORA_REGISTRO, horaregistro);
        values.put(DbCentralContract.CobranzaEntry.EJECUTIVO, ejecutivo);
        values.put(DbCentralContract.CobranzaEntry.LATITUD, latitud);
        values.put(DbCentralContract.CobranzaEntry.LONGITUD, longitud);
        values.put(DbCentralContract.CobranzaEntry.PROCESO, proceso);
        values.put(DbCentralContract.CobranzaEntry.RUTA, ruta);
        values.put(DbCentralContract.CobranzaEntry.IMEI, imei);
        values.put(DbCentralContract.CobranzaEntry.OFICIO, oficio);
        values.put(DbCentralContract.CobranzaEntry.VUELTA,vuelta);
        return values;
    }

}
