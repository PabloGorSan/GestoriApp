package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

public class BarChartBeneficiosActivity extends AppCompatActivity {

    private SortedMap<String, Object> map;
    private List<Establecimiento> establecimientosList = new ArrayList<>();
    private Calendar cal = Calendar.getInstance();
    private List<BarEntry> entradas;
    private List<String> ejeX;
    private Resources res;

    private ListView listviewEstablecimientos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart_beneficios);

        // Pillamos los resources para usar los strings definidos en los xmls de strings.xml
        res = getResources();
        //inicializar diccionario
        initDict();

        // Obtenemos todos los establecimientos del mapa
        for(String key : map.keySet()){
            if(!key.equals("ESTABLECIMIENTO_SELECCIONADO") && key.charAt(0) == 'E'){
                Establecimiento est = (Establecimiento) map.get(key);
                establecimientosList.add(est);
            }
        }

        // Inicializamos el gráfico de barras
        BarChart barChart = findViewById(R.id.barChartBeneficios);
        barChart.setNoDataText(res.getString(R.string.initialTextChart));
        barChart.setNoDataTextColor(R.color.black);

        // Sacamos la listView de la interfaz, metemos la lista de establecimientos en el
        // ArrayAdapter y luego asignamos el ArrayAdapter al listview.
        listviewEstablecimientos = (ListView) findViewById(R.id.listViewEstBarChart);
        ArrayAdapter<Establecimiento> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,establecimientosList);
        listviewEstablecimientos.setAdapter(arrayAdapter);

        // Configuramos el comportamiento de seleccionar un elemento
        listviewEstablecimientos.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //Cogemos el establecimiento que ha sido seleccionado de la listviewEstablecimientos
                Establecimiento selectedItem = (Establecimiento) parent.getItemAtPosition(position);
                // Creamos las entradas del gráfico
                actualizarEntradas(selectedItem);
                // Se configura y actualiza la gráfica
                actualizarChart();
            }
        });
    }


    // Extendemos la clase BarDataSet para asignar nuestros propio criterio para colorear
    // la gráfica
    public class MyBarDataSet extends BarDataSet {

        public MyBarDataSet(List<BarEntry> yVals, String label) {
            super(yVals, label);
        }

        @Override
        public int getColor(int index) {
            if(getEntryForIndex(index).getY() > 0)
                return mColors.get(0);
            else
                return mColors.get(1);
        }

    }

    // Funcion que agrupa los gastos/ingresos de un establecimiento por mes y
    // crea las entradas de la grafica
    private void actualizarEntradas(Establecimiento e){
        ejeX = new ArrayList<>();
        entradas = new ArrayList<>();
        List<Float> beneficios = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        List<GastoIngreso> giList = e.getListaBeneficios();

        if(!giList.isEmpty()){
            cal.setTime(giList.get(0).getFecha());
            int mes = cal.get(Calendar.MONTH);
            int anyo = cal.get(Calendar.YEAR);
            float ben = 0;
            int indice = 0;

            for(GastoIngreso gi : giList){
                cal.setTime(gi.getFecha());
                int mesActual = cal.get(Calendar.MONTH);
                int anyoActual = cal.get(Calendar.YEAR);

                if(mesActual != mes || anyoActual != anyo){
                    while(mesActual != mes || anyoActual != anyo){
                        beneficios.add(ben);
                        indices.add(indice);
                        actualizarEjeX(mes, anyo, ejeX);
                        indice++;
                        ben = 0;
                        mes--;
                        if(mes < 0){
                            mes = 11;
                            anyo--;
                        }
                    }
                    mes = mesActual;
                    anyo = anyoActual;
                    ben = gi.getImporte().floatValue();
                }else{
                    ben += gi.getImporte().floatValue();
                }
            }
            beneficios.add(ben);
            indices.add(indice);
            actualizarEjeX(mes, anyo,ejeX);

            Collections.reverse(ejeX);
            Collections.reverse(indices);
            for(int i = 0; i < indices.size(); i++){
                entradas.add(new BarEntry(indices.get(i), beneficios.get(i)));
            }
        }

    }

    // Introduce las entradas en la gráfica y actualiza sus propiedades (color, tamaño, animación, texto)
    // Se controlan los casos de error cuando no hay ningún GastoIngreso en el establecimiento
    private void actualizarChart() {
        BarChart barChart = findViewById(R.id.barChartBeneficios);

        if(!entradas.isEmpty()){
            MyBarDataSet barDataSet = new MyBarDataSet(entradas,"");
            barDataSet.setColors(
                    ContextCompat.getColor(barChart.getContext(), R.color.green),
                    ContextCompat.getColor(barChart.getContext(), R.color.red)
            );
            barDataSet.setValueTextColor(Color.BLACK);
            barDataSet.setValueTextSize(16f);

            BarData barData = new BarData(barDataSet);

            barChart.setFitBars(true);
            barChart.setData(barData);
            barChart.getDescription().setText(res.getString(R.string.descriptionBarChart));
            barChart.animateY(2000);

            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(ejeX));
            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            barChart.getXAxis().setTextSize(14f);
        }else{
            barChart.clear();
            barChart.setNoDataText(res.getString(R.string.textBeneficiosSinTransacciones));
            barChart.setNoDataTextColor(R.color.black);
        }
    }

    private void initDict () {
        //Sabemos que este map no a ver null porque para llegar a este Activity ha tenido que seleccionar
        // un Establecimiento que se encuentra en el propio map
        map = (SortedMap<String,Object>) SingletonMap.getInstance().get(MainActivity.SHARED_DATA_KEY);
    }

    //Pone en el ejeX la fecha correspondiente a la entrada del gráfico
    private void actualizarEjeX(int mes, int anyo, List<String> ejeX){
        mes++;
        String stringAnyo = anyo+"";
        stringAnyo = stringAnyo.substring(2);
        String f = ""+ mes + "-" +stringAnyo;
        if(!ejeX.contains(f)){
            ejeX.add(f);
        }
    }
}