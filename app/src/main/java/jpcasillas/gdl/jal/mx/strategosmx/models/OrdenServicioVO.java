package jpcasillas.gdl.jal.mx.strategosmx.models;

import java.io.Serializable;

public class OrdenServicioVO implements Serializable {

    private long idordenservicio;
    private String manzana;
    private String fecharegistro;
    private String horaregistro;
    private String ejecutivo;
    private double latitud;
    private double longitud;
    private String imei;
    private int proceso = 104;
    private String bimestre;
    private String ordenservicio;
    private int vuelta;
    private long idclaveorden;

    public long getIdordenservicio() {
        return idordenservicio;
    }

    public void setIdordenservicio(long idordenservicio) {
        this.idordenservicio = idordenservicio;
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

    public String getOrdenservicio() {
        return ordenservicio;
    }

    public void setOrdenservicio(String ordenservicio) {
        this.ordenservicio = ordenservicio;
    }

    public int getVuelta() {
        return vuelta;
    }

    public void setVuelta(int vuelta) {
        this.vuelta = vuelta;
    }

    public long getIdclaveorden() {
        return idclaveorden;
    }

    public void setIdclaveorden(long idclaveorden) {
        this.idclaveorden = idclaveorden;
    }
}
