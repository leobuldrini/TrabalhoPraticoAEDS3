package main;

import DAO.Registros;
import DAO.indexes.BTree;
import DAO.indexes.ExtendedHash;
import DAO.indexes.InvertedIndex;
import core.CRUDMain;
import models.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main {

    public static void main(String[] args) throws IOException {
        BTree bTree = new BTree(System.getProperty("user.dir") + "/src/dataset/index.btree", 8);
        ExtendedHash exHash = new ExtendedHash(4, System.getProperty("user.dir") + "/src/dataset/buckets.hash", System.getProperty("user.dir") + "/src/dataset/index.hash");
        String[] stopWords = {"call", "upon", "still", "nevertheless", "down", "every", "forty", "‘re", "always", "whole", "side", "n't", "now", "however", "an", "show", "least", "give", "below", "did", "sometimes", "which", "'s", "nowhere", "per", "hereupon", "yours", "she", "moreover", "eight", "somewhere", "within", "whereby", "few", "has", "so", "have", "for", "noone", "top", "were", "those", "thence", "eleven", "after", "no", "’ll", "others", "ourselves", "themselves", "though", "that", "nor", "just", "’s", "before", "had", "toward", "another", "should", "herself", "and", "these", "such", "elsewhere", "further", "next", "indeed", "bottom", "anyone", "his", "each", "then", "both", "became", "third", "whom", "‘ve", "mine", "take", "many", "anywhere", "to", "well", "thereafter", "besides", "almost", "front", "fifteen", "towards", "none", "be", "herein", "two", "using", "whatever", "please", "perhaps", "full", "ca", "we", "latterly", "here", "therefore", "us", "how", "was", "made", "the", "or", "may", "’re", "namely", "'ve", "anyway", "amongst", "used", "ever", "of", "there", "than", "why", "really", "whither", "in", "only", "wherein", "last", "under", "own", "therein", "go", "seems", "‘m", "wherever", "either", "someone", "up", "doing", "on", "rather", "ours", "again", "same", "over", "‘s", "latter", "during", "done", "'re", "put", "'m", "much", "neither", "among", "seemed", "into", "once", "my", "otherwise", "part", "everywhere", "never", "myself", "must", "will", "am", "can", "else", "although", "as", "beyond", "are", "too", "becomes", "does", "a", "everyone", "but", "some", "regarding", "‘ll", "against", "throughout", "yourselves", "him", "'d", "it", "himself", "whether", "move", "’m", "hereafter", "re", "while", "whoever", "your", "first", "amount", "twelve", "serious", "other", "any", "off", "seeming", "four", "itself", "nothing", "beforehand", "make", "out", "very", "already", "various", "until", "hers", "they", "not", "them", "where", "would", "since", "everything", "at", "together", "yet", "more", "six", "back", "with", "thereupon", "becoming", "around", "due", "keep", "somehow", "n‘t", "across", "all", "when", "i", "empty", "nine", "five", "get", "see", "been", "name", "between", "hence", "ten", "several", "from", "whereupon", "through", "hereby", "'ll", "alone", "something", "formerly", "without", "above", "onto", "except", "enough", "become", "behind", "’d", "its", "most", "n’t", "might", "whereas", "anything", "if", "her", "via", "fifty", "is", "thereby", "twenty", "often", "whereafter", "their", "also", "anyhow", "cannot", "our", "could", "because", "who", "beside", "by", "whence", "being", "meanwhile", "this", "afterwards", "whenever", "mostly", "what", "one", "nobody", "seem", "less", "do", "‘d", "say", "thus", "unless", "along", "yourself", "former", "thru", "he", "hundred", "three", "sixty", "me", "sometime", "whose", "you", "quite", "’ve", "about", "even", "-", "\\.", ","};
        InvertedIndex invertedIndex = new InvertedIndex(System.getProperty("user.dir") + "/src/dataset/", "description", stopWords);
        InvertedIndex invertedIndexSector = new InvertedIndex(System.getProperty("user.dir") + "/src/dataset/", "sector", stopWords);
        Registros r = new Registros(System.getProperty("user.dir") + "/src/dataset/breaches.db", bTree, exHash, invertedIndex, invertedIndexSector);
        CRUDMain crudMain = new CRUDMain(r);
        crudMain.menu();
    }
}
