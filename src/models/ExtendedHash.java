package models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ExtendedHash {
    final String bucketsTablePath;
    final String hashTablePath;
    final int bucketLength;

    int p;

    ExtendedHash(int bucketLength, String bucketsTablePath, String hashTablePath){
        this.bucketsTablePath = bucketsTablePath;
        this.hashTablePath = hashTablePath;
        this.bucketLength = bucketLength;
        setParameters();
    }

    private int hash(int id) {
        return (int) (id % (Math.pow(2, p)));
    }

    public long retrieveAddress(int id){
        long addressFound = -1;
        int bucketNumber = hash(id);
        try{
            RandomAccessFile raf = new RandomAccessFile(hashTablePath, "r");
            raf.seek(4 + (bucketNumber * 8L));
            long bucketAddress = raf.readLong();
            RandomAccessFile rafBucket = new RandomAccessFile(bucketsTablePath, "r");
            rafBucket.seek(bucketAddress);
            Bucket bucket = new Bucket(raf, bucketLength);
            addressFound = bucket.retrieveAddress(id);
            raf.close();
            rafBucket.close();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
        return addressFound;
    }

    public void insert(KeyAddressPair keyAddressPair){
        int bucketNumber = hash(keyAddressPair.key);
        try{
            RandomAccessFile raf = new RandomAccessFile(hashTablePath, "r");
            raf.seek(4 + (bucketNumber * 8L));
            long bucketAddress = raf.readLong();
            RandomAccessFile rafBucket = new RandomAccessFile(bucketsTablePath, "r");
            int i = 0;
            boolean freeEspace = false;
            int pBucket = rafBucket.readInt();
            while(i < bucketLength){
                int bId = rafBucket.readInt();
                long bAddress = rafBucket.readLong();
                if(bId == -1) freeEspace = true;
                i++;
            }
            if(freeEspace){
                rafBucket.seek(rafBucket.getFilePointer() - 12);
                rafBucket.writeInt(keyAddressPair.key);
                rafBucket.writeLong(keyAddressPair.address);
            }else{

            }
            raf.close();
            rafBucket.close();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }

    private void increaseP(){}

    private void setParameters() {
        try {
            RandomAccessFile raf = new RandomAccessFile(hashTablePath, "rw");
            this.p = raf.readInt();
            if(this.p == 0){
                this.p++;
                raf.seek(0);
                raf.writeInt(p);
                raf.writeLong(0);
                raf.writeLong(-1);
            }
            raf.close();
        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }
}
