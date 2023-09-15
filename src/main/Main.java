package main;

import models.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.US);
        Breach b1 = new Breach(0, "Shanghai Police", 500000000, LocalDate.parse("01 Jul 2022", formatter), "A database containing records of over a billion Chinese civilians – allegedly stolen from the Shangh...", new String[]{"financial", "hacked"});
        Breach b2 = new Breach(1, "Twitter", 5400000, LocalDate.parse("01 Dec 2022", formatter), "Zero day vulnerability allowed a threat actor to create profiles of 5.4 million Twitter users inc. a...", new String[]{"web", "hacked"});
        Breach b3 = new Breach(2, "Plex", 15000000, LocalDate.parse("01 Aug 2022", formatter), "Intruders access password data, usernames, and emails for at least half of its 30 million users.", new String[]{"web", "hacked"});
        Breach b6 = new Breach(1, "Twitt", 5400000, LocalDate.parse("01 Dec 2022", formatter), "Zero day vulnerability allowed a threat actor to create profiles of", new String[]{"web", "hacked"});

        BTree bTree = new BTree(System.getProperty("user.dir") + "dataset/index.btree", 8);
        Registros r = new Registros(System.getProperty("user.dir") + "dataset/breaches.db", bTree);
//        try{
//            new FileOutputStream("src/dataset/index.btree").close();
//            RandomAccessFile raf = new RandomAccessFile("src/dataset/index.btree", "rw");
//            raf.writeLong(0);
//            raf.close();
//        }catch (FileNotFoundException e){
//            System.out.println("Arquivo não encontrado");
//        }catch (IOException e){
//            System.out.println(e.getMessage());
//        }
        try {
            System.out.println(r.retrieveBreach(2));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void loadDatabaseInsideIndex(Registros r) throws Exception {
        r.readAllBreaches();
    }
}
