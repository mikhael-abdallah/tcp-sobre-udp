package org.tcp.Destinatario;


import org.tcp.Pacote;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;

public class Destinatario {
    public static int porta = 9123;
    private DatagramSocket socketDestinatario;
    InetAddress enderecoIP;

    private HashMap<Integer, byte[]> bufferDeRecepcao = new HashMap<>();

    Integer numSequenciaEsperado = 0;

    // Destinatário começa com número de sequência 0
    private int destinatarioSeqNum = 0;

    private FileOutputStream fileOutputStream;

    private int janelaRecepcaoDisponivel = 50;

    private int maxBufferRecepcao;

    private double delayLeituraMsPorKB;

    public static int velocidadeTransmissaoKBPorS;

    public Destinatario(int maxBufferRecepcao, int delayLeituraMsPorKB, int velocidadeTransmissaoKBPorS) throws SocketException, UnknownHostException, FileNotFoundException {
        this.socketDestinatario = new DatagramSocket(this.porta);
        this.enderecoIP = InetAddress.getByName("127.0.0.1");
        File outputFile = new File("output.txt");
        this.fileOutputStream = new FileOutputStream(outputFile);
        this.maxBufferRecepcao = maxBufferRecepcao;
        this.delayLeituraMsPorKB = delayLeituraMsPorKB;
        Destinatario.velocidadeTransmissaoKBPorS = velocidadeTransmissaoKBPorS;
    }

    public void aguardaSocket() throws IOException, InterruptedException {

        while (true) {
            System.out.println(Destinatario.velocidadeTransmissaoKBPorS);
            byte[ ] dadosRecebidos = new byte[1024 + 20];
            DatagramPacket pacoteRecebido =
                    new DatagramPacket(dadosRecebidos, dadosRecebidos.length);

            socketDestinatario.receive(pacoteRecebido);

            int tamanhoPacote = pacoteRecebido.getLength();

            dadosRecebidos = pacoteRecebido.getData();

            byte[] cabecalhoTCP = Arrays.copyOfRange(dadosRecebidos, 0, 20);

            byte[] dados = Arrays.copyOfRange(dadosRecebidos, 20, tamanhoPacote);

            Pacote pacoteTCPRecebido = new Pacote(cabecalhoTCP, dados);

            if(pacoteTCPRecebido.getSyn())
                verificaSeEhNovaConexao(pacoteTCPRecebido);
            else
                recebePacote(pacoteTCPRecebido);

        }
    }

    private void recebePacote(Pacote pacote) throws IOException, InterruptedException {
        Integer numSequencia = pacote.getNumeroSequencia();
        Integer tamanho = pacote.dados.length;

        long delayTransmissao = (long) (((float) tamanho / 1024) / ((float) Destinatario.velocidadeTransmissaoKBPorS / 1000));
        Thread.sleep(delayTransmissao);

        System.out.println("recebeu pacote");
        System.out.println(pacote);

        Integer reconhecimentoAEnviar = this.numSequenciaEsperado + tamanho;
        if(!numSequencia.equals(this.numSequenciaEsperado)) {
            reconhecimentoAEnviar = this.numSequenciaEsperado;
            if(bufferDeRecepcao.size() < this.maxBufferRecepcao)
                bufferDeRecepcao.put(numSequencia, pacote.dados);
        } else {
            this.numSequenciaEsperado = reconhecimentoAEnviar;
            this.fileOutputStream.write(pacote.dados);
            while(bufferDeRecepcao.containsKey(this.numSequenciaEsperado)) {
                byte[] dados = this.leBufferRecepcao(this.numSequenciaEsperado);
                this.fileOutputStream.write(dados);
                bufferDeRecepcao.remove(this.numSequenciaEsperado);
                this.numSequenciaEsperado += dados.length;
                reconhecimentoAEnviar = this.numSequenciaEsperado;
            }
        }
        bufferDeRecepcao.remove(reconhecimentoAEnviar);

        Pacote pacoteAEnviar = new Pacote(Destinatario.porta, pacote.getPortaOrigem(), 0, reconhecimentoAEnviar, true, false, false, false, false, false, this.janelaRecepcaoDisponivel);
        this.enviaPacote(pacoteAEnviar);
    }

    private byte[] leBufferRecepcao(int numSequenciaEsperado) throws InterruptedException {
        byte[] dados = bufferDeRecepcao.get(numSequenciaEsperado);
        int bytes = dados.length;
        double delay = (bytes /1024) * this.delayLeituraMsPorKB;
        Thread.sleep((long) delay);
        return dados;
    }

    /*
    Verifica flag SYN, ela indica uma nova conexão.
     */
    public void verificaSeEhNovaConexao(Pacote pacote) throws IOException {
        int portaRemetente = pacote.getPortaOrigem();
        int numSequencia = this.destinatarioSeqNum;
        int numReconhecimento = numSequencia + pacote.dados.length + 1;
        this.numSequenciaEsperado += numReconhecimento;
        Pacote confirmacaoSyn = new Pacote(Destinatario.porta, portaRemetente, this.destinatarioSeqNum, numReconhecimento, false, true, true, false, false, false, this.janelaRecepcaoDisponivel );
        this.enviaPacote(confirmacaoSyn);
    }

    private void enviaPacote(Pacote pacote) throws IOException {
        byte[] segmento = pacote.criaSegmentoDeBytes();
        DatagramPacket pacoteEnviado = new DatagramPacket(segmento, segmento.length, this.enderecoIP, pacote.getPortaDestino());
        this.socketDestinatario.send(pacoteEnviado);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Destinatario destinatario = new Destinatario(50, 10, 250);
        Destinatario.velocidadeTransmissaoKBPorS = 250;


        destinatario.aguardaSocket();
    }

}
