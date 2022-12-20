package org.tcp;


import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Destinatario {
    public static int porta = 9123;
    private DatagramSocket socketDestinatario;
    InetAddress enderecoIP;


    // Destinatário começa com número de sequência 0
    private int destinatarioSeqNum = 0;

    private int tamanho_buffer = 1024 * 50;

    Destinatario() throws SocketException, UnknownHostException {
        this.socketDestinatario = new DatagramSocket(this.porta);
        this.enderecoIP = InetAddress.getByName("127.0.0.1");

    }

    public void aguardaSocket() throws IOException {

        while (true) {
            byte[ ] dadosRecebidos = new byte[1024 + 20];
            DatagramPacket pacoteRecebido =
                    new DatagramPacket(dadosRecebidos, dadosRecebidos.length);

            socketDestinatario.receive(pacoteRecebido);

            dadosRecebidos = pacoteRecebido.getData();

            byte[] cabecalhoTCP = Arrays.copyOfRange(dadosRecebidos, 0, 20);

            byte[] dados = Arrays.copyOfRange(dadosRecebidos, 20, 1024 + 20);

            Pacote pacoteTCPRecebido = new Pacote(cabecalhoTCP, dados);

            verificaSeEhNovaConexao(pacoteTCPRecebido);

            System.out.println(pacoteTCPRecebido);
        }
    }

    /*
    Verifica flag SYN, ela indica uma nova coneção.
     */
    public void verificaSeEhNovaConexao(Pacote pacote) throws IOException {
        if(!pacote.getSyn()) return;

        int portaRemetente = pacote.getPortaOrigem();
        int numSequencia = this.destinatarioSeqNum;
        int numReconhecimento = numSequencia + 1 + pacote.dados.length;
        Pacote confirmacaoSyn = new Pacote(Destinatario.porta, portaRemetente, this.destinatarioSeqNum, numReconhecimento, false, true, true, false, false, false, 0 );
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
