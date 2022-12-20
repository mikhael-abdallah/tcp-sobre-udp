package org.tcp;

import java.io.IOException;
import java.net.UnknownHostException;

public class ThreadEnviaPacote extends Thread {

    private Pacote pacote;

    private Remetente remetente;

    public ThreadEnviaPacote(Remetente remetente, Pacote pacote) throws UnknownHostException {
        this.remetente = remetente;
        this.pacote = pacote;
    }

    @Override
    public void run() {
        try {
            Integer reconhecimentoEsperado = this.pacote.getNumeroSequencia() + this.pacote.dados.length + 1;
            boolean confirmado = false;
            while(!confirmado) {
                remetente.enviaPacote(this.pacote);
                confirmado = this.remetente.reconhecimentoFoiRecebido(reconhecimentoEsperado);
                Thread.sleep(1000); // Aguarda 1 segundo antes de reenviar o pacote.
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
