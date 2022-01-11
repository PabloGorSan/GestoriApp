package com.example.gestoriapp;

import android.provider.BaseColumns;

//Siguiendo las recomendaciones vistas en clase se crea un GastoIngresoContract y GastoIngresoEntry para
// su uso posterior en el DBHelper
public final class GastoIngresoContract {
    private GastoIngresoContract(){ }

    public static abstract class GastoIngresoEntry implements BaseColumns{
        public static final String TABLE_NAME = "GastoIngreso";
        public static final String COLUMN_NAME_IMPORTE = "importe";
        public static final String COLUMN_NAME_FECHA = "fecha";
        public static final String COLUMN_NAME_DESCRIPCION = "descripcion";
        public static final String COLUMN_NAME_CONCEPTO = "concepto";
        public static final String COLUMN_NAME_REGISTRO = "fechaRegistroApp";
        public static final String COLUMN_NAME_IDESTABLECIMIENTO = "idEstablecimiento";
    }

}
