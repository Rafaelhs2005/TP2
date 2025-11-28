import java.io.*;
import java.util.*;
import java.text.*;

public class TP7Q4 {

    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader("/tmp/games.csv"));
        String header = br.readLine();

        ArrayList<Games> banco = new ArrayList<>();
        String linha;

        while ((linha = lerLinhaCompleta(br)) != null) {
            Games g = new Games();
            g.ler(linha);
            banco.add(g);
        }
        br.close();

        Arvore arvore = new Arvore();

        Scanner in = new Scanner(System.in);

        while (true) {
            String id = in.nextLine();
            if (id.equals("FIM")) break;

            Games g = buscarPorID(banco, id);
            if (g != null) arvore.inserir(g);
        }

        while (true) {
            String nome = in.nextLine();
            if (nome.equals("FIM")) break;

            boolean achou = arvore.pesquisar(nome);
        }

        in.close();
    }

    public static Games buscarPorID(ArrayList<Games> banco, String id) {
        for (Games g : banco) {
            if (g.getAppId().equals(id)) return g;
        }
        return null;
    }

    public static String lerLinhaCompleta(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        String linha;
        int aspas = 0;

        while ((linha = br.readLine()) != null) {
            sb.append(linha);
            aspas += contarAspas(linha);
            if (aspas % 2 == 0) break;
            sb.append("\n");
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    public static int contarAspas(String linha) {
        int count = 0;
        for (char c : linha.toCharArray()) if (c == '"') count++;
        return count;
    }

    public static String[] splitCSV(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean aspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
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


class No {
    Games elemento;
    No esq, dir, pai;
    boolean cor; 

    public No(Games elemento) {
        this.elemento = elemento;
        this.esq = this.dir = this.pai = null;
        this.cor = true; 
    }
}

class Arvore {

    private No raiz;

    public Arvore() {
        raiz = null;
    }

    public void inserir(Games g) {
        No novo = new No(g);
        raiz = inserirRec(raiz, novo);
        corrigirInsercao(novo);
    }

    private No inserirRec(No raiz, No novo) {
        if (raiz == null) return novo;

        int cmp = novo.elemento.getName().compareTo(raiz.elemento.getName());

        if (cmp < 0) {
            raiz.esq = inserirRec(raiz.esq, novo);
            raiz.esq.pai = raiz;
        } else if (cmp > 0) {
            raiz.dir = inserirRec(raiz.dir, novo);
            raiz.dir.pai = raiz;
        }

        return raiz;
    }


    private void rotacaoEsq(No x) {
        No y = x.dir;

        x.dir = y.esq;
        if (y.esq != null) y.esq.pai = x;

        y.pai = x.pai;

        if (x.pai == null)
            raiz = y;
        else if (x == x.pai.esq)
            x.pai.esq = y;
        else
            x.pai.dir = y;

        y.esq = x;
        x.pai = y;
    }

    private void rotacaoDir(No x) {
        No y = x.esq;

        x.esq = y.dir;
        if (y.dir != null) y.dir.pai = x;

        y.pai = x.pai;

        if (x.pai == null)
            raiz = y;
        else if (x == x.pai.dir)
            x.pai.dir = y;
        else
            x.pai.esq = y;

        y.dir = x;
        x.pai = y;
    }

    private void corrigirInsercao(No z) {

        while (z != raiz && z.pai.cor == true) {

            if (z.pai == z.pai.pai.esq) {

                No tio = z.pai.pai.dir;

                if (tio != null && tio.cor == true) {
                    z.pai.cor = false;
                    tio.cor = false;
                    z.pai.pai.cor = true;
                    z = z.pai.pai;

                } else {

                    if (z == z.pai.dir) {
                        z = z.pai;
                        rotacaoEsq(z);
                    }

                    z.pai.cor = false;
                    z.pai.pai.cor = true;
                    rotacaoDir(z.pai.pai);
                }

            } else { 

                No tio = z.pai.pai.esq;

                if (tio != null && tio.cor == true) {
                    z.pai.cor = false;
                    tio.cor = false;
                    z.pai.pai.cor = true;
                    z = z.pai.pai;

                } else {

                    if (z == z.pai.esq) {
                        z = z.pai;
                        rotacaoDir(z);
                    }

                    z.pai.cor = false;
                    z.pai.pai.cor = true;
                    rotacaoEsq(z.pai.pai);
                }
            }
        }

        raiz.cor = false; 
    }

    public boolean pesquisar(String nome) {
        System.out.print(nome + ": => ");
        return pesquisar(nome, raiz);
    }

    private boolean pesquisar(String nome, No i) {
        if (i == null) {
            System.out.println("NAO");
            return false;
        }

        if (i == raiz) System.out.print("raiz ");
        else if (nome.compareTo(i.elemento.getName()) < 0) System.out.print("esq ");
        else System.out.print("dir ");

        if (nome.equals(i.elemento.getName())) {
            System.out.println("SIM");
            return true;
        }

        if (nome.compareTo(i.elemento.getName()) < 0)
            return pesquisar(nome, i.esq);
        else
            return pesquisar(nome, i.dir);
    }
}


class Games {
    private String appId;
    private String name;
    private String releaseDate;
    private String estimateOwners;
    private float price;
    private String[] supportedLanguages;
    private int metacriticScore;
    private float userScore;
    private int achievements;
    private String publisher;
    private String developers;
    private String[] categories;
    private String[] genres;
    private String[] tags;

    public String getAppId() { return appId; }
    public String getName() { return name; }

    public Games() {}

    private String getField(String[] campos, int idx) {
        if (campos == null) return "NaN";
        if (idx < campos.length && campos[idx] != null && !campos[idx].isEmpty())
            return campos[idx].trim();
        return "NaN";
    }

    private String[] parseListField(String s) {
        if (s == null || s.equals("NaN")) return new String[0];
        String t = s.trim();
        if (t.startsWith("[") && t.endsWith("]")) {
            t = t.substring(1, t.length() - 1);
        }
        t = t.replace("'", "");
        if (t.isEmpty()) return new String[0];
        String[] parts = t.split(",\\s*");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String q = p.trim();
            if (!q.isEmpty()) out.add(q);
        }
        return out.toArray(new String[0]);
    }

    private String formatarData(String original) {
        if (original == null || original.equals("NaN") || original.isEmpty()) return "NaN";
        try {
            SimpleDateFormat entrada = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            SimpleDateFormat saida = new SimpleDateFormat("dd/MM/yyyy");
            Date data = entrada.parse(original);
            return saida.format(data);
        } catch (Exception e) {
            return original;
        }
    }

    public void ler(String linha) {
        try {
            String[] campos = TP7Q4.splitCSV(linha);

            this.appId = getField(campos, 0);
            this.name = getField(campos, 1);
            this.releaseDate = formatarData(getField(campos, 2));
            this.estimateOwners = getField(campos, 3);

            String priceStr = getField(campos, 4);
            try { this.price = priceStr.equals("NaN") ? 0f : Float.parseFloat(priceStr); }
            catch (Exception e) { this.price = 0f; }

            this.supportedLanguages = parseListField(getField(campos, 5));

            try { this.metacriticScore = Integer.parseInt(getField(campos, 6)); }
            catch (Exception e) { this.metacriticScore = 0; }

            try { this.userScore = Float.parseFloat(getField(campos, 7)); }
            catch (Exception e) { this.userScore = 0; }

            try { this.achievements = Integer.parseInt(getField(campos, 8)); }
            catch (Exception e) { this.achievements = 0; }

            this.publisher = getField(campos, 9);
            this.developers = getField(campos, 10);
            this.categories = parseListField(getField(campos, 11));
            this.genres = parseListField(getField(campos, 12));
            this.tags = parseListField(getField(campos, 13));

        } catch (Exception e) {
            System.out.println("Erro ao ler linha: " + e.getMessage());
        }
    }
}
