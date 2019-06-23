package jpcasillas.gdl.jal.mx.strategosmx.dao;

import android.provider.BaseColumns;

public class NotificacionesContract {


    public static abstract class NotificacionEntry implements BaseColumns {
        public static final String TABLE_NAME = "notificaciones";
        public static final String TABLE_NAME_EJE = "ejecutivo";

        public static final String ID = "id";
        public static final String MANZANA = "manzana";
        public static final String FECHA_REGISTRO = "fechaRegistro";
        public static final String HORA_REGISTRO = "horaRegistro";
        public static final String EJECUTIVO = "ejecutivo";
        public static final String LATITUD = "latituc";
        public static final String LONGITUD = "longitud";
        public static final String CONSECUTIVO = "consecutivo";
        public static final String MEDIDOR = "medidor";
        public static final String RUTA = "ruta";
        public static final String PROCESO = "proceso";
        public static final String LECTURA = "lectura";
        public static final String BIMESTRE = "bimestre";
    }
}