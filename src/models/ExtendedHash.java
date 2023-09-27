package models;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class ExtendedHash implements HashInterface {
    final String bucketsTablePath;
    final String hashTablePath;
    final int bucketLength;

    int p;

    public ExtendedHash(int bucketLength, String bucketsTablePath, String hashTablePath) {
        this.bucketsTablePath = bucketsTablePath;
        this.hashTablePath = hashTablePath;
        this.bucketLength = bucketLength;
        setParameters();
    }

    public int hash(int id) {
        return (int) (id % (Math.pow(2, p)));
    }

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
        rafBucket.seek(rafBucket.length());
        Bucket newBucket = new Bucket(bucketLength, p);
        raf.writeLong(rafBucket.getFilePointer());
        rafBucket.write(newBucket.toByteArray());
        rafBucket.close();
        raf.close();
        for (int i = 0; i < reallocated.size(); i++) {
            insert(reallocated.get(i));
        }
    }

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

interface HashInterface {
    int hash(int id);
}
