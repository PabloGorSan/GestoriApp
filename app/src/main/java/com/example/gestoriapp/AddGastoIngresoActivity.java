package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.SortedMap;

public class AddGastoIngresoActivity extends AppCompatActivity {

    private Establecimiento establecimiento;
    private TextView textFecha;
    private TextView spinnerLabel;
    private SortedMap<String, Object> map;
    private TextInputEditText textInputEditTextImporte;
    private TextInputEditText textInputEditTextDescripcion;

    private RadioButton radioButtonGasto, radioButtonIngreso;
    private Boolean esIngreso = null;
    private String fechaCalendario="";
    private String concepto;

    private DatePickerDialog datePickerDialog;

    private Spinner spinner;
    private ArrayAdapter<String> adapterConcepto;

    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gasto_ingreso);

        //Cogemos los elementos de la interfaz
        textFecha = (TextView) findViewById(R.id.textViewFecha);
        radioButtonGasto = (RadioButton) findViewById(R.id.radioButtonGasto);
        radioButtonIngreso = (RadioButton) findViewById(R.id.radioButtonIngreso);
        textInputEditTextImporte = (TextInputEditText) findViewById(R.id.textInputEditTextImporte);
        textInputEditTextDescripcion = (TextInputEditText) findViewById(R.id.textInputEditTextDescripcion);

        spinner = (Spinner) findViewById(R.id.spinnerId);
        spinnerLabel = (TextView) findViewById(R.id.spinnerLabel);

        // Pillamos los resources para usar los strings definidos en los xmls de strings.xml
        res = getResources();

        //inicializar diccionario
        initDict();
        //Cogemos el establecimiento seleccionado
        establecimiento = (Establecimiento) map.get("ESTABLECIMIENTO_SELECCIONADO");

        // Utilizamos un ArrayAdapter al que le pasamos la lista de conceptos del establecimiento seleccionado
        ArrayAdapter<String> adp = new ArrayAdapter<String> (this,android.R.layout.simple_spinner_dropdown_item,establecimiento.getConceptos());
        spinner.setAdapter(adp);

        //Definimos el comportamiento de la selecci??n de un elemento
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
                concepto = parent.getItemAtPosition(position).toString();
            }

            public void onNothingSelected(AdapterView<?> arg0)
            {
                // No es necesario implementar nada aqui
            }
        });

    }



    private void initDict () {
        //Sabemos que este map no a ver null
        map = (SortedMap<String,Object>) SingletonMap.getInstance().get(MainActivity.SHARED_DATA_KEY);
    }

    // Se crea un Gasto/Ingreso con los datos del formulario y se inserta en el mapa y la base de datos
    // pero primero se hacen comprobaciones sobre la validez de los datos.
    // En caso de error se muestran dialogs o toasts informando de lo que pasa.
    public void onClickAddGastoIngreso(View view) throws ParseException {
        String importeString = textInputEditTextImporte.getText().toString();
        String descripcion = textInputEditTextDescripcion.getText().toString();
        long fechaRegistroApp = new Date().getTime();


        if(!importeString.equals("") && !fechaCalendario.equals("")){
            Double importe = new Double(importeString);
            if(esIngreso!=null){
                if(importe>0){
                    if(esIngreso){
                        //Aqui no es necesario comprobar el campo concepto ya que este se inicializa a "Ingreso" cuando se trata de un Ingreso
                        GastoIngreso ingreso = new GastoIngreso(importe, fechaCalendario, descripcion, "Ingreso",fechaRegistroApp);
                        map.put("I" + fechaRegistroApp, ingreso);
                        establecimiento.addIngreso(ingreso);
                        insertarGastoIngresoEnlaBd((SQLiteDatabase) map.get("db") ,ingreso.getConcepto(), ingreso.getDescripcion(), fechaCalendario, ingreso.getImporte(), ingreso.getFechaRegistroApp());
                        goToEstablecimientoActivity();
                    }else{
                        //En este else sabemos que la transaccion es un Gasto y no un Ingreso, por lo que comprobamos que el concepto
                        // del Gasto sea distinto de nulo y que tampoco sea una cadena vacia
                        if(concepto != null && !concepto.equals("")){
                            GastoIngreso gasto = new GastoIngreso(-importe, fechaCalendario, descripcion, concepto,fechaRegistroApp);
                            map.put("G" + fechaRegistroApp, gasto);
                            establecimiento.addGasto(gasto);
                            insertarGastoIngresoEnlaBd((SQLiteDatabase) map.get("db") ,gasto.getConcepto(), gasto.getDescripcion(), fechaCalendario, gasto.getImporte(), gasto.getFechaRegistroApp());
                            goToEstablecimientoActivity();
                        }else{
                            crearDialog(res.getString(R.string.titleAddGastoConceptoVacio),res.getString(R.string.bodyAddGastoConceptoVacio)).show();
                        }
                    }
                }else{
                    Toast.makeText(AddGastoIngresoActivity.this, res.getString(R.string.addGastoIngresoImportePositivo), Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(AddGastoIngresoActivity.this, res.getString(R.string.addGastoIngresoTipoTransaccion), Toast.LENGTH_LONG).show();
            }

        }else{
            crearDialog(res.getString(R.string.titleAddGastoIngresoCamposVacios),res.getString(R.string.bodyAddGastoIngresoCamposVacios)).show();
        }
    }

    // Funcion para ir a Establecimiento Activity
    private void goToEstablecimientoActivity(){
        Intent intent = new Intent(this, EstablecimientoActivity.class);
        Toast.makeText(AddGastoIngresoActivity.this,res.getString(R.string.confirmacionAddTransaction) , Toast.LENGTH_LONG).show();
        startActivity(intent);
    }

    //Mostramos la seleccion de conceptos cuando hace click en Gasto en el formulario
    public void onClickGasto(View view){
        esIngreso = false;
        spinner.setVisibility(View.VISIBLE);
        spinnerLabel.setVisibility(View.VISIBLE);
    }

    //Ocultamos la seleccion de conceptos cuando hace click en Ingreso en el formulario
    public void onClickIngreso(View view){
        esIngreso = true;
        spinner.setVisibility(View.INVISIBLE);
        spinnerLabel.setVisibility(View.INVISIBLE);
    }

    // Mostramos un dialog con un calendario del cual sacamos la informaci??n de la fecha
    public void onClickFecha(View view){

        Calendar calendar = Calendar.getInstance();
        int day, month, year;

        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);


        datePickerDialog = new DatePickerDialog(AddGastoIngresoActivity.this, new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker dp, int a??o, int mes, int dia){

                fechaCalendario = dia+ "/" + (mes+1) + "/" + a??o;
                textFecha.setText(fechaCalendario);

            }
        },year, month, day);

        datePickerDialog.show();
    }

    // Crea un dialog con un titulo y cuerpo
    private Dialog crearDialog(String titulo, String cuerpo){
        AlertDialog.Builder builder = new AlertDialog.Builder(AddGastoIngresoActivity.this);
        builder.setTitle(titulo);

        builder.setMessage(cuerpo).setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });

        return builder.create();
    }

    // Mete un Gasto/Ingreso en la base de datos
    private void insertarGastoIngresoEnlaBd(SQLiteDatabase db, String concepto, String descripcion, String fecha, Double importe, long registro){
        ContentValues values = new ContentValues();
        values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_CONCEPTO, concepto);
        values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_DESCRIPCION, descripcion);
        values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_FECHA, fecha);
        values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IMPORTE, importe);
        values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_REGISTRO, registro);
        values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IDESTABLECIMIENTO, establecimiento.getFechaRegistroApp());

        db.insert(GastoIngresoContract.GastoIngresoEntry.TABLE_NAME, null, values);
    }

}