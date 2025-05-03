import java.io.*;
import java.util.*;

public class c11 {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("disneyplus.csv"));
        String header = br.readLine(); // pula cabeçalho

        List<Show> shows = new ArrayList<>();
        String linha;

        while ((linha = lerLinhaCompleta(br)) != null) {
            Show s = new Show();
            s.ler(linha);
            shows.add(s);
        }

        br.close();

        Scanner in = new Scanner(System.in);
        List<Show> selecionados = new ArrayList<>();

        while (true) {
            String id = in.nextLine();
            if (id.equals("FIM")) break;

            for (Show s : shows) {
                if (s.getShowId().equals(id)) {
                    selecionados.add(s);
                    break;
                }
            }
        }

        Show[] vetor = selecionados.toArray(new Show[0]);

        long inicio = System.currentTimeMillis();
        int comparacoes = countingSortPorAno(vetor);
        long fim = System.currentTimeMillis();
        long tempo = fim - inicio;

        for (Show s : vetor) s.imprimir();

        try (PrintWriter out = new PrintWriter("866308_countingsort.txt")) {
            out.println("866308\t" + tempo + "\t" + comparacoes);
        }
        in.close();
    }

    public static int countingSortPorAno(Show[] vetor) {
        if (vetor.length == 0) return 0;

        int[] comparacoes = new int[1]; // Correção: para usar dentro da lambda

        int min = vetor[0].getReleaseYear();
        int max = vetor[0].getReleaseYear();

        for (Show s : vetor) {
            if (s.getReleaseYear() < min) min = s.getReleaseYear();
            if (s.getReleaseYear() > max) max = s.getReleaseYear();
        }

        int range = max - min + 1;
        List<List<Show>> buckets = new ArrayList<>(range);
        for (int i = 0; i < range; i++) buckets.add(new ArrayList<>());

        for (Show s : vetor) {
            buckets.get(s.getReleaseYear() - min).add(s);
        }

        int index = 0;
        for (List<Show> bucket : buckets) {
            bucket.sort((a, b) -> {
                comparacoes[0]++;
                return a.getTitle().compareToIgnoreCase(b.getTitle());
            });

            for (Show s : bucket) {
                vetor[index++] = s;
            }
        }

        return comparacoes[0];
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

class Show {
    private String showId;
    private String type;
    private String title;
    private String[] director;
    private String[] cast;
    private String country;
    private String dateAdded;
    private int releaseYear;
    private String rating;
    private String duration;
    private String[] listedIn;

    public Show() {}

    public void ler(String linha) {
        try {
            String[] campos = c11.splitCSV(linha);
            if (campos.length < 11) throw new IllegalArgumentException("Linha inválida");

            this.showId = campos[0].trim();
            this.type = campos[1].trim();
            this.title = campos[2].trim();
            this.director = campos[3].equals("NaN") ? new String[0] : campos[3].split(",\\s*");
            this.cast = campos[4].equals("NaN") ? new String[0] : campos[4].split(",\\s*");
            this.country = campos[5].equals("NaN") ? "NaN" : campos[5].trim();
            this.dateAdded = campos[6].equals("NaN") ? "March 1, 1900" : campos[6].trim();
            this.releaseYear = campos[7].equals("NaN") ? 0 : Integer.parseInt(campos[7]);
            this.rating = campos[8].trim();
            this.duration = campos[9].trim();
            this.listedIn = campos[10].equals("NaN") ? new String[0] : campos[10].split(",\\s*");
        } catch (Exception e) {
            System.out.println("Erro ao ler linha: " + e.getMessage());
        }
    }

    public void imprimir() {
        System.out.print("=> " + showId + " ## " + title + " ## " + type + " ## ");
        System.out.print((director.length > 0 ? String.join(", ", director) : "NaN") + " ## ");
        System.out.print((cast.length > 0 ? Arrays.toString(cast) : "NaN") + " ## ");
        System.out.print(country + " ## ");
        System.out.print(dateAdded + " ## ");
        System.out.print(releaseYear + " ## ");
        System.out.print(rating + " ## ");
        System.out.print(duration + " ## ");
        System.out.print((listedIn.length > 0 ? Arrays.toString(listedIn) : "NaN") + " ##\n");
    }

    public String getShowId() {
        return showId;
    }

    public String getTitle() {
        return title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }
}
