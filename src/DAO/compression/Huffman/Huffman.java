package DAO.compression.Huffman;
import java.io.*;
import java.util.*;

public class Huffman {

    private final String source;

    public Huffman() {
        this.source = "";
        this.isDecompressed = false;
    }

    public boolean isDecompressed;

    public long originalFileSize = 0;

    HuffmanNode root;

    public Huffman(String source, boolean isDecompressed) throws IOException {
        StringBuilder strFull = new StringBuilder();
        RandomAccessFile raf = new RandomAccessFile(source, "r");
        long length = raf.length();
        originalFileSize = length;
        while (raf.getFilePointer() != length) {
            strFull.append(raf.readLine());
        }
        raf.close();
        this.source = strFull.toString();
        this.isDecompressed = isDecompressed;
    }

    private final HashMap<Character, HashMap<Integer, byte[]>> dictionary = new HashMap<>();


    HashMap<String, Integer> elems(String source) {
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < source.length(); i++) {
            String key = source.substring(i, i + 1);
            if (map.containsKey(key)) {
                map.put(key, map.get(key) + 1);
            } else {
                map.put(key, 1);
            }
        }
        return map;
    }

    private void insertByteInDict(char key, String code) {
        byte[] bytes = new byte[3];
        int i = 0;
        while (i < code.length()) {
            boolean isZero = code.charAt(i) != '1';
            byte buffer1 = (byte)((bytes[2] & 0xFF) >>> 7);
            byte buffer2 = (byte) ((bytes[1] & 0xFF) >>> 7);
            bytes[2] <<= 1;
            bytes[2] = (byte) (bytes[2] | (isZero ? 0x0 : 0x1));
            bytes[1] = (byte) (bytes[1] << 1);
            bytes[1] = (byte) (bytes[1] | buffer1);
            bytes[0] = (byte) (bytes[0] << 1);
            bytes[0] = (byte) (bytes[0] | buffer2);
            i++;
        }
        HashMap<Integer, byte[]> map = new HashMap<>();
        map.put(i, bytes);
        dictionary.put(key, map);
    }

    public void saveDictionary() throws Exception {
        new FileOutputStream("dictionary.bin").close();
        RandomAccessFile raf = new RandomAccessFile("dictionary.bin", "rw");
        raf.seek(0);
        List<Character> keys = new ArrayList<>(dictionary.keySet());
        for (int i = 0; i < keys.size(); i++) {
            char key = keys.get(i);
            HashMap<Integer, byte[]> map = dictionary.get(key);
            List<Integer> mapKeys = new ArrayList<>(map.keySet());
            raf.writeChar(key);
            int codeLength = mapKeys.get(0);
            raf.writeInt(codeLength);
            raf.write(map.get(codeLength));
        }
        raf.close();
    }

    public void saveTree(HuffmanNode node, RandomAccessFile raf) throws Exception {
        long address = raf.getFilePointer();
        raf.writeChar(node.key.charAt(0));
        raf.writeInt(node.value);
        if(node.left != null){
            raf.writeLong(address + 2 + 4 + 8 + 8);
            saveTree(node.left, raf);
        } else {
            raf.writeLong(-1);
        }
        if(node.right != null){
            raf.writeLong(address + 2 + 4 + 8 + 8 + (2 + 4 + 8));
            saveTree(node.right, raf);
        } else {
            raf.writeLong(-1);
        }
    }

    public void saveTree(HuffmanNode root) throws Exception{
        new FileOutputStream("tree.bin").close();
        RandomAccessFile raf = new RandomAccessFile("tree.bin", "rw");
        saveTree(root, raf);
        raf.close();
    }

    public long compress(int version) throws Exception {
        String userDir = System.getProperty("user.dir");

        // Verifica se o diretório contém "src" e, se não, adiciona "/src" ao final
        if(!userDir.contains("src")){
            userDir += "/src";
        }


        String directory = userDir + "/dataset/compressed";
        new FileOutputStream(directory + "/breachesHuffmanCompressao" + version + ".bin").close();
        RandomAccessFile raf = new RandomAccessFile(directory + "/breachesHuffmanCompressao" + version + ".bin", "rw");
        BitWriter bw = new BitWriter(raf);
        raf.seek(0);
        long stringLength = source.length();
        for (int i = 0; i < stringLength; i++) {
            char key = source.charAt(i);
            HashMap<Integer, byte[]> map = dictionary.get(key);
            List<Integer> mapKeys = new ArrayList<>(map.keySet());
            int codeLength = mapKeys.get(0);
            byte[] code = map.get(codeLength);
            int lastByte = 2 - (codeLength / 8);
            int remainingBits = codeLength % 8;
            if (remainingBits != 0) {
                byte b = code[lastByte];
                bw.writeBit(b, remainingBits);
            }
            for (int k = 2; k > lastByte; k--) {
                byte b = code[k];
                bw.writeBit(b, 8);
            }
        }
        bw.writeLastByte();
        long size = raf.getFilePointer();
        raf.close();
        return size;
    }

    public String decompress(int version) throws Exception {
        String userDir = System.getProperty("user.dir");

        // Verifica se o diretório contém "src" e, se não, adiciona "/src" ao final
        if(!userDir.contains("src")){
            userDir += "/src";
        }


        String directory = userDir + "/dataset/compressed";
        List<Byte> byteArray = new ArrayList<>();
        RandomAccessFile raf = new RandomAccessFile(directory + "/breachesHuffmanCompressao" + version + ".bin", "r");
        StringBuilder finalString = new StringBuilder();
        long length = raf.length();

        System.out.println("Tamanho do arquivo comprimido: " + length + " bytes");
        while (raf.getFilePointer() != length) {
            byte b = raf.readByte();
            byteArray.add(b);
        }
        HuffmanNode node = root;
        int k = 7;
        int i = 0;
        boolean maybeFinal = false;
        int size = byteArray.size();
        while(i < size && !maybeFinal){
            byte b = byteArray.get(i);
            b = (byte) ((b & 0xFF) >>> k);
            byte decision = b &= 0x1;
            if(decision == 0x0){
                node = node.left;
            } else {
                node = node.right;
            }
            assert node != null;
            if(node.left == null && node.right == null){
                finalString.append(node.key);
                node = root;
            }
            k--;
            if(k == -1){
                k = 7;
                i++;
            }
        }
        raf.close();
        return finalString.toString();
    }

    public void printCode(HuffmanNode root, String s) {
        if (root == null) {
            return;
        }
        if (root.left == null && root.right == null) {

            System.out.println(root.key + "   |  " + s);
            insertByteInDict(root.key.charAt(0), s);
            return;
        }
        printCode(root.left, s + "0");
        printCode(root.right, s + "1");
    }

    public void encode() throws Exception {
        HashMap<String, Integer> map = elems(source);
        ArrayList<String> keys = new ArrayList<>(map.keySet());
        ArrayList<Integer> values = new ArrayList<>(map.values());
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>(values.size(), new ImplementComparator());
        for (int i = 0; i < values.size(); i++) {
            HuffmanNode node = new HuffmanNode(keys.get(i), values.get(i));
            queue.add(node);
        }
        root = null;
        while (queue.size() > 1) {
            HuffmanNode x = queue.peek();
            queue.poll();
            HuffmanNode y = queue.peek();
            queue.poll();
            HuffmanNode f = new HuffmanNode("\\", x.value + y.value, x, y);
            root = f;
            queue.add(f);
        }
        System.out.println(" Char | Huffman code ");
        System.out.println("--------------------");
        saveTree(root);
        printCode(root, "");
    }
}

