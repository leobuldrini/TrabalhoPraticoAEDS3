package DAO;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;

public class RSA {
    private final RandomAccessFile raf;
    private int n;
    private int e;
    private int d;

    public RSA(String filename, int p, int q) throws Exception
    {
        raf = new RandomAccessFile(filename, "rw");
        raf.seek(0);
        setParameters(p, q);

    }

    public void setParameters(int p, int q) throws Exception {
        n = p*q;
        int z = (p - 1) * (q - 1);
        d = primeiroPrimoRelativo(z);
        if(d == -1)
            throw new Exception("Não foi possível encontrar um primo relativo a " + z);
        e = inversoMultiplicativo(d, z).intValue();
        if(e == -1)
            throw new Exception("Não foi possível encontrar o inverso multiplicativo de " + d + " mod " + z);
        System.out.println("Chave pública: (" + n + ", " + e + ")");
        System.out.println("Chave privada: (" + n + ", " + d + ")");

    }

    public void encrypt(String newFile) throws Exception {
        new FileOutputStream(newFile).close();
        RandomAccessFile raf2 = new RandomAccessFile(newFile, "rw");
        raf.seek(0);
        String line;
        int b;
        while(raf.getFilePointer() != raf.length()) {
            line = readLineIncludingNewLine(raf);
            //System.out.println("Lendo byte " + b);
            int length = line.length();
            for(int i = 0; i < length; i++) {
                b = line.charAt(i);
                byte[] c = powmodVerbose(b, e, n).toByteArray();
                //System.out.println("Escrevendo byte " + c);
                raf2.writeInt(c.length);
                raf2.write(c);
            }
//            int c = powmodVerbose(b, e, n).intValue();
            //System.out.println("Escrevendo byte " + c);
//            raf2.write(c);
        }
        raf2.close();
    }

    private String readLineIncludingNewLine(RandomAccessFile file) throws IOException {
        StringBuilder line = new StringBuilder();
        int ch = 0;
        boolean foundEndOfLine = false;
        long currentPosition;

        while (!foundEndOfLine && (ch = file.read()) != -1) {
            line.append((char) ch);

            if (ch == '\n') {
                foundEndOfLine = true;
            } else if (ch == '\r') {
                // Verifica se o próximo caractere é '\n'
                currentPosition = file.getFilePointer();
                if (file.read() != '\n') {
                    file.seek(currentPosition); // Volta um caractere se não for '\n'
                } else {
                    line.append('\n');
                }
                foundEndOfLine = true;
            }
        }

        return (line.isEmpty() && ch == -1) ? null : line.toString();
    }

    public void decrypt(String encryptedFile, String newFile) throws Exception {
        new FileOutputStream(newFile).close();
        RandomAccessFile rafDecrypt = new RandomAccessFile(newFile, "rw");
        RandomAccessFile rafEncrypt = new RandomAccessFile(encryptedFile, "rw");
        rafEncrypt.seek(0);
        rafDecrypt.seek(0);
        byte[] bArray;
        StringBuilder sb = new StringBuilder();
        while(rafEncrypt.getFilePointer() != rafEncrypt.length()) {
            int byteLength = rafEncrypt.readInt();
            bArray = new byte[byteLength];
            rafEncrypt.read(bArray);
            BigInteger b = new BigInteger(bArray);
            //System.out.println("Lendo byte " + b);
            char c = (char) powmodVerbose(b, d, n).intValue();
            //System.out.println("Escrevendo byte " + c);
            if(c == '\n') {
                rafDecrypt.writeBytes(sb.toString());
                rafDecrypt.writeBytes(String.valueOf(c));
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        rafDecrypt.close();
    }

    private BigInteger inversoMultiplicativo(int d, int z) {
        BigInteger bigD = BigInteger.valueOf(d % z);
        BigInteger bigZ = BigInteger.valueOf(z);
        for (BigInteger e = BigInteger.valueOf(1); e.compareTo(bigZ) < 0; e = e.add(BigInteger.valueOf(1))) {
            if ((e.multiply(bigD)).mod(bigZ).compareTo(BigInteger.valueOf(1)) == 0) {
                return e;
            }
        }
        return BigInteger.valueOf(-1); // Retorna -1 se não encontrar o inverso
    }

    private int mdc(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    // Função para encontrar o primeiro número primo relativo a um dado número n
    private int primeiroPrimoRelativo(int n) {
        for (int i = 2; i < n; i++) {
            if (mdc(n, i) == 1) {
                return i;
            }
        }
        return -1; // Retorna -1 se não encontrar um primo relativo
    }

    private BigInteger powmodVerbose(int base, int exponent, int modulus) {
        return powmodVerbose(BigInteger.valueOf(base), exponent, modulus);
    }

    private BigInteger powmodVerbose(BigInteger baseP, int exponentP, int modulusP) {
        BigInteger modulus = BigInteger.valueOf(modulusP);
        BigInteger result = BigInteger.valueOf(1);
        //System.out.println("Calculando " + base + " elevado a " + exponent + ", modulo " + modulus + ".");
        //System.out.println("Expoente " + exponent + " em binário é " + Integer.toBinaryString(exponentP) + ", vamos BIT a BIT agora, iniciando com " + result);

        String bitString = Integer.toBinaryString(exponentP);

        for (char bit : bitString.toCharArray()) {
            BigInteger sqResult = (result.multiply(result)).mod(modulus); // Sempre calculamos o módulo do resultado ao quadrado

            if (bit == '0') {
                //System.out.println("BIT 0: " + result + " ao quadrado=" + (result.multiply(result)) + ", mod " + modulus + " é igual a " + sqResult);
                result = sqResult; // Se BIT for zero, mantém apenas o cálculo acima
            }

            if (bit == '1') {
                //System.out.println("BIT 1: " + result + " ao quadrado=" + (result.multiply(result)) + " multiplicado por " + base + "=" + ((result.multiply(result)).multiply(base)) + ", mod " + modulus + " é igual a " + newResult);
                result = (sqResult.multiply(baseP)).mod(modulus);
            }
        }

//        System.out.println("Resultado " + result);
        return result;
    }
}