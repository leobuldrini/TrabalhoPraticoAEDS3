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
        RandomAccessFile raf = new RandomAccessFile(this.path, "r");
        raf.seek(root);
        Page rootPage = new Page(raf, T);
        rootPage.add(raf,keyAddressPair,root,0);
    }

    private void setRoot(){
        try{
            RandomAccessFile raf = new RandomAccessFile(path, "r");
            raf.seek(0);
            root = raf.readLong();
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
