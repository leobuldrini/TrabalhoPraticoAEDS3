package DAO;

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

public class IntercalacaoBalanceadaBreach {

    public static List<Breach> intercalacaoBalanceada(List<Breach[]> breachArrays) {
        List<Breach> result = new ArrayList<>();
        PriorityQueue<BreachMergeHelper> pq = new PriorityQueue<>(Comparator.comparingInt(b -> b.breach.id));

        for (int i = 0; i < breachArrays.size(); i++) {
            if (breachArrays.get(i).length > 0 && breachArrays.get(i)[0] != null) {
                pq.add(new BreachMergeHelper(breachArrays.get(i)[0], i, 0));
            }
        }

        while (!pq.isEmpty()) {
            BreachMergeHelper current = pq.poll();
            result.add(current.breach);

            int nextIndex = current.arrayIndex + 1;
            if (nextIndex < breachArrays.get(current.listIndex).length && breachArrays.get(current.listIndex)[nextIndex] != null) {
                pq.add(new BreachMergeHelper(breachArrays.get(current.listIndex)[nextIndex], current.listIndex, nextIndex));
            }
        }

        return result;
    }

    static class BreachMergeHelper {
        Breach breach;
        int listIndex;
        int arrayIndex;

        public BreachMergeHelper(Breach breach, int listIndex, int arrayIndex) {
            this.breach = breach;
            this.listIndex = listIndex;
            this.arrayIndex = arrayIndex;
        }
    }
}
