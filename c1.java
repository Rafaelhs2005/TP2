import java.io.*;
import java.util.*;
import java.text.*;

public class c1 {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("/tmp/games.csv"));
        String header = br.readLine(); // pula o cabeçalho

        List<Games> games = new ArrayList<>();
        String linha;

        while ((linha = lerLinhaCompleta(br)) != null) {
            Games g = new Games();
            g.ler(linha);
            games.add(g);
        }

        br.close();

        Scanner in = new Scanner(System.in);
        while (true) {
            String idBuscado = in.nextLine();

            if (idBuscado.equals("FIM")) break;

            boolean encontrado = false;
            for (Games g : games) {
                if (g.getAppId().equals(idBuscado)) {
                    g.imprimir();
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                System.out.println("Game com ID \"" + idBuscado + "\" não encontrado.");
            }
        }
        in.close();
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
        t = t.replace("'", ""); // remove aspas simples internas
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
            return original; // se não conseguir converter, retorna o valor original
        }
    }

    public void ler(String linha) {
        try {
            String[] campos = c1.splitCSV(linha);

            this.appId = getField(campos, 0);
            this.name = getField(campos, 1);

            String rawDate = getField(campos, 2);
            this.releaseDate = formatarData(rawDate);

            this.estimateOwners = getField(campos, 3);

            String priceStr = getField(campos, 4);
            try { this.price = (priceStr.equals("NaN") ? 0f : Float.parseFloat(priceStr)); }
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

    public void imprimir() {
        System.out.print("=> " + appId + " ## " + name + " ## " + releaseDate + " ## ");
        System.out.print(estimateOwners + " ## " + price + " ## ");
        System.out.print(Arrays.toString(supportedLanguages) + " ## ");
        System.out.print(metacriticScore + " ## " + userScore + " ## " + achievements + " ## ");
        System.out.print("[" + publisher + "] ## ");
        System.out.print("[" + developers + "] ## ");
        System.out.print(Arrays.toString(categories) + " ## ");
        System.out.print(Arrays.toString(genres) + " ## ");
        System.out.print(Arrays.toString(tags) + " ##\n");
    }
}
