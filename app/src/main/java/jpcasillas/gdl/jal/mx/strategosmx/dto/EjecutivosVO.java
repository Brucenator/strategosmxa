package jpcasillas.gdl.jal.mx.strategosmx.dto;

import android.content.ContentValues;

import java.io.Serializable;

import jpcasillas.gdl.jal.mx.strategosmx.dao.NotificacionesContract;

public class EjecutivosVO implements Serializable {

    private long id;
    private String ejecutivo;
    private long consecutivo;

    public EjecutivosVO(long id, String ejecutivo, long consecutivo) {
        this.id = id;
        this.ejecutivo = ejecutivo;
        this.consecutivo = consecutivo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEjecutivo() {
        return ejecutivo;
    }

    public void setEjecutivo(String ejecutivo) {
        this.ejecutivo = ejecutivo;
    }

    public long getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(long consecutivo) {
        this.consecutivo = consecutivo;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(NotificacionesContract.NotificacionEntry.ID, id);
        values.put(NotificacionesContract.NotificacionEntry.EJECUTIVO, ejecutivo);
        values.put(NotificacionesContract.NotificacionEntry.CONSECUTIVO, consecutivo);
        return values;
    }
}
