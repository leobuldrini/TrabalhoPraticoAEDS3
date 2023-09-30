//Arthur L F Pfeilsticker - 617553
//Leonardo B Marques - 793952
package main;

import DAO.Registros;
import DAO.indexes.BTree;
import DAO.indexes.ExtendedHash;
import DAO.indexes.InvertedIndex;
import core.CRUDMain;
import java.io.IOException;

// Classe principal que executa o programa
public class Main {

    // Método principal
    public static void main(String[] args) throws IOException {
        // Obtém o diretório do usuário atual
        String userDir = System.getProperty("user.dir");
        
        // Verifica se o diretório contém "src" e, se não, adiciona "/src" ao final
        if(!userDir.contains("src")){
            userDir += "/src";
        }
        
        // Cria uma instância da árvore B com o caminho especificado e grau T=8
        BTree bTree = new BTree(userDir + "/dataset/index.btree", 8);
        
        // Cria uma instância do hash estendido com os caminhos especificados e tamanho inicial de 4
        ExtendedHash exHash = new ExtendedHash(4, userDir + "/dataset/buckets.hash", userDir + "/dataset/index.hash");
        
        // Lista de palavras de parada (stop words) para serem ignoradas na indexação
        String[] stopWords = { "call", "upon", "still", "nevertheless", "down", "every", "forty", "'re", "always", "whole", "side", "n't", "now", "however", "an", "show", "least", "give", "below", "did", "sometimes", "which", "'s", "nowhere", "per", "hereupon", "yours", "she", "moreover", "eight", "somewhere", "within", "whereby", "few", "has", "so", "have", "for", "noone", "top", "were", "those", "thence", "eleven", "after", "no", "’ll", "others", "ourselves", "themselves", "though", "that", "nor", "just", "’s", "before", "had", "toward", "another", "should", "herself", "and", "these", "such", "elsewhere", "further", "next", "indeed", "bottom", "anyone", "his", "each", "then", "both", "became", "third", "whom", "‘ve", "mine", "take", "many", "anywhere", "to", "well", "thereafter", "besides", "almost", "front", "fifteen", "towards", "none", "be", "herein", "two", "using", "whatever", "please", "perhaps", "full", "ca", "we", "latterly", "here", "therefore", "us", "how", "was", "made", "the", "or", "may", "’re", "namely", "'ve", "anyway", "amongst", "used", "ever", "of", "there", "than", "why", "really", "whither", "in", "only", "wherein", "last", "under", "own", "therein", "go", "seems", "‘m", "wherever", "either", "someone", "up", "doing", "on", "rather", "ours", "again", "same", "over", "‘s", "latter", "during", "done", "'re", "put", "'m", "much", "neither", "among", "seemed", "into", "once", "my", "otherwise", "part", "everywhere", "never", "myself", "must", "will", "am", "can", "else", "although", "as", "beyond", "are", "too", "becomes", "does", "a", "everyone", "but", "some", "regarding", "‘ll", "against", "throughout", "yourselves", "him", "'d", "it", "himself", "whether", "move", "’m", "hereafter", "re", "while", "whoever", "your", "first", "amount", "twelve", "serious", "other", "any", "off", "seeming", "four", "itself", "nothing", "beforehand", "make", "out", "very", "already", "various", "until", "hers", "they", "not", "them", "where", "would", "since", "everything", "at", "together", "yet", "more", "six", "back", "with", "thereupon", "becoming", "around", "due", "keep", "somehow", "n‘t", "across", "all", "when", "i", "empty", "nine", "five", "get", "see", "been", "name", "between", "hence", "ten", "several", "from", "whereupon", "through", "hereby", "'ll", "alone", "something", "formerly", "without", "above", "onto", "except", "enough", "become", "behind", "’d", "its", "most", "n’t", "might", "whereas", "anything", "if", "her", "via", "fifty", "is", "thereby", "twenty", "often", "whereafter", "their", "also", "anyhow", "cannot", "our", "could", "because", "who", "beside", "by", "whence", "being", "meanwhile", "this", "afterwards", "whenever", "mostly", "what", "one", "nobody", "seem", "less", "do", "‘d", "say", "thus", "unless", "along", "yourself", "former", "thru", "he", "hundred", "three", "sixty", "me", "sometime", "whose", "you", "quite", "’ve", "about", "even", "-", "\\.", "," }; // (lista truncada para brevidade)
        
        // Cria uma instância do índice invertido para a coluna "description" ignorando as stop words
        InvertedIndex invertedIndex = new InvertedIndex(userDir + "/dataset/", "description", stopWords);
        
        // Cria uma instância do índice invertido para a coluna "sector" ignorando as stop words
        InvertedIndex invertedIndexSector = new InvertedIndex(userDir + "/dataset/", "sector", stopWords);
        
        // Cria uma instância de registros usando os índices criados anteriormente
        Registros r = new Registros(userDir + "/dataset/breaches.db", bTree, exHash, invertedIndex, invertedIndexSector);
        
        // Cria uma instância da classe CRUDMain e inicia o menu
        CRUDMain crudMain = new CRUDMain(r);
        crudMain.menu();
    }
}