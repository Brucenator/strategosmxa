package jpcasillas.gdl.jal.mx.strategosmx.dao;

import android.provider.BaseColumns;

public class DbCentralContract {

    public static abstract class BoletasEntry implements BaseColumns {
        public static final String TABLE_NAME = "trepcamboletas";

        public static final String ID = "idboleta";
        public static final String MANZANA = "manzana";
        public static final String FECHA_REGISTRO = "fechaRegistro";
        public static final String HORA_REGISTRO = "horaRegistro";
        public static final String EJECUTIVO = "ejecutivo";
        public static final String LATITUD = "latituc";
        public static final String LONGITUD = "longitud";
        public static final String PROCESO = "proceso";
        public static final String BIMESTRE = "bimestre";
        public static final String IMEI = "imei";
        public static final String ENVIADO = "enviado";
    }

    public static abstract class LecturasEntry implements BaseColumns {
        public static final String TABLE_NAME = "trepcamlecturas";

        public static final String IDLECTURAS = "idlecturas";
        public static final String MANZANA = "manzana";
        public static final String BIMESTRE = "bimestre";
        public static final String FECHA_REGISTRO = "fechaRegistro";
        public static final String HORA_REGISTRO = "horaRegistro";
        public static final String EJECUTIVO = "ejecutivo";
        public static final String LATITUD = "latituc";
        public static final String LONGITUD = "longitud";
        public static final String IMEI = "imei";
        public static final String PROCESO = "proceso";
        public static final String MEDIDOR = "medidor";
        public static final String LECTURA = "lectura";
        public static final String RUTA = "ruta";
        public static final String ENVIADO = "enviado";
        public static final String NOTASLECTURA = "idnotaslectura";
    }

    public static abstract class EjecutivosEntry implements BaseColumns {
        public static final String TABLE_NAME_EJE = "tejecutivos";

        public static final String ID = "idejecutivo";
        public static final String EJECUTIVO = "ejecutivo";
        public static final String CONSECUTIVO = "consecutivo";

    }

    public static abstract class CobranzaEntry implements BaseColumns {
        public static final String TABLE_NAME = "trepcamcobranza";

        public static final String IDCOBRANZA = "idcobranza";
        public static final String MANZANA = "manzana";
        public static final String FECHA_REGISTRO = "fechaRegistro";
        public static final String HORA_REGISTRO = "horaRegistro";
        public static final String EJECUTIVO = "ejecutivo";
        public static final String LATITUD = "latituc";
        public static final String LONGITUD = "longitud";
        public static final String PROCESO = "proceso";
        public static final String RUTA = "ruta";
        public static final String IMEI = "imei";
        public static final String OFICIO = "oficio";
        public static final String ENVIADO = "enviado";
        public static final String VUELTA = "vuelta";
    }

    public static abstract class CensoEntry implements BaseColumns {
        public static final String TABLE_NAME_CENSO = "tcenso";
        public static final String TABLE_REPCAMCENSO = "trepcamcenso";

        public static final String IDCENSO = "idcenso";
        public static final String CUENTA = "cuenta";
        public static final String CALLE = "calle";
        public static final String EXTERIOR = "exterior";
        public static final String INTERIOR = "interior";
        public static final String COLONIA = "colonia";
        public static final String CODIGOPOSTAL = "codigopostal";
        public static final String USO = "uso";
        public static final String VIVIENDAS = "viviendas";
        public static final String LOCALES = "locales";
        public static final String DIAMTOMA = "diamtoma";
        public static final String SERIEMEDIDOR = "seriemedidor";
        public static final String MARCAMEDIDOR = "marcamedidor";
        public static final String DIAMMEDIDOR = "diammedidor";
        public static final String TOMAS = "tomas";
        public static final String FECHA_REGISTRO = "fechaRegistro";
        public static final String HORA_REGISTRO = "horaRegistro";
        public static final String EJECUTIVO = "ejecutivo";
        public static final String LATITUD = "latituc";
        public static final String LONGITUD = "longitud";
        public static final String IMEI = "imei";
        public static final String PROCESO = "proceso";
        public static final String OBSERVACIONES = "observaciones";
        public static final String IDREPCAMCENSO = "idrepcamcenso";
    }

    public static abstract class FotograciasCensoEntry implements BaseColumns {
        public static final String TABLE_NAME = "tfotografiascenso";

        public static final String IDFOTOGRAFIACENSO = "idfotografiacnenso";
        public static final String IDREPCAMCENSO = "idrepcamcenso";
        public static final String FOTOGRAFIA = "fotografia";
    }

    public static abstract class OrdenServicioEntry implements BaseColumns {
        public static final String TABLE_NAME = "trepcamordenservicio";

        public static final String ID = "idordenservicio";
        public static final String MANZANA = "manzana";
        public static final String FECHA_REGISTRO = "fechaRegistro";
        public static final String HORA_REGISTRO = "horaRegistro";
        public static final String EJECUTIVO = "ejecutivo";
        public static final String LATITUD = "latituc";
        public static final String LONGITUD = "longitud";
        public static final String PROCESO = "proceso";
        public static final String BIMESTRE = "bimestre";
        public static final String IMEI = "imei";
        public static final String ORDENSERVICIO = "ordenservicio";
        public static final String VUELTA = "vuelta";
        public static final String IDCLAVEORDEN = "idclaveorden";
    }
}
