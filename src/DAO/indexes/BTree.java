//Arthur L F Pfeilsticker - 617553
//Leonardo B Marques - 793952
package DAO.indexes;

import java.io.IOException;
import java.io.RandomAccessFile;

// Classe que representa uma árvore B
public class BTree {

    // Endereço da raiz da árvore B
    public long root = 0;
    // Grau mínimo da árvore B
    final public int T;

    // Caminho do arquivo que armazena a árvore B
    final public String path;

    // Construtor da árvore B
    public BTree(String path, int T){
        this.T = T;
        this.path = path;
        setRoot();
    }

    // Método para buscar um ID na árvore B
    public boolean search(int id) throws IOException{
        RandomAccessFile raf = new RandomAccessFile(this.path, "r");
        raf.seek(root);
        Page rootPage = new Page(raf, T);
        long found = rootPage.search(raf, id, root);
        raf.close();
        return found != -1;
    }

    // Método para recuperar o endereço de uma violação associada a um ID
    public long retrieveBreachAddress(int id) throws IOException{
        RandomAccessFile raf = new RandomAccessFile(this.path, "r");
        raf.seek(root);
        Page rootPage = new Page(raf, T);
        long found = rootPage.search(raf, id, root);
        raf.close();
        return found;
    }

    // Método para atualizar o endereço associado a um ID
    public void updateKeyAddress(int id, long newAddress) throws IOException{
        RandomAccessFile raf = new RandomAccessFile(this.path, "rw");
        raf.seek(root);
        Page rootPage = new Page(raf, T);
        boolean updated = rootPage.update(raf, root, id, newAddress);
        raf.close();
    }

    // Método para adicionar um índice à árvore B
    public void addIndex(KeyAddressPair keyAddressPair) throws IOException{
        RandomAccessFile raf = new RandomAccessFile(this.path, "rw");
        raf.seek(root);
        Page rootPage = new Page(raf, T);

        if(rootPage.occuppied == T-1){
            raf.seek(raf.length());
            Page newPage = new Page(T, false);
            newPage.pointers[0] = this.root;
            this.root = raf.getFilePointer();
            raf.write(newPage.toByteArray());
            this.split_child(raf, this.root, 0);
            this.addNonFullIndex(raf, this.root, keyAddressPair);
            raf.seek(0);
            raf.writeLong(this.root);
        }else{
            this.addNonFullIndex(raf, this.root, keyAddressPair);
        }
    }

    // Método para adicionar um índice a uma página não preenchida
    public void addNonFullIndex(RandomAccessFile raf, long pageAdd, KeyAddressPair keyAddressPair) throws IOException{
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
                raf.seek(pageAdd);
                page = new Page(raf, T);
                if(keyAddressPair.key > page.elements[i].key){
                    i+=1;
                }
            }
            raf.seek(pageAdd);
            page = new Page(raf, T);
            addNonFullIndex(raf, page.pointers[i], keyAddressPair);
        }
    }

    // Método para dividir uma página filho
    public void split_child(RandomAccessFile raf, long dadAddress, int fullChildIndex) throws IOException{
        raf.seek(dadAddress);
        Page dad = new Page(raf, T);
        long fullChildAddress = dad.pointers[fullChildIndex];
        raf.seek(fullChildAddress);
        Page fullChild = new Page(raf, this.T);
        Page newPage = new Page(this.T, fullChild.isLeaf);
        long newPageAddress = raf.length();
        raf.seek(newPageAddress);
        for(int i = T-1; i > fullChildIndex + 1; i--){
            dad.pointers[i] = dad.pointers[i-1];
        }
        dad.pointers[fullChildIndex+1] = raf.getFilePointer();
        for(int i = T - 2; i > fullChildIndex; i--){
            dad.elements[i] = dad.elements[i-1];
        }
        dad.elements[fullChildIndex] = fullChild.elements[(T/2) - 1];
        dad.occuppied++;
        System.arraycopy(fullChild.elements, (T/2), newPage.elements, 0, T/2 - 1);
        newPage.occuppied = fullChild.occuppied/2;
        for(int i = T/2-1; i < T-1; i++){
            fullChild.elements[i] = null;
        }
        fullChild.occuppied /= 2;
        if(!fullChild.isLeaf){
            System.arraycopy(fullChild.pointers, (T/2), newPage.pointers, 0, (T/2));
            for(int i = T/2; i < T; i++){
                fullChild.pointers[i] = -1;
            }
        }
        raf.write(newPage.toByteArray());
        raf.seek(fullChildAddress);
        raf.write(fullChild.toByteArray());
        raf.seek(dadAddress);
        raf.write(dad.toByteArray());
    }

    // Método para definir a raiz da árvore B
    public void setRoot(){
        try{
            RandomAccessFile raf = new RandomAccessFile(path, "rw");
            raf.seek(0);
            root = raf.readLong();
            if(root == 0){
                Page newRoot = new Page(T, true);
                root = raf.getFilePointer();
                raf.write(newRoot.toByteArray());
            }
            raf.seek(0);
            raf.writeLong(root);
            raf.close();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    // Método para salvar o índice em um novo caminho
    public void saveIndex(String newPath) throws IOException{
        RandomAccessFile rafWrite = new RandomAccessFile(newPath, "rw");
        RandomAccessFile rafRead = new RandomAccessFile(path, "r");
        rafWrite.writeLong(root);
        rafWrite.seek(root);
        saveNodes(rafRead,rafWrite,root);
        rafWrite.close();
    }

    // Método recursivo para salvar os nós da árvore B
    public void saveNodes(RandomAccessFile rafRead, RandomAccessFile rafWrite, long address) throws IOException{
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

    // Método para remover um ID da árvore B
    public boolean remove(int id) throws IOException{
        updateKeyAddress(id, -1);
        return true;
    }

}
