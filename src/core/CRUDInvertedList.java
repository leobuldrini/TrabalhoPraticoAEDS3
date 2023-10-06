//Arthur L F Pfeilsticker - 617553
//Leonardo B Marques - 793952
package core;

import models.Breach;
import DAO.Registros;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

// Classe responsável por implementar as operações CRUD para a estrutura de Lista Invertida
public class CRUDInvertedList {

    // Scanner para leitura de entrada do usuário
    final private Scanner sc = new Scanner(System.in);
    // Objeto responsável por gerenciar os registros
    final Registros registros;
    // Formato de data para a entrada do usuário
    final private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.US);

    // Construtor da classe
    public CRUDInvertedList(Registros registros){
        this.registros = registros;
    }

    // Método que exibe o menu principal e permite ao usuário escolher uma operação
    public void menu() throws IOException {
        int op;
        do{
            printMenu();
            String input = sc.nextLine();
            // Validação da entrada do usuário
            while (input.length() > 1 || input.charAt(0) < '0' || input.charAt(0) > '9'){
                printMenu();
                input = sc.nextLine();
            }
            op = Integer.parseInt(input);
            switch (op) {
                case 1:
                    // Lista todas as palavras
                    registros.listAllWords();
                    break;
                case 2:
                    // Lista todas as palavras e permite ao usuário buscar por uma palavra específica
                    registros.listAllWords();
                    String inputSearch = "";
                    do{
                        System.out.print("Digite a palavra que será buscada: ");
                        inputSearch = sc.nextLine();
                    }while (!registros.checkIfWordsExistInIndex(inputSearch));
                    // Recupera e exibe os registros associados à palavra buscada
                    Breach[] results = registros.retrieveBreachesByWord(inputSearch);
                    for(int i = 0; i < results.length; i++){
                        System.out.println(results[i]);
                    }
                    break;
                case 3:
                    // Permite ao usuário adicionar uma nova palavra ao índice
                    String inputAddWord;
                    System.out.print("Digite a palavra que será adicionada: ");
                    inputAddWord = sc.nextLine();
                    boolean added = registros.addWordToIndex(inputAddWord);
                    System.out.println(added ? "Palavra " + inputAddWord +  " adicionada ao índice" : "Palavra já existente no índice");
                    break;
                case 4:
                    // Permite ao usuário remover uma palavra do índice
                    System.out.print("Digite a palavra que deseja remover do banco de dados: ");
                    String inputDelete = sc.nextLine();
                    if(registros.checkIfWordsExistInIndex(inputDelete)){
                        registros.removeWordFromIndex(inputDelete);
                    }else{
                        System.out.println("Palavra não existente na base de dados");
                    }
                    break;
                default:
                    break;
            }
        }while(op != 0);
    }

    // Método que exibe o menu principal
    private void printMenu() {
        System.out.println("\t\n\nBEM VINDO AO CRUD INDEXADO POR LISTA INVERDIDA\nEscolha sua ação:\n");
        System.out.println("(1) Listar todos as palavras");
        System.out.println("(2) Buscar breachs por palavra");
        System.out.println("(3) Adicionar uma palavra ao índice");
        System.out.println("(4) Remover uma palavra");
        System.out.println("\n (0) Voltar");
    }
}
