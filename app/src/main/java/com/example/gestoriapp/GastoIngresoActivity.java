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

        textImporte = (TextView) findViewById(R.id.valorImporte);
        textFecha = (TextView) findViewById(R.id.valorFecha);
        textDescripcion = (TextView) findViewById(R.id.valorDescripcion);
        textConcepto = (TextView) findViewById(R.id.valorConcepto);

        // Pillamos los resources para usar los strings definidos en los xmls de strings.xml
        res = getResources();

        initDict();
        gastoIngreso = (GastoIngreso) map.get("GASTOINGRESO_SELECCIONADO");
        establecimiento = (Establecimiento) map.get("ESTABLECIMIENTO_SELECCIONADO");

        textImporte.setText(gastoIngreso.getImporte().toString());
        textFecha.setText(gastoIngreso.getFecha().toString());
        textDescripcion.setText(gastoIngreso.getDescripcion());
        textConcepto.setText(gastoIngreso.getConcepto());

    }

    private void initDict () {
        //Sabemos que este map no a ver null porque para llegar a este Activity ha tenido que seleccionar
        // un GastoIngreso que se encuentra en el propio map
        map = (SortedMap<String,Object>) SingletonMap.getInstance().get(MainActivity.SHARED_DATA_KEY);
    }

    public void onClickBorrarTransaccion(View view){
        //Cuando se confirma la accion dentro del crearDialog se borra la transacción
        crearDialog(res.getString(R.string.titleBorrarConfirmacion),res.getString(R.string.bodyBorrarConfirmacion)).show();
    }


    public Dialog crearDialog(String titulo, String cuerpo){
        AlertDialog.Builder builder = new AlertDialog.Builder(GastoIngresoActivity.this);
        builder.setTitle(titulo);

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
                //
            }
        })
        ;

        return builder.create();
    }


    private void goToEstablecimiento(){
        Intent intent = new Intent(this, EstablecimientoActivity.class);
        startActivity(intent);
    }

}