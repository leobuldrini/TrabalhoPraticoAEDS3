package DAO.patternMatch;
import java.util.Arrays;

public class BoyerMoore
{
    int comparacoes = 0;

    public void findPattern(String t, String p, int currentLine)
    {
        char[] text = t.toCharArray();
        indexOf(text, p, currentLine);
    }

    public void indexOf(char[] text, String p, int currentLine)
    {
        char[] pattern = p.toCharArray();
        if (pattern.length == 0)
            return;
        int[] charTable = makeCharTable(pattern);
        int[] offsetTable = makeOffsetTable(pattern);
        comparacoes++;
        for (int i = pattern.length - 1, j; i < text.length;)
        {
            comparacoes++;
            for (j = pattern.length - 1; pattern[j] == text[i]; --i, --j) {
                comparacoes++;
                if (j == 0) {
                    System.out.println("Padrão \"" + p + "\" encontrado na linha " + currentLine + " na coluna " + i);
                    System.out.println("Número de comparações desde o último match ou inicio do arquivo: " + comparacoes);
                    comparacoes = 0;
                    break;
                }
                comparacoes++;
            }
            // i += pattern.length - j; // For naive method
            i += Math.max(offsetTable[pattern.length - 1 - j], charTable[text[i]]);
            comparacoes++;
        }
    }

    private int[] makeCharTable(char[] pattern)
    {
        final int ALPHABET_SIZE = 256;
        int[] table = new int[ALPHABET_SIZE];
        Arrays.fill(table, pattern.length);
        for (int i = 0; i < pattern.length - 1; ++i)
            table[pattern[i]] = pattern.length - 1 - i;
        return table;
    }

    private static int[] makeOffsetTable(char[] pattern)
    {
        int[] table = new int[pattern.length];
        int lastPrefixPosition = pattern.length;
        for (int i = pattern.length - 1; i >= 0; --i)
        {
            if (isPrefix(pattern, i + 1))
                lastPrefixPosition = i + 1;
            table[pattern.length - 1 - i] = lastPrefixPosition - i + pattern.length - 1;
        }
        for (int i = 0; i < pattern.length - 1; ++i)
        {
            int slen = suffixLength(pattern, i);
            table[slen] = pattern.length - 1 - i + slen;
        }
        return table;
    }

    private static boolean isPrefix(char[] pattern, int p)
    {
        for (int i = p, j = 0; i < pattern.length; ++i, ++j)
            if (pattern[i] != pattern[j])
                return false;
        return true;
    }

    private static int suffixLength(char[] pattern, int p)
    {
        int len = 0;
        for (int i = p, j = pattern.length - 1; i >= 0 && pattern[i] == pattern[j]; --i, --j)
            len += 1;
        return len;
    }
}