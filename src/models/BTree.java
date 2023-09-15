package models;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BTree {

    private long root = 0;
    final private int T;

    final private String path;

    public BTree(String path, int T){
        this.T = T;
        this.path = path;
        setRoot();
    }

    public boolean search(int id) throws IOException{
        RandomAccessFile raf = new RandomAccessFile(this.path, "r");
        raf.seek(root);
        Page rootPage = new Page(raf, T);
        long found = rootPage.search(raf, id, root);
        raf.close();
        return found != -1;
    }

    public void addIndex(KeyAddressPair keyAddressPair) throws Exception{
        RandomAccessFile raf = new RandomAccessFile(this.path, "rw");
        raf.seek(root);
        Page rootPage = new Page(raf, T);

        if(rootPage.occuppied == T-1){
            raf.seek(raf.length() - 1);
            Page newPage = new Page(T, false);
            newPage.pointers[0] = this.root;
            this.root = raf.getFilePointer();
            raf.write(newPage.toByteArray());
            this.split_child(raf, this.root, 0);
            this.addNonFullIndex(raf, this.root, keyAddressPair);
        }else{
            this.addNonFullIndex(raf, this.root, keyAddressPair);
        }
    }

    private void addNonFullIndex(RandomAccessFile raf, long pageAdd, KeyAddressPair keyAddressPair) throws IOException{
        raf.seek(pageAdd);
        Page page = new Page(raf, this.T);
        int i = page.occuppied - 1;

        if(page.isLeaf){
            while(i >= 0 && (keyAddressPair.key < page.elements[i].key)){
                page.elements[i+1] = page.elements[i];
                i-=1;
            }
            page.elements[i+1] = keyAddressPair;
            page.occuppied++;
            raf.seek(pageAdd);
            raf.write(page.toByteArray());
        }else{
            while(i >= 0 && (keyAddressPair.key < page.elements[i].key)){
                i-=1;
            }
            i+=1;
            long tempPageAdd = page.pointers[i];
            raf.seek(tempPageAdd);
            Page tempPage = new Page(raf, this.T);
            if(tempPage.occuppied == T-1){
                split_child(raf, pageAdd, i);
                if(keyAddressPair.key > page.elements[i].key){
                    i+=1;
                }
            }
            addNonFullIndex(raf, page.pointers[i], keyAddressPair);
        }
    }

    public void split_child(RandomAccessFile raf, long dadAddress, int fullChildIndex) throws IOException{
        raf.seek(dadAddress);
        Page dad = new Page(raf, T);
        raf.seek(dad.pointers[fullChildIndex]);
        Page fullChild = new Page(raf, this.T);
        Page newPage = new Page(this.T, fullChild.isLeaf);
        raf.seek(raf.length() - 1);
        for(int i = T-1; i > fullChildIndex + 1; i--){
            dad.pointers[i] = dad.pointers[i-1];
        }
        dad.pointers[fullChildIndex+1] = raf.getFilePointer();
        for(int i = T - 2; i > fullChildIndex; i--){
            dad.elements[i] = dad.elements[i-1];
        }
        dad.elements[fullChildIndex] = fullChild.elements[(T/2) - 1];
        dad.occuppied++;
        System.arraycopy(fullChild.elements, (T/2) - 1, newPage.elements, 0, T-1);
        newPage.occuppied = fullChild.occuppied/2;
        for(int i = T/2-2; i < T; i++){
            fullChild.elements[i] = null;
        }
        fullChild.occuppied /= 2;
        if(!fullChild.isLeaf){
            System.arraycopy(fullChild.pointers, (T/2) - 1, newPage.pointers, 0, T-1);
            for(int i = T/2-1; i < T; i++){
                fullChild.pointers[i] = -1;
            }
        }
        raf.write(newPage.toByteArray());
    }

    private void setRoot(){
        try{
            RandomAccessFile raf = new RandomAccessFile(path, "rw");
            raf.seek(0);
            root = raf.readLong();
            if(root == 0){
                Page newRoot = new Page(T, true);
                root = raf.getFilePointer();
                raf.write(newRoot.toByteArray());
            }
            raf.close();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void saveIndex(String newPath) throws IOException{
        RandomAccessFile rafWrite = new RandomAccessFile(newPath, "rw");
        RandomAccessFile rafRead = new RandomAccessFile(path, "r");
        rafWrite.writeLong(root);
        rafWrite.seek(root);
        saveNodes(rafRead,rafWrite,root);
        rafWrite.close();
    }

    private void saveNodes(RandomAccessFile rafRead, RandomAccessFile rafWrite, long address) throws IOException{
        rafRead.seek(address);
        Page page = new Page(rafRead, T);
        rafWrite.write(page.toByteArray());
        if(!page.isLeaf){
            for(int i = 0; i < T; i++){
                if(page.pointers[i] > 0) {
                    saveNodes(rafRead, rafWrite, page.pointers[i]);
                }
            }
        }
    }


}
