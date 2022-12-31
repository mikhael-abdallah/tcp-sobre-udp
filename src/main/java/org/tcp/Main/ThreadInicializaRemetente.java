package org.tcp.Main;

import org.tcp.CriaArquivo;
import org.tcp.Destinatario.Destinatario;
import org.tcp.remetente.Remetente;
import org.tcp.remetente.ThreadRecebePacotes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ThreadInicializaRemetente extends Thread{
    public Remetente remetente;

    ThreadInicializaRemetente(int probabilidadePerda, int maxTamJanela, int delayPropagacao, int velocidadeTransmissaoKBPorS, long tamArquivo) throws IOException {
        this.remetente = new Remetente(probabilidadePerda, maxTamJanela, delayPropagacao);
        Remetente.velocidadeTransmissaoKBPorS = velocidadeTransmissaoKBPorS;

        CriaArquivo.criarArquivo(tamArquivo);

    }

    @Override
    public void run() {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get("file.txt"));

            ThreadRecebePacotes threadRecebePacotes = new ThreadRecebePacotes(remetente);
            threadRecebePacotes.start();

            remetente.startTime = System.currentTimeMillis();

            remetente.estabeleceConexao();

            remetente.enviaMensagem(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
