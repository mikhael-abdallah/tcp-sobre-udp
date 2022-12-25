package org.tcp.remetente;

/*
Mecanismos de transferência confiável de dados e sua utilização:

Temporizador: Usado para controlar a temporização/retransmissão de um pacote.

Número de sequência: Usado para numeração sequencial de pacotes de dados que transitam do remetente ao destinatário.

Reconhecimento: Usado pelo destinatário para avisar o remetente que um pacote ou conjunto de pacotes foi recebido
corretamente.

Reconhecimento negativo: Usado pelo destinatário para avisar o remetente que um pacote não foi recebido corretamente.

Janela, paralelismo: O remetente pode ficar restrito a enviar somente pacotes com números de sequência que caiam dentro de uma
determinada faixa. Permitindo que vários pacotes sejam transmitidos, ainda que não reconhecidos, a utilização
do remetente pode ser aumentada em relação ao modo de operação pare e espere.
 */

import org.tcp.Destinatario;
import org.tcp.Pacote;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public  class Remetente {

    private final int MSS = 1024; // Tamanho máximo de segmento: 1024 bytes para dados. MSS não inclui tamanho do cabeçalho TCP.
    private final int TAM_CABECALHO = 20;
    private int portaDestino = Destinatario.porta;
    private int portaOrigem;
     final InetAddress enderecoIP = InetAddress.getByName("127.0.0.1");
    private DatagramSocket socket;

    // Atributo usado dar ao remetente o espaço de buffer livre disponível no destinatário
    Integer janelaRecepcao = 0;
    Integer reconhecimentoAtual = 0;

    Integer numSequencia = 0;

    Integer janelaEnvio = 1;


    Remetente() throws SocketException, UnknownHostException {
        this.socket= new DatagramSocket();
        this.portaOrigem = this.socket.getLocalPort();
    }

    /*
    Apresentação de três vias (3-way handshake).
    O cliente primeiro envia um segmento TCP especial; o servidor responde com um segundo segmento TCP especial e, por fim, o cliente
responde novamente com um terceiro segmento especial.
     */
    public void estabeleceConexao() throws IOException {
        /*
        Etapa 1 da conexão: O lado cliente do TCP primeiro envia um segmento TCP especial ao lado servidor do TCP. Esse
segmento não contém nenhum dado de camada de aplicação, mas um dos bits de flag no seu cabeçalho, o bit SYN, é ajustado para 1.
         */
        this.enviaSegmentoSYN();
    }


    public void recebePacote() throws IOException {
        byte[ ] dadosRecebidos = new byte[this.MSS + this.TAM_CABECALHO];
        DatagramPacket pacoteRecebido =
                new DatagramPacket(dadosRecebidos, dadosRecebidos.length);

        socket.receive(pacoteRecebido);

        dadosRecebidos = pacoteRecebido.getData();

        byte[] cabecalhoTCP = Arrays.copyOfRange(dadosRecebidos, 0, this.TAM_CABECALHO);

        byte[] dados = Arrays.copyOfRange(dadosRecebidos, this.TAM_CABECALHO, this.MSS + this.TAM_CABECALHO);

        Pacote pacoteTCPRecebido = new Pacote(cabecalhoTCP, dados);

        Integer reconhecimentoRecebido = pacoteTCPRecebido.getNumeroReconhecimento();
        if(reconhecimentoRecebido > this.reconhecimentoAtual) {
            this.janelaEnvio += (reconhecimentoRecebido - this.reconhecimentoAtual) / this.MSS;
            this.janelaEnvio = Math.max(this.janelaEnvio, 50);
        }

        this.reconhecimentoAtual = Math.max(this.reconhecimentoAtual, pacoteTCPRecebido.getNumeroReconhecimento());

        this.janelaRecepcao = pacoteTCPRecebido.getJanelaRecepcao();
    }

    private void enviaSegmentoSYN() throws IOException {
        int numSequenciaInicial = 0;
        Pacote pacoteSYN = new Pacote(this.portaOrigem, this.portaDestino, numSequenciaInicial,0, false, false, true, false, false, false, 0);
        this.criaThreadEnviaPacote(pacoteSYN);
    }

    public void criaThreadEnviaPacote(Pacote pacote) throws UnknownHostException {
        ThreadEnviaPacote thread = new ThreadEnviaPacote(this, pacote);
        thread.start();
    }

    public void enviaPacote(Pacote pacote) throws IOException {
        byte[] segmento = pacote.criaSegmentoDeBytes();
        DatagramPacket pacoteEnviado = new DatagramPacket(segmento, segmento.length, enderecoIP, pacote.getPortaDestino());
        socket.send(pacoteEnviado);
        System.out.println("enviado");
    }

    public void enviaMensagem(byte[] mensagem) throws UnknownHostException {
        /*
        TODO:
            Criar uma thread para o remetente já ir adicionando a mensagem que será enviada no buffer. Implementar limitações
            para não ultrapassar o tamanho máximo do buffer do remetente
         */

        byte[][] segmentos = this.separaSegmentos(mensagem);

        Integer numSequencia = 1;

        // Prepara as threads que serão disparadas para o envio.
        Queue<ThreadEnviaPacote> threadEnviaPacotes = new LinkedList<>();
        for(byte[] segmento: segmentos) {
            Pacote pacote = new Pacote(this.portaOrigem, this.portaDestino, numSequencia, 0, false, false, false, false, false, false, 0);
            pacote.dados = segmento;
            numSequencia += segmento.length;
            threadEnviaPacotes.add(new ThreadEnviaPacote(this, pacote));
        }

        while (threadEnviaPacotes.size() != 0) {
            int janelaAtual = this.janelaEnvio;
            int reconhecimentoAtual = this.reconhecimentoAtual;
            int maxNumSequencia = reconhecimentoAtual + janelaAtual * this.MSS;

            Pacote pacote = threadEnviaPacotes.element().pacote;
            int numSequenciaEnv = pacote.getNumeroSequencia();
            if(numSequenciaEnv < maxNumSequencia) {
                ThreadEnviaPacote thread = threadEnviaPacotes.poll();
                thread.start();
            }

        }
    }

    private byte[][] separaSegmentos(byte[] mensagem) {
        int numSegmentos = (int) Math.ceil((double)mensagem.length / this.MSS);
        int restoBytes = mensagem.length % this.MSS;
        boolean ultimaLinhaIncompleta = (restoBytes > 0);
        int count = 0;

        byte[][] segmentos = new byte[numSegmentos][];

        for(int segmento = 0; segmento < numSegmentos; segmento++) {
            int bytesNaLinha;
            if(ultimaLinhaIncompleta && segmento == numSegmentos - 1) {
                bytesNaLinha = restoBytes;
            } else {
                bytesNaLinha = this.MSS;
            }
            segmentos[segmento] = new byte[bytesNaLinha];
            for(int col = 0; col < bytesNaLinha; col++) {
                segmentos[segmento][col] = mensagem[count];
                count++;
            }
        }

        return segmentos;
    }


    public boolean reconhecimentoFoiRecebido(Integer numReconhecimento) {
        System.out.println(this.reconhecimentoAtual);
        System.out.println(numReconhecimento);
        return this.reconhecimentoAtual >= numReconhecimento;
    }
    /*
    Implementar Buffer de envio
     */

    public static void main(String[] args) throws IOException {
        Remetente remetente = new Remetente();

        byte[] bytes = Files.readAllBytes(Paths.get("file.txt")); // 588kB para serem enviados



        ThreadRecebePacotes threadRecebePacotes = new ThreadRecebePacotes(remetente);
        threadRecebePacotes.start();
        remetente.estabeleceConexao();

        remetente.enviaMensagem(bytes);
    }

}