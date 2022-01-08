package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    static final String SHARED_DATA_KEY = "SHARED_MAP_KEY";

    private TextView textInitialScreen;
    private ListView listviewEstablecimientos;
    private SortedMap<String,Object> map;
    private Resources res;
    private List<Establecimiento> establecimientosList = new ArrayList<>();

    //TODO: proximo dia
    // 6. Hacerlo más responsive y ponerlo bonito (Fuente, colores, detalles estéticos...)



    private void initDict () {
        map = (SortedMap<String,Object>) SingletonMap.getInstance().get(MainActivity.SHARED_DATA_KEY);
        if ( map == null ) {
            //Creamos el map si no existe anteriormente
            map = new TreeMap<>();

            DbHelper dbHelper = new DbHelper(getApplicationContext(), "dbGestori.db");
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            map.put("db", db);

            insertarEstablecimientosEnMapa(db);

            insertarConceptosEnEstablecimiento(db);

            insertarGastoIngresosEnMapa(db);


            SingletonMap.getInstance().put(SHARED_DATA_KEY, map);
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textInitialScreen = (TextView) findViewById(R.id.textInitialScreen);
        listviewEstablecimientos = (ListView) findViewById(R.id.listviewEstablecimientos);

        res = getResources();
        initDict();


        for(String key : map.keySet()){
            if(!key.equals("ESTABLECIMIENTO_SELECCIONADO") && key.charAt(0) == 'E'){
                Establecimiento est = (Establecimiento) map.get(key);
                establecimientosList.add(est);
            }
        }

        ArrayAdapter<Establecimiento> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,establecimientosList);
        listviewEstablecimientos.setAdapter(arrayAdapter);

        listviewEstablecimientos.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //Cogemos el establecimiento que ha sido seleccionado de la listviewEstablecimientos
                Establecimiento selectedItem = (Establecimiento) parent.getItemAtPosition(position);

                //Guardamos dentro del map el establecimiento seleccionado para que la Activity que
                // muestra info de un Establecimiento sepa cual ha sido seleccionado de forma directa
                map.put("ESTABLECIMIENTO_SELECCIONADO", selectedItem);

                //Navegamos a la Activity que muestra info de un Establecimiento
                goToEstablecimiento(view);
            }
        });
    }

    @Override
    public void onBackPressed() {
        //Se desactiva el comportamiento por defecto del onBackPressed para
        // no vuelva a entrar al formulario de creacion de un establecimiento recien creado
    }

    public void onClickGoToGraficas(View view){
        if(establecimientosList.isEmpty() || !hayGastoIngresos()){
            crearDialog(res.getString(R.string.titleIrAGraficasFailure),res.getString(R.string.bodyIrAGraficasFailure)).show();
        } else {
            Intent intent = new Intent(this, GraficasActivity.class);
            startActivity(intent);
        }
    }

    public boolean hayGastoIngresos(){
        boolean hayGastoIngresos = false;
        int i = 0;

        while(!hayGastoIngresos && establecimientosList.size() > i ){
            hayGastoIngresos = !(establecimientosList.get(i).getListaBeneficios().isEmpty());
            i++;
        }

        return hayGastoIngresos;
    }

    public void onClickGoToAddEstablecimiento(View view){
        Intent intent = new Intent(this, AddEstablecimientoActivity.class);
        startActivity(intent);
    }

    public void goToEstablecimiento(View view){
        Intent intent = new Intent(this, EstablecimientoActivity.class);
        startActivity(intent);
    }

    private void insertarGastoIngresosEnMapa(SQLiteDatabase db){
        String[] columns = {
                GastoIngresoContract.GastoIngresoEntry._ID,
                GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_CONCEPTO,
                GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_DESCRIPCION,
                GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_FECHA,
                GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IMPORTE,
                GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_REGISTRO,
                GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IDESTABLECIMIENTO
        };

        Cursor cursor = db.query(GastoIngresoContract.GastoIngresoEntry.TABLE_NAME, columns, null, null, null, null, null);

        try{
            while(cursor.moveToNext()){
                String concepto = cursor.getString(cursor.getColumnIndexOrThrow(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_CONCEPTO));
                String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_DESCRIPCION));
                String fecha = cursor.getString(cursor.getColumnIndexOrThrow(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_FECHA));
                Double importe = cursor.getDouble(cursor.getColumnIndexOrThrow(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IMPORTE));
                long registro = cursor.getLong(cursor.getColumnIndexOrThrow(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_REGISTRO));
                long idestablecimiento = cursor.getLong(cursor.getColumnIndexOrThrow(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IDESTABLECIMIENTO));

                // Creamos el objeto GastoIngreso, lo añadimos al mapa y se lo añadimos tambien a su establecimiento correspondiente
                GastoIngreso gi = new GastoIngreso(importe, fecha, descripcion, concepto, registro);
                Establecimiento e = (Establecimiento) map.get("E"+idestablecimiento);

                if(importe<0){
                    map.put("G" + gi.getFechaRegistroApp(), gi);
                    e.addGasto(gi);
                }else{
                    map.put("I" + gi.getFechaRegistroApp(), gi);
                    e.addIngreso(gi);
                }

            }
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
    }


    private void insertarEstablecimientosEnMapa(SQLiteDatabase db) {
        String[] columns = {
                EstablecimientoContract.EstablecimientoEntry._ID,
                EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_NOMBRE,
                EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CIUDAD,
                EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CALLE,
                EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CP,
                EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_REGISTRO
        };

        Cursor cursor = db.query(EstablecimientoContract.EstablecimientoEntry.TABLE_NAME, columns, null, null, null, null, null);

        try{
            while(cursor.moveToNext()){

                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_NOMBRE));
                String ciudad = cursor.getString(cursor.getColumnIndexOrThrow(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CIUDAD));
                String calle = cursor.getString(cursor.getColumnIndexOrThrow(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CALLE));
                String cp = cursor.getString(cursor.getColumnIndexOrThrow(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CP));
                long registro = cursor.getLong(cursor.getColumnIndexOrThrow(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_REGISTRO));

                //Creamos el objeto Establecimiento y lo metemos en el mapa
                Establecimiento e = new Establecimiento(nombre, ciudad, calle, cp, new ArrayList<String>(), registro);
                map.put("E" + e.getFechaRegistroApp(), e);

            }
        } finally {
            cursor.close();
        }
    }

    private void insertarConceptosEnEstablecimiento(SQLiteDatabase db) {
        String[] columns = {
                ConceptoContract.ConceptoEntry._ID,
                ConceptoContract.ConceptoEntry.COLUMN_NAME_NOMBRE,
                ConceptoContract.ConceptoEntry.COLUMN_NAME_IDESTABLECIMIENTO
        };

        Cursor cursor = db.query(ConceptoContract.ConceptoEntry.TABLE_NAME, columns, null, null, null, null, null);

        try{
            while(cursor.moveToNext()){
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(ConceptoContract.ConceptoEntry.COLUMN_NAME_NOMBRE));
                long idestablecimiento = cursor.getLong(cursor.getColumnIndexOrThrow(ConceptoContract.ConceptoEntry.COLUMN_NAME_IDESTABLECIMIENTO));

                //Añadimos el nombre del concepto a su establecimiento
                Establecimiento e = (Establecimiento) map.get("E"+idestablecimiento);
                e.addConcepto(nombre);

            }
        } finally {
            cursor.close();
        }
    }

    public Dialog crearDialog(String titulo, String cuerpo){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(titulo);

        builder.setMessage(cuerpo).setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });

        return builder.create();
    }
}