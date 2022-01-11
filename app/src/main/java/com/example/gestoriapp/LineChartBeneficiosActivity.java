package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

public class LineChartBeneficiosActivity extends AppCompatActivity {

    private SortedMap<String, Object> map;
    List<Establecimiento> establecimientosList = new ArrayList<>();
    Calendar cal = Calendar.getInstance();
    List<Entry> entradas;
    List<String> ejeX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart_beneficios);
        //inicializar diccionario
        initDict();
        // Obtenemos todos los establecimientos del mapa
        for(String key : map.keySet()){
            if(!key.equals("ESTABLECIMIENTO_SELECCIONADO") && key.charAt(0) == 'E'){
                Establecimiento est = (Establecimiento) map.get(key);
                establecimientosList.add(est);
            }
        }
        // Creamos las entradas del gráfico
        actualizarEntradas();
        // Se configura y actualiza la gráfica
        actualizarChart();
    }


    // Funcion que agrupa los gastos/ingresos de todos los establecimiento por mes y
    // crea las entradas de la grafica
    private void actualizarEntradas(){
        ejeX = new ArrayList<>();
        entradas = new ArrayList<>();
        List<Float> beneficios = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        List<GastoIngreso> giList = new ArrayList<>();
        for(Establecimiento e : establecimientosList){
            giList.addAll(e.getListaBeneficios());
        }
        Collections.sort(giList);

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
        Collections.reverse(beneficios);

        float num = 0;

        for(int i = 0; i < indices.size(); i++){
            entradas.add(new BarEntry(indices.get(i), num + beneficios.get(i)));
            num += beneficios.get(i);
        }

    }

    // Introduce las entradas en la gráfica y actualiza sus propiedades (color, tamaño, animación, texto)
    // Los casos de error ya han sido controlados previos a entrar a esta actividad
    private void actualizarChart(){
        LineChart lineChart = findViewById(R.id.lineChartBeneficios);

        LineDataSet lineDataSet = new LineDataSet(entradas, "");
        ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
        iLineDataSets.add(lineDataSet);
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(16f);

        LineData lineData = new LineData(iLineDataSets);

        lineChart.setData(lineData);
        lineChart.invalidate();

        lineChart.getDescription().setText("");
        lineChart.animateY(2000);


        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(ejeX));
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setTextSize(14f);
        lineChart.getXAxis().setGranularity(1);

    }


    private void initDict() {
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