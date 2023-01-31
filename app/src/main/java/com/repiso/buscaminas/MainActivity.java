package com.repiso.buscaminas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  implements View.OnTouchListener{

    private Tablero tablero;
    int x, y;
    private Casilla[][] casillasArray;
    private boolean activo = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Desactiva la barra de título
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Realizamos las asociaciones
        LinearLayout layout = findViewById(R.id.layout1);

        //Agregamos el tablero al LinearLayout
        tablero = new Tablero(this);
        tablero.setOnTouchListener(this);
        layout.addView(tablero);

        //Creamos una matriz de casillas de 8x8 y la llenamos
        casillasArray = new Casilla[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                casillasArray[i][j] = new Casilla();
            }
        }
        //Construimos la partida
        this.disponerBombas();
        this.contarBombasPerimetro();

        //Ocultamos la barra de menú
        getSupportActionBar().hide();
    }

    /**
     * Reiniciamos la partida
     * @param v
     */
    public void reiniciar(View v) {
        //Creamos una matriz de casillas de 8x8 y la llenamos
        casillasArray = new Casilla[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                casillasArray[i][j] = new Casilla();
            }
        }
        this.disponerBombas();
        this.contarBombasPerimetro();
        activo = true;
        //Redibuja la vista
        tablero.invalidate();
    }

    /**
     * Gestiona los eventos táctiles
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *        the event.
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (activo)
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (casillasArray[i][j].dentro((int) event.getX(),
                            (int) event.getY())) {
                        casillasArray[i][j].destapado = true;
                        if (casillasArray[i][j].contenido == 80) {
                            Toast.makeText(this, "BOOM",
                                    Toast.LENGTH_LONG).show();
                            activo = false;
                        } else if (casillasArray[i][j].contenido == 0)
                            recorrer(i, j);
                        tablero.invalidate();
                    }
                }
            }
        if (ganar() && activo) {
            Toast.makeText(this, "Ganaste", Toast.LENGTH_LONG).show();
            activo = false;
        }

        return true;
    }

    class Tablero extends View {

        /**
         * Método constructor
         * @param context
         */
        public Tablero(Context context) {
            super(context);
        }

        /**
         * Método principal. Pinta el dibujo en la pantalla
         * @param canvas the canvas on which the background will be drawn
         */
        protected void onDraw(Canvas canvas) {
            //Establece el color del fondo
            canvas.drawRGB(0, 0, 0);
            //Establece las dimensiones:  obtiene el ancho y alto en píxeles del dispositivo mediante los métodos getWidth() y getHeight()
            int ancho = 0;
            if (canvas.getWidth() < canvas.getHeight())
                ancho = tablero.getWidth();
            else
                ancho = tablero.getHeight();
            int anchoCasilla = ancho / 8; //El ancho se reparte entre todas las casillas de la fila

            //Creamos un objeto de la clase Paint (Pincel) y definimos sus características
            Paint paint = new Paint();
            paint.setTextSize(50);
            Paint paint2 = new Paint();
            //Textos
            paint2.setTextSize(50);
            paint2.setTypeface(Typeface.DEFAULT_BOLD);
            paint2.setARGB(255, 0, 0, 255);
            //Líneas blancas separadoras
            Paint paintlinea1 = new Paint();
            paintlinea1.setARGB(255, 255, 255, 255);


            int filaActual = 0;
            //Dibujamos las casillas sobre el tablero
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    //coordenadas x, y. Ancho de la casilla
                    casillasArray[i][j].fijarxy(j * anchoCasilla, filaActual, anchoCasilla);

                    //Establecemos el color, en función si ha sido destapada o no
                    if (casillasArray[i][j].destapado == false)
                        paint.setARGB(153, 204, 204, 204);
                    else
                        paint.setARGB(255, 153, 153, 153);

                    //Dibujamos un cuadrado en las coordeandas x, y
                    canvas.drawRect(j * anchoCasilla, filaActual, j * anchoCasilla
                            + anchoCasilla - 2, filaActual + anchoCasilla - 2, paint);
                    //Dibuja las líneas separadoras
                    canvas.drawLine(j * anchoCasilla, filaActual, j * anchoCasilla
                            + anchoCasilla, filaActual, paintlinea1);
                    canvas.drawLine(j * anchoCasilla + anchoCasilla - 1, filaActual, j
                                    * anchoCasilla + anchoCasilla - 1, filaActual + anchoCasilla,
                            paintlinea1);

                    //Dibuja el número de bombas del perímetro 
                    if (casillasArray[i][j].contenido >= 1
                            && casillasArray[i][j].contenido <= 8
                            && casillasArray[i][j].destapado)
                        canvas.drawText(
                                String.valueOf(casillasArray[i][j].contenido), j
                                        * anchoCasilla + (anchoCasilla / 2) - 8,
                                filaActual + anchoCasilla / 2, paint2);
                    //Dibuja las bombas
                    if (casillasArray[i][j].contenido == 80
                            && casillasArray[i][j].destapado) {
                        Paint bomba = new Paint();
                        bomba.setARGB(255, 255, 0, 0);
                        canvas.drawCircle(j * anchoCasilla + (anchoCasilla / 2),
                                filaActual + (anchoCasilla / 2), 8, bomba);
                    }

                }
                filaActual = filaActual + anchoCasilla;
            }
        }
    }

    /**
     * Dispone las bombas de forma aleatoria sobre el tablero
     */
    private void disponerBombas() {
        int cantidad = 8; //número de bombas
        do {
            int fila = (int) (Math.random() * 8);
            int columna = (int) (Math.random() * 8);
            if (casillasArray[fila][columna].contenido == 0) {
                casillasArray[fila][columna].contenido = 80; // = bomba 
                cantidad--;
            }
        } while (cantidad != 0);
    }

    /**
     * Cuenta las casillas destapadas
     * @return
     */
    private boolean ganar() {
        int cantidad = 0;
        for (int f = 0; f < 8; f++)
            for (int c = 0; c < 8; c++)
                if (casillasArray[f][c].destapado)
                    cantidad++;
        if (cantidad == 56) //((8×8)-8)
            return true;
        else
            return false;
    }

    /**
     * Cuenta las bombas del perímetro
     */
    private void contarBombasPerimetro() {
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                if (casillasArray[f][c].contenido == 0) {
                    int cantidad = contarCoordenada(f, c);
                    casillasArray[f][c].contenido = cantidad;
                }
            }
        }
    }

    //Busca en el perímetro de la casilla si existen casillas con bomba
    int contarCoordenada(int fila, int columna) {
        int total = 0;
        if (fila - 1 >= 0 && columna - 1 >= 0) {
            if (casillasArray[fila - 1][columna - 1].contenido == 80)
                total++;
        }
        if (fila - 1 >= 0) {
            if (casillasArray[fila - 1][columna].contenido == 80)
                total++;
        }
        if (fila - 1 >= 0 && columna + 1 < 8) {
            if (casillasArray[fila - 1][columna + 1].contenido == 80)
                total++;
        }

        if (columna + 1 < 8) {
            if (casillasArray[fila][columna + 1].contenido == 80)
                total++;
        }
        if (fila + 1 < 8 && columna + 1 < 8) {
            if (casillasArray[fila + 1][columna + 1].contenido == 80)
                total++;
        }

        if (fila + 1 < 8) {
            if (casillasArray[fila + 1][columna].contenido == 80)
                total++;
        }
        if (fila + 1 < 8 && columna - 1 >= 0) {
            if (casillasArray[fila + 1][columna - 1].contenido == 80)
                total++;
        }
        if (columna - 1 >= 0) {
            if (casillasArray[fila][columna - 1].contenido == 80)
                total++;
        }
        return total;
    }

    private void recorrer(int fil, int col) {
        if (fil >= 0 && fil < 8 && col >= 0 && col < 8) {
            if (casillasArray[fil][col].contenido == 0) {
                casillasArray[fil][col].destapado = true;
                casillasArray[fil][col].contenido = 50;
                recorrer(fil, col + 1);
                recorrer(fil, col - 1);
                recorrer(fil + 1, col);
                recorrer(fil - 1, col);
                recorrer(fil - 1, col - 1);
                recorrer(fil - 1, col + 1);
                recorrer(fil + 1, col + 1);
                recorrer(fil + 1, col - 1);
            } else if (casillasArray[fil][col].contenido >= 1
                    && casillasArray[fil][col].contenido <= 8) {
                casillasArray[fil][col].destapado = true;
            }
        }
    }



}
