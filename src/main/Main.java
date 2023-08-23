package main;

import models.Breach;
import models.Registros;

import java.io.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main {
    public static void main(String[] args){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.US);
        Breach b1 = new Breach(0,"Shanghai Police", 500000000, LocalDate.parse( "01 Jul 2022", formatter), "A database containing records of over a billion Chinese civilians – allegedly stolen from the Shangh...", new String[]{"financial", "hacked"});
        Breach b2 = new Breach(1,"Twitter", 5400000, LocalDate.parse( "01 Dec 2022", formatter), "Zero day vulnerability allowed a threat actor to create profiles of 5.4 million Twitter users inc. a...", new String[]{"web", "hacked"});
        Breach b3 = new Breach(2, "Plex", 15000000, LocalDate.parse( "01 Aug 2022", formatter), "Intruders access password data, usernames, and emails for at least half of its 30 million users.", new String[]{"web", "hacked"});
        Breach b6 = new Breach(1,"Twitt", 5400000, LocalDate.parse( "01 Dec 2022", formatter), "Zero day vulnerability allowed a threat actor to create profiles of", new String[]{"web", "hacked"});
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        Registros r = new Registros("src/dataset/breaches.db");
        try{
            r.limparRegistros();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

        r.inserirRegistro(b1);
        r.inserirRegistro(b2);
        r.inserirRegistro(b3);
        r.readAllBreaches();
        Breach removed = r.deletarRegistro(1);
        System.out.println("REMOVI AQUI Ó " + removed.id);
        r.readAllBreaches();
        r.inserirRegistro(b6);
        r.readAllBreaches();
    }
}
