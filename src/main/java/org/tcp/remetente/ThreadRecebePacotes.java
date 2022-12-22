package org.tcp.remetente;

import java.io.IOException;
import java.net.UnknownHostException;

public class ThreadRecebePacotes extends Thread {

    private Remetente remetente;

    public ThreadRecebePacotes(Remetente remetente) throws UnknownHostException {
        this.remetente = remetente;
    }

    @Override
    public void run() {
        try {
            while(true) {
                remetente.recebePacote();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
