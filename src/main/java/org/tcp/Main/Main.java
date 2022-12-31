package org.tcp.Main;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ThreadInicializaRemetente threadInicializaRemetente = new ThreadInicializaRemetente(0, 50, 1, 500, 588000);
        ThreadInicializaDestinatario threadInicializaDestinatario = new ThreadInicializaDestinatario(50, 1, 500);
        threadInicializaDestinatario.start();
        threadInicializaRemetente.start();
    }
}
