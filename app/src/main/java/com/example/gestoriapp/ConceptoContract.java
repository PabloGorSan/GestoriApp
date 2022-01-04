package com.example.gestoriapp;

import android.provider.BaseColumns;

public class ConceptoContract {

    private ConceptoContract(){ }

    public static abstract class ConceptoEntry implements BaseColumns {
        public static final String TABLE_NAME = "Concepto";
        public static final String COLUMN_NAME_NOMBRE = "nombre";
        public static final String COLUMN_NAME_IDESTABLECIMIENTO = "idestablecimiento";
    }

}
