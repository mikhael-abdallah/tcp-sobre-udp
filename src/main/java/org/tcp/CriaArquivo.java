package org.tcp;

import java.io.*;

public class CriaArquivo {

    public  static File criarArquivo(long fileSize) throws IOException {
        File fout = new File("file.txt");
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        int i = 0;
        while(fout.length() < fileSize) {
            bw.write(Integer.toString(i));
            bw.newLine();
            i++;
        }

        bw.close();

        return new File("file.txt");
    }
    public static void main(String[] args) throws IOException {
        File fout = new File("file.txt");
        FileOutputStream fos = new FileOutputStream(fout);

        long fileSize = 583000; //100MB

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        int i = 0;
        while(fout.length() < fileSize) {
            bw.write(Integer.toString(i));
            bw.newLine();
            i++;
        }

        bw.close();
    }
}
