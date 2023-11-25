//Arthur L F Pfeilsticker - 617553
//Leonardo B Marques - 793952
package core;

import DAO.Registros;

import java.util.Scanner;

// Classe principal responsável por gerenciar as operações CRUD para diferentes estruturas de dados
public class CRUDMain {

    // Scanner para leitura de entrada do usuário
    final private Scanner sc = new Scanner(System.in);
    // Objeto responsável por gerenciar os registros
    final Registros registros;

    // Objetos CRUD para diferentes estruturas de dados
    final CRUDSequencial crudSequencial;
    final CRUDHash crudHash;
    final CRUDBTree crudBTree;
    final CRUDInvertedList crudInvertedList;
    final CRUDInvertedListSector crudInvertedListSector;
    final CRUDHuffman crudHuffman;
    final CRUDPatternMatch crudKMP;
    // Flag para verificar se os registros foram carregados
    boolean loaded = false;

    // Construtor da classe
    public CRUDMain(Registros registros) {
        this.registros = registros;
        this.crudSequencial = new CRUDSequencial(registros);
        this.crudHash = new CRUDHash(registros);
        this.crudBTree = new CRUDBTree(registros);
        this.crudInvertedList = new CRUDInvertedList(registros);
        this.crudInvertedListSector = new CRUDInvertedListSector(registros);
        this.crudHuffman = new CRUDHuffman(registros);
        this.crudKMP = new CRUDPatternMatch(registros);
        this.loaded = registros.isThereAny();
    }

    // Método que exibe o menu principal e permite ao usuário escolher uma operação
    public void menu() throws Exception {
        int op;
        do {
            printMenu();
            String input = sc.nextLine();
            // Validação da entrada do usuário
            while (input.length() > 2 || input.charAt(0) < '0' || input.charAt(0) > '9') {
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
                    crudInvertedListSector.menu();
                    break;
                case 6:
                    // Opção para limpar todos os registros
                    System.out.println("Tem certeza que deseja deletar toda a base de registros?\n(0)Não\n(1)Sim");
                    String opInputLimparRegistros = sc.nextLine();
                    // Validação da entrada do usuário
                    while (opInputLimparRegistros.length() > 1 || opInputLimparRegistros.charAt(0) < '0' || opInputLimparRegistros.charAt(0) > '1') {
                        System.out.println("Tem certeza que deseja deletar toda a base de registros?\n(0)Não\n(1)Sim");
                        opInputLimparRegistros = sc.nextLine();
                    }
                    int opClear = Integer.parseInt(opInputLimparRegistros);
                    if (opClear == 1) {
                        registros.limparRegistros();
                    }
                    this.loaded = false;
                    break;
                case 7:
                    // Opção para carregar registros da base
                    String userDir = System.getProperty("user.dir");
                    if (!userDir.contains("src")) {
                        userDir += "/src";
                    }
                    registros.convertCSVtoBreach(userDir + "/dataset/breaches.csv");
                    loaded = true;
                    break;
                case 8:
                    // Opção para ordenar a base de arquivos
                    registros.read100BreachesAndIntercalate();
                    loaded = true;
                    break;
                case 9:
                    crudHuffman.menu();
                case 10:
                    break;
                case 11:
                    crudKMP.menu();
                default:
                    break;
            }
        } while (op != 0);
    }

    // Método que exibe o menu principal
    private void printMenu() {
        System.out.println("\t\n\nBEM VINDO AO CRUD DE BREACHS\nEscolha sua ação:\n");
        System.out.println("(1) Utilizar o CRUD Sequencial");
        System.out.println("(2) Utilizar o CRUD Indexado por BTree");
        System.out.println("(3) Utilizar o CRUD Indexado por Hash Extendido");
        System.out.println("(4) Utilizar o CRUD Indexado por Lista Invertida para descrição");
        System.out.println("(5) Utilizar o CRUD Indexado por Lista Invertida para setores");
        System.out.println(loaded ? "(6) Limpar todos os registros" : "");
        System.out.println(loaded ? "" : "(7) Carregar os registros da base");
        System.out.println(loaded ? "(8) Ordenar a base de arquivos" : "");
        System.out.println("(9) Utilizar o CRUD Compressão por Huffman");
        System.out.println("(10) Utilizar o CRUD Compressão por LZW");
        System.out.println("(11) Utilizar o CRUD Casamento de Padroes");
        System.out.println("\n (0) Sair");
    }
}
