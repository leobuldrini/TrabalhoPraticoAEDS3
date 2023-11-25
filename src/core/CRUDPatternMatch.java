package core;

import DAO.Registros;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

public class CRUDPatternMatch {
    // Scanner para leitura de entrada do usuário
    final private Scanner sc = new Scanner(System.in);
    // Objeto responsável por gerenciar os registros
    final Registros registros;
    // Formato de data para a entrada do usuário
    final private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.US);

    // Construtor da classe

    public CRUDPatternMatch(Registros registros){
        this.registros = registros;
    }

    public void menu() throws Exception {
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
                    System.out.println("Digite o padrão a ser buscado:");
                    String pattern = sc.nextLine();
                    registros.matchBYKMP(pattern);
                    break;
                case 2:
                    System.out.println("Digite o padrão a ser buscado:");
                    String patternBoyerMoore = sc.nextLine();
                    registros.matchByBoyerMoore(patternBoyerMoore);
                    break;
                default:
                    break;
            }
        }while(op != 0);
    }

    private void printMenu() {
        System.out.println("\t\n\nBEM VINDO AO CRUD CASAMENTO DE PADRÕES\nEscolha sua ação:\n");
        System.out.println("(1) Padrão por KMP");
        System.out.println("(2) Padrão por Boyer-Moore");
        System.out.println("\n (0) Voltar");
    }


    // Método que verifica se a entrada é um número válido
    private boolean isValidNumber(String input){
        for(int i = 0; i < input.length(); i++){
            if(input.charAt(i) < '0' || input.charAt(i) > '9'){
                return false;
            }
        }
        return true;
    }

}
