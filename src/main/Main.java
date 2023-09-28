package main;

import core.CRUDMain;
import models.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main {

    public static void main(String[] args) throws IOException {
        BTree bTree = new BTree(System.getProperty("user.dir") + "/src/dataset/index.btree", 8);
        ExtendedHash exHash = new ExtendedHash(4, System.getProperty("user.dir") + "/src/dataset/buckets.hash", System.getProperty("user.dir") + "/src/dataset/index.hash");
        Registros r = new Registros(System.getProperty("user.dir") + "/src/dataset/breaches.db", bTree, exHash);
        CRUDMain crudMain = new CRUDMain(r);
        crudMain.menu();
//        teste();
    }

    public static void loadTempBase() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.US);
        Breach b1 = new Breach(0, "Shanghai Police", 500000000, LocalDate.parse("01 Jul 2022", formatter), "A database containing records of over a billion Chinese civilians – allegedly stolen from the Shangh...", new String[]{"financial", "hacked"});
        Breach b2 = new Breach(1, "Twitter", 5400000, LocalDate.parse("01 Dec 2022", formatter), "Zero day vulnerability allowed a threat actor to create profiles of 5.4 million Twitter users inc. a...", new String[]{"web", "hacked"});
        Breach b3 = new Breach(2, "Plex", 15000000, LocalDate.parse("01 Aug 2022", formatter), "Intruders access password data, usernames, and emails for at least half of its 30 million users.", new String[]{"web", "hacked"});

        try {
            new FileOutputStream("src/dataset/index.hash").close();
            RandomAccessFile raf = new RandomAccessFile("src/dataset/index.hash", "rw");
            raf.writeLong(0);
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            new FileOutputStream("src/dataset/index.btree").close();
            RandomAccessFile raf = new RandomAccessFile("src/dataset/index.btree", "rw");
            raf.writeLong(0);
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        BTree bTree = new BTree(System.getProperty("user.dir") + "/src/dataset/index.btree", 8);
        ExtendedHash exHash = new ExtendedHash(4, System.getProperty("user.dir") + "/src/dataset/buckets.hash", System.getProperty("user.dir") + "/src/dataset/index.hash");
        Registros r = new Registros(System.getProperty("user.dir") + "/src/dataset/breaches.db", bTree, exHash);
        try {
            r.limparRegistros();
            r.inserirRegistro(b1);
            r.inserirRegistro(b2);
            r.inserirRegistro(b3);
            Breach fakeBreach = new Breach();
            fakeBreach.id = 2;
            fakeBreach.company = "The Walt Disney Company and The official disney channel";
            r.readAllBreaches();
            System.out.println(r.retrieveBreachByBTree(2));
            System.out.println(r.retrieveBreachByExtendedHash(2));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void loadDatabaseInsideIndex(Registros r) {
        r.readAllBreaches();
    }
}
