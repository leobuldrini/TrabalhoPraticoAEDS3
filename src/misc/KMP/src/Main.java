import java.io.RandomAccessFile;

public class Main {


    public static void main(String[] args) throws Exception {
        KMP kmpSearch = new KMP();
        RandomAccessFile raf = new RandomAccessFile("src/breaches.csv", "r");
        long length = raf.length();
        int currentLine = 1;
        String pat = "plex";
        while (raf.getFilePointer() != length) {
            String line = raf.readLine();
            kmpSearch.kmpSearch(pat, line, currentLine);
            currentLine++;
        }
        raf.close();
    }
}

class KMP {
    int comparacoes = 0;
    void kmpSearch(String pat, String txt, int currentLine) {
        int patternLength = pat.length();
        int lineLength = txt.length();
        int[] lps = new int[patternLength];
        int j = 0; // index para o padrão

        // Constrói o vetor de falha do kMP
        computeLPSArray(pat, patternLength, lps);

        int i = 0; // index para a linha
        comparacoes++;
        while (i < lineLength) { //Se ainda é possível o padrão ser encontrado na linha
            comparacoes++;
            if (pat.charAt(j) == txt.charAt(i)) { // Avança de estado
                j++;
                i++;
            }
            comparacoes++;
            if (j == patternLength) { // Chegou no último estado do autômato
                System.out.println("Padrão encontrado na linha " + (currentLine) + " e coluna " + (i - j + 1));
                System.out.println("Número de comparações necessário: " + comparacoes);
                comparacoes = 0;
                j = lps[j - 1]; // Usa a função de falha para continuar a busca
            }
            

            // Falha de estado
            else if (i < lineLength && pat.charAt(j) != txt.charAt(i)) {
                comparacoes++;
                // Se estiver no estado 0, avança apenas na linha, caso contrário, utiliza do vetor de falha
                comparacoes++;
                if (j != 0)
                    j = lps[j - 1];
                else
                    i = i + 1;
            }else{
                comparacoes++;
            }
            comparacoes++;
        }
    }

    void computeLPSArray(String pattern, int patternLength, int[] failVector) {
        // Tamanho do maior prefixo possível
        int len = 0;
        failVector[0] = 0; // o primeiro estado sempre volta pra ele mesmo
        int i = 1; // Começa a partir do segundo estado

        comparacoes++;
        while (i < patternLength) {
            // Se as letras forem iguais, avança o estado, incrementa o tamanho do maior prefixo e registra o valor no vetor de falha
            comparacoes++;
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                failVector[i] = len;
                i++;
            } else // (pat[i] != pat[len])
            {
                // S
                comparacoes++;
                if (len != 0) {
                    len = failVector[len - 1];

                    // Also, note that we do not increment
                    // i here
                } else // if (len == 0)
                {
                    failVector[i] = len;
                    i++;
                }
            }
            comparacoes++;
        }
    }
}