//Arthur L F Pfeilsticker - 617553
//Leonardo B Marques - 793952
package DAO.indexes;

// Classe pública que representa um par de chave-endereço.
public class KeyAddressPair {

    // Variáveis de instância.
    public int key;       // Chave do par.
    public long address;  // Endereço associado à chave.

    // Construtor da classe que inicializa o par de chave-endereço.
    public KeyAddressPair(int key, long address){
        this.key = key;       // Define a chave.
        this.address = address; // Define o endereço.
    }
}
