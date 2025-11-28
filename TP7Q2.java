import java.io.*;
import java.util.*;

class No2 {
    String nome;
    No2 esq, dir;

    public No2(String nome) {
        this.nome = nome;
        this.esq = this.dir = null;
    }
}

class No1 {
    int chave;
    No1 esq, dir;
    No2 raizSegunda;

    public No1(int chave) {
        this.chave = chave;
        this.esq = this.dir = null;
        this.raizSegunda = null;
    }
}

class ArvoreDeArvore {
    private No1 raiz1;

    public ArvoreDeArvore() {
        int[] valores = {7,3,11,1,5,9,13,0,2,4,6,8,10,12,14};
        for (int v : valores) {
            raiz1 = inserirPrimeira(raiz1, v);
        }
    }

    private No1 inserirPrimeira(No1 i, int x) {
        if (i == null) return new No1(x);
        if (x < i.chave) i.esq = inserirPrimeira(i.esq, x);
        else if (x > i.chave) i.dir = inserirPrimeira(i.dir, x);
        return i;
    }

    private No2 inserirSegunda(No2 i, String nome) {
        if (i == null) return new No2(nome);

        int cmp = nome.compareTo(i.nome);
        if (cmp < 0) i.esq = inserirSegunda(i.esq, nome);
        else if (cmp > 0) i.dir = inserirSegunda(i.dir, nome);
        return i;
    }

    public void inserirRegistro(String nome, int estimated) {
        int indice = estimated % 15;
        No1 no = buscarPrimeira(raiz1, indice);

        if (no != null) {
            no.raizSegunda = inserirSegunda(no.raizSegunda, nome);
        }
    }

    private No1 buscarPrimeira(No1 i, int x) {
        if (i == null) return null;
        if (x == i.chave) return i;
        if (x < i.chave) return buscarPrimeira(i.esq, x);
        return buscarPrimeira(i.dir, x);
    }

    public void pesquisar(String nome) {
        StringBuilder out = new StringBuilder();

        // MOSTRAR CAMINHO NA PRIMEIRA ÁRVORE (para mostrar estrutura)
        out.append("=> ");
        mostrarEstruturaPrimeira(raiz1, out);
        out.append("\n");

        boolean encontrado = false;

        // PESQUISAR EM TODAS AS SEGUNDAS ÁRVORES
        List<No1> lista = new ArrayList<>();
        coletarNosEmOrdem(raiz1, lista);

        for (No1 no : lista) {
            if (no.raizSegunda != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(nome).append(" (busca em ").append(no.chave).append(") => ");
                boolean achou = pesquisarSegunda(no.raizSegunda, nome, sb);
                out.append(sb).append("\n");
                if (achou) encontrado = true;
            }
        }

        out.append(encontrado ? "SIM" : "NAO");
        System.out.println(out);
    }

    // MÉTODO CORRIGIDO: Mostrar estrutura da PRIMEIRA árvore com ESQ/DIR
    private void mostrarEstruturaPrimeira(No1 i, StringBuilder sb) {
        if (i == null) return;
        
        Stack<No1> pilha = new Stack<>();
        pilha.push(i);
        
        while (!pilha.isEmpty()) {
            No1 atual = pilha.pop();
            
            if (atual == raiz1) {
                sb.append("raiz ");
            }
            
            // Empilhar direito primeiro para processar esquerdo depois (pré-ordem)
            if (atual.dir != null) {
                sb.append("DIR ");
                pilha.push(atual.dir);
            }
            if (atual.esq != null) {
                sb.append("ESQ ");
                pilha.push(atual.esq);
            }
        }
    }

    // MÉTODO CORRIGIDO: Pesquisar na SEGUNDA árvore com esq/dir
    private boolean pesquisarSegunda(No2 i, String nome, StringBuilder sb) {
        No2 atual = i;
        boolean primeiro = true;

        while (atual != null) {
            if (primeiro) {
                sb.append("raiz ");
                primeiro = false;
            }

            int cmp = nome.compareTo(atual.nome);

            if (cmp == 0) {
                sb.append("SIM");
                return true;
            } else if (cmp < 0) {
                sb.append("esq ");
                atual = atual.esq;
            } else {
                sb.append("dir ");
                atual = atual.dir;
            }
        }

        sb.append("NAO");
        return false;
    }

    private void coletarNosEmOrdem(No1 i, List<No1> lista) {
        if (i == null) return;
        coletarNosEmOrdem(i.esq, lista);
        lista.add(i);
        coletarNosEmOrdem(i.dir, lista);
    }
}

public class TP7Q2 {
    public static void main(String[] args) throws Exception {
        ArvoreDeArvore arvore = new ArvoreDeArvore();
        Scanner sc = new Scanner(System.in);

        // Leitura dos registros
        while (true) {
            String linha = sc.nextLine();
            if (linha.equals("FIM")) break;

            String[] partes = linha.split(";");
            if (partes.length != 2) continue;

            String nome = partes[0].trim();
            int estimated = Integer.parseInt(partes[1].trim());
            arvore.inserirRegistro(nome, estimated);
        }

        // Pesquisas
        while (true) {
            String nome = sc.nextLine().trim();
            if (nome.equals("FIM")) break;
            arvore.pesquisar(nome);
        }

        sc.close();
    }
}