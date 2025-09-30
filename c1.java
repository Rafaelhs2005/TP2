import java.io.*;
import java.util.*;

public class c1 {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("games.csv"));
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
    private int userScore;
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

    public void ler(String linha) {
        try {
            String[] campos = c1.splitCSV(linha);

            this.appId = getField(campos, 0);
            this.name = getField(campos, 1);
            this.releaseDate = getField(campos, 2);
            this.estimateOwners = getField(campos, 3);

            String priceStr = getField(campos, 4);
            try { this.price = (priceStr.equals("NaN") ? 0f : Float.parseFloat(priceStr)); }
            catch (Exception e) { this.price = 0f; }

            this.supportedLanguages = parseListField(getField(campos, 5));

            String mStr = getField(campos, 6);
            try { this.metacriticScore = mStr.equals("NaN") ? 0 : Integer.parseInt(mStr); }
            catch (Exception e) { this.metacriticScore = 0; }

            String uStr = getField(campos, 7);
            try { this.userScore = uStr.equals("NaN") ? 0 : Integer.parseInt(uStr); }
            catch (Exception e) { this.userScore = 0; }

            String aStr = getField(campos, 8);
            try { this.achievements = aStr.equals("NaN") ? 0 : Integer.parseInt(aStr); }
            catch (Exception e) { this.achievements = 0; }


            this.publisher = getField(campos, 9);
            this.developers = getField(campos, 10);

            String cat = getField(campos, 11);
            this.categories = cat.equals("NaN") ? new String[0] : cat.split(",\\s*");

            String gen = getField(campos, 12);
            this.genres = gen.equals("NaN") ? new String[0] : gen.split(",\\s*");

            String tg = getField(campos, 13);
            this.tags = tg.equals("NaN") ? new String[0] : tg.split(",\\s*");

        } catch (Exception e) {
            System.out.println("Erro ao ler linha: " + e.getMessage());
        }
    }

    public void imprimir() {
        System.out.print("===> " + appId + " ## " + name + " ## " + releaseDate + " ## ");
        System.out.print(estimateOwners + " ## " + price + " ## ");
        System.out.print((supportedLanguages.length > 0 ? Arrays.toString(supportedLanguages) : "NaN") + " ## ");
        System.out.print(metacriticScore + " ## " + userScore + " ## " + achievements + " ## ");
        System.out.print((publisher == null ? "NaN" : publisher) + " ## ");
        System.out.print((developers == null ? "NaN" : developers) + " ## ");
        System.out.print((categories.length > 0 ? Arrays.toString(categories) : "NaN") + " ## ");
        System.out.print((genres.length > 0 ? Arrays.toString(genres) : "NaN") + " ## ");
        System.out.print((tags.length > 0 ? Arrays.toString(tags) : "NaN") + " ##\n");
    }
}
