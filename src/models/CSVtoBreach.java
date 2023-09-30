package models;

import DAO.*;
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
            int code = 0;
            // Pular a primeira linha (cabeçalho)
            br.readLine();
            while ((line = br.readLine()) != null) {
                code++;
                String[] values = line.split(";");
                int id = code;
                String company = values[0];
                long recordsLost = Long.parseLong(values[2]);
                LocalDate date = LocalDate.parse(values[4]);
                String detailedStory = values[5];
                String sector = values[6];
                String method = values[7];
                String[] sectorAndMethod = (sector + "," + method).split(","); // Concatenando sector e method e depois dividindo

                Breach breach = new Breach(id, company, recordsLost, date, detailedStory, sectorAndMethod);
                //inserirRegistro(breach);
                //breaches.add(breach);
            }
        }

        return breaches;
    }
}