class HuffmanNode {
    String key;
    int value;
    HuffmanNode left;
    HuffmanNode right;

    HuffmanNode(String key, int value) {
        this.key = key;
        this.value = value;
        this.left = null;
        this.right = null;
    }

    HuffmanNode(String key, int value, HuffmanNode left, HuffmanNode right) {
        this.key = key;
        this.value = value;
        this.left = left;
        this.right = right;
    }
}

class ImplementComparator implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode x, HuffmanNode y) {
        return x.value - y.value;
    }
}

class BitWriter {
    private byte buffer;
    private int bitsLeft;
    private RandomAccessFile raf;

    public BitWriter(RandomAccessFile raf) {
        this.buffer = 0x0;
        this.bitsLeft = 8;
        this.raf = raf;
    }

    public void writeBit(byte toWrite, int bitLength) throws Exception {
        byte mask = toWrite;
        if (bitsLeft - bitLength >= 0) {
            mask = (byte) (mask << (bitsLeft - bitLength));
            buffer = (byte) (buffer | mask);
            bitsLeft -= bitLength;
        } else {
            mask = (byte) ((mask & 0xFF) >>> (bitLength - bitsLeft));
            buffer = (byte) (buffer | mask);
            raf.write(buffer);
            bitsLeft = (8 - (bitLength - bitsLeft));
            buffer = (byte) (toWrite << bitsLeft);
        }
        if (bitsLeft == 0) {
            raf.write(buffer);
            buffer = 0x0;
            bitsLeft = 8;
        }
    }

    public void writeLastByte() throws Exception {
        if (bitsLeft != 8) {
            raf.write(buffer);
        }
    }
}
