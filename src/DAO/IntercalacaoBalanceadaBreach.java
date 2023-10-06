//Arthur L F Pfeilsticker - 617553
//Leonardo B Marques - 793952
package DAO;

// Importações de classes e pacotes necessários para o funcionamento do código.
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.util.PriorityQueue;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Locale;
import java.text.DecimalFormat;
import models.*;
import java.util.*;

// Classe pública que realiza a intercalação balanceada de "Breach".
public class IntercalacaoBalanceadaBreach {

    // Método público que realiza a intercalação balanceada de uma lista de arrays de "Breach".
    public static List<Breach> intercalacaoBalanceada(List<Breach[]> breachArrays) {
        // Lista que armazenará o resultado da intercalação.
        List<Breach> result = new ArrayList<>();
        
        // Fila de prioridade que ajudará na intercalação dos "Breach".
        PriorityQueue<BreachMergeHelper> pq = new PriorityQueue<>(Comparator.comparingInt(b -> b.breach.id));

        // Loop para inicializar a fila de prioridade com o primeiro "Breach" de cada array.
        for (int i = 0; i < breachArrays.size(); i++) {
            if (breachArrays.get(i).length > 0 && breachArrays.get(i)[0] != null) {
                pq.add(new BreachMergeHelper(breachArrays.get(i)[0], i, 0));
            }
        }

        // Enquanto a fila de prioridade não estiver vazia, continue a intercalação.
        while (!pq.isEmpty()) {
            // Retire o "Breach" de menor ID da fila.
            BreachMergeHelper current = pq.poll();
            result.add(current.breach);

            // Verifique o próximo "Breach" no array atual e, se existir, adicione-o à fila de prioridade.
            int nextIndex = current.arrayIndex + 1;
            if (nextIndex < breachArrays.get(current.listIndex).length && breachArrays.get(current.listIndex)[nextIndex] != null) {
                pq.add(new BreachMergeHelper(breachArrays.get(current.listIndex)[nextIndex], current.listIndex, nextIndex));
            }
        }

        // Retorne a lista resultante da intercalação.
        return result;
    }

    // Classe auxiliar para ajudar na intercalação dos "Breach".
    static class BreachMergeHelper {
        Breach breach;       // Objeto "Breach".
        int listIndex;       // Índice da lista de arrays.
        int arrayIndex;      // Índice dentro do array atual.

        // Construtor da classe auxiliar.
        public BreachMergeHelper(Breach breach, int listIndex, int arrayIndex) {
            this.breach = breach;
            this.listIndex = listIndex;
            this.arrayIndex = arrayIndex;
        }
    }
}
