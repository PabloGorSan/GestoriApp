package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

public class AddEstablecimientoActivity extends AppCompatActivity {

    private SortedMap<String, Object> map;
    private TextInputEditText textInputEditTextNombre;
    private TextInputEditText textInputEditTextCiudad;
    private TextInputEditText textInputEditTextCalle;
    private TextInputEditText textInputEditTextCodigoPostal;
    private TextInputEditText textInputEditTextConceptos;

    List<String> conceptos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_establecimiento);

        initDict();
        conceptos = new ArrayList<>();

        textInputEditTextNombre = (TextInputEditText) findViewById(R.id.textInputEditTextNombre);
        textInputEditTextCiudad = (TextInputEditText) findViewById(R.id.textInputEditTextCiudad);
        textInputEditTextCalle = (TextInputEditText) findViewById(R.id.textInputEditTextCalle);
        textInputEditTextCodigoPostal = (TextInputEditText) findViewById(R.id.textInputEditTextCodigoPostal);
        textInputEditTextConceptos = (TextInputEditText) findViewById(R.id.textInputEditTextConceptos);

    }

    private void initDict () {
        map = (SortedMap<String,Object>) SingletonMap.getInstance().get(MainActivity.SHARED_DATA_KEY);
    }

    public void onClickAddConcepto(View view){

        String concepto = textInputEditTextConceptos.getText().toString();

        //Los conceptos de gasto deben ser distintos de "" para ser válidos y añadidos a la lista
        if(!concepto.equals("")){
            Toast.makeText(AddEstablecimientoActivity.this, "El concepto ha sido añadido", Toast.LENGTH_LONG).show();
            conceptos.add(concepto);

            textInputEditTextConceptos.setText("");
        }else{
            Toast.makeText(AddEstablecimientoActivity.this, "El concepto no puede ser vacío", Toast.LENGTH_LONG).show();
        }

    }

    public void onClickAddEstablecimiento(View view){
        Establecimiento establecimiento;
        String nombre = textInputEditTextNombre.getText().toString();
        String ciudad = textInputEditTextCiudad.getText().toString();
        String calle = textInputEditTextCalle.getText().toString();
        String codigoPostal = textInputEditTextCodigoPostal.getText().toString();

        if(!nombre.equals("") && !ciudad.equals("") && !calle.equals("") && !codigoPostal.equals("")){
            if(conceptos.size() > 0){
                long fechaRegistroApp = new Date().getTime();
                establecimiento = new Establecimiento(nombre, ciudad, calle, codigoPostal, conceptos, fechaRegistroApp);
                map.put("E" + fechaRegistroApp, establecimiento);
                insertarEstablecimientoEnlaBd((SQLiteDatabase) map.get("db"), establecimiento.getNombre(), establecimiento.getCiudad(), establecimiento.getCalle(), establecimiento.getCodigoPostal(), establecimiento.getFechaRegistroApp());
                insertarConceptoEnlaBd((SQLiteDatabase) map.get("db"), conceptos, establecimiento.getFechaRegistroApp());

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }else{
                crearDialog("Faltan conceptos","No se ha añadido ningún concepto a la lista de conceptos de gasto. Por favor, añada alguno.").show();
            }
        }else{
            crearDialog("Campos vacíos","Rellene todos los campos, por favor.").show();
        }

    }

    public Dialog crearDialog(String titulo, String cuerpo){
        AlertDialog.Builder builder = new AlertDialog.Builder(AddEstablecimientoActivity.this);
        builder.setTitle(titulo);

        builder.setMessage(cuerpo).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });

        return builder.create();
    }

    private void insertarEstablecimientoEnlaBd(SQLiteDatabase db, String nombre, String ciudad, String calle, String cp, long registro){
        ContentValues values = new ContentValues();
        values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_NOMBRE, nombre);
        values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CIUDAD, ciudad);
        values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CALLE, calle);
        values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CP, cp);
        values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_REGISTRO, registro);

        db.insert(EstablecimientoContract.EstablecimientoEntry.TABLE_NAME, null, values);
    }

    private void insertarConceptoEnlaBd(SQLiteDatabase db, List<String> conceptos, long registro) {
        for(String concepto : conceptos){
            ContentValues values = new ContentValues();
            values.put(ConceptoContract.ConceptoEntry.COLUMN_NAME_NOMBRE, concepto);
            values.put(ConceptoContract.ConceptoEntry.COLUMN_NAME_IDESTABLECIMIENTO, registro);

            db.insert(ConceptoContract.ConceptoEntry.TABLE_NAME, null, values);
        }

    }

}