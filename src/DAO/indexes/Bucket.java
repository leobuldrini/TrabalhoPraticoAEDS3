//Arthur L F Pfeilsticker - 617553
//Leonardo B Marques - 793952
package DAO.indexes;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

// Classe que representa um bucket no índice hash extensível
public class Bucket implements HashInterface {
    // Profundidade local do bucket
    int p;
    // Tamanho máximo do bucket
    final private int bucketLength;
    // Array de pares chave-endereço armazenados no bucket
    KeyAddressPair[] keyAddressPairs;

    // Construtor que inicializa um bucket vazio
    Bucket(int bucketLength, int p) {
        this.p = p;
        this.bucketLength = bucketLength;
        this.keyAddressPairs = new KeyAddressPair[bucketLength];
    }

    // Construtor que lê um bucket de um arquivo
    Bucket(RandomAccessFile raf, int bucketLength) throws IOException {
        this.bucketLength = bucketLength;
        p = raf.readInt();
        this.keyAddressPairs = new KeyAddressPair[bucketLength];
        for (int i = 0; i < bucketLength; i++) {
            int key = raf.readInt();
            long add = raf.readLong();
            if (key >= 0 && add > 0) {
                keyAddressPairs[i] = new KeyAddressPair(key, add);
            }
        }
    }

    // Função hash para calcular o valor hash de um ID
    public int hash(int id) {
        return (int) (id % (Math.pow(2, p)));
    }

    // Método para recuperar o endereço associado a um ID
    long retrieveAddress(int id) {
        boolean found = false;
        int i = 0;
        while (!found && i < bucketLength) {
            if (keyAddressPairs[i] != null && keyAddressPairs[i].key == id) found = true;
            i++;
        }
        i--;
        if (found) return keyAddressPairs[i].address;
        else return -1;
    }

    // Método privado para recuperar o índice de um ID dentro do bucket, se existir
    private int retrieveIndexInsideBucketIfExists(int id){
        boolean found = false;
        int i = 0;
        while (!found && i < bucketLength) {
            if (keyAddressPairs[i] != null && keyAddressPairs[i].key == id) found = true;
            i++;
        }
        i--;
        if (found) return i;
        else return -1;
    }

    // Método para atualizar o endereço associado a um ID
    public boolean update(int id, long newAddress){
        int index = retrieveIndexInsideBucketIfExists(id);
        if(index >= 0){
            keyAddressPairs[index].address = newAddress;
            return true;
        }else{
            return false;
        }
    }

    // Método para inserir um par chave em um espaço livre do bucket
    public boolean insertFreeSpace(KeyAddressPair keyAddressPair) {
        int i = 0;
        try {
            while (keyAddressPairs[i] != null) {
                i++;
            }
            keyAddressPairs[i] = keyAddressPair;
            return true;
        } catch (IndexOutOfBoundsException err) {
            return false;
        }
    }

    // Método para recalcular o valor de p e realocar pares chave
    public List<KeyAddressPair> recalculateNewP(int bucketNumber) {
        this.p++;
        List<KeyAddressPair> reallocated = new ArrayList<>();
        if (hash(keyAddressPairs[bucketLength - 1].key) != bucketNumber) {
            reallocated.add(keyAddressPairs[bucketLength - 1]);
            keyAddressPairs[bucketLength - 1] = null;
        }
        for (int i = bucketLength - 2; i >= 0 && keyAddressPairs[i] != null; i--) {
            int newHash = hash(keyAddressPairs[i].key);
            if (newHash != bucketNumber) {
                reallocated.add(keyAddressPairs[i]);
                keyAddressPairs[i] = null;
                for (int j = i; j < keyAddressPairs.length - 1; j++) {
                    keyAddressPairs[j] = keyAddressPairs[j + 1];
                }
                keyAddressPairs[keyAddressPairs.length - 1] = null; // Define o último elemento como null
                i--;
            }
        }
        return reallocated;
    }

    // Método para remover um ID do bucket
    public boolean remove(int id){
        boolean found = false;
        int i = 0;
        while (!found && i < bucketLength) {
            if (keyAddressPairs[i] != null && keyAddressPairs[i].key == id) found = true;
            i++;
        }
        i--;
        if (found) {
            keyAddressPairs[i] = null;
            if(i+1 != bucketLength){
                for (int j = i; j < keyAddressPairs.length - 1; j++) {
                    keyAddressPairs[j] = keyAddressPairs[j + 1];
                }
                keyAddressPairs[keyAddressPairs.length - 1] = null;
            }
            return true;
        }
        else return false;
    }

    // Método para obter o número de itens no bucket
    public int getBucketItemsLength(){
        int i = 0;
        try {
            while (keyAddressPairs[i] != null) {
                i++;
            }
            return i;
        } catch (IndexOutOfBoundsException err) {
            return i;
        }
    }
    
    // Método para limpar o bucket
    public void makeBlank(){
        for(int i = 0; i < bucketLength; i++){
            keyAddressPairs[i] = null;
        }
        this.p = 0;
    }

    // Método para converter o bucket em um array de bytes
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(p);
        for (int i = 0; i < bucketLength; i++) {
            if (keyAddressPairs[i] != null) {
                dos.writeInt(keyAddressPairs[i].key);
                dos.writeLong(keyAddressPairs[i].address);
            } else {
                dos.writeInt(-1);
                dos.writeLong(-1);
            }
        }
        return baos.toByteArray();
    }
}
