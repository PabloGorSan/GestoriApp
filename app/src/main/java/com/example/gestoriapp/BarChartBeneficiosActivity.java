package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class BarChartBeneficiosActivity extends AppCompatActivity {

    private SortedMap<String, Object> map;
    List<Establecimiento> establecimientos = new ArrayList<>();
    Calendar cal = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart_beneficios);

        initDict();
        BarChart barChart = findViewById(R.id.barChartBeneficios);

        for(Map.Entry<String, Object> entry: map.entrySet()){
            if (entry.getKey().charAt(0) == 'E'){
                establecimientos.add((Establecimiento) entry.getValue());
            }
        }

        Date d = new Date();
        cal.setTime(d);
        cal.get(Calendar.MONTH);

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {

                return ""+Math.floor(value)+"-"+(value-Math.floor(value));
            }
        };
        List<BarEntry> entradas = new ArrayList<>();
        for(Establecimiento e : establecimientos){


            List<GastoIngreso> giList = e.getListaBeneficios();

            cal.setTime(giList.get(0).getFecha());
            float mes = cal.get(Calendar.MONTH);
            float anyo = cal.get(Calendar.YEAR);
            float ben = 0;


            for(GastoIngreso gi : giList){
                cal.setTime(gi.getFecha());
                float mesActual = cal.get(Calendar.MONTH);
                float anyoActual = cal.get(Calendar.YEAR);
                if(mesActual != mes || anyoActual != anyo){
                    entradas.add(new BarEntry(mes+(anyo/10000),ben));
                    mes = mesActual;
                    anyo = anyoActual;
                    ben = 0;
                }else{
                    ben += gi.getImporte().floatValue();
                }

            }

            entradas.add(new BarEntry(mes+(anyo/10000),ben));


        }

        //entradas.add(new BarEntry(300,500));
        //entradas.add(new BarEntry(200,600));

        BarDataSet barDataSet = new BarDataSet(entradas,"Beneficios");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        BarData barData = new BarData(barDataSet);

        barChart.setFitBars(true);
        barChart.setData(barData);
        barChart.getDescription().setText("Estos son tus beneficios por mes");
        barChart.animateY(2000);




    }

    private void initDict () {
        //Sabemos que este map no a ver null porque para llegar a este Activity ha tenido que seleccionar
        // un Establecimiento que se encuentra en el propio map
        map = (SortedMap<String,Object>) SingletonMap.getInstance().get(MainActivity.SHARED_DATA_KEY);
    }
}