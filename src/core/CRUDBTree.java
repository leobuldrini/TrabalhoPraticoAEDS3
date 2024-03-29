//Arthur L F Pfeilsticker - 617553
//Leonardo B Marques - 793952
package core;

import models.Breach;
import DAO.Registros;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;

// Classe responsável por implementar as operações CRUD para a estrutura BTree
public class CRUDBTree {
    // Scanner para leitura de entrada do usuário
    final private Scanner sc = new Scanner(System.in);
    // Objeto responsável por gerenciar os registros
    final Registros registros;
    // Formato de data para a entrada do usuário
    final private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.US);

    // Construtor da classe
    public CRUDBTree(Registros registros){
        this.registros = registros;
    }

    // Método que exibe o menu principal e permite ao usuário escolher uma operação
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
                    registros.readAllBreaches();
                    break;
                case 2:
                    // Busca um registro pelo ID
                    String inputId;
                    do{
                        System.out.print("Digite um id para realizar a busca: ");
                        inputId = sc.nextLine();
                    }while (!isValidNumber(inputId));
                    Breach breach = registros.retrieveBreachByBTree(Integer.parseInt(inputId));
                    if(breach != null){
                        System.out.println("\n" + breach);
                    }else{
                        System.out.println("Breach de id " + inputId + "não encontrado.");
                    }
                    break;
                case 3:
                    // Adiciona um novo registro
                    System.out.println("NOVO BREACH!");
                    System.out.print("Digite a empresa que sofreu a ruptura de dados: ");
                    String company = sc.nextLine();
                    String inputRecordsLost;
                    do{
                        System.out.print("Digite a quantidade de dados perdidos: ");
                        inputRecordsLost = sc.nextLine();
                    }while (!isValidNumber(inputRecordsLost));
                    long recordsLost = Long.parseLong(inputRecordsLost);
                    String inputDate;
                    do{
                        System.out.print("Digite a data no formato DD/MM/YYYY do evento de ruptura: ");
                        inputDate = sc.nextLine();
                    }while (!isValidDate(inputDate));
                    LocalDate date = LocalDate.parse(inputDate, formatter);
                    System.out.print("Digite uma breve descrição do ocorrido: ");
                    String briefing = sc.nextLine();
                    System.out.print("Digite de qual setor pertence a empresa que sofreu a ruptura: ");
                    String sector = sc.nextLine();
                    System.out.print("Digite em uma palavra como os dados foram vazados: ");
                    String method = sc.nextLine();
                    String[] sectorAndMethod = {sector, method};
                    Breach newBreach = new Breach(-1, company, recordsLost, date, briefing, sectorAndMethod);
                    registros.inserirRegistro(newBreach);
                    System.out.println("Registro de id "+ newBreach.id + " adicionado!");
                    break;
                case 4:
                    // Atualiza um registro existente
                    System.out.print("Digite o ID do Breach que deseja atualizar: ");
                    String inputIdUpdate;
                    do{
                        inputIdUpdate = sc.nextLine();
                    }while (!isValidNumber(inputIdUpdate));
                    Breach oldBreach = registros.retrieveBreachByBTree(Integer.parseInt(inputIdUpdate));
                    System.out.println(oldBreach);
                    updateMenu(oldBreach);
                    break;
                case 5:
                    // Remove um registro
                    System.out.print("Digite o ID do registro que deseja remover: ");
                    String inputIdDelete;
                    do{
                        inputIdDelete = sc.nextLine();
                    }while (!isValidNumber(inputIdDelete));
                    Breach breachToDelete = registros.retrieveBreachByBTree(Integer.parseInt(inputIdDelete));
                    System.out.println("Tem certeza que deseja deletar o seguinte registro?" + breachToDelete + "\n(0)Não\n(1)Sim");
                    String opInputDelete = sc.nextLine();
                    while (opInputDelete.length() > 1 || opInputDelete.charAt(0) < '0' || opInputDelete.charAt(0) > '1'){
                        System.out.println("Tem certeza que deseja deletar o seguinte registro?" + breachToDelete + "\n(0)Não\n(1)Sim");
                        opInputDelete = sc.nextLine();
                    }
                    int opDelete = Integer.parseInt(opInputDelete);
                    if(opDelete == 1){
                        Breach deleted = registros.deletarRegistro(breachToDelete.id);
                        System.out.println("Registro de id "+ deleted.id + " deletado!");
                    }
                    break;
                default:
                    break;
            }
        }while(op != 0);
    }

    // Método que exibe o menu principal
    private void printMenu() {
        System.out.println("\t\n\nBEM VINDO AO CRUD INDEXADO POR BTREE\nEscolha sua ação:\n");
        System.out.println("(1) Listar todos os breachs");
        System.out.println("(2) Buscar um breach pelo ID");
        System.out.println("(3) Adicionar um breach ao registro");
        System.out.println("(4) Atualizar algum dado do breach");
        System.out.println("(5) Remover um registro");
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

    // Método que verifica se a entrada é uma data válida
    private boolean isValidDate(String input){
        try{
            LocalDate.parse(input, formatter);
            return true;
        } catch(DateTimeParseException err){
            return false;
        }
    }

    // Método que exibe o menu de atualização
    private void printUpdateMenu(){
        System.out.println("Qual o elemento que vc deseja realizar o update?");
        System.out.println("(1) Empresa");
        System.out.println("(2) Quantidade de dados perdidos");
        System.out.println("(3) Data");
        System.out.println("(4) Briefing");
        System.out.println("(5) Setor da empresa");
        System.out.println("(6) Método de ruptura que a empresa sofreu");
        System.out.println("\n(0) Voltar");
    }

    // Método que permite ao usuário escolher qual atributo do registro deseja atualizar
    private void updateMenu(Breach oldBreach){
        int op;
        boolean anyChange = false;
        Breach breach = new Breach();
        do{
            printUpdateMenu();
            String opInput = sc.nextLine();
            while (opInput.length() > 1 || opInput.charAt(0) < '0' || opInput.charAt(0) > '9'){
                printMenu();
                opInput = sc.nextLine();
            }
            op = Integer.parseInt(opInput);
            breach.id = oldBreach.id;
            switch (op){
                case 1:
                    anyChange = true;
                    System.out.print("Digite o novo nome da empresa: ");
                    breach.company = sc.nextLine();
                    break;
                case 2:
                    anyChange = true;
                    String inputRecordsLost;
                    do{
                        System.out.print("Digite a quantidade de dados perdidos: ");
                        inputRecordsLost = sc.nextLine();
                    }while (!isValidNumber(inputRecordsLost));
                    breach.recordsLost = Long.parseLong(inputRecordsLost);
                    break;
                case 3:
                    anyChange = true;
                    String inputDate;
                    do{
                        System.out.print("Digite a data no formato DD/MM/YYYY do evento de ruptura: ");
                        inputDate = sc.nextLine();
                    }while (!isValidDate(inputDate));
                    breach.date = LocalDate.parse(inputDate);
                    break;
                case 4:
                    anyChange = true;
                    System.out.print("Digite uma breve descrição do ocorrido: ");
                    breach.detailedStory = sc.nextLine();
                    break;
                case 5:
                    anyChange = true;
                    System.out.print("Digite de qual setor pertence a empresa que sofreu a ruptura: ");
                    String sector = sc.nextLine();
                    breach.sectorAndMethod[0] = sector;
                    break;
                case 6:
                    anyChange = true;
                    System.out.print("Digite em uma palavra como os dados foram vazados: ");
                    String method = sc.nextLine();
                    breach.sectorAndMethod[1] = method;
                    break;
                default:
                    break;
            }
            System.out.println("Deseja realizar alguma outra alteração?\n(0) Não, já alterei o que eu precisava\n(1) Sim, quero atualizar outro dado");
            opInput = sc.nextLine();
            while (opInput.length() > 1 || opInput.charAt(0) < '0' || opInput.charAt(0) > '9'){
                System.out.println("Deseja realizar alguma outra alteração?\n(0) Não, já alterei o que eu precisava\n(1) Sim, quero atualizar outro dado");
                opInput = sc.nextLine();
            }
            op = Integer.parseInt(opInput);
        }while (op != 0);
        if(anyChange){
            registros.updateBreach(breach);
        }
    }
}
