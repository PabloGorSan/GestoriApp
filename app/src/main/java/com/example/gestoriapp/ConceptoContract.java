package com.example.gestoriapp;

import android.provider.BaseColumns;

//Siguiendo las recomendaciones vistas en clase se crea un ConceptoContract y ConceptoEntry para
// su uso posterior en el DBHelper
public final class ConceptoContract {

    private ConceptoContract(){ }

    public static abstract class ConceptoEntry implements BaseColumns {
        public static final String TABLE_NAME = "Concepto";
        public static final String COLUMN_NAME_NOMBRE = "nombre";
        public static final String COLUMN_NAME_IDESTABLECIMIENTO = "idestablecimiento";
    }

}
