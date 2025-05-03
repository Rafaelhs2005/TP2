import java.io.*;
import java.util.*;

public class c9 {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("/tmp/disneyplus.csv"));
        String header = br.readLine(); // pula o cabeçalho

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
            String idBuscado = in.nextLine();
            if (idBuscado.equals("FIM")) break;

            for (Show s : shows) {
                if (s.getShowId().equals(idBuscado)) {
                    selecionados.add(s);
                    break;
                }
            }
        }

        Show[] vetor = selecionados.toArray(new Show[0]);

        long inicio = System.currentTimeMillis();
        int comparacoes = heapSortPorDirector(vetor);
        long fim = System.currentTimeMillis();
        long tempoExecucao = fim - inicio;

        for (Show s : vetor) {
            s.imprimir();
        }

        try (PrintWriter writer = new PrintWriter("866308_heapsort.txt")) {
            writer.println("866308\t" + tempoExecucao + "\t" + comparacoes);
        }

        in.close();
    }

    public static int heapSortPorDirector(Show[] arr) {
        int n = arr.length;
        int comparacoes = 0;

        for (int i = n / 2 - 1; i >= 0; i--) {
            comparacoes += heapify(arr, n, i);
        }

        for (int i = n - 1; i > 0; i--) {
            Show temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;
            comparacoes += heapify(arr, i, 0);
        }

        return comparacoes;
    }

    public static int heapify(Show[] arr, int n, int i) {
        int comparacoes = 0;
        int maior = i;
        int esq = 2 * i + 1;
        int dir = 2 * i + 2;

        if (esq < n && comparar(arr[esq], arr[maior]) > 0) {
            maior = esq;
        }
        comparacoes++;

        if (dir < n && comparar(arr[dir], arr[maior]) > 0) {
            maior = dir;
        }
        comparacoes++;

        if (maior != i) {
            Show swap = arr[i];
            arr[i] = arr[maior];
            arr[maior] = swap;
            comparacoes += heapify(arr, n, maior);
        }

        return comparacoes;
    }

    public static int comparar(Show a, Show b) {
        String d1 = a.getDirector();
        String d2 = b.getDirector();
        int cmp = d1.compareToIgnoreCase(d2);
        if (cmp != 0) return cmp;
        return a.getTitle().compareToIgnoreCase(b.getTitle());
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

    public String getShowId() {
        return showId;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    private String[] director;
    private String[] cast;
    private String country;
    private String dateAdded;
    private int releaseYear;
    private String rating;
    private String duration;
    private String[] listedIn;

    public Show() {}

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

    public void ler(String linha) {
        try {
            String[] campos = c9.splitCSV(linha);
            if (campos.length < 11) throw new IllegalArgumentException("Linha com campos insuficientes");

            this.showId = campos[0].isEmpty() ? "NaN" : campos[0].trim();
            this.type = campos[1].isEmpty() ? "NaN" : campos[1].trim();
            this.title = campos[2].isEmpty() ? "NaN" : campos[2].trim();

            this.director = campos[3].equals("NaN") ? new String[0] : campos[3].split(",\\s*");
            Arrays.sort(this.director);

            this.cast = campos[4].equals("NaN") ? new String[0] : campos[4].split(",\\s*");
            Arrays.sort(this.cast);

            this.country = campos[5].equals("NaN") || campos[5].isEmpty() ? "NaN" : campos[5].trim();
            this.dateAdded = campos[6].equals("NaN") || campos[6].isEmpty() ? "March 1, 1900" : campos[6].trim();
            this.releaseYear = campos[7].equals("NaN") || campos[7].isEmpty() ? 0 : Integer.parseInt(campos[7]);
            this.rating = campos[8].equals("NaN") || campos[8].isEmpty() ? "NaN" : campos[8].trim();
            this.duration = campos[9].equals("NaN") || campos[9].isEmpty() ? "NaN" : campos[9].trim();
            this.listedIn = campos[10].equals("NaN") ? new String[0] : campos[10].split(",\\s*");
            Arrays.sort(this.listedIn);
        } catch (Exception e) {
            System.out.println("Erro ao ler linha: " + e.getMessage());
        }
    }

    public String getDirector() {
        return director.length > 0 ? director[0] : "NaN";
    }
}
