package models;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Registros {
    final private String filePath;
    final private BTree index;

    public Registros(String filepath, BTree index){
        this.filePath = filepath;
        this.index = index;
    };

    public void readAllBreaches() throws Exception{
        try{
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            Breach breach = new Breach();
            raf.seek(0);
            int lastId = raf.readInt();
            System.out.println("\n\n> Último id: " + lastId + "\n---------------------");
            while(raf.getFilePointer() < raf.length()){
                long lapideAdd = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();
                if(lapide == 0x01){
                    raf.seek(raf.getFilePointer() + tamanhoRegistro);
                    continue;
                }
                byte[] registro = new byte[tamanhoRegistro];
                raf.read(registro);
                breach.fromByteArray(registro);
                System.out.println(breach);
            }
            System.out.println("---------------------");
            raf.close();
        }catch (FileNotFoundException e){
            System.out.println("Arquivo não encontrado");
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void inserirRegistro(Breach breach) throws Exception{
        try{
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            int lastId = raf.readInt();
            lastId++;
            breach.id = lastId;
            byte[] breachBytes = breach.toByteArray();
            raf.seek(0);
            raf.writeInt(lastId);
            boolean espacoVago = false;
            int regLength = 0;
            while(!espacoVago && raf.getFilePointer() < raf.length()){
                boolean lapide = raf.readByte() != 0;
                regLength = raf.readInt();
                if(lapide && breachBytes.length <= regLength){
                    espacoVago = true;
                }else{
                    raf.seek(raf.getFilePointer() + regLength);
                }
            }
            if(espacoVago){
                raf.seek(raf.getFilePointer() - 5);
            }
            index.addIndex(new KeyAddressPair(breach.id, raf.getFilePointer()));
            raf.write((byte)0x00);
            raf.writeInt(Math.max(regLength, breachBytes.length));
            raf.write(breachBytes);
            raf.close();
        }catch (FileNotFoundException e){
            System.out.println("Arquivo não encontrado");
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void limparRegistros() throws IOException {
        new FileOutputStream(filePath).close();
        try{
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            raf.writeInt(-1);
            raf.close();
        }catch (FileNotFoundException e){
            System.out.println("Arquivo não encontrado");
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public Breach deletarRegistro(int id){
        Breach removed = new Breach();
        try{
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            boolean found = false;
            int currentId = 0;
            int registroLength = 0;
            raf.seek(4);
            while(!found && raf.getFilePointer() < raf.length()){
                boolean dead = raf.readByte() != 0;
                registroLength = raf.readInt();
                if(dead){
                    raf.seek(raf.getFilePointer() + registroLength);
                    continue;
                }
                byte[] registroBytes = new byte[registroLength];
                raf.read(registroBytes);
                removed.fromByteArray(registroBytes);
                if(removed.id == id){
                    found = true;
                }
            }
            if(found){
                raf.seek(raf.getFilePointer() - registroLength - 5);
                raf.write((byte)0x01);
            }
            raf.close();
        }catch (FileNotFoundException e){
            System.out.println("Arquivo não encontrado");
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        return removed;
    }

    public Breach retrieveBreach(int id) throws IOException{
        long address = index.retrieveBreachAddress(id);
        if(address < 0) return null;
        RandomAccessFile raf = new RandomAccessFile(filePath, "r");
        raf.seek(address);
        Breach breach = new Breach();
        byte lapide = raf.readByte();
        int tamanhoRegistro = raf.readInt();
        if(lapide == 0x01){
            raf.seek(raf.getFilePointer() + tamanhoRegistro);
            return null;
        }
        byte[] registro = new byte[tamanhoRegistro];
        raf.read(registro);
        breach.fromByteArray(registro);
        raf.close();
        return breach;
    }
}
