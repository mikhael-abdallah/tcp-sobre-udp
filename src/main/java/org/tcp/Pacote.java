package org.tcp;

import java.lang.reflect.Array;
import java.util.Arrays;


public class Pacote {
    byte[] cabecalho = new byte[20];
    public byte[] dados = new byte[0];

    private final int ACK_POSITION = 0;
    private final int RST_POSITION = 1;
    private final int SYN_POSITION = 2;
    private final int FIN_POSITION = 3;
    private final int PSH_POSITION = 4;
    private final int URG_POSITION = 5;


    public Pacote(int portaOrigem, int portaDestino, int numeroSequencia, int numeroReconhecimento, boolean ack, boolean rst, boolean syn,
                  boolean fin, boolean psh, boolean urg, int janelaRecepcao) {
        this.setPortaOrigem(portaOrigem);
        this.setPortaDestino(portaDestino);
        this.setNumeroSequencia(numeroSequencia);
        this.setNumeroReconhecimento(numeroReconhecimento);
        this.setTamanhoCabecalho(5);
        this.setAck(ack);
        this.setRst(rst);
        this.setSyn(syn);
        this.setFin(fin);
        this.setPsh(psh);
        this.setUrg(urg);
        this.setJanelaRecepcao(janelaRecepcao);
    }

    public Pacote(byte[] cabecalho, byte[] dados) {
        this.cabecalho = cabecalho;
        this.dados = dados;
    }

    //métodos de get
    public int getPortaOrigem(){
        return (cabecalho[0] & 0xFF) << 8 | (cabecalho[1] & 0xFF);
    }

    public void setPortaOrigem(int portaOrigem){
        cabecalho[0] = (byte)((portaOrigem >> 8) & 0xff);
        cabecalho[1] = (byte)(portaOrigem & 0xFF);
    }

    public int getPortaDestino(){
        return ((cabecalho[2] & 0xFF) << 8) | (cabecalho[3] & 0xFF);
    }

    public void setPortaDestino(int portaDestino){
        cabecalho[2] = (byte)((portaDestino >> 8) & 0xFF);
        cabecalho[3] = (byte)(portaDestino & 0xFF);
    }

    public int getNumeroSequencia(){
        return ((cabecalho[4] & 0xFF) << 24) |
                ((cabecalho[5] & 0xFF) << 16) |
                ((cabecalho[6] & 0xFF) << 8 ) |
                ((cabecalho[7] & 0xFF) << 0 );
    }

    public void setNumeroSequencia(int numeroSequencia){
        cabecalho[4] = (byte)((numeroSequencia >> 24) & 0xFF);
        cabecalho[5] = (byte)((numeroSequencia >> 16) & 0xFF);
        cabecalho[6] = (byte)((numeroSequencia >> 8) & 0xFF);
        cabecalho[7] = (byte)(numeroSequencia & 0xFF);
    }

    public int getNumeroReconhecimento(){
        return ((cabecalho[8] & 0xFF) << 24) |
                ((cabecalho[9] & 0xFF) << 16) |
                ((cabecalho[10] & 0xFF) << 8 ) |
                ((cabecalho[11] & 0xFF) << 0 );
    }

    public void setNumeroReconhecimento(int numeroReconhecimento){
        cabecalho[8] = (byte)(numeroReconhecimento >> 24);
        cabecalho[9] = (byte)((numeroReconhecimento >> 16) & 0xff);
        cabecalho[10] = (byte)((numeroReconhecimento >> 8) & 0xff);
        cabecalho[11] = (byte)(numeroReconhecimento & 0xff);
    }

    public int getTamanhoCabecalho(){
        return (cabecalho[12] & 0xF0) >> 4;
    }


    /**
     * @param tamanhoCabecalho é o tamanho em palavras de 32 bits
     *                         Usaremos 5 palavras de 32 bits (20 bytes) como padrão.
     */
    public void setTamanhoCabecalho(int tamanhoCabecalho){
        tamanhoCabecalho = 5;
        cabecalho[12] = (byte)(tamanhoCabecalho << 4 | 0);
    }

    public boolean getAck(){
        return this.getFlagBit(cabecalho[13], this.ACK_POSITION);
    }

    public void setAck(boolean ack){
        cabecalho[13] = this.setFlagBit(cabecalho[13], this.ACK_POSITION, ack);
    }

    public boolean getRst(){
        return this.getFlagBit(cabecalho[13], this.RST_POSITION);
    }

    public void setRst(boolean rst){
        cabecalho[13] = this.setFlagBit(cabecalho[13], this.RST_POSITION, rst);
    }

    public boolean getSyn(){
        return this.getFlagBit(cabecalho[13], this.SYN_POSITION);
    }

    public void setSyn(boolean syn){
        cabecalho[13] = this.setFlagBit(cabecalho[13], this.SYN_POSITION, syn);
    }

    public boolean getFin(){
        return this.getFlagBit(cabecalho[13], this.FIN_POSITION);
    }

    public void setFin(boolean fin){
        cabecalho[13] = this.setFlagBit(cabecalho[13], this.FIN_POSITION, fin);
    }

    public boolean getPsh(){
        return this.getFlagBit(cabecalho[13], this.PSH_POSITION);
    }

    public void setPsh(boolean psh){
        cabecalho[13] = this.setFlagBit(cabecalho[13], this.PSH_POSITION, psh);
    }

    public boolean getUrg(){
        return this.getFlagBit(cabecalho[13], this.URG_POSITION);
    }
    public void setUrg(boolean urg){
        cabecalho[13] = this.setFlagBit(cabecalho[13], this.URG_POSITION, urg);
    }

    public int getJanelaRecepcao(){
        return (cabecalho[14] & 0xFF) << 8 | (cabecalho[15] & 0xFF);
    }

    public void setJanelaRecepcao(int janelaRecepcao){
        cabecalho[14] = (byte)((janelaRecepcao >> 8) & 0xff);
        cabecalho[15] = (byte)(janelaRecepcao & 0xFF);
    }

    // Soma de verificação e ponteiro de urgência usa os bytes 16 a 19, mas não serão implentados.


    private byte setFlagBit(byte _byte, int bitPosition, boolean bitValue) {
        if(bitValue)
            return (byte) (_byte | (1 << bitPosition));
        return (byte) (_byte & ~(1 << bitPosition));
    }

    public Boolean getFlagBit(byte _byte, int bitPosition)
    {
        return (_byte & (1 << bitPosition)) != 0;
    }

    public byte[] criaSegmentoDeBytes() {
        Class<?> compType1 = this.cabecalho.getClass().getComponentType();

        int len1 = Array.getLength(this.cabecalho);
        int len2 = Array.getLength(this.dados);

        byte[] result = (byte[]) Array.newInstance(compType1, len1 + len2);

        System.arraycopy(this.cabecalho, 0, result, 0, len1);
        System.arraycopy(this.dados, 0, result, len1, len2);

        return result;
    }

    @Override
    public String toString() {
        return "Pacote{" +
                "cabecalho={" +
                "portaOrigem=" + this.getPortaOrigem() +
                ", portaDestino=" + this.getPortaDestino() +
                ", numeroSequencia="+ this.getNumeroSequencia() +
                ", numeroReconhecimento="+ this.getNumeroReconhecimento() +
                ", comprimentoCabecalho=" + this.getTamanhoCabecalho() +
                ", URG=" + this.getUrg() +
                ", ACK=" + this.getAck() +
                ", PSH=" + this.getPsh() +
                ", RST=" + this.getRst() +
                ", SYN=" + this.getSyn() +
                ", FIN=" + this.getFin() +
                '}';
    }
}