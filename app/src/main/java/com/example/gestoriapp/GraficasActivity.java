package com.example.gestoriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
}