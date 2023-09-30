package core;

import DAO.Registros;
import models.Breach;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

public class CRUDInvertedListSector {

    final private Scanner sc = new Scanner(System.in);
    final Registros registros;
    final private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.US);

    public CRUDInvertedListSector(Registros registros){
        this.registros = registros;
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
                    registros.listAllSectors();
                    break;
                case 2:
                    registros.listAllSectors();
                    String inputSearch = "";
                    do{
                        System.out.print("Digite a palavra que será buscada: ");
                        inputSearch = sc.nextLine();
                    }while (!registros.checkIfSectorExistInIndex(inputSearch));
                    Breach[] results = registros.retrieveBreachesBySector(inputSearch);
                    for(int i = 0; i < results.length; i++){
                        System.out.println(results[i]);
                    }
                    break;
                case 3:
                    String inputAddWord;
                    System.out.print("Digite a palavra que será adicionada: ");
                    inputAddWord = sc.nextLine();
                    boolean added = registros.addSectorToIndex(inputAddWord);
                    System.out.println(added ? "Palavra " + inputAddWord +  " adicionada ao índice" : "Palavra já existente no índice");
                    break;
                case 4:
                    System.out.print("Digite a palavra que deseja remover do banco de dados: ");
                    String inputDelete = sc.nextLine();
                    if(registros.checkIfSectorExistInIndex(inputDelete)){
                        registros.removeSectorFromIndex(inputDelete);
                    }else{
                        System.out.println("Palavra não existente na base de dados");
                    }
                    break;
                default:
                    break;
            }
        }while(op != 0);
    }

    private void printMenu() {
        System.out.println("\t\n\nBEM VINDO AO CRUD INDEXADO POR LISTA INVERDIDA DOS SETORES\nEscolha sua ação:\n");
        System.out.println("(1) Listar todos as palavras");
        System.out.println("(2) Buscar breachs por palavra");
        System.out.println("(3) Adicionar uma palavra ao índice");
        System.out.println("(4) Remover uma palavra");
        System.out.println("\n (0) Voltar");
    }
}
