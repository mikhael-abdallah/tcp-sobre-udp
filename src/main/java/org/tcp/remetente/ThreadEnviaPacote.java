package org.tcp.remetente;

import org.tcp.Pacote;

import java.io.IOException;
import java.net.UnknownHostException;

public class ThreadEnviaPacote extends Thread {

    public Pacote pacote;

    public Remetente remetente;

    public ThreadEnviaPacote(Remetente remetente, Pacote pacote) throws UnknownHostException {
        this.remetente = remetente;
        this.pacote = pacote;
    }

    @Override
    public void run() {
        try {
            int delayPropagacao = remetente.delayPropagacao;
            remetente.numSequencia += this.pacote.dados.length;
            Integer reconhecimentoEsperado = this.pacote.getNumeroSequencia() + this.pacote.dados.length;
            if(pacote.getSyn()) {
                reconhecimentoEsperado++;
            }
            boolean confirmado = this.remetente.reconhecimentoFoiRecebido(reconhecimentoEsperado);
            while(!confirmado) {
                Thread.sleep(delayPropagacao); // Delay propagação
                remetente.enviaPacote(this.pacote);
                Thread.sleep(1000); // Aguarda 1 segundo antes de reenviar o pacote.
                confirmado = this.remetente.reconhecimentoFoiRecebido(reconhecimentoEsperado);
                if(!confirmado) remetente.setJanelaEnvio(1);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}