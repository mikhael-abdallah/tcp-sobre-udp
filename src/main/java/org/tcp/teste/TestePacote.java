package org.tcp.teste;

import org.tcp.Pacote;

public class TestePacote {
    public static  void main(String[] args) {
        Pacote pacote = new Pacote(55536, 55537, 1294967292, 23796721, true,
                false, true, false, false, true, 4521);

        System.out.println(pacote.getPortaOrigem());
        System.out.println(pacote.getPortaDestino());
        System.out.println(pacote.getNumeroSequencia());
        System.out.println(pacote.getNumeroReconhecimento());
        System.out.println(pacote.getTamanhoCabecalho());
        System.out.println(pacote.getAck());
        System.out.println(pacote.getRst());
        System.out.println(pacote.getSyn());
        System.out.println(pacote.getFin());
        System.out.println(pacote.getPsh());
        System.out.println(pacote.getUrg());
        System.out.println(pacote.getJanelaRecepcao());
    }
}
