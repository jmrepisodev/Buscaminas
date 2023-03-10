package com.repiso.buscaminas;

public class Casilla {

    public int x,y,ancho;
    public int contenido=0;
    public boolean destapado=false;

    /**
     * Establece el ancho y las coordenadas de la casilla
     * @param x
     * @param y
     * @param ancho
     */
    public void fijarxy(int x,int y, int ancho) {
        this.x=x;
        this.y=y;
        this.ancho=ancho;
    }

   /**
   * Devuelve true si la pulsación táctil está dentro de la casilla actual
   * @return lugar posición táctil 
   */
    public boolean dentro(int xx,int yy) {
        if (xx>=this.x && xx<=this.x+ancho && yy>=this.y && yy<=this.y+ancho)
            return true;
        else
            return false;
    }
}
