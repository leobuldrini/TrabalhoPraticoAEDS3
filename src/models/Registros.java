package models;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Registros {
    final private String filePath;
    final private BTree bTreeIndex;
    final private ExtendedHash extendedHashIndex;

    public Registros(String filepath, BTree bTreeIndex, ExtendedHash extendedHash) {
        this.filePath = filepath;
        this.bTreeIndex = bTreeIndex;
        this.extendedHashIndex = extendedHash;
    }

    ;

    public void readAllBreaches() {
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            Breach breach = new Breach();
            raf.seek(0);
            int lastId = raf.readInt();
            System.out.println("\n\n> Último id: " + lastId + "\n---------------------");
            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();
                if (lapide == 0x01) {
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
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Breach retrieveBreachSequentially(int id) {
        Breach breach = new Breach();
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            raf.seek(0);
            raf.readInt();
            boolean found = false;
            int i = 1;
            while (raf.getFilePointer() < raf.length() && !found) {
                byte lapide = raf.readByte();
                int tamanhoRegistro = raf.readInt();
                if (lapide == 0x01) {
                    raf.seek(raf.getFilePointer() + tamanhoRegistro);
                    continue;
                }
                byte[] registro = new byte[tamanhoRegistro];
                raf.read(registro);
                breach.fromByteArray(registro);
                if (breach.id == id) {
                    found = true;
                }else{
                    System.out.print(i + " ");
                    i++;
                }
            }
            raf.close();
            System.out.println("---------------------");
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return breach;
    }

    public void inserirRegistro(Breach breach){
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            int lastId = raf.readInt();
            lastId++;
            breach.id = lastId;
            byte[] breachBytes = breach.toByteArray();
            raf.seek(0);
            raf.writeInt(lastId);
            boolean espacoVago = false;
            int regLength = 0;
            while (!espacoVago && raf.getFilePointer() < raf.length()) {
                boolean lapide = raf.readByte() != 0;
                regLength = raf.readInt();
                if (lapide && breachBytes.length <= regLength) {
                    espacoVago = true;
                } else {
                    raf.seek(raf.getFilePointer() + regLength);
                }
            }
            if (espacoVago) {
                raf.seek(raf.getFilePointer() - 5);
            }
            KeyAddressPair keyAddressPair = new KeyAddressPair(breach.id, raf.getFilePointer());
            bTreeIndex.addIndex(keyAddressPair);
            extendedHashIndex.insert(keyAddressPair);
            raf.write((byte) 0x00);
            raf.writeInt(Math.max(regLength, breachBytes.length));
            raf.write(breachBytes);
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void limparRegistros(){
        try {
            new FileOutputStream(filePath).close();
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            raf.writeInt(-1);
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Breach deletarRegistro(int id) {
        Breach removed = new Breach();
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            boolean found = false;
            int currentId = 0;
            int registroLength = 0;
            raf.seek(4);
            while (!found && raf.getFilePointer() < raf.length()) {
                boolean dead = raf.readByte() != 0;
                registroLength = raf.readInt();
                if (dead) {
                    raf.seek(raf.getFilePointer() + registroLength);
                    continue;
                }
                byte[] registroBytes = new byte[registroLength];
                raf.read(registroBytes);
                removed.fromByteArray(registroBytes);
                if (removed.id == id) {
                    found = true;
                }
            }
            if (found) {
                raf.seek(raf.getFilePointer() - registroLength - 5);
                raf.write((byte) 0x01);
            }
            raf.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return removed;
    }

    public Breach retrieveBreachByBTree(int id) throws IOException {
        long address = bTreeIndex.retrieveBreachAddress(id);
        if (address < 0) return null;
        RandomAccessFile raf = new RandomAccessFile(filePath, "r");
        raf.seek(address);
        Breach breach = new Breach();
        byte lapide = raf.readByte();
        int tamanhoRegistro = raf.readInt();
        if (lapide == 0x01) {
            raf.seek(raf.getFilePointer() + tamanhoRegistro);
            return null;
        }
        byte[] registro = new byte[tamanhoRegistro];
        raf.read(registro);
        breach.fromByteArray(registro);
        raf.close();
        return breach;
    }

    public Breach retrieveBreachByExtendedHash(int id) throws IOException {
        long address = extendedHashIndex.retrieveAddress(id);
        if (address < 0) return null;
        RandomAccessFile raf = new RandomAccessFile(filePath, "r");
        raf.seek(address);
        Breach breach = new Breach();
        byte lapide = raf.readByte();
        int tamanhoRegistro = raf.readInt();
        if (lapide == 0x01) {
            raf.seek(raf.getFilePointer() + tamanhoRegistro);
            return null;
        }
        byte[] registro = new byte[tamanhoRegistro];
        raf.read(registro);
        breach.fromByteArray(registro);
        raf.close();
        return breach;
    }

    public boolean updateBreach(Breach breach) {
        try {
            long address = bTreeIndex.retrieveBreachAddress(breach.id);
            if (address < 0) return false;
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            raf.seek(address);
            byte lapide = raf.readByte();
            int tamanhoRegistro = raf.readInt();
            if (lapide == 0x01) {
                return false;
            }
            long add = raf.getFilePointer();
            byte[] registro = new byte[tamanhoRegistro];
            raf.read(registro);
            Breach oldBreach = new Breach();
            oldBreach.fromByteArray(registro);
            oldBreach.update(breach);
            byte[] updatedBreach = oldBreach.toByteArray();
            boolean canFit = updatedBreach.length <= tamanhoRegistro;
            if (canFit) {
                raf.seek(add);
                raf.write(updatedBreach);
            } else {
                raf.seek(add - 5);
                raf.write((byte) 0x01);
                raf.seek(raf.length());
                long newAddress = raf.getFilePointer();
                raf.write((byte) 0x00);
                raf.writeInt(updatedBreach.length);
                bTreeIndex.updateKeyAddress(breach.id, newAddress);
                extendedHashIndex.updateKeyAddress(breach.id, newAddress);
                raf.write(updatedBreach);
            }
            raf.close();
            return true;
        } catch (IOException err) {
            System.out.println("Erro no I/O do arquivo: " + err.getMessage());
        }
        return false;
    }
}
