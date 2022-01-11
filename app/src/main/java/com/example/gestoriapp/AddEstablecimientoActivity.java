package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_establecimiento);

        //inicializar diccionario
        initDict();
        conceptos = new ArrayList<>();

        //Cogemos los inputs de la interfaz
        textInputEditTextNombre = (TextInputEditText) findViewById(R.id.textInputEditTextNombre);
        textInputEditTextCiudad = (TextInputEditText) findViewById(R.id.textInputEditTextCiudad);
        textInputEditTextCalle = (TextInputEditText) findViewById(R.id.textInputEditTextCalle);
        textInputEditTextCodigoPostal = (TextInputEditText) findViewById(R.id.textInputEditTextCodigoPostal);
        textInputEditTextConceptos = (TextInputEditText) findViewById(R.id.textInputEditTextConceptos);

        // Pillamos los resources para usar los strings definidos en los xmls de strings.xml
        res = getResources();

    }

    private void initDict () {
        //Sabemos que este mapa no va a ser null
        map = (SortedMap<String,Object>) SingletonMap.getInstance().get(MainActivity.SHARED_DATA_KEY);
    }

    // Funcion utilizada para añadir un concepto a la lista de conceptos de un establecimiento
    public void onClickAddConcepto(View view){

        String concepto = textInputEditTextConceptos.getText().toString();

        //Los conceptos de gasto deben ser distintos de "" para ser válidos y añadidos a la lista
        // Enseñamos un toast de confirmacion y negacion en sus respectivos casos
        if(!concepto.equals("")){
            Toast.makeText(AddEstablecimientoActivity.this, res.getString(R.string.addConceptoSuccess) , Toast.LENGTH_LONG).show();
            conceptos.add(concepto);
            textInputEditTextConceptos.setText("");
        }else{
            Toast.makeText(AddEstablecimientoActivity.this, res.getString(R.string.addConceptoFailure), Toast.LENGTH_LONG).show();
        }

    }

    // Se crea un establecimiento con los datos del formulario y se inserta en el mapa y la base de datos
    // pero primero se hacen comprobaciones sobre la validez de los datos.
    // En caso de error se muestran dialogs informando de lo que pasa.
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
                crearDialog(res.getString(R.string.titleAddEstablecimientoSinConcepto),res.getString(R.string.bodyAddEstablecimientoSinConcepto)).show();
            }
        }else{
            crearDialog(res.getString(R.string.titleAddEstablecimientoCamposVacios),res.getString(R.string.bodyAddEstablecimientoCamposVacios)).show();
        }

    }

    // Crea un dialog con un titulo y cuerpo
    private Dialog crearDialog(String titulo, String cuerpo){
        AlertDialog.Builder builder = new AlertDialog.Builder(AddEstablecimientoActivity.this);
        builder.setTitle(titulo);

        builder.setMessage(cuerpo).setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });

        return builder.create();
    }

    // Mete un establecimiento en la base de datos
    private void insertarEstablecimientoEnlaBd(SQLiteDatabase db, String nombre, String ciudad, String calle, String cp, long registro){
        ContentValues values = new ContentValues();
        values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_NOMBRE, nombre);
        values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CIUDAD, ciudad);
        values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CALLE, calle);
        values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CP, cp);
        values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_REGISTRO, registro);

        db.insert(EstablecimientoContract.EstablecimientoEntry.TABLE_NAME, null, values);
    }

    // Mete un concepto en la base de datos
    private void insertarConceptoEnlaBd(SQLiteDatabase db, List<String> conceptos, long registro) {
        for(String concepto : conceptos){
            ContentValues values = new ContentValues();
            values.put(ConceptoContract.ConceptoEntry.COLUMN_NAME_NOMBRE, concepto);
            values.put(ConceptoContract.ConceptoEntry.COLUMN_NAME_IDESTABLECIMIENTO, registro);

            db.insert(ConceptoContract.ConceptoEntry.TABLE_NAME, null, values);
        }

    }

}