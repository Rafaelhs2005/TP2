import java.io.*;
import java.util.*;
import java.text.*;

public class c3 {

    static int comparacoes = 0; // contador global de comparações

    public static void main(String[] args) throws Exception {
        long inicio = System.currentTimeMillis(); // início da contagem do tempo

        BufferedReader br = new BufferedReader(new FileReader("games.csv"));
        String header = br.readLine(); // pula o cabeçalho

        // Lê todos os games do CSV
        List<Games> todos = new ArrayList<>();
        String linha;
        while ((linha = lerLinhaCompleta(br)) != null) {
            Games g = new Games();
            g.ler(linha);
            todos.add(g);
        }
        br.close();

        // --- Parte 1: ler IDs até "FIM" e guardar em um array ---
        Scanner in = new Scanner(System.in);
        List<Games> selecionados = new ArrayList<>();

        while (true) {
            String id = in.nextLine();
            if (id.equals("FIM")) break;

            for (Games g : todos) {
                if (g.getAppId().equals(id)) {
                    selecionados.add(g);
                    break;
                }
            }
        }

        // Converter lista em array
        Games[] array = selecionados.toArray(new Games[0]);

        // --- Parte 2: ordenar por nome (e appId em caso de empate) ---
        Arrays.sort(array, new Comparator<Games>() {
            @Override
            public int compare(Games a, Games b) {
                int cmp = a.getName().compareToIgnoreCase(b.getName());
                if (cmp == 0) return a.getAppId().compareTo(b.getAppId());
                return cmp;
            }
        });

        // --- Parte 3: ler nomes até "FIM" e pesquisar com busca binária ---
        while (true) {
            String nome = in.nextLine();
            if (nome.equals("FIM")) break;

            boolean encontrado = buscaBinaria(array, nome);
            System.out.println(encontrado ? "SIM" : "NAO");
        }

        in.close();

        long fim = System.currentTimeMillis();
        double tempoExec = (fim - inicio) / 1000.0;

        // --- Parte 4: gerar log ---
        String matricula = "866308"; // 🔁 altere para a sua matrícula
        try (PrintWriter log = new PrintWriter(new FileWriter(matricula + "_binaria.txt"))) {
            log.printf("%s\t%.3f\t%d%n", matricula, tempoExec, comparacoes);
        }
    }

    // --- Função de busca binária com contagem de comparações ---
    public static boolean buscaBinaria(Games[] array, String chave) {
        int esq = 0, dir = array.length - 1;
        while (esq <= dir) {
            int meio = (esq + dir) / 2;
            comparacoes++;
            int cmp = array[meio].getName().compareToIgnoreCase(chave);

            if (cmp == 0) return true;
            else if (cmp < 0) esq = meio + 1;
            else dir = meio - 1;
        }
        return false;
    }

    // --- Funções auxiliares para leitura CSV ---
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
        for (char c : linha.toCharArray()) {
            if (c == '"') count++;
        }
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
            String[] campos = c3.splitCSV(linha);

            this.appId = getField(campos, 0);
            this.name = getField(campos, 1);
            this.releaseDate = formatarData(getField(campos, 2));
            this.estimateOwners = getField(campos, 3);

            String priceStr = getField(campos, 4);
            try { this.price = priceStr.equals("NaN") ? 0f : Float.parseFloat(priceStr); }
            catch (Exception e) { this.price = 0f; }

            this.supportedLanguages = parseListField(getField(campos, 5));

            String mStr = getField(campos, 6);
            try { this.metacriticScore = mStr.equals("NaN") ? 0 : Integer.parseInt(mStr); }
            catch (Exception e) { this.metacriticScore = 0; }

            String uStr = getField(campos, 7);
            try { this.userScore = uStr.equals("NaN") ? 0 : Float.parseFloat(uStr); }
            catch (Exception e) { this.userScore = 0; }

            String aStr = getField(campos, 8);
            try { this.achievements = aStr.equals("NaN") ? 0 : Integer.parseInt(aStr); }
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
