package jpcasillas.gdl.jal.mx.strategosmx.models;

import java.io.Serializable;

public class ClavesOrdenServicioVO implements Serializable {

    private Long idorden;
    private String clave;
    private String descripcion;
    private String tipo;
    private String concepto;

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Long getIdorden() {
        return idorden;
    }

    public void setIdorden(Long idorden) {
        this.idorden = idorden;
    }
}
