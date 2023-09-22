package models;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Bucket {
    int p;
    int bucketLength;
    KeyAddressPair[] keyAddressPairs;

    Bucket(int bucketLength){
        this.p = 1;
        this.bucketLength = bucketLength;
        this.keyAddressPairs = new KeyAddressPair[bucketLength];
    }

    Bucket(RandomAccessFile raf, int bucketLength) throws IOException {
        this.bucketLength = bucketLength;
        p = raf.readInt();
        this.keyAddressPairs = new KeyAddressPair[bucketLength];
        for(int i = 0; i < bucketLength; i++){
            int key = raf.readInt();
            long add = raf.readLong();
            if(key >= 0 && add > 0){
                keyAddressPairs[i] = new KeyAddressPair(key, add);
            }
        }
    }

    long retrieveAddress(int id){
        boolean found = false;
        int i = 0;
        while(!found && i < bucketLength){
            if(keyAddressPairs[i] != null && keyAddressPairs[i].key == id) found = true;
            i++;
        }
        i--;
        if(found) return keyAddressPairs[i].address; else return -1;
    }

    public boolean insertFreeSpace(KeyAddressPair keyAddressPair){
        int i = 0;
        try{
            while(keyAddressPairs[i] != null){
                i++;
            }
            keyAddressPairs[i] = keyAddressPair;
            return true;
        }catch (IndexOutOfBoundsException err){
            return false;
        }
    }

    public byte[] toByteArray() throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(p);
        for(int i = 0; i < bucketLength; i++){
            if(keyAddressPairs[i] != null){
                dos.writeInt(keyAddressPairs[i].key);
                dos.writeLong(keyAddressPairs[i].address);
            }
        }
        return baos.toByteArray();
    }
}
