package com.example.gestoriapp;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GastoIngreso implements Comparable<GastoIngreso>{
    private Double importe;
    private Date fecha;
    private String descripcion;
    private String concepto;
    private long fechaRegistroApp;

    private static final DecimalFormat df = new DecimalFormat("0.00");
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();

    public GastoIngreso(Double importe, String fecha, String descripcion, String concepto, long fechaRegistroApp) throws ParseException {
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        this.importe = new Double(df.format(importe));
        this.fecha = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
        this.descripcion = descripcion;
        this.concepto = concepto;
        this.fechaRegistroApp = fechaRegistroApp;
    }

    @Override
    public int compareTo(GastoIngreso g){
        return g.getFecha().compareTo(this.fecha);
    }

    public Double getImporte() {
        return importe;
    }

    public void setImporte(Double importe) {
        this.importe = importe;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public long getFechaRegistroApp() {
        return fechaRegistroApp;
    }

    public void setFechaRegistroApp(long fechaRegistroApp) {
        this.fechaRegistroApp = fechaRegistroApp;
    }

    @Override
    public String toString() {
        return concepto + " " + importe;
    }
}
