package DAO.indexes;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class InvertedIndex {
    final public String path;
    final public String attr;
    final private String[] stopWords;

    public InvertedIndex(String path, String attr, String[] stopWords){
        this.attr = attr;
        this.path = path + attr + "/";
        this.stopWords = stopWords;
    }

    public ArrayList<Long> retrieveBreachsByWord(String term) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(path + term + ".invIndex", "rw");
        long quantidadeDeDocumentos = raf.length() / 8;
        ArrayList<Long> docs = new ArrayList<>();
        for(int i = 0; i < quantidadeDeDocumentos; i++){
            long address = raf.readLong();
            docs.add(address);
        }
        raf.close();
        return docs;
    }

    public void retrieveWords() throws IOException{
        RandomAccessFile raf = new RandomAccessFile(path + "__words.invIndex", "rw");
        raf.seek(0);
        int qnt = raf.readInt();
        int i = 0;
        System.out.println("Total de palavras indexadas: " + qnt);
        while(i<qnt){
            String finalP = raf.readUTF();
            i++;
            if(i+3 <= qnt){
                finalP += " - " + raf.readUTF() + " - " + raf.readUTF() + " - " + raf.readUTF();
                i+=3;
            } else if (i+2 <= qnt) {
                finalP += " - " + raf.readUTF() + " - " + raf.readUTF();
                i+=2;
            } else if(i+1 <= qnt){
                finalP += " - " + raf.readUTF();
                i++;
            }
            System.out.println(finalP);
        }
        raf.close();
    }

    private void createWordFile(String word) throws IOException {
        new FileOutputStream(path + word + ".invIndex").close();
        RandomAccessFile raf = new RandomAccessFile(path + "/__words.invIndex", "rw");
        raf.seek(0);
        int qnt = raf.readInt();
        qnt++;
        raf.seek(0);
        raf.writeInt(qnt);
        raf.seek(raf.length());
        raf.writeUTF(word);
        raf.close();
    }

    public void insert(String source, long address) throws IOException {
        source = source.replaceAll(",", "");
        source = source.replaceAll("\\.", "");
        source = source.replaceAll("–", "");
        source = source.toLowerCase(Locale.ROOT);
        String regex = "\\b(" + String.join("|", stopWords) + ")\\b";
        source = source.replaceAll(regex, "");
        source = source.replaceAll("[^a-zA-Z\s]", "");
        while(source.contains("\s\s")){
            source = source.replaceAll("\s\s", " ");
        }
        String[] tokens = source.split(" ");
        int index = 0;
        for (int i=0; i<tokens.length; i++)
            if (!Objects.equals(tokens[i], ""))
                tokens[index++] = tokens[i];
        tokens = Arrays.copyOf(tokens, index);
        for(int i = 0; i < tokens.length; i++){
            String token = tokens[i];
            File f = new File(path + token + ".invIndex");
            if(!f.exists()){
                createWordFile(token);
            }
            RandomAccessFile raf = new RandomAccessFile(path + token + ".invIndex", "rw");
            raf.seek(raf.length());
            raf.writeLong(address);
            raf.close();
        }
    }

    public boolean insertWordToIndex(String word) throws IOException{
        boolean alreadyExists = checkIfWordExists(word);
        if(!alreadyExists){
            createWordFile(word);
            return true;
        }
        return false;
    }

    public boolean removeWordFromIndex(String word) throws IOException{
        File file = new File(path+word+".invIndex");
        if(file.delete()){
            RandomAccessFile raf = new RandomAccessFile(path + "__words.invIndex", "rw");
            boolean found = false;
            String currentWord = "";
            long pointerToWord = -1;
            raf.seek(0);
            int qnt = raf.readInt();
            while(!found && raf.getFilePointer() != raf.length()){
                pointerToWord = raf.getFilePointer();
                currentWord = raf.readUTF();
                if (currentWord.equals(word)) {
                    found = true;
                }
            }
            if (found) {
                long endPosition = raf.getFilePointer();
                long stringLength = endPosition - pointerToWord;

                qnt--;
                raf.seek(0);
                raf.writeInt(qnt);

                raf.seek(pointerToWord);

                for (long i = pointerToWord; i < endPosition; i++) {
                    raf.writeByte(0);
                }

                raf.seek(endPosition);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = raf.read(buffer)) != -1) {
                    raf.seek(raf.getFilePointer() - bytesRead - stringLength);
                    raf.write(buffer, 0, bytesRead);
                    raf.seek(raf.getFilePointer() + stringLength);
                }

                raf.setLength(raf.length() - stringLength);
            } else {
                System.out.println("String não encontrada no arquivo.");
            }
            raf.close();
            return true;
        }else {
            return false;
        }
    }

    public void remove(long address) throws IOException{
        RandomAccessFile rafWords = new RandomAccessFile(path + "__words.invIndex", "rw");
        ArrayList<String> wordsToRemove = new ArrayList<>();
        rafWords.seek(0);
        int qnt = rafWords.readInt();
        int j = 0;
        while(j < qnt) {
            String word = rafWords.readUTF();
            if(word.equals("half")){
                System.out.println("CALMA AI");
            }
            RandomAccessFile raf = new RandomAccessFile(path + word + ".invIndex", "rw");
            long currentPosition;
            long startPosition = -1;
            while (raf.getFilePointer() < raf.length()) {
                currentPosition = raf.getFilePointer();
                long currentLong = raf.readLong();
                if (currentLong == address) {
                    startPosition = currentPosition;
                    break;
                }
            }
            if (startPosition != -1) {
                long endPosition = raf.getFilePointer();
                long longSize = 8; // Tamanho de um valor long em bytes

                // Posiciona o ponteiro no início do valor long a ser removido
                raf.seek(startPosition);

                // Preenche o espaço com bytes nulos
                for (long i = startPosition; i < endPosition; i++) {
                    raf.writeByte(0);
                }

                // Posiciona o ponteiro após o valor long removido
                raf.seek(endPosition);

                // Lê e escreve os bytes subsequentes para preencher o espaço vazio
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = raf.read(buffer)) != -1) {
                    raf.seek(raf.getFilePointer() - bytesRead - longSize);
                    raf.write(buffer, 0, bytesRead);
                    raf.seek(raf.getFilePointer() + longSize);
                }

                // Reduz o tamanho do arquivo
                raf.setLength(raf.length() - longSize);
            }
            if(raf.length() == 0) wordsToRemove.add(word);
            raf.close();
            j++;
        }
        rafWords.close();
        for(int i = 0; i < wordsToRemove.size(); i++){
            removeWordFromIndex(wordsToRemove.get(i));
        }
    }

    public boolean checkIfWordExists(String term) throws IOException{
        boolean found = false;
        RandomAccessFile raf = new RandomAccessFile(path + "__words.invIndex", "rw");
        raf.seek(0);
        int qnt = raf.readInt();
        int i = 0;
        while(!found && i < qnt) {
            String foundP = raf.readUTF();
            if(Objects.equals(term, foundP)) found = true;
            i++;
        }
        raf.close();
        return found;
    }

    public boolean updateOneIndexWithAddress(String word, long address) throws IOException{
        boolean fileExists = checkIfWordExists(word);
        if(fileExists){
            RandomAccessFile raf = new RandomAccessFile(path + word + ".invIndex", "rw");
            raf.seek(raf.length());
            raf.writeLong(address);
            raf.close();
            return true;
        }
        return false;
    }
}
