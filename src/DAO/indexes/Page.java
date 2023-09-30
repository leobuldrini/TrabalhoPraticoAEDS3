package DAO.indexes;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Page {

    final private int n;
    public int occuppied;
    public KeyAddressPair[] elements;
    public long[] pointers;
    public boolean isLeaf;

    public Page(RandomAccessFile raf, int n) throws IOException {
        this.n = n;
        occuppied = raf.readInt();
        this.elements = new KeyAddressPair[n - 1];
        this.pointers = new long[n];
        boolean leaf = true;
        for (int i = 0; i < n; i++) {
            pointers[i] = raf.readLong();
            if (pointers[i] > 0) {
                leaf = false;
            }else{
                pointers[i] = -1;
            }
            if (i < n - 1) {
                int key = raf.readInt();
                long add = raf.readLong();
                if(key >= 0 && add > 0){
                    elements[i] = new KeyAddressPair(key, add);
                }
            }
        }
        this.isLeaf = leaf;
    }

    public Page(int n, boolean leaf) {
        this.n = n;
        occuppied = 0;
        this.elements = new KeyAddressPair[n - 1];
        this.pointers = new long[n];
        for(int i = 0; i < n; i++){
            pointers[i] = -1;
        }
        this.isLeaf = leaf;
    }

    public long search(RandomAccessFile raf, int id, long address) throws IOException {
        raf.seek(address);
        Page page = new Page(raf, n);
        int i = 0;
        while (i < n - 1) {
            if (i >= page.occuppied || id < page.elements[i].key) {
                if (isLeaf) return -1;
                return search(raf, id, page.pointers[i]);
            } else if (page.elements[i].key == id) {
                return page.elements[i].address;
            }
            i++;
        }
        if (isLeaf) {
            return -1;
        }
        return search(raf, id, page.pointers[i]);
    }
    public boolean update(RandomAccessFile raf, long address, int id, long newAddress) throws IOException {
        raf.seek(address);
        Page page = new Page(raf, n);
        int i = 0;
        while (i < n - 1) {
            if (i >= page.occuppied || id < page.elements[i].key) {
                if (isLeaf) return false;
                return update(raf, page.pointers[i], id, newAddress);
            } else if (page.elements[i].key == id) {
                page.elements[i].address = newAddress;
                page.writeNode(raf, address);
                return true;
            }
            i++;
        }
        if (isLeaf) {
            return false;
        }
        return update(raf, page.pointers[i], id, newAddress);
    }

    private void writeNode(RandomAccessFile raf, long address) throws IOException{
        raf.seek(address);
        raf.write(this.toByteArray());
    }

    public byte[] toByteArray() throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(occuppied);
        int i;
        for (i = 0; i < n - 1; i++) {
            dos.writeLong(pointers[i]);
            if(elements[i] != null){
                dos.writeInt(elements[i].key);
                dos.writeLong(elements[i].address);
            }else{
                dos.writeInt(-1);
                dos.writeLong(-1);
            }
        }
        dos.writeLong(pointers[i]);
        return baos.toByteArray();
    }
}
