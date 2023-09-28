package core;

import models.Breach;
import models.Registros;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class CRUDMain {

    final private Scanner sc = new Scanner(System.in);
    final Registros registros;

    final CRUDSequencial crudSequencial;
    final CRUDHash crudHash;
    final CRUDBTree crudBTree;
    final CRUDInvertedList crudInvertedList;
    static boolean loaded = false;
    CRUDMain(Registros registros){
        this.registros = registros;
        this.crudSequencial = new CRUDSequencial(registros);
        this.crudHash = new CRUDHash(registros);
        this.crudBTree = new CRUDBTree(registros);
        this.crudInvertedList = new CRUDInvertedList(registros);
    }

    public void menu() throws IOException {
        int op;
        do{
            printMenu();
            String input = sc.nextLine();
            while (input.length() > 1 || input.charAt(0) < '0' || input.charAt(0) > '9'){
                printMenu();
                input = sc.nextLine();
            }
            op = Integer.parseInt(input);
            switch (op) {
                case 1:
                    crudSequencial.menu();
                    break;
                case 2:
                    crudBTree.menu();
                    break;
                case 3:
                    crudHash.menu();
                    break;
                case 4:
                    crudInvertedList.menu();
                    break;
                case 5:
                    System.out.println("Tem certeza que deseja deletar toda a base de registros?\n(0)Não\n(1)Sim");
                    String opInputLimparRegistros = sc.nextLine();
                    while (opInputLimparRegistros.length() > 1 || opInputLimparRegistros.charAt(0) < '0' || opInputLimparRegistros.charAt(0) > '1'){
                        System.out.println("Tem certeza que deseja deletar toda a base de registros?\n(0)Não\n(1)Sim");
                        opInputLimparRegistros = sc.nextLine();
                    }
                    int opClear = Integer.parseInt(opInputLimparRegistros);
                    if(opClear == 1){
                        registros.limparRegistros();
                    }
                    break;
                default:
                    break;
            }
        }while(op != 0);
    }

    private void printMenu() {
        System.out.println("\tBEM VINDO AO CRUD DE BREACHS\nEscolha sua ação:\n");
        System.out.println("(1) Utilizar o CRUD Sequencial");
        System.out.println("(2) Utilizar o CRUD Indexado por BTree");
        System.out.println("(3) Utilizar o CRUD Indexado por Hash Extendido");
        System.out.println("(4) Utilizar o CRUD Indexado por Lista Invertida");
        System.out.println("(5) Limpar todos os registros");
        System.out.println(loaded ? "" : "(6) Carregar os registros da base");
        System.out.println("\n (0) Sair");
    }
}
