package org.tcp;


//Classe do receptor

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receptor {

    private static int ACK_SIZE = 4;
    private static int WINDOW_SIZE = 10;
    private static int PACKET_SIZE = 1024;

    public static void main(String[] args) {

        //Configurações iniciais
        int porta = 5000;

        try {
            //Criando socket e recebendo dados
            DatagramSocket socket = new DatagramSocket(porta);

            //Inicializando variáveis
            int num_sequencia = 0;
            int janela_inicial = 0;
            int janela_atual = 0;
            int num_confirmacao = 0;

            //Recebendo pacotes
            for (int i=0; i<Remetente.NUM_PACKETS; i++) {

                //Verifica se a janela está cheia
                if (janela_atual == WINDOW_SIZE) {
                    System.out.println("Janela Cheia, enviando ack");

                    //Envia ACK
                    byte[] bufferAck = {(byte) num_confirmacao};
                    DatagramPacket ackPacket = new DatagramPacket(bufferAck, bufferAck.length);
                    socket.send(ackPacket);

                    //Atualização da janela
                    num_confirmacao = (num_confirmacao + 1) % Remetente.NUM_PACKETS;
                    janela_atual = num_confirmacao - janela_inicial;
                }

                //Recebe pacote
                byte[] buffer = new byte[PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                Pacote pacote = new Pacote(packet.getData());

                //Exibe mensagem de log
                System.out.println("Pacote recebido: #" + pacote.getNumSequencia());

                //Verifica se o pacote recebido é o esperado
                if (pacote.getNumSequencia() == num_sequencia) {
                    //Aumenta janela
                    janela_atual++;

                    //Atualiza número de sequência
                    if (num_sequencia == Remetente.NUM_PACKETS-1) {
                        num_sequencia = 0;
                        janela_inicial = 0;
                    } else {
                        num_sequencia++;
                        janela_inicial++;
                    }
                }
            }

            //Fecha socket
            socket.close();

            System.out.println("Todos os pacotes foram recebidos com sucesso!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
