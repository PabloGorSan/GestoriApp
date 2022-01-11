package com.example.gestoriapp;

import android.provider.BaseColumns;

//Siguiendo las recomendaciones vistas en clase se crea un EstablecimientoContract y EstablecimientoEntry
// para su uso posterior en el DBHelper
public final class EstablecimientoContract {
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
