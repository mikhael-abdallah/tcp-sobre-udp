package org.tcp;

//Classe do remetente

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

public class Remetente {

    private static int ACK_SIZE = 4;
    private static int WINDOW_SIZE = 10;
    private static int TIMEOUT_MS = 1000;
    private static int PACKET_SIZE = 1024;
    public static int NUM_PACKETS = 50;

    public static void main(String[] args) {

        //Configurações iniciais
        int porta = 5000;
        String destino = "127.0.0.1";
        int num_sequencia = 0;
        int janela_inicial = 0;
        int janela_atual = 0;
        int num_confirmacao = 0;

        //Criando lista de pacotes
        ArrayList<Pacote> pacotes = new ArrayList<Pacote>();
        for (int i=0; i<NUM_PACKETS; i++) {
            Pacote pacote = new Pacote(i, new Random().nextInt(1000));
            pacotes.add(pacote);
        }

        try {
            //Criando socket e envio de dados
            InetAddress endereco = InetAddress.getByName(destino);
            DatagramSocket socket = new DatagramSocket();

            //Enviando pacotes
            for (int i=0; i<NUM_PACKETS; i++) {
                Pacote pacote = pacotes.get(i);

                //Verifica se a janela está cheia
                if (janela_atual == WINDOW_SIZE) {
                    System.out.println("Janela Cheia, aguardando ack");

                    //Aguarda por ACK
                    byte[] bufferAck = new byte[ACK_SIZE];
                    DatagramPacket ackPacket = new DatagramPacket(bufferAck, bufferAck.length);
                    socket.receive(ackPacket);

                    //Atualização da janela
                    num_confirmacao = bufferAck[0];
                    janela_atual = num_confirmacao - janela_inicial;
                }

                //Seleciona pacote a ser enviado
                int num_sequencia_atual = num_sequencia + janela_atual;

                //Envia pacote
                pacote.setNumSequencia(num_sequencia_atual);
                byte[] buffer = pacote.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, endereco, porta);
                socket.send(packet);

                //Exibe mensagem de log
                System.out.println("Pacote enviado: #" + pacote.getNumSequencia());

                //Aumenta janela
                janela_atual++;

                //Atualiza número de sequência
                if (num_sequencia_atual == NUM_PACKETS-1) {
                    num_sequencia = 0;
                    janela_inicial = 0;
                } else {
                    num_sequencia++;
                    janela_inicial++;
                }

                //Aguarda próximo pacote
                Thread.sleep(TIMEOUT_MS);
            }

            //Fecha socket
            socket.close();

            System.out.println("Todos os pacotes foram enviados com sucesso!");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
