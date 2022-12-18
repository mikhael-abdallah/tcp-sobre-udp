package org.tcp;


public class Pacote {

    private int num_sequencia;
    private int dado;

    public Pacote(int num_sequencia, int dado) {
        this.num_sequencia = num_sequencia;
        this.dado = dado;
    }

    public Pacote(byte[] bytes) {
        this.num_sequencia = bytes[0];
        this.dado = bytes[1];
    }

    public int getNumSequencia() {
        return this.num_sequencia;
    }

    public void setNumSequencia(int num_sequencia) {
        this.num_sequencia = num_sequencia;
    }

    public int getDado() {
        return this.dado;
    }

    public void setDado(int dado) {
        this.dado = dado;
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) this.num_sequencia;
        bytes[1] = (byte) this.dado;
        return bytes;
    }
}
