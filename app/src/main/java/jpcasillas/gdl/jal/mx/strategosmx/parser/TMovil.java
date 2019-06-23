package jpcasillas.gdl.jal.mx.strategosmx.parser;

public class TMovil {

    private Long idmovil;
    private String imei;
    private String numero;
    private int habilitado;


    public Long getIdmovil() {
        return idmovil;
    }

    public void setIdmovil(Long idmovil) {
        this.idmovil = idmovil;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(int habilitado) {
        this.habilitado = habilitado;
    }
}
