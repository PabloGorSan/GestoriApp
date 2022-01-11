package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class PieChartGastosActivity extends AppCompatActivity {

    private SortedMap<String, Object> map;
    private List<Establecimiento> establecimientosList = new ArrayList<>();
    private ListView listviewEstablecimientos;
    private List<PieEntry> entradas;
    private Resources res;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart_gastos);

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

        // Inicializamos el gráfico circular
        PieChart pieChart = findViewById(R.id.pieChartGastos);
        pieChart.setNoDataText(res.getString(R.string.initialTextChart));
        pieChart.setNoDataTextColor(R.color.black);

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

    private void initDict () {
        //Sabemos que este map no a ver null porque para llegar a este Activity ha tenido que seleccionar
        // un Establecimiento que se encuentra en el propio map
        map = (SortedMap<String,Object>) SingletonMap.getInstance().get(MainActivity.SHARED_DATA_KEY);
    }

    // Funcion que agrupa los gastos/ingresos de un establecimiento por conceptos y
    // crea las entradas de la grafica
    private void actualizarEntradas(Establecimiento e){
        entradas = new ArrayList<>();
        Map<String, Float> gastos = new HashMap<>();
        float total = 0;
        for(GastoIngreso g : e.getListaGastos()){
            String concepto = g.getConcepto();
            if(gastos.containsKey(concepto)){
                gastos.put(concepto, gastos.get(concepto) + g.getImporte().floatValue());
                total += g.getImporte().floatValue();
            } else {
                gastos.put(concepto, g.getImporte().floatValue());
                total += g.getImporte().floatValue();
            }
        }
        for(String c : gastos.keySet()){
            entradas.add(new PieEntry((gastos.get(c)/total) * 100, c));
        }

    }

    // Introduce las entradas en la gráfica y actualiza sus propiedades (color, tamaño, animación, texto)
    // Se controlan los casos de error cuando no hay ningún GastoIngreso en el establecimiento
    private void actualizarChart(){
        PieChart pieChart = findViewById(R.id.pieChartGastos);

        PieDataSet pieDataSet = new PieDataSet(entradas, "");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(16f);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(pieData);

        pieChart.invalidate();
        pieChart.getDescription().setEnabled(false);

        if(entradas.isEmpty()){
            pieChart.setCenterText(res.getString(R.string.textGastosPorCategoriaSinGastos));
        }else{
            pieChart.setCenterText(res.getString(R.string.textGastos));
        }

        pieChart.animate();
        pieChart.setUsePercentValues(true);


    }

}