package DAO.indexes;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class Bucket implements HashInterface {
    int p;
    final private int bucketLength;
    KeyAddressPair[] keyAddressPairs;

    Bucket(int bucketLength, int p) {
        this.p = p;
        this.bucketLength = bucketLength;
        this.keyAddressPairs = new KeyAddressPair[bucketLength];
    }

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

    public int hash(int id) {
        return (int) (id % (Math.pow(2, p)));
    }

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

    public boolean update(int id, long newAddress){
        int index = retrieveIndexInsideBucketIfExists(id);
        if(index >= 0){
            keyAddressPairs[index].address = newAddress;
            return true;
        }else{
            return false;
        }
    }

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

    public void makeBlank(){
        for(int i = 0; i < bucketLength; i++){
            keyAddressPairs[i] = null;
        }
        this.p = 0;
    }

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
