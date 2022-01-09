package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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


    private void initDict () {
        map = (SortedMap<String,Object>) SingletonMap.getInstance().get(MainActivity.SHARED_DATA_KEY);
        if ( map == null ) {
            //Creamos el map si no existe anteriormente
            map = new TreeMap<>();

            DbHelper dbHelper = new DbHelper(getApplicationContext(), "dbGestori.db");
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            map.put("db", db);


            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            boolean firstStart = prefs.getBoolean("firstStart", true);
            if(firstStart){
                inicializarDB(db);
            }

            crearDialog(res.getString(R.string.titleBienvenida),res.getString(R.string.bodyBienvenida)).show();

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

    public void inicializarDB(SQLiteDatabase db) {
        //Insertar Establecimientos
        String[] nombre = new String[] {"Panadería El Mollete", "Frutería Las Dos Naranjas", "Pescadería Pez Espada","Quiosco Fuente Colores"};
        String[] ciudad = new String[] {"Málaga", "Málaga", "Málaga", "Málaga"};
        String[] calle = new String[] {"Calle Antonio Ruíz", "Calle Maestro Jiménez", "Calle Alhaurín", "Avenida Jorge Luis Borges"};
        String[] cp = new String[] {"29010","29010","29010","29010"};
        long[] registro = new long[] {1,2,3,4};

        for(int i = 0; i < nombre.length; i++) {
            ContentValues values = new ContentValues();
            values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_NOMBRE, nombre[i]);
            values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CIUDAD, ciudad[i]);
            values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CALLE, calle[i]);
            values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_CP, cp[i]);
            values.put(EstablecimientoContract.EstablecimientoEntry.COLUMN_NAME_REGISTRO, registro[i]);

            db.insert(EstablecimientoContract.EstablecimientoEntry.TABLE_NAME, null, values);
        }
        //Insertar Conceptos de Gasto
        String[] concepto1 = new String[] {"Alquiler","Luz","Ingredientes","Pastelería"};
        String[] concepto2 = new String[] {"Alquiler","Luz","Fruta","Verdura"};
        String[] concepto3 = new String[] {"Alquiler","Luz","Pescado"};
        String[] concepto4 = new String[] {"Alquiler","Luz","Revistas","Comestibles","Juguetes"};

        List<String[]> conceptos = new ArrayList<>();
        conceptos.add(concepto1);
        conceptos.add(concepto2);
        conceptos.add(concepto3);
        conceptos.add(concepto4);
        for(int i = 0; i < conceptos.size(); i++){
            String[] concepto = conceptos.get(i);
            ContentValues values = new ContentValues();
            for(int j = 0; j < concepto.length; j++){
                values.put(ConceptoContract.ConceptoEntry.COLUMN_NAME_NOMBRE, concepto[j]);
                values.put(ConceptoContract.ConceptoEntry.COLUMN_NAME_IDESTABLECIMIENTO, registro[i]);
                db.insert(ConceptoContract.ConceptoEntry.TABLE_NAME, null, values);
            }
        }
        //Insertar GastoIngreso
        String[] concepto = new String[] {"Alquiler","Luz","Ingredientes","Pastelería","Ingreso","Ingreso","Ingreso","Ingreso"};
        String[] descripcion = new String[] {"Alquiler de Enero","Luz de diciembre","Inredientes Enero","Pasteles Noviembre","Dinero inicial", "Primer ingreso", "Segundo Ingreso", "Tercer Ingreso"};
        String[] fecha = new String[] {"12/01/2022","03/12/2021","05/01/2022","17/11/2021","22/09/2021","30/11/2021","30/12/2021","30/01/2022"};
        Double[] importe = new Double[] {-500.0,-250.0,-300.0,-300.0,800.0,100.0,400.0,900.0};
        long[] registroGI = new long[] {1,2,3,4,5,6,7,8};

        for(int i = 0; i < concepto.length; i++){
            ContentValues values = new ContentValues();
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_CONCEPTO, concepto[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_DESCRIPCION, descripcion[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_FECHA, fecha[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IMPORTE, importe[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_REGISTRO, registroGI[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IDESTABLECIMIENTO, 1);

            db.insert(GastoIngresoContract.GastoIngresoEntry.TABLE_NAME, null, values);
        }
        concepto = new String[] {"Alquiler","Fruta","Verdura","Ingreso","Ingreso"};
        descripcion = new String[] {"Alquiler de Enero","Fruta","Verdura","Primer ingreso", "Segundo Ingreso"};
        fecha = new String[] {"12/01/2022","03/12/2021","05/11/2021","30/12/2021","30/01/2022"};
        importe = new Double[] {-400.0,-250.0,-300.0,400.0,500.0};
        registroGI = new long[] {21,22,23,24,25};

        for(int i = 0; i < concepto.length; i++){
            ContentValues values = new ContentValues();
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_CONCEPTO, concepto[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_DESCRIPCION, descripcion[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_FECHA, fecha[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IMPORTE, importe[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_REGISTRO, registroGI[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IDESTABLECIMIENTO, 2);

            db.insert(GastoIngresoContract.GastoIngresoEntry.TABLE_NAME, null, values);
        }
        concepto = new String[] {"Alquiler","Luz","Revistas","Comestibles","Juguetes","Ingreso","Ingreso","Ingreso","Ingreso"};
        descripcion = new String[] {"Alquiler de Enero","Luz","Revistas","Comestibles","Juguetes","Dinero inicial", "Primer ingreso", "Segundo Ingreso", "Tercer Ingreso"};
        fecha = new String[] {"12/01/2022","03/12/2021","05/01/2022","17/11/2021","12/11/2021","22/10/2021","30/11/2021","30/12/2021","30/01/2022"};
        importe = new Double[] {-500.0,-250.0,-300.0,-300.0,-300.0,600.0,100.0,400.0,700.0};
        registroGI = new long[] {11,12,13,14,15,16,17,18,19};

        for(int i = 0; i < concepto.length; i++){
            ContentValues values = new ContentValues();
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_CONCEPTO, concepto[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_DESCRIPCION, descripcion[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_FECHA, fecha[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IMPORTE, importe[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_REGISTRO, registroGI[i]);
            values.put(GastoIngresoContract.GastoIngresoEntry.COLUMN_NAME_IDESTABLECIMIENTO, 4);

            db.insert(GastoIngresoContract.GastoIngresoEntry.TABLE_NAME, null, values);
        }

        //Configurar primer start
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();

    }




}