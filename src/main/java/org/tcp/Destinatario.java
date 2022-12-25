package org.tcp;


import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Destinatario {
    public static int porta = 9123;
    private DatagramSocket socketDestinatario;
    InetAddress enderecoIP;

    private HashMap<Integer, byte[]> pacotesRecebidos = new HashMap<>();

    Integer numSequenciaEsperado = 0;

    // Destinatário começa com número de sequência 0
    private int destinatarioSeqNum = 0;

    private FileOutputStream fileOutputStream;

    private int tamanho_buffer = 1024 * 50;

    // TODO: simular buffer de recepção e atualizar o valor da janela de recepção
    private int janelaRecepcaoDisponivel = 50;

    Destinatario() throws SocketException, UnknownHostException, FileNotFoundException {
        this.socketDestinatario = new DatagramSocket(this.porta);
        this.enderecoIP = InetAddress.getByName("127.0.0.1");
        File outputFile = new File("output.txt");
        this.fileOutputStream = new FileOutputStream(outputFile);
    }

    public void aguardaSocket() throws IOException {

        while (true) {
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


            System.out.println(pacoteTCPRecebido);
        }
    }

    private void recebePacote(Pacote pacote) throws IOException {
        Integer numSequencia = pacote.getNumeroSequencia();
        Integer tamanho = pacote.dados.length;
        System.out.println("recebeu pacote");
        System.out.println(pacote);

        Integer reconhecimentoAEnviar = this.numSequenciaEsperado + tamanho;
        if(!numSequencia.equals(this.numSequenciaEsperado)) {
            reconhecimentoAEnviar = this.numSequenciaEsperado;
            pacotesRecebidos.put(numSequencia, pacote.dados);
        } else {
            this.numSequenciaEsperado = reconhecimentoAEnviar;
            this.fileOutputStream.write(pacote.dados);
            while(pacotesRecebidos.containsKey(this.numSequenciaEsperado)) {
                byte[] dados = pacotesRecebidos.get(this.numSequenciaEsperado);
                this.fileOutputStream.write(dados);
                this.numSequenciaEsperado += dados.length;
                reconhecimentoAEnviar = this.numSequenciaEsperado;
            }
        }

        Pacote pacoteAEnviar = new Pacote(Destinatario.porta, pacote.getPortaOrigem(), 0, reconhecimentoAEnviar, true, false, false, false, false, false, this.janelaRecepcaoDisponivel);
        this.enviaPacote(pacoteAEnviar);
    }

    /*
    Verifica flag SYN, ela indica uma nova conexão.
     */
    public void verificaSeEhNovaConexao(Pacote pacote) throws IOException {
        int portaRemetente = pacote.getPortaOrigem();
        int numSequencia = this.destinatarioSeqNum;
        int numReconhecimento = numSequencia + pacote.dados.length + 1;
        this.numSequenciaEsperado += numReconhecimento;
        System.out.println("num esperado");
        System.out.println(this.numSequenciaEsperado);
        Pacote confirmacaoSyn = new Pacote(Destinatario.porta, portaRemetente, this.destinatarioSeqNum, numReconhecimento, false, true, true, false, false, false, this.janelaRecepcaoDisponivel );
        this.enviaPacote(confirmacaoSyn);
    }

    private void enviaPacote(Pacote pacote) throws IOException {
        byte[] segmento = pacote.criaSegmentoDeBytes();
        DatagramPacket pacoteEnviado = new DatagramPacket(segmento, segmento.length, this.enderecoIP, pacote.getPortaDestino());
        this.socketDestinatario.send(pacoteEnviado);
    }

    public static void main(String[] args) throws IOException {
        Destinatario destinatario = new Destinatario();
        destinatario.aguardaSocket();
    }

    /*
    Implementar Buffer de recepção.
     */

}
