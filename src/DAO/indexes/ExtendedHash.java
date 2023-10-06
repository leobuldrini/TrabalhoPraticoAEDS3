//Arthur L F Pfeilsticker - 617553
//Leonardo B Marques - 793952
package DAO.indexes;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

// Classe que implementa um índice hash extensível
public class ExtendedHash implements HashInterface {
    //Atributos da classe
    final public String bucketsTablePath;
    final public String hashTablePath;
    final public int bucketLength;

    // Variável que armazena o valor de p (profundidade global)
    int p;

    // Construtor da classe
    public ExtendedHash(int bucketLength, String bucketsTablePath, String hashTablePath) {
        this.bucketsTablePath = bucketsTablePath;
        this.hashTablePath = hashTablePath;
        this.bucketLength = bucketLength;
        setParameters();
    }

    // Função hash que retorna o valor hash de um ID
    public int hash(int id) {
        return (int) (id % (Math.pow(2, p)));
    }

    // Função hash sobrecarregada que aceita um valor p específico
    public int hash(int id, int p) {
        return (int) (id % (Math.pow(2, p)));
    }

    // Método para recuperar o endereço de um ID
    public long retrieveAddress(int id) {
        long addressFound = -1;
        int bucketNumber = hash(id);
        try {
            RandomAccessFile raf = new RandomAccessFile(hashTablePath, "r");
            int bucketLocation = 4 + (bucketNumber * 8);
            raf.seek(bucketLocation);
            long bucketAddress = raf.readLong();
            RandomAccessFile rafBucket = new RandomAccessFile(bucketsTablePath, "r");
            rafBucket.seek(bucketAddress);
            Bucket bucket = new Bucket(rafBucket, bucketLength);
            addressFound = bucket.retrieveAddress(id);
            raf.close();
            rafBucket.close();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
        return addressFound;
    }

    // Método para inserir um par key
    public void insert(KeyAddressPair keyAddressPair) {
        int bucketNumber = hash(keyAddressPair.key);
        try {
            RandomAccessFile raf = new RandomAccessFile(hashTablePath, "rw");
            raf.seek(4 + (bucketNumber * 8L));
            long bucketAddress = raf.readLong();
            RandomAccessFile rafBucket = new RandomAccessFile(bucketsTablePath, "rw");
            rafBucket.seek(bucketAddress);
            Bucket bucket = new Bucket(rafBucket, bucketLength);
            final boolean canInsert = bucket.insertFreeSpace(keyAddressPair);
            if (canInsert) {
                rafBucket.seek(bucketAddress);
                rafBucket.write(bucket.toByteArray());
                raf.close();
                rafBucket.close();
            } else {
                raf.close();
                rafBucket.close();
                increaseBucketP(bucketNumber, keyAddressPair);
                insert(keyAddressPair);
            }
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }

    // Método privado para aumentar o valor de p de um bucket
    private void increaseBucketP(int bucketNumber, KeyAddressPair keyAddressPair) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(hashTablePath, "rw");
        raf.seek(4 + (bucketNumber * 8L));
        long bucketAddress = raf.readLong();
        RandomAccessFile rafBucket = new RandomAccessFile(bucketsTablePath, "rw");
        rafBucket.seek(bucketAddress);
        Bucket bucket = new Bucket(rafBucket, bucketLength);
        List<KeyAddressPair> reallocated = bucket.recalculateNewP(bucketNumber);
        rafBucket.seek(bucketAddress);
        rafBucket.write(bucket.toByteArray());
        if (bucket.p > this.p) {
            this.p++;
            raf.seek(4);
            byte[] bytes = new byte[((int) Math.pow(2, p)) * 8];
            raf.read(bytes);
            raf.write(bytes);
        }
        int newBucketNumber;
        if (reallocated.size() > 0) {
            newBucketNumber = hash(reallocated.get(0).key);
        } else {
            newBucketNumber = hash(keyAddressPair.key);
        }
        raf.seek(4 + (newBucketNumber * 8L));
        rafBucket.seek(findEmptySpace(rafBucket));
        Bucket newBucket = new Bucket(bucketLength, p);
        raf.writeLong(rafBucket.getFilePointer());
        rafBucket.write(newBucket.toByteArray());
        rafBucket.close();
        raf.close();
        for (int i = 0; i < reallocated.size(); i++) {
            insert(reallocated.get(i));
        }
    }

    // Método privado para encontrar um espaço vazio
    private long findEmptySpace(RandomAccessFile raf) throws IOException{
        raf.seek(0);
        boolean found = false;
        long propL = 0;
        while(!found && raf.getFilePointer() != raf.length()) {
            Bucket prop = new Bucket(raf, bucketLength);
            propL = prop.toByteArray().length;
            if(prop.p == 0){
                found = true;
            }
        }
        if(found){
            raf.seek(raf.getFilePointer() - propL);
            return raf.getFilePointer();
        }else{
            return raf.getFilePointer();
        }
    }

    // Método para atualizar o endereço de uma chave
    public boolean updateKeyAddress(int id, long newAddress) throws IOException{
        RandomAccessFile raf = new RandomAccessFile(hashTablePath, "r");
        int bucketNumber = hash(id);
        raf.seek(4 + (bucketNumber * 8L));
        long bucketAddress = raf.readLong();
        RandomAccessFile rafBucket = new RandomAccessFile(bucketsTablePath, "rw");
        rafBucket.seek(bucketAddress);
        Bucket bucket = new Bucket(rafBucket, bucketLength);
        boolean updated = bucket.update(id, newAddress);
        if(updated){
            rafBucket.seek(bucketAddress);
            rafBucket.write(bucket.toByteArray());
        }
        raf.close();
        rafBucket.close();
        return updated;
    }

    // Método para remover uma chave
    public boolean remove(int id) throws IOException{
        RandomAccessFile raf = new RandomAccessFile(hashTablePath, "rw");
        int bucketNumber = hash(id);
        raf.seek(4 + (bucketNumber * 8L));
        long bucketAddress = raf.readLong();
        RandomAccessFile rafBucket = new RandomAccessFile(bucketsTablePath, "rw");
        rafBucket.seek(bucketAddress);
        Bucket bucket = new Bucket(rafBucket, bucketLength);
        boolean removed = bucket.remove(id);
        int bucketItemsLength = bucket.getBucketItemsLength();
        if(removed){
            int possibleBucketNumber = hash(id, bucket.p-1);
            raf.seek(4 + (possibleBucketNumber * 8L));
            long possibleBucketAddress = raf.readLong();
            rafBucket.seek(possibleBucketAddress);
            Bucket possibleBucket = new Bucket(rafBucket, bucketLength);
            int possibleBucketItemsLength = possibleBucket.getBucketItemsLength();
            if(possibleBucketNumber != bucketNumber && bucket.p-1 != 0 && bucketItemsLength + possibleBucketItemsLength <= bucketLength){
                for(int i = 0; i < bucketItemsLength; i++){
                    possibleBucket.insertFreeSpace(bucket.keyAddressPairs[i]);
                }
                bucket.makeBlank();
                rafBucket.seek(possibleBucketAddress);
                rafBucket.write(possibleBucket.toByteArray());
                raf.seek(4 + (bucketNumber * 8L));
                raf.writeLong(possibleBucketAddress);
            }
            rafBucket.seek(bucketAddress);
            rafBucket.write(bucket.toByteArray());
        }
        rafBucket.close();
        raf.close();
        return removed;
    }

    // Método privado para definir os parâmetros iniciais
    private void setParameters() {
        try {
            RandomAccessFile raf = new RandomAccessFile(hashTablePath, "rw");
            this.p = raf.readInt();
            if (this.p == 0) {
                try {
                    new FileOutputStream(bucketsTablePath).close();
                    RandomAccessFile raf2 = new RandomAccessFile(bucketsTablePath, "rw");
                    raf2.writeLong(0);
                    raf2.close();
                } catch (FileNotFoundException e) {
                    System.out.println("Arquivo não encontrado");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                this.p++;
                raf.seek(0);
                raf.writeInt(p);
                raf.writeLong(0);
                RandomAccessFile rafBucket = new RandomAccessFile(bucketsTablePath, "rw");
                rafBucket.seek(0);
                rafBucket.write(new Bucket(bucketLength, p).toByteArray());
                raf.writeLong(rafBucket.getFilePointer());
                rafBucket.write(new Bucket(bucketLength, p).toByteArray());
                rafBucket.close();
            }
            raf.close();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }
}

// Interface para a função hash
interface HashInterface {
    int hash(int id);
}
