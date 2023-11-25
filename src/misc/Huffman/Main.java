import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception{
        RandomAccessFile raf = new RandomAccessFile("breaches.csv", "r");
        StringBuilder source = new StringBuilder();
        long length = raf.length();
        while (raf.getFilePointer() != length) {
            String line = raf.readLine();
            source.append(line);
        }
        raf.close();
        Huffman tree = new Huffman(String.valueOf(source));
        tree.encode();
        tree.saveDictionary();
        tree.compress();
        String decompressed = tree.decompress();
        new FileOutputStream("decompressed.csv").close();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("decompressed.txt"))) {
            // Parse and format the data as CSV.
            // This might involve splitting the data into records and fields and writing them line by line.
            writer.write(decompressed);
        }

        RandomAccessFile raf2 = new RandomAccessFile("teste.txt", "rw");
        raf2.seek(0);
        raf2.writeUTF("a banda dos urubus batuca muito");
        raf2.close();
    }
}
