package DAO;

import java.util.Arrays;
import java.util.Scanner;
import java.io.*;
import java.util.PriorityQueue;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Locale;
import java.text.DecimalFormat;
import models.*;

public class IntercalacaoBalanceadaBreach {

    public static void intercalacaoBalanceada(Breach[] breaches) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o número de blocos: ");
        int numBlocos = scanner.nextInt();
        System.out.print("Digite o número de caminhos: ");
        int numCaminhos = scanner.nextInt();
        int tamanhoBloco = breaches.length / numBlocos;

        // Dividir e ordenar os blocos, salvando em arquivos temporários
        for (int i = 0; i < numBlocos; i++) {
            int inicio = i * tamanhoBloco;
            int fim = Math.min(inicio + tamanhoBloco, breaches.length);
            Arrays.sort(breaches, inicio, fim, (a, b) -> Integer.compare(a.id, b.id));
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("temp" + i + ".bin"))) {
                for (int j = inicio; j < fim; j++) {
                    dos.write(breaches[j].toByteArray());
                }
            }
        }

                // Intercalar os arquivos temporários usando os caminhos
        PriorityQueue<Pair> pq = new PriorityQueue<>((a, b) -> Integer.compare(a.breach.id, b.breach.id));
        DataInputStream[] readers = new DataInputStream[numBlocos];
        byte[] buffer = new byte[1024]; // buffer para leitura

        for (int i = 0; i < numBlocos; i++) {
            readers[i] = new DataInputStream(new FileInputStream("temp" + i + ".bin"));
            if (readers[i].available() > 0) {
                int bytesRead = readers[i].read(buffer);
                Breach breach = new Breach();
                breach.fromByteArray(Arrays.copyOf(buffer, bytesRead));
                pq.add(new Pair(i, breach));
            }
        }

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("output.bin"))) {
            while (!pq.isEmpty()) {
                Pair pair = pq.poll();
                dos.write(pair.breach.toByteArray());
                if (readers[pair.index].available() > 0) {
                    int bytesRead = readers[pair.index].read(buffer);
                    Breach breach = new Breach();
                    breach.fromByteArray(Arrays.copyOf(buffer, bytesRead));
                    pq.add(new Pair(pair.index, breach));
                }
            }
        }

        for (DataInputStream reader : readers) {
            reader.close();
        }
    }

static class Pair {
        int index;
        Breach breach;

        Pair(int index, Breach breach) {
            this.index = index;
            this.breach = breach;
        }
    }

}