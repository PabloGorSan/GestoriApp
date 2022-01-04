package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

public class EstablecimientoActivity extends AppCompatActivity {

    private TextView textEstablecimiento;
    private ListView listviewGastosIngresos;

    private SortedMap<String, Object> map;
    private Establecimiento establecimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_establecimiento);

        textEstablecimiento = (TextView) findViewById(R.id.textEstablecimiento);
        listviewGastosIngresos = (ListView) findViewById(R.id.listviewGastosIngresos);

        initDict();
        establecimiento = (Establecimiento) map.get("ESTABLECIMIENTO_SELECCIONADO");

        textEstablecimiento.setText(establecimiento.toString());

        List<GastoIngreso> listaGastosIngresos = new ArrayList<>();

        listaGastosIngresos.addAll(establecimiento.getListaIngresos());
        listaGastosIngresos.addAll(establecimiento.getListaGastos());
        Collections.sort(listaGastosIngresos);

        ArrayAdapter<GastoIngreso> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,listaGastosIngresos);
        listviewGastosIngresos.setAdapter(arrayAdapter);

        listviewGastosIngresos.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //Cogemos el GastoIngreso que ha sido seleccionado de la listviewGastosIngresos
                GastoIngreso selectedItem = (GastoIngreso) parent.getItemAtPosition(position);

                //Guardamos dentro del map el gasto ingreso seleccionado para que la Activity que
                // muestra info de un GastoIngreso sepa cual ha sido seleccionado de forma directa
                map.put("GASTOINGRESO_SELECCIONADO", selectedItem);

                //Navegamos a la Activity que muestra info de un GastoIngreso
                goToGastoIngreso(view);
            }
        });
    }

    private void initDict () {
        //Sabemos que este map no a ver null porque para llegar a este Activity ha tenido que seleccionar
        // un Establecimiento que se encuentra en el propio map
        map = (SortedMap<String,Object>) SingletonMap.getInstance().get(MainActivity.SHARED_DATA_KEY);
    }

    public void goToGastoIngreso(View view){
        //TODO: No sabemos si ahora mismo hace falta el View aqui

        Intent intent = new Intent(this, GastoIngresoActivity.class);
        startActivity(intent);
    }


    public void onClickGoToAddGastoIngreso(View view){
        Intent intent = new Intent(this, AddGastoIngresoActivity.class);
        startActivity(intent);
    }


    public void onClickBorrarEstablecimiento(View view){
        crearDialog("Confirmación","¿Estás seguro de que quieres borrar este establecimiento?").show();
    }


    //TODO: Probar el borrado del establecimiento cuando tengamos la BD y borremos el codigo auxiliar de Main Activity
    public Dialog crearDialog(String titulo, String cuerpo){
        AlertDialog.Builder builder = new AlertDialog.Builder(EstablecimientoActivity.this);
        builder.setTitle(titulo);

        builder.setMessage(cuerpo).setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                SQLiteDatabase db = (SQLiteDatabase) map.get("db");

                for(GastoIngreso gasto : establecimiento.getListaGastos()){
                    map.remove("G"+gasto.getFechaRegistroApp());
                }

                for(GastoIngreso ingreso : establecimiento.getListaIngresos()){
                    map.remove("I"+ingreso.getFechaRegistroApp());
                }

                db.delete(GastoIngresoContract.GastoIngresoEntry.TABLE_NAME,
                        GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IDESTABLECIMIENTO + "=" + establecimiento.getFechaRegistroApp(), null);

                db.delete(ConceptoContract.ConceptoEntry.TABLE_NAME,
                        ConceptoContract.ConceptoEntry.COLUMN_NAME_IDESTABLECIMIENTO + "=" + establecimiento.getFechaRegistroApp(), null);

                map.remove("E" + establecimiento.getFechaRegistroApp());
                db.delete(EstablecimientoContract.EstablecimientoEntry.TABLE_NAME,
                        EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_REGISTRO + "=" + establecimiento.getFechaRegistroApp(), null);

                establecimiento = null;
                goToPantallaPrincipal();
            }
        }).setNegativeButton("No",  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        })
        ;

        return builder.create();
    }


    private void goToPantallaPrincipal(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}