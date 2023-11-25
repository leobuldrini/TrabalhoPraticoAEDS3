package core;

import DAO.Registros;
import models.Breach;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;

public class CRUDHuffman {
    // Scanner para leitura de entrada do usuário
    final private Scanner sc = new Scanner(System.in);
    // Objeto responsável por gerenciar os registros
    final Registros registros;
    // Formato de data para a entrada do usuário
    final private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.US);

    // Construtor da classe

    public CRUDHuffman(Registros registros){
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
                    registros.compress();
                    break;
                case 2:
                    decompressMenu();
                    break;
                default:
                    break;
            }
        }while(op != 0);
    }

    private void decompressMenu() throws Exception {
        int op;
        printListMenu();
        String opInput = sc.nextLine();
        while (opInput.length() > 1 || opInput.charAt(0) < '0' || opInput.charAt(0) > '9'){
            printListMenu();
            opInput = sc.nextLine();
        }
        op = Integer.parseInt(opInput);
        registros.decompress(op);
    }

    private void printListMenu() {
        // Replace this with the path of your directory
        String userDir = System.getProperty("user.dir");

        // Verifica se o diretório contém "src" e, se não, adiciona "/src" ao final
        if(!userDir.contains("src")){
            userDir += "/src";
        }


        File directory = new File(userDir + "/dataset/compressed");

        // Check if the directory exists and is indeed a directory
        if (directory.exists() && directory.isDirectory()) {

            // List all files in the directory
            File[] files = directory.listFiles();

            if (files != null) {
                String[] fileNames = new String[files.length];
                // Create an array to store the file names

                int lastVersion = 0;

                // Populate the array with the names of the files
                for (int i = 0; i < files.length; i++) {
                    fileNames[i] = files[i].getName();
                    int version = Integer.parseInt(fileNames[i].charAt(fileNames[i].length() - 5) + "");
                    if(version > lastVersion) {
                        lastVersion = version;
                    }
                }

                for(String name : fileNames){
                    System.out.println(name);
                }

            } else {
                System.out.println("No files found in the directory.");
            }
        } else {
            System.out.println("Directory does not exist or is not a directory.");
        }
    }

    private void printMenu() {
        System.out.println("\t\n\nBEM VINDO AO CRUD DE COMPRESSÃO POR HUFFMAN\nEscolha sua ação:\n");
        System.out.println("(1) Comprimir o registro");
        System.out.println("(2) Descomprimir um registro");
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