package org.tcp.Main;

import org.tcp.Destinatario.Destinatario;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ThreadInicializaDestinatario extends Thread{
    public Destinatario destinatario;

    ThreadInicializaDestinatario(int maxBufferRecepcao, int delayLeituraMsPorKB, int velocidadeTransmissaoKBPorS) throws SocketException, UnknownHostException, FileNotFoundException {
        this.destinatario = new Destinatario(maxBufferRecepcao, delayLeituraMsPorKB, velocidadeTransmissaoKBPorS);
        Destinatario.velocidadeTransmissaoKBPorS = velocidadeTransmissaoKBPorS;
    }

    @Override
    public void run() {
        try {
            destinatario.aguardaSocket();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
