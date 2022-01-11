package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedMap;

public class GastoIngresoActivity extends AppCompatActivity {


    private TextView textImporte;
    private TextView textFecha;
    private TextView textDescripcion;
    private TextView textConcepto;

    private SortedMap<String, Object> map;
    private Establecimiento establecimiento;
    private GastoIngreso gastoIngreso;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gasto_ingreso);

        //Cogemos los elementos de la interfaz
        textImporte = (TextView) findViewById(R.id.valorImporte);
        textFecha = (TextView) findViewById(R.id.valorFecha);
        textDescripcion = (TextView) findViewById(R.id.valorDescripcion);
        textConcepto = (TextView) findViewById(R.id.valorConcepto);

        // Pillamos los resources para usar los strings definidos en los xmls de strings.xml
        res = getResources();
        //inicializar diccionario
        initDict();
        //Cogemos el GastoIngreso y establecimiento seleccionado
        gastoIngreso = (GastoIngreso) map.get("GASTOINGRESO_SELECCIONADO");
        establecimiento = (Establecimiento) map.get("ESTABLECIMIENTO_SELECCIONADO");

        //Mostramos los datos del GastoIngreso por pantalla
        textImporte.setText(gastoIngreso.getImporte().toString());
        Date gastoIngresoFecha = gastoIngreso.getFecha();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        textFecha.setText(sdf.format(gastoIngresoFecha));
        textDescripcion.setText(gastoIngreso.getDescripcion());
        textConcepto.setText(gastoIngreso.getConcepto());

    }

    // Sobrescribimos el comportamiento de onBackPressed para no mostrar un estado anterior
    // desactualizado de la app e ir a EstablecimientoActivity.
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, EstablecimientoActivity.class);
        startActivity(intent);
    }

    private void initDict () {
        //Sabemos que este map no a ver null porque para llegar a este Activity ha tenido que seleccionar
        // un GastoIngreso que se encuentra en el propio map
        map = (SortedMap<String,Object>) SingletonMap.getInstance().get(MainActivity.SHARED_DATA_KEY);
    }

    // Creamos el Dialog pidiendo confirmacion sobre el borrado del GastoIngreso
    public void onClickBorrarTransaccion(View view){
        crearDialog(res.getString(R.string.titleBorrarConfirmacion),res.getString(R.string.bodyBorrarConfirmacion)).show();
    }


    public Dialog crearDialog(String titulo, String cuerpo){
        AlertDialog.Builder builder = new AlertDialog.Builder(GastoIngresoActivity.this);
        builder.setTitle(titulo);

        //Cuando se confirma la accion dentro del crearDialog se borra el GastoIngreso.
        // El borrado se produce tanto en el mapa como en la base de datos
        builder.setMessage(cuerpo).setPositiveButton(res.getString(R.string.respuestaPositiva), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(gastoIngreso.getImporte()>0){
                    //Aqui borramos un Ingreso
                    establecimiento.deleteIngreso(gastoIngreso);
                    map.remove("I"+gastoIngreso.getFechaRegistroApp());

                }else{
                    //Aqui borramos un Gasto
                    establecimiento.deleteGasto(gastoIngreso);
                    map.remove("G"+gastoIngreso.getFechaRegistroApp());
                }

                SQLiteDatabase db = (SQLiteDatabase) map.get("db");
                db.delete(GastoIngresoContract.GastoIngresoEntry.TABLE_NAME,
                        GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_REGISTRO + "=" + gastoIngreso.getFechaRegistroApp(), null);
                gastoIngreso = null;
                goToEstablecimiento();
            }
        }).setNegativeButton(res.getString(R.string.respuestaNegativa),  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //En caso negativo no se debe borrar
            }
        })
        ;

        return builder.create();
    }

    //Navegamos a EstablecimientoActivity
    private void goToEstablecimiento(){
        Intent intent = new Intent(this, EstablecimientoActivity.class);
        startActivity(intent);
    }

}