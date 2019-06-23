package jpcasillas.gdl.jal.mx.strategosmx.models;

import java.io.Serializable;

public class CensoVO implements Serializable {

    private Long idcenso;
    private String cuenta;
    private String calle;
    private String exterior;
    private String interior;
    private String colonia;
    private String codigopostal;
    private int uso;
    private int viviendas;
    private int locales;
    private int diamtoma;
    private String seriemedidor;
    private String marcamedidor;
    private int diammedidor;
    private int tomas;
    private String observaciones;
    private String fecharegistro;
    private String horaregistro;
    private String ejecutivo;
    private double latitud;
    private double longitud;
    private String imei;
    private int proceso = 103;
    private int idrepcamcenso;

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public Long getIdcenso() {
        return idcenso;
    }

    public void setIdcenso(Long idcenso) {
        this.idcenso = idcenso;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getExterior() {
        return exterior;
    }

    public void setExterior(String exterior) {
        this.exterior = exterior;
    }

    public String getInterior() {
        return interior;
    }

    public void setInterior(String interior) {
        this.interior = interior;
    }

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public String getCodigopostal() {
        return codigopostal;
    }

    public void setCodigopostal(String codigopostal) {
        this.codigopostal = codigopostal;
    }

    public int getUso() {
        return uso;
    }

    public void setUso(int uso) {
        this.uso = uso;
    }

    public int getViviendas() {
        return viviendas;
    }

    public void setViviendas(int viviendas) {
        this.viviendas = viviendas;
    }

    public int getLocales() {
        return locales;
    }

    public void setLocales(int locales) {
        this.locales = locales;
    }

    public int getDiamtoma() {
        return diamtoma;
    }

    public void setDiamtoma(int diamtoma) {
        this.diamtoma = diamtoma;
    }

    public String getSeriemedidor() {
        return seriemedidor;
    }

    public void setSeriemedidor(String seriemedidor) {
        this.seriemedidor = seriemedidor;
    }

    public String getMarcamedidor() {
        return marcamedidor;
    }

    public void setMarcamedidor(String marcamedidor) {
        this.marcamedidor = marcamedidor;
    }

    public int getDiammedidor() {
        return diammedidor;
    }

    public void setDiammedidor(int diammedidor) {
        this.diammedidor = diammedidor;
    }

    public int getTomas() {
        return tomas;
    }

    public void setTomas(int tomas) {
        this.tomas = tomas;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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

    public int getIdrepcamcenso() {
        return idrepcamcenso;
    }

    public void setIdrepcamcenso(int idrepcamcenso) {
        this.idrepcamcenso = idrepcamcenso;
    }
}
