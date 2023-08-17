package main;

import models.Breach;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main {
    public static void main(String[] args) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.US);
        Breach b1 = new Breach(0, "Shanghai Police", 500000000, LocalDate.parse( "01 Jul 2022", formatter), "A database containing records of over a billion Chinese civilians – allegedly stolen from the Shangh...", new String[]{"financial", "hacked"});
        Breach b2 = new Breach(1, "Twitter", 5400000, LocalDate.parse( "01 Dec 2022", formatter), "Zero day vulnerability allowed a threat actor to create profiles of 5.4 million Twitter users inc. a...", new String[]{"web", "hacked"});
        Breach b3 = new Breach(2, "Plex", 15000000, LocalDate.parse( "01 Aug 2022", formatter), "Intruders access password data, usernames, and emails for at least half of its 30 million users.", new String[]{"web", "hacked"});
        Breach b_temp = new Breach();

        FileOutputStream arq;
        DataOutputStream dos;

        FileInputStream arq2;
        DataInputStream dis;

        byte[] ba;
        int len;

        try {

            arq = new FileOutputStream("dataset/breaches.db");
            dos = new DataOutputStream(arq);

            ba = b1.toByteArray();
            dos.writeInt(ba.length); //Tamano do registro em bytes
            dos.write(ba);

            ba = b2.toByteArray();
            dos.writeInt(ba.length);
            dos.write(ba);

            ba = b3.toByteArray();
            dos.writeInt(ba.length);
            dos.write(ba);

            dos.close();

            arq2 =  new FileInputStream("dataset/breaches.db");
            dis = new DataInputStream(arq2);

            len = dis.readInt(); //Tamano do registro em bytes
            ba = new byte[len];
            dis.read(ba);
            b_temp.fromByteArray(ba);
            System.out.println(b_temp);

            len = dis.readInt();
            ba = new byte[len];
            dis.read(ba);
            b_temp.fromByteArray(ba);
            System.out.println(b_temp);

            len = dis.readInt();
            ba = new byte[len];
            dis.read(ba);
            b_temp.fromByteArray(ba);
            System.out.println(b_temp);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
