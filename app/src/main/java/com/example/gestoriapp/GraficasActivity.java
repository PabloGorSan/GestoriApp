package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class GraficasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficas);
    }
    public void onClickGoToBarChartBeneficios(View view){
        Intent intent = new Intent(this, BarChartBeneficiosActivity.class);
        startActivity(intent);
    }
    public void onClickGoToPieChartGastos(View view){
        Intent intent = new Intent(this, PieChartGastosActivity.class);
        startActivity(intent);
    }

    public void onClickGoToLineChartBeneficios(View view){
        Intent intent = new Intent(this, LineChartBeneficiosActivity.class);
        startActivity(intent);
    }

}