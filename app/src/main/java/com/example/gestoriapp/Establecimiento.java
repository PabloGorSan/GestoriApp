package com.example.gestoriapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Establecimiento {

    private String nombre;
    private String ciudad;
    private String calle;
    private String codigoPostal;

    private List<String> conceptos;

    private List<GastoIngreso> listaGastos;
    private List<GastoIngreso> listaIngresos;
    private long fechaRegistroApp;


    public Establecimiento(String nombre, String ciudad, String calle, String codigoPostal, List<String> conceptos, long fechaRegistroApp){
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.calle = calle;
        this.codigoPostal = codigoPostal;
        this.conceptos = conceptos;
        Collections.sort(this.conceptos);
        this.fechaRegistroApp = fechaRegistroApp;
        listaGastos = new ArrayList<>();
        listaIngresos = new ArrayList<>();
    }



    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public long getFechaRegistroApp() {
        return fechaRegistroApp;
    }

    public void setFechaRegistroApp(long fechaRegistroApp) {
        this.fechaRegistroApp = fechaRegistroApp;
    }

    public List<String> getConceptos() {
        return conceptos;
    }

    public void setConceptos(List<String> conceptos) {
        this.conceptos = conceptos;
    }

    public List<GastoIngreso> getListaGastos() {
        return listaGastos;
    }

    public void setListaGastos(List<GastoIngreso> listaGastos) {
        this.listaGastos = listaGastos;
        Collections.sort(this.listaGastos);
    }

    public void addGasto(GastoIngreso g){
        this.listaGastos.add(g) ;
        Collections.sort(this.listaGastos);
    }

    public void deleteGasto(GastoIngreso g){
        this.listaGastos.remove(g);
    }

    public List<GastoIngreso> getListaIngresos() {
        return listaIngresos;
    }

    public void setListaIngresos(List<GastoIngreso> listaIngresos) {
        this.listaIngresos = listaIngresos;
        Collections.sort(this.listaIngresos);
    }

    public void addIngreso(GastoIngreso i){
        this.listaIngresos.add(i) ;
        Collections.sort(this.listaIngresos);
    }

    public void deleteIngreso(GastoIngreso i){
        this.listaIngresos.remove(i);
    }

    public void addConcepto(String nombre) {
        conceptos.add(nombre);
        Collections.sort(this.conceptos);
    }

    @Override
    public String toString() {
        return this.nombre + " | " + this.ciudad +", " + this.calle;
    }


    public List<GastoIngreso> getListaBeneficios() {
        List<GastoIngreso> ben = new ArrayList<>();
        ben.addAll(listaGastos);
        ben.addAll(listaIngresos);
        Collections.sort(ben);
        return ben;
    }
}
