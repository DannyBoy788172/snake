package com.example.snake;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;

public class Juego extends Application implements EventHandler<KeyEvent> {
    public static int ALTO_MENU = 30; // Margen vertical para el menú (se deja abajo)
    public static int ANCHO_PANTALLA = 800;
    public static int ALTO_PANTALLA = 600;
    public static int ANCHO = 10; // Ancho de la serpiente
    public static int RETARDO_ANIMADOR = 100000000; // Controla la velocidad del juego
    public static int MAX_SEGUNDOS = 60000;

    public enum Direccion {
        Arriba, Abajo, Izquierda, Derecha
    }

    private Direccion dirJugador1 = Direccion.Arriba; // Dirección del jugador 1
    private Direccion dirJugador2 = Direccion.Arriba; // Dirección del jugador 2
    private double cabezaXJugador1 = ANCHO_PANTALLA / 4; // Posición inicial del jugador 1
    private double cabezaYJugador1 = ALTO_PANTALLA * 4 / 5; // Posición inicial del jugador 1
    private double cabezaXJugador2 = 3 * ANCHO_PANTALLA / 4; // Posición inicial del jugador 2
    private double cabezaYJugador2 = ALTO_PANTALLA * 4 / 5; // Posición inicial del jugador 2
    private int tamanoJugador1 = 1; // Tamaño inicial del jugador 1
    private int tamanoJugador2 = 1; // Tamaño inicial del jugador 2
    private Group raiz; // Raíz de la escena JavaFX
    private ArrayList<Rectangle> trozosJugador1 = new ArrayList<>();// Lista de trozos del jugador 1
    private ArrayList<Rectangle> trozosJugador2 = new ArrayList<>();// Lista de trozos del jugador 2
    private Circle fruta; // Fruta
    private Circle poder; // Poder
    private Text puntosJugador1; // Puntos del jugador 1
    private Text puntosJugador2; // Puntos del jugador 2
    private Text tiempo; // Tiempo transcurrido
    private AnimationTimer anim; // Animador del juego
    private long tiempoInicio; // Tiempo de inicio del juego
    private int numJugadores = 0;
    private boolean juegoTerminado = false;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage ventana) throws Exception {
        ventana.setWidth(ANCHO_PANTALLA);
        ventana.setHeight(ALTO_PANTALLA);
        ventana.setTitle("SNAKE");
        ventana.setResizable(false);
        raiz = new Group();
        Scene escena = new Scene(raiz, Color.BLACK); // Fondo negro

        ventana.setScene(escena);

        // Preguntar si se jugará con una o dos personas
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Selección de jugadores");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Cuántos jugadores serán?");
        ButtonType boton1Jugador = new ButtonType("Un jugador");
        ButtonType boton2Jugadores = new ButtonType("Dos jugadores");
        alerta.getButtonTypes().setAll(boton1Jugador, boton2Jugadores);
        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent()) {
            if (resultado.get() == boton1Jugador) {
                numJugadores = 1;
            } else if (resultado.get() == boton2Jugadores) {
                numJugadores = 2;
            }
        }

        ponerFruta();
        generarPoder();

        puntosJugador1 = new Text();
        puntosJugador1.setFont(Font.font("Verdana", 20));
        puntosJugador1.setFill(Color.CYAN);
        puntosJugador1.setX(10);
        puntosJugador1.setY(20);
        raiz.getChildren().add(puntosJugador1);
        ponerPuntosJugador1();

        if (numJugadores == 2) {
            puntosJugador2 = new Text();
            puntosJugador2.setFont(Font.font("Verdana", 20));
            puntosJugador2.setFill(Color.MAGENTA);
            puntosJugador2.setX(ANCHO_PANTALLA - 200);
            puntosJugador2.setY(20);
            raiz.getChildren().add(puntosJugador2);
            ponerPuntosJugador2();
        }

        tiempo = new Text();
        tiempo.setFont(Font.font("Verdana", 20));
        tiempo.setFill(Color.WHITE);
        tiempo.setX(ANCHO_PANTALLA / 2 - 50);
        tiempo.setY(20);
        raiz.getChildren().add(tiempo);
        tiempoInicio = System.nanoTime();

        escena.setOnKeyPressed(this);

        anim = new AnimationTimer() {
            long ultimaAnimacion = 0;
            int segundos = 0;

            @Override
            public void handle(long now) {
                if (ultimaAnimacion == 0) {
                    ultimaAnimacion = now;
                    return;
                }
                if (now - ultimaAnimacion < RETARDO_ANIMADOR)
                    return;

                // Movimiento del jugador 1
                switch (dirJugador1) {
                    case Abajo:
                        cabezaYJugador1 += ANCHO;
                        break;
                    case Arriba:
                        cabezaYJugador1 -= ANCHO;
                        break;
                    case Derecha:
                        cabezaXJugador1 += ANCHO;
                        break;
                    case Izquierda:
                        cabezaXJugador1 -= ANCHO;
                        break;
                }
                // Movimiento del jugador 2
                if (numJugadores == 2) {
                    switch (dirJugador2) {
                        case Abajo:
                            cabezaYJugador2 += ANCHO;
                            break;
                        case Arriba:
                            cabezaYJugador2 -= ANCHO;
                            break;
                        case Derecha:
                            cabezaXJugador2 += ANCHO;
                            break;
                        case Izquierda:
                            cabezaXJugador2 -= ANCHO;
                            break;
                    }
                }

                // Colisiones con bordes para el jugador 1
                if (cabezaXJugador1 < 0)
                    cabezaXJugador1 = ANCHO_PANTALLA;
                if (cabezaXJugador1 > ANCHO_PANTALLA)
                    cabezaXJugador1 = 0;
                if (cabezaYJugador1 < 0)
                    cabezaYJugador1 = ALTO_PANTALLA - ALTO_MENU;
                if (cabezaYJugador1 > ALTO_PANTALLA - ALTO_MENU)
                    cabezaYJugador1 = 0;

                // Colisiones con bordes para el jugador 2
                if (numJugadores == 2) {
                    if (cabezaXJugador2 < 0)
                        cabezaXJugador2 = ANCHO_PANTALLA;
                    if (cabezaXJugador2 > ANCHO_PANTALLA)
                        cabezaXJugador2 = 0;
                    if (cabezaYJugador2 < 0)
                        cabezaYJugador2 = ALTO_PANTALLA - ALTO_MENU;
                    if (cabezaYJugador2 > ALTO_PANTALLA - ALTO_MENU)
                        cabezaYJugador2 = 0;
                }

                // Colisiones con la serpiente para el jugador 1
                for (Rectangle r : trozosJugador1)
                    if (r.getX() == cabezaXJugador1)
                        if (r.getY() == cabezaYJugador1)
                            gameOver();

                // Colisiones con la serpiente para el jugador 2
                if (numJugadores == 2) {
                    for (Rectangle r : trozosJugador2)
                        if (r.getX() == cabezaXJugador2)
                            if (r.getY() == cabezaYJugador2)
                                gameOver();
                }

                // ¿Comió fruta el jugador 1?
                if (fruta.getCenterX() >= cabezaXJugador1 && fruta.getCenterX() <= cabezaXJugador1 + ANCHO)
                    if (fruta.getCenterY() >= cabezaYJugador1 && fruta.getCenterY() <= cabezaYJugador1 + ANCHO) {
                        tamanoJugador1++;
                        raiz.getChildren().remove(fruta);
                        ponerFruta();
                        ponerPuntosJugador1();
                    }

                // ¿Comió fruta el jugador 2?
                if (numJugadores == 2) {
                    if (fruta.getCenterX() >= cabezaXJugador2 && fruta.getCenterX() <= cabezaXJugador2 + ANCHO)
                        if (fruta.getCenterY() >= cabezaYJugador2 && fruta.getCenterY() <= cabezaYJugador2 + ANCHO) {
                            tamanoJugador2++;
                            raiz.getChildren().remove(fruta);
                            ponerFruta();
                            ponerPuntosJugador2();
                        }
                }

                // ¿Obtuvo poder el jugador 1?
                if (poder.getCenterX() >= cabezaXJugador1 && poder.getCenterX() <= cabezaXJugador1 + ANCHO)
                    if (poder.getCenterY() >= cabezaYJugador1 && poder.getCenterY() <= cabezaYJugador1 + ANCHO) {
                        aplicarPoderJugador1();
                        raiz.getChildren().remove(poder);
                        generarPoder();
                    }

                // ¿Obtuvo poder el jugador 2?
                if (numJugadores == 2) {
                    if (poder.getCenterX() >= cabezaXJugador2 && poder.getCenterX() <= cabezaXJugador2 + ANCHO)
                        if (poder.getCenterY() >= cabezaYJugador2 && poder.getCenterY() <= cabezaYJugador2 + ANCHO) {
                            aplicarPoderJugador2();
                            raiz.getChildren().remove(poder);
                            generarPoder();
                        }
                }

                // Mover al jugador 1
                Rectangle trozoJugador1 = new Rectangle(ANCHO, ANCHO);
                trozoJugador1.setFill(Color.CYAN); // Serpiente del jugador 1 de color cyan
                trozoJugador1.setStroke(Color.BLACK);
                trozoJugador1.setX(cabezaXJugador1);
                trozoJugador1.setY(cabezaYJugador1);
                raiz.getChildren().add(trozoJugador1);
                if (!trozosJugador1.isEmpty()) {
                    if (trozosJugador1.size() >= tamanoJugador1)
                        // Quitamos el último trozo
                        raiz.getChildren().remove(trozosJugador1.remove(0));
                }
                trozosJugador1.add(trozoJugador1);    // Ponemos un nuevo trozo para el jugador 1

                // Mover al jugador 2
                if (numJugadores == 2) {
                    Rectangle trozoJugador2 = new Rectangle(ANCHO, ANCHO);
                    trozoJugador2.setFill(Color.MAGENTA); // Serpiente del jugador 2 de color magenta
                    trozoJugador2.setStroke(Color.BLACK);
                    trozoJugador2.setX(cabezaXJugador2);
                    trozoJugador2.setY(cabezaYJugador2);
                    raiz.getChildren().add(trozoJugador2);
                    if (!trozosJugador2.isEmpty()) {
                        if (trozosJugador2.size() >= tamanoJugador2)
                            // Quitamos el último trozo
                            raiz.getChildren().remove(trozosJugador2.remove(0));
                    }
                    trozosJugador2.add(trozoJugador2);    // Ponemos un nuevo trozo para el jugador 2
                }

                ultimaAnimacion = now;

                // Actualizar el tiempo transcurrido
                long tiempoTranscurrido = (now - tiempoInicio) / 1000000000;
                if (tiempoTranscurrido <= MAX_SEGUNDOS) {
                    segundos = (int) tiempoTranscurrido;
                }
                int minutos = segundos / 60;
                int seg = segundos % 60;
                tiempo.setText("Tiempo: " + minutos + " min " + seg + " seg");

                // Detener el juego después de 60 segundos
                if (segundos >= MAX_SEGUNDOS) {
                    stop();
                }

                // Verificar si hay un ganador
                verificarGanador();
            }
        };

        ventana.show();
        anim.start();
    }

    private void ponerPuntosJugador1() {
        puntosJugador1.setText("Pts J1: " + (tamanoJugador1 - 1));
    }

    private void ponerPuntosJugador2() {
        puntosJugador2.setText("Pts J2: " + (tamanoJugador2 - 1));
    }

    private void ponerFruta() {
        fruta = new Circle(ANCHO / 2 - 1);
        fruta.setFill(Color.LIGHTGREEN);
        double x = Math.rint(Math.random() * (ANCHO_PANTALLA - ANCHO) / ANCHO) * ANCHO + ANCHO / 2;
        double y = Math.rint(Math.random() * (ALTO_PANTALLA - ALTO_MENU - ANCHO) / ANCHO) * ANCHO + ANCHO / 2;
        fruta.setCenterX(x);
        fruta.setCenterY(y);
        raiz.getChildren().add(fruta);
    }

    private void generarPoder() {
        poder = new Circle(ANCHO / 2 - 1);
        poder.setFill(Color.RED);
        double x = Math.rint(Math.random() * (ANCHO_PANTALLA - ANCHO) / ANCHO) * ANCHO + ANCHO / 2;
        double y = Math.rint(Math.random() * (ALTO_PANTALLA - ALTO_MENU - ANCHO) / ANCHO) * ANCHO + ANCHO / 2;
        poder.setCenterX(x);
        poder.setCenterY(y);
        raiz.getChildren().add(poder);
    }

    private void aplicarPoderJugador1() {
        // Aquí podemos meter poderes para el jugador uno
        // Por ejemplo, incrementar la velocidad, aumentar tamaño, etc.

        // Incrementa la velocidad del jugador 1 durante un período de tiempo
        RETARDO_ANIMADOR /= 2; // Dividir el retardo actual por 2 para hacerlo más rápido
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        RETARDO_ANIMADOR *= 2; // Restaurar el retardo original después de un tiempo
                    }
                },
                5000 // Duración del poder en milisegundos (en este caso, 5 segundos)
        );

        tamanoJugador1++; // Incrementa el tamaño del jugador 1
        ponerPuntosJugador1(); // Actualiza los puntos del jugador 1 en pantalla
    }


    private void aplicarPoderJugador2() {
        // Aquí definimos los poderes del jugador dos
        // Por ejemplo, incrementar la velocidad, aumentar tamaño, etc.

        // Incrementa la velocidad del jugador 1 durante un período de tiempo
        RETARDO_ANIMADOR /= 2; // Dividir el retardo actual por 2 para hacerlo más rápido
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        RETARDO_ANIMADOR *= 2; // Restaurar el retardo original después de un tiempo
                    }
                },
                5000 // Duración del poder en milisegundos (en este caso, 5 segundos)
        );

        tamanoJugador2++; // Incrementa el tamaño del jugador 1
        ponerPuntosJugador1(); // Actualiza los puntos del jugador 1 en pantalla
    }



    @Override
    public void handle(KeyEvent event) {
        switch (event.getCode()) {
            case UP:
                if (!dirJugador1.equals(Direccion.Abajo))
                    dirJugador1 = Direccion.Arriba;
                break;
            case DOWN:
                if (!dirJugador1.equals(Direccion.Arriba))
                    dirJugador1 = Direccion.Abajo;
                break;
            case RIGHT:
                if (!dirJugador1.equals(Direccion.Izquierda))
                    dirJugador1 = Direccion.Derecha;
                break;
            case LEFT:
                if (!dirJugador1.equals(Direccion.Derecha))
                    dirJugador1 = Direccion.Izquierda;
                break;
            case W:
                if (!dirJugador2.equals(Direccion.Abajo))
                    dirJugador2 = Direccion.Arriba;
                break;
            case S:
                if (!dirJugador2.equals(Direccion.Arriba))
                    dirJugador2 = Direccion.Abajo;
                break;
            case D:
                if (!dirJugador2.equals(Direccion.Izquierda))
                    dirJugador2 = Direccion.Derecha;
                break;
            case A:
                if (!dirJugador2.equals(Direccion.Derecha))
                    dirJugador2 = Direccion.Izquierda;
                break;
            default:
        }
    }

    private void gameOver() {
        if (!juegoTerminado) {
            anim.stop();
            Text gameOver = new Text("GAME OVER");
            gameOver.setFont(Font.font("Arial", 35));
            gameOver.setFill(Color.WHITE);
            gameOver.setStroke(Color.RED);
            gameOver.setY(ALTO_PANTALLA / 2);
            gameOver.setX(ANCHO_PANTALLA / 2 - 125);
            raiz.getChildren().add(gameOver);
            juegoTerminado = true;
        }
    }

    private void verificarGanador() {
        if (tamanoJugador1 >= 10) {
            mostrarGanador("Jugador 1");
        } else if (tamanoJugador2 >= 10) {
            mostrarGanador("Jugador 2");
        }
    }

    private void mostrarGanador(String jugador) {
        anim.stop();
        Text ganador = new Text(jugador + " GANA");
        ganador.setFont(Font.font("Arial", 35));
        ganador.setFill(Color.WHITE);
        ganador.setStroke(Color.GREEN);
        ganador.setY(ALTO_PANTALLA / 2);
        ganador.setX(ANCHO_PANTALLA / 2 - 125);
        raiz.getChildren().add(ganador);
        juegoTerminado = true;
    }
}
