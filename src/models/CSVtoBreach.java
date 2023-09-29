package models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CSVtoBreach {
    public static List<Breach> convertCSVtoBreach(String csvFilePath) throws IOException {
        List<Breach> breaches = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            // Pular a primeira linha (cabeçalho)
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                int id = Integer.parseInt(values[0]);
                String company = values[1];
                long recordsLost = Long.parseLong(values[2]);
                LocalDate date = LocalDate.parse(values[3]);
                String detailedStory = values[4];
                String[] sectorAndMethod = values[5].split(";"); // Supondo que os setores e métodos são separados por ponto e vírgula dentro da mesma coluna

                Breach breach = new Breach(id, company, recordsLost, date, detailedStory, sectorAndMethod);
                breaches.add(breach);
            }
        }

        return breaches;
    }
}
