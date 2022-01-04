package com.example.gestoriapp;

import android.provider.BaseColumns;

public class EstablecimientoContract {
    private EstablecimientoContract(){ }

    public static abstract class EstablecimientoEntry implements BaseColumns {
        public static final String TABLE_NAME = "Establecimiento";
        public static final String COLUMN_NAME_NOMBRE = "nombre";
        public static final String COLUMN_NAME_CIUDAD = "ciudad";
        public static final String COLUMN_NAME_CALLE = "calle";
        public static final String COLUMN_NAME_CP = "codigopostal";
        public static final String COLUMN_NAME_REGISTRO = "fechaRegistroApp";
    }
}
