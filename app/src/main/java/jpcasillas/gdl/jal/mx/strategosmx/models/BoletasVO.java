package jpcasillas.gdl.jal.mx.strategosmx.models;

import android.content.ContentValues;

import java.io.Serializable;

import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralContract;
import jpcasillas.gdl.jal.mx.strategosmx.dao.DbCentralDBHelper;

public class BoletasVO implements Serializable{

    private long idboletas;
    private String manzana;
    private String fecharegistro;
    private String horaregistro;
    private String ejecutivo;
    private double latitud;
    private double longitud;
    private String imei;
    private int proceso = 100;
    private String bimestre;
    //private int enviado;

    public long getIdboletas() {
        return idboletas;
    }

    public void setIdboletas(long idboletas) {
        this.idboletas = idboletas;
    }

    //public int getEnviado() {
     //   return enviado;
    //}

   // public void setEnviado(int enviado) {
    //    this.enviado = enviado;
    //}

    public int getProceso() {
        return proceso;
    }

    public void setProceso(int proceso) {
        this.proceso = proceso;
    }

    public String getBimestre() {
        return bimestre;
    }

    public void setBimestre(String bimestre) {
        this.bimestre = bimestre;
    }

    public long getIdBoletas() {
        return idboletas;
    }

    public void setIdBoletas(long idboletas) {
        this.idboletas = idboletas;
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

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(DbCentralContract.BoletasEntry.ID, idboletas);
        values.put(DbCentralContract.BoletasEntry.MANZANA, manzana);
        values.put(DbCentralContract.BoletasEntry.FECHA_REGISTRO, fecharegistro);
        values.put(DbCentralContract.BoletasEntry.HORA_REGISTRO, horaregistro);
        values.put(DbCentralContract.BoletasEntry.EJECUTIVO, ejecutivo);
        values.put(DbCentralContract.BoletasEntry.LATITUD, latitud);
        values.put(DbCentralContract.BoletasEntry.LONGITUD, longitud);
        values.put(DbCentralContract.BoletasEntry.PROCESO, proceso);
        values.put(DbCentralContract.BoletasEntry.BIMESTRE, bimestre);
        values.put(DbCentralContract.BoletasEntry.IMEI, imei);
        //values.put(DbCentralContract.BoletasEntry.ENVIADO,enviado);
        return values;
    }

}
