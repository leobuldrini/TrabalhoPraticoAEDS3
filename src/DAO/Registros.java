package DAO;

import DAO.indexes.BTree;
import DAO.indexes.ExtendedHash;
import DAO.indexes.InvertedIndex;
import DAO.indexes.KeyAddressPair;
import models.Breach;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Registros {
    final private String filePath;
    private BTree bTreeIndex;
    private ExtendedHash extendedHashIndex;
    final private InvertedIndex invertedIndex;

    final private InvertedIndex invertedIndexSector;

    public Registros(String filepath, BTree bTreeIndex, ExtendedHash extendedHash, InvertedIndex invertedIndex, InvertedIndex invertedIndexSector) {
        this.filePath = filepath;
        this.bTreeIndex = bTreeIndex;
        this.extendedHashIndex = extendedHash;
        this.invertedIndex = invertedIndex;
        this.invertedIndexSector = invertedIndexSector;
    }

    public void readAllBreaches() {
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            Breach breach = new Breach();
            raf.seek(0);
            int lastId = raf.readInt();
            System.out.println("\n\n> Último id: " + lastId + "\n---------------------");
            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();
                if (lapide == 0x01) {
                    raf.seek(raf.getFilePointer() + tamanhoRegistro);
                    continue;
                }
                byte[] registro = new byte[tamanhoRegistro];
                raf.read(registro);
                breach.fromByteArray(registro);
                System.out.println(breach);
            }
            System.out.println("---------------------");
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Breach retrieveBreachSequentially(int id) {
        Breach breach = new Breach();
        boolean found = false;
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            raf.seek(0);
            raf.readInt();
            int i = 1;
            while (raf.getFilePointer() < raf.length() && !found) {
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();
                if (lapide == 0x01) {
                    raf.seek(raf.getFilePointer() + tamanhoRegistro);
                    continue;
                }
                byte[] registro = new byte[tamanhoRegistro];
                raf.read(registro);
                breach.fromByteArray(registro);
                if (breach.id == id) {
                    found = true;
                }else{
                    System.out.print(i + " ");
                    i++;
                }
            }
            raf.close();
            System.out.println("---------------------");
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return found ? breach : null;
    }

    public void inserirRegistro(Breach breach){
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            int lastId = raf.readInt();
            lastId++;
            breach.id = lastId;
            byte[] breachBytes = breach.toByteArray();
            raf.seek(0);
            raf.writeInt(lastId);
            boolean espacoVago = false;
            int regLength = 0;
            while (!espacoVago && raf.getFilePointer() < raf.length()) {
                boolean lapide = raf.readByte() != 0;
                regLength = raf.readInt();
                if (lapide && breachBytes.length <= regLength) {
                    espacoVago = true;
                } else {
                    raf.seek(raf.getFilePointer() + regLength);
                }
            }
            if (espacoVago) {
                raf.seek(raf.getFilePointer() - 5);
            }
            System.out.println("\n" + breach.id + "\t" + breach.company);
            KeyAddressPair keyAddressPair = new KeyAddressPair(breach.id, raf.getFilePointer());
            bTreeIndex.addIndex(keyAddressPair);
            extendedHashIndex.insert(keyAddressPair);
            invertedIndex.insert(breach.detailedStory, keyAddressPair.address);
            invertedIndexSector.insert(breach.sectorAndMethod[0], keyAddressPair.address);
            raf.write((byte) 0x00);
            raf.writeInt(Math.max(regLength, breachBytes.length));
            raf.write(breachBytes);
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void limparRegistros(){
        try {
            new FileOutputStream(filePath).close();
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            raf.writeInt(-1);
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        String[] paths = {invertedIndex.path, invertedIndexSector.path};
        for(int i = 0; i < 2; i++){
            File directory = new File(paths[i] + "/");
            // Verifica se o caminho especificado é um diretório
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();

                // Verifica se o diretório não está vazio
                if (files != null) {
                    for (File file : files) {
                        // Exclui cada arquivo individualmente
                        if (file.isFile()) {
                            if (!file.delete()) {
                                System.err.println("Não foi possível excluir o arquivo: " + file.getName());
                            }
                        }
                    }
                }
            } else {
                System.err.println("O caminho especificado não é um diretório válido.");
            }
            try {
                new FileOutputStream(paths[i] + "/__words.invIndex").close();
                RandomAccessFile raf = new RandomAccessFile(paths[i] + "/__words.invIndex", "rw");
                raf.writeInt(0);
                raf.close();
            } catch (FileNotFoundException e) {
                System.out.println("Arquivo não encontrado");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        try {
            new FileOutputStream("src/dataset/index.hash").close();
            RandomAccessFile raf = new RandomAccessFile("src/dataset/index.hash", "rw");
            raf.writeLong(0);
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            new FileOutputStream("src/dataset/index.btree").close();
            RandomAccessFile raf = new RandomAccessFile("src/dataset/index.btree", "rw");
            raf.writeLong(0);
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        bTreeIndex = new BTree(bTreeIndex.path, bTreeIndex.T);
        extendedHashIndex = new ExtendedHash(extendedHashIndex.bucketLength, extendedHashIndex.bucketsTablePath, extendedHashIndex.hashTablePath);
    }

    public Breach deletarRegistroSequencial(int id) {
        Breach removed = new Breach();
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            boolean found = false;
            int currentId = 0;
            int registroLength = 0;
            raf.seek(4);
            long prop1 = 0;
            while (!found && raf.getFilePointer() < raf.length()) {
                prop1 = raf.getFilePointer();
                boolean dead = raf.readByte() != 0;
                registroLength = raf.readInt();
                if (dead) {
                    raf.seek(raf.getFilePointer() + registroLength);
                    continue;
                }
                byte[] registroBytes = new byte[registroLength];
                raf.read(registroBytes);
                removed.fromByteArray(registroBytes);
                if (removed.id == id) {
                    found = true;
                }
            }
            if (found) {
                raf.seek(prop1);
                raf.write((byte) 0x01);
                bTreeIndex.remove(id);
                extendedHashIndex.remove(id);
                invertedIndex.remove(prop1);
                invertedIndexSector.remove(prop1);
            }
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return removed;
    }

    public Breach deletarRegistro(int id){
        Breach removed = new Breach();
        try{
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            boolean found = false;
            long removeAddress = extendedHashIndex.retrieveAddress(id);
            raf.seek(removeAddress);
            long prop1 = raf.getFilePointer();
            boolean dead = raf.readByte() != 0;
            int registroLength = raf.readInt();
            if (dead) {
                return null;
            }
            byte[] registroBytes = new byte[registroLength];
            raf.read(registroBytes);
            removed.fromByteArray(registroBytes);
            if (removed.id == id) {
                found = true;
            }
            if (found) {
                raf.seek(prop1);
                raf.write((byte) 0x01);
                bTreeIndex.remove(id);
                extendedHashIndex.remove(id);
                invertedIndex.remove(prop1);
                invertedIndexSector.remove(prop1);
            }
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return removed;
    }

    public Breach retrieveBreachByBTree(int id) throws IOException {
        long address = bTreeIndex.retrieveBreachAddress(id);
        if (address < 0) return null;
        RandomAccessFile raf = new RandomAccessFile(filePath, "r");
        raf.seek(address);
        Breach breach = new Breach();
        byte lapide = raf.readByte();
        int tamanhoRegistro = raf.readInt();
        if (lapide == 0x01) {
            raf.seek(raf.getFilePointer() + tamanhoRegistro);
            return null;
        }
        byte[] registro = new byte[tamanhoRegistro];
        raf.read(registro);
        breach.fromByteArray(registro);
        raf.close();
        return breach;
    }

    public Breach retrieveBreachByExtendedHash(int id) throws IOException {
        long address = extendedHashIndex.retrieveAddress(id);
        if (address < 0) return null;
        RandomAccessFile raf = new RandomAccessFile(filePath, "r");
        raf.seek(address);
        Breach breach = new Breach();
        byte lapide = raf.readByte();
        int tamanhoRegistro = raf.readInt();
        if (lapide == 0x01) {
            raf.seek(raf.getFilePointer() + tamanhoRegistro);
            return null;
        }
        byte[] registro = new byte[tamanhoRegistro];
        raf.read(registro);
        breach.fromByteArray(registro);
        raf.close();
        return breach;
    }

    public boolean updateBreach(Breach breach) {
        try {
            long address = bTreeIndex.retrieveBreachAddress(breach.id);
            if (address < 0) return false;
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            raf.seek(address);
            byte lapide = raf.readByte();
            int tamanhoRegistro = raf.readInt();
            if (lapide == 0x01) {
                return false;
            }
            long add = raf.getFilePointer();
            byte[] registro = new byte[tamanhoRegistro];
            raf.read(registro);
            Breach oldBreach = new Breach();
            oldBreach.fromByteArray(registro);
            oldBreach.update(breach);
            byte[] updatedBreach = oldBreach.toByteArray();
            boolean canFit = updatedBreach.length <= tamanhoRegistro;
            if (canFit) {
                raf.seek(add);
                raf.write(updatedBreach);
                invertedIndex.remove(address);
                invertedIndexSector.remove(address);
                invertedIndex.insert(breach.detailedStory, address);
                invertedIndexSector.insert(breach.sectorAndMethod[0], address);
            } else {
                raf.seek(add - 5);
                raf.write((byte) 0x01);
                raf.seek(raf.length());
                long newAddress = raf.getFilePointer();
                raf.write((byte) 0x00);
                raf.writeInt(updatedBreach.length);
                bTreeIndex.updateKeyAddress(breach.id, newAddress);
                extendedHashIndex.updateKeyAddress(breach.id, newAddress);
                invertedIndex.remove(address);
                invertedIndexSector.remove(address);
                invertedIndex.insert(breach.detailedStory, newAddress);
                invertedIndexSector.insert(breach.sectorAndMethod[0], newAddress);
                raf.write(updatedBreach);
            }
            raf.close();
            return true;
        } catch (IOException err) {
            System.out.println("Erro no I/O do arquivo: " + err.getMessage());
        }
        return false;
    }

    public void listAllWords() throws IOException{
        invertedIndex.retrieveWords();
        System.out.println(" ");
    }

    public void listAllSectors() throws IOException {
        invertedIndexSector.retrieveWords();
        System.out.println(" ");
    }

    public boolean checkIfWordsExistInIndex(String term) throws IOException{
        return invertedIndex.checkIfWordExists(term);
    }

    public boolean checkIfSectorExistInIndex(String term) throws IOException{
        return invertedIndexSector.checkIfWordExists(term);
    }

    public Breach[] retrieveBreachesByWord(String word) throws IOException{
        ArrayList<Long> addresses = invertedIndex.retrieveBreachsByWord(word);
        Breach[] results = new Breach[addresses.size()];
        RandomAccessFile raf = new RandomAccessFile(filePath, "r");
        for(int i = 0; i < addresses.size(); i++){
            long address = addresses.get(i);
            if (address < 0) return null;
            raf.seek(address);
            Breach breach = new Breach();
            byte lapide = raf.readByte();
            int tamanhoRegistro = raf.readInt();
            if (lapide == 0x01) {
                raf.seek(raf.getFilePointer() + tamanhoRegistro);
                return null;
            }
            byte[] registro = new byte[tamanhoRegistro];
            raf.read(registro);
            breach.fromByteArray(registro);
            results[i] = breach;
        }
        raf.close();
        return results;
    }

    public Breach[] retrieveBreachesBySector(String word) throws IOException{
        ArrayList<Long> addresses = invertedIndexSector.retrieveBreachsByWord(word);
        Breach[] results = new Breach[addresses.size()];
        RandomAccessFile raf = new RandomAccessFile(filePath, "r");
        for(int i = 0; i < addresses.size(); i++){
            long address = addresses.get(i);
            if (address < 0) return null;
            raf.seek(address);
            Breach breach = new Breach();
            byte lapide = raf.readByte();
            int tamanhoRegistro = raf.readInt();
            if (lapide == 0x01) {
                raf.seek(raf.getFilePointer() + tamanhoRegistro);
                return null;
            }
            byte[] registro = new byte[tamanhoRegistro];
            raf.read(registro);
            breach.fromByteArray(registro);
            results[i] = breach;
        }
        raf.close();
        return results;
    }

    public boolean addWordToIndex(String word) throws IOException{
        boolean fileCreated = invertedIndex.insertWordToIndex(word);
        if(fileCreated){
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            Breach breach = new Breach();
            raf.seek(0);
            int lastId = raf.readInt();
            long prop1 = 0;
            while (raf.getFilePointer() < raf.length()) {
                prop1 = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();
                if (lapide == 0x01) {
                    raf.seek(raf.getFilePointer() + tamanhoRegistro);
                    continue;
                }
                byte[] registro = new byte[tamanhoRegistro];
                raf.read(registro);
                breach.fromByteArray(registro);
                if(breach.detailedStory.contains(word)){
                    invertedIndex.updateOneIndexWithAddress(word, prop1);
                }
            }
            System.out.println("---------------------");
            raf.close();
            return true;
        }
        return false;
    }

    public boolean addSectorToIndex(String word) throws IOException{
        boolean fileCreated = invertedIndexSector.insertWordToIndex(word);
        if(fileCreated){
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            Breach breach = new Breach();
            raf.seek(0);
            int lastId = raf.readInt();
            long prop1 = 0;
            while (raf.getFilePointer() < raf.length()) {
                prop1 = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();
                if (lapide == 0x01) {
                    raf.seek(raf.getFilePointer() + tamanhoRegistro);
                    continue;
                }
                byte[] registro = new byte[tamanhoRegistro];
                raf.read(registro);
                breach.fromByteArray(registro);
                if(breach.detailedStory.contains(word)){
                    invertedIndexSector.updateOneIndexWithAddress(word, prop1);
                }
            }
            System.out.println("---------------------");
            raf.close();
            return true;
        }
        return false;
    }

    public boolean removeWordFromIndex(String word) throws IOException{
        return invertedIndex.removeWordFromIndex(word);
    }

    public boolean removeSectorFromIndex(String word) throws IOException{
        return invertedIndexSector.removeWordFromIndex(word);
    }

    public boolean isThereAny(){
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            Breach breach = new Breach();
            raf.seek(0);
            int lastId = raf.readInt();
            raf.close();
            return lastId != -1;
        }catch (IOException err){
            return false;
        }
    }

    public List<Breach> convertCSVtoBreach(String csvFilePath) throws IOException {
        List<Breach> breaches = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(Locale.US);
            // Pular a primeira linha (cabeçalho)
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                int id = Integer.parseInt(values[15]);
                String company = values[0];
                long recordsLost = Long.parseLong(values[2].replaceAll(",", ""));
                values[4] = "01 " + values[4];
                values[4] = values[4].replaceAll("  ", " ");
                LocalDate date = LocalDate.parse(values[4], formatter);
                String detailedStory = values[5];
                String sector = values[6];
                String method = values[7];
                String[] sectorAndMethod = (sector + "," + method).split(","); // Concatenando sector e method e depois dividindo

                Breach breach = new Breach(id, company, recordsLost, date, detailedStory, sectorAndMethod);
                inserirRegistro(breach);
            }
        }

        return breaches;
    }
    public void read100BreachesAndIntercalate() {
        try {
            List<Breach[]> allBreachArrays = new ArrayList<>();
            Breach[] breachArray = new Breach[100];
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            raf.seek(0);
            int lastId = raf.readInt();
            System.out.println("\n\n> Último id: " + lastId + "\n---------------------");
            int cont = 0;
            while (raf.getFilePointer() < raf.length()) {
                Breach breach = new Breach();
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();
                if (lapide == 0x01) {
                    raf.seek(raf.getFilePointer() + tamanhoRegistro);
                    continue;
                }
                byte[] registro = new byte[tamanhoRegistro];
                raf.read(registro);
                breach.fromByteArray(registro);
                breachArray[cont] = breach;
                cont++;
                if (cont == 100) {
                    allBreachArrays.add(breachArray);
                    breachArray = new Breach[100];
                    cont = 0;
                }
            }
            //if (cont > 0) {
                //allBreachArrays.add(breachArray);
            //}
            raf.close();
    
            List<Breach> sortedBreaches = IntercalacaoBalanceadaBreach.intercalacaoBalanceada(allBreachArrays);
            
            // Salva os registros intercalados em um arquivo binário
            try (FileOutputStream fos = new FileOutputStream("breachesSorted.bin");
            BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                for (Breach b : sortedBreaches) {
                    bos.write(b.toByteArray());
                }
            }
            System.out.println(sortedBreaches);
    
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
