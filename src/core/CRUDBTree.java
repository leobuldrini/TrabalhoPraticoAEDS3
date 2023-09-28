package core;

import models.Registros;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

public class CRUDBTree {
    final private Scanner sc = new Scanner(System.in);
    final Registros registros;
    final private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.US);
    static boolean loaded = false;

    public CRUDBTree(Registros registros){
        this.registros = registros;
    }

    public void menu(){}
}
