import java.io.*;
import java.util.*;

public class TP7Q2 {
    public static void main(String[] args) throws Exception {
        long inicio = System.nanoTime();
        int comparacoes = 0;

        BufferedReader br = new BufferedReader(new FileReader("/tmp/games.csv"));
        br.readLine(); 

        List<Show> shows = new ArrayList<>();
        String linha;
        while ((linha = lerLinhaCompleta(br)) != null) {
            Show s = new Show();
            s.ler(linha);
            shows.add(s);
        }
        br.close();

        ArvoreArvore arvore = new ArvoreArvore();
        arvore.criarArvoreExterna();

        Scanner in = new Scanner(System.in);

        while (true) {
            String id = in.nextLine();
            if (id.equals("FIM")) break;

            boolean encontrado = false;
            for (Show s : shows) {
                comparacoes++;
                if (s.getShowId().equals(id)) {
                    arvore.inserir(s);
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado)
                System.out.println("ID não encontrado: " + id);
        }

        while (true) {
            String titulo = in.nextLine();
            if (titulo.equals("FIM")) break;

            arvore.pesquisar(titulo);
            comparacoes += arvore.getUltimaComparacao();
        }

        in.close();
        long fim = System.nanoTime();
        double tempo = (fim - inicio) / 1e6;

        BufferedWriter log = new BufferedWriter(new FileWriter("866308_arvoreArvore.txt"));
        log.write("866308\t" + String.format("%.3f", tempo) + "\t" + comparacoes);
        log.close();
    }

    public static String lerLinhaCompleta(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        String linha;
        int aspas = 0;

        while ((linha = br.readLine()) != null) {
            sb.append(linha);
            for (char c : linha.toCharArray())
                if (c == '"') aspas++;
            if (aspas % 2 == 0) break;
            sb.append("\n");
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    public static String[] splitCSV(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean aspas = false;

        for (char c : linha.toCharArray()) {
            if (c == '"') {
                aspas = !aspas;
            } else if (c == ',' && !aspas) {
                campos.add(sb.length() == 0 ? "NaN" : sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        campos.add(sb.length() == 0 ? "NaN" : sb.toString());
        return campos.toArray(new String[0]);
    }
}

class Show {
    private String showId, title, type, dateAdded;
    private int releaseYear;
    private String[] director, cast, listedIn;

    public String getShowId() { return showId; }
    public String getTitle() { return title; }
    public int getReleaseYear() { return releaseYear; }

    public void ler(String linha) {
        String[] c = TP7Q2.splitCSV(linha);
        showId = c[0];
        type = c[1];
        title = c[2];
        director = c[3].equals("NaN") ? new String[0] : c[3].split(", ");
        cast = c[4].equals("NaN") ? new String[0] : c[4].split(", ");
        dateAdded = c[6];
        releaseYear = parseIntSafe(c[7]);
        listedIn = c[10].equals("NaN") ? new String[0] : c[10].split(", ");
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return 0; }
    }
}

class NoInterno {
    String titulo;
    NoInterno esq, dir;

    NoInterno(String titulo) {
        this.titulo = titulo;
        this.esq = this.dir = null;
    }
}

class NoExterno {
    int chave;
    NoExterno esq, dir;
    NoInterno raizInterna;

    NoExterno(int chave) {
        this.chave = chave;
        this.raizInterna = null;
    }
}

class ArvoreArvore {
    private NoExterno raiz;
    private int ultimaComparacao = 0;
    private int[] ordem = {7, 3, 11, 1, 5, 9, 13, 0, 2, 4, 6, 8, 10, 12, 14};

    public void criarArvoreExterna() {
        for (int chave : ordem) {
            raiz = inserirExterno(raiz, chave);
        }
    }

    private NoExterno inserirExterno(NoExterno no, int chave) {
        if (no == null) return new NoExterno(chave);
        if (chave < no.chave) {
            no.esq = inserirExterno(no.esq, chave);
        } else if (chave > no.chave) {
            no.dir = inserirExterno(no.dir, chave);
        }
        return no;
    }

    public void inserir(Show s) {
        int mod = s.getReleaseYear() % 15;
        inserirInterno(raiz, mod, s.getTitle());
    }

    private void inserirInterno(NoExterno no, int chave, String titulo) {
        if (no == null) return;
        
        if (chave == no.chave) {
            no.raizInterna = inserirNaInterna(no.raizInterna, titulo);
        } else if (chave < no.chave) {
            inserirInterno(no.esq, chave, titulo);
        } else {
            inserirInterno(no.dir, chave, titulo);
        }
    }

    private NoInterno inserirNaInterna(NoInterno raiz, String titulo) {
        if (raiz == null) return new NoInterno(titulo);
        
        int cmp = titulo.compareTo(raiz.titulo);
        if (cmp < 0) {
            raiz.esq = inserirNaInterna(raiz.esq, titulo);
        } else if (cmp > 0) {
            raiz.dir = inserirNaInterna(raiz.dir, titulo);
        }
        return raiz;
    }

    public boolean pesquisar(String titulo) {
        ultimaComparacao = 0;
        
        StringBuilder saidaCompleta = new StringBuilder();
        saidaCompleta.append("=> ").append(titulo).append(" => ");
        
        mostrarCaminhoPrimeiraArvore(raiz, saidaCompleta);
        
        boolean encontrado = false;
        List<NoExterno> nosExternos = new ArrayList<>();
        coletarNosEmOrdem(raiz, nosExternos);

        for (NoExterno noExterno : nosExternos) {
            if (noExterno.raizInterna != null) {
                boolean achou = pesquisarArvoreInterna(noExterno.raizInterna, titulo, saidaCompleta);
                if (achou) encontrado = true;
            }
        }

        saidaCompleta.append(encontrado ? " SIM" : " NAO");
        
        System.out.println(saidaCompleta.toString());
        return encontrado;
    }

    private void mostrarCaminhoPrimeiraArvore(NoExterno no, StringBuilder sb) {
        if (no == null) return;
        
        // Mostrar raiz
        if (no == raiz) {
            sb.append("raiz ");
        }
        
        if (no.esq != null) {
            sb.append("ESQ ");
            mostrarCaminhoPrimeiraArvore(no.esq, sb);
        }
        if (no.dir != null) {
            sb.append("DIR ");
            mostrarCaminhoPrimeiraArvore(no.dir, sb);
        }
    }

    private boolean pesquisarArvoreInterna(NoInterno no, String titulo, StringBuilder sb) {
        if (no == null) {
            sb.append("NAO ");
            return false;
        }

        sb.append("raiz ");

        NoInterno atual = no;
        while (atual != null) {
            ultimaComparacao++;
            int cmp = titulo.compareTo(atual.titulo);
            
            if (cmp == 0) {
                sb.append("SIM ");
                return true;
            } else if (cmp < 0) {
                sb.append("esq ");
                atual = atual.esq;
            } else {
                sb.append("dir ");
                atual = atual.dir;
            }
        }
        
        sb.append("NAO ");
        return false;
    }

    private void coletarNosEmOrdem(NoExterno no, List<NoExterno> lista) {
        if (no == null) return;
        coletarNosEmOrdem(no.esq, lista);
        lista.add(no);
        coletarNosEmOrdem(no.dir, lista);
    }

    public int getUltimaComparacao() {
        return ultimaComparacao;
    }
}
