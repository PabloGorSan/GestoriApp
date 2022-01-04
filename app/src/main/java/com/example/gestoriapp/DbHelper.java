package com.example.gestoriapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES_GastoIngreso =
            "CREATE TABLE " + GastoIngresoContract.GastoIngresoEntry.TABLE_NAME + " (" + GastoIngresoContract.GastoIngresoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_CONCEPTO + " TEXT," +
                    GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_DESCRIPCION + " TEXT," +
                    GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_FECHA + " TEXT," +
                    GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IMPORTE + " REAL," +
                    GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_REGISTRO + " INTEGER UNIQUE," +
                    GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IDESTABLECIMIENTO + " INTEGER," +
                    " FOREIGN KEY (" + GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IDESTABLECIMIENTO + ") REFERENCES " +
                    EstablecimientoContract.EstablecimientoEntry.TABLE_NAME + "("+ EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_REGISTRO + "))";


    private static final String SQL_CREATE_ENTRIES_Establecimiento =
            "CREATE TABLE " + EstablecimientoContract.EstablecimientoEntry.TABLE_NAME + " (" + EstablecimientoContract.EstablecimientoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_NOMBRE + " TEXT," +
                    EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CIUDAD + " TEXT," +
                    EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CALLE + " TEXT," +
                    EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CP + " TEXT," +
                    EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_REGISTRO + " INTEGER UNIQUE)";


    private static final String SQL_CREATE_ENTRIES_Concepto =
            "CREATE TABLE " + ConceptoContract.ConceptoEntry.TABLE_NAME + " (" + ConceptoContract.ConceptoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ConceptoContract.ConceptoEntry.COLUMN_NAME_NOMBRE + " TEXT," +
                    ConceptoContract.ConceptoEntry.COLUMN_NAME_IDESTABLECIMIENTO + " INTEGER," +
                    " FOREIGN KEY (" + ConceptoContract.ConceptoEntry.COLUMN_NAME_IDESTABLECIMIENTO + ") REFERENCES " +
                    EstablecimientoContract.EstablecimientoEntry.TABLE_NAME + "("+ EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_REGISTRO + "))";


    private static final String SQL_DELETE_ENTRIES_GastoIngreso =
            "DROP TABLE IF EXISTS " + GastoIngresoContract.GastoIngresoEntry.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES_Establecimiento =
            "DROP TABLE IF EXISTS " + EstablecimientoContract.EstablecimientoEntry.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES_Concepto =
            "DROP TABLE IF EXISTS " + ConceptoContract.ConceptoEntry.TABLE_NAME;

    public DbHelper(Context context, String dbName){
        super(context, dbName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_GastoIngreso);
        db.execSQL(SQL_CREATE_ENTRIES_Establecimiento);
        db.execSQL(SQL_CREATE_ENTRIES_Concepto);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES_GastoIngreso);
        db.execSQL(SQL_DELETE_ENTRIES_Establecimiento);
        db.execSQL(SQL_DELETE_ENTRIES_Concepto);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DELETE_ENTRIES_GastoIngreso);
        db.execSQL(SQL_DELETE_ENTRIES_Establecimiento);
        db.execSQL(SQL_DELETE_ENTRIES_Concepto);
        onCreate(db);
    }
}
