//Arthur L F Pfeilsticker - 617553
//Leonardo B Marques - 793952
package core;

import DAO.Registros;
import models.Breach;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

// Classe responsável por implementar as operações CRUD para a estrutura de Lista Invertida com foco em setores
public class CRUDInvertedListSector {

    // Scanner para leitura de entrada do usuário
    final private Scanner sc = new Scanner(System.in);
    // Objeto responsável por gerenciar os registros
    final Registros registros;
    // Formato de data para a entrada do usuário
    final private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.US);

    // Construtor da classe
    public CRUDInvertedListSector(Registros registros){
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
                    // Lista todos os setores
                    registros.listAllSectors();
                    break;
                case 2:
                    // Lista todos os setores e permite ao usuário buscar por um setor específico
                    registros.listAllSectors();
                    String inputSearch = "";
                    do{
                        System.out.print("Digite a palavra que será buscada: ");
                        inputSearch = sc.nextLine();
                    }while (!registros.checkIfSectorExistInIndex(inputSearch));
                    // Recupera e exibe os registros associados ao setor buscado
                    Breach[] results = registros.retrieveBreachesBySector(inputSearch);
                    for(int i = 0; i < results.length; i++){
                        System.out.println(results[i]);
                    }
                    break;
                case 3:
                    // Permite ao usuário adicionar um novo setor ao índice
                    String inputAddWord;
                    System.out.print("Digite a palavra que será adicionada: ");
                    inputAddWord = sc.nextLine();
                    boolean added = registros.addSectorToIndex(inputAddWord);
                    System.out.println(added ? "Palavra " + inputAddWord +  " adicionada ao índice" : "Palavra já existente no índice");
                    break;
                case 4:
                    // Permite ao usuário remover um setor do índice
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

    // Método que exibe o menu principal
    private void printMenu() {
        System.out.println("\t\n\nBEM VINDO AO CRUD INDEXADO POR LISTA INVERDIDA DOS SETORES\nEscolha sua ação:\n");
        System.out.println("(1) Listar todos as palavras");
        System.out.println("(2) Buscar breachs por palavra");
        System.out.println("(3) Adicionar uma palavra ao índice");
        System.out.println("(4) Remover uma palavra");
        System.out.println("\n (0) Voltar");
    }
}
