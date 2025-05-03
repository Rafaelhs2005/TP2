import java.io.*;
import java.util.*;

public class c13 {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("/tmp/disneyplus.csv"));
        String header = br.readLine();

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
        int comparacoes = mergeSort(vetor, 0, vetor.length - 1);
        long fim = System.currentTimeMillis();

        for (Show s : vetor) s.imprimir();

        try (PrintWriter out = new PrintWriter("866308_mergesort.txt")) {
            out.println("866308\t" + (fim - inicio) + "\t" + comparacoes);
        }

        in.close();
    }

    public static int mergeSort(Show[] arr, int esq, int dir) {
        int comparacoes = 0;
        if (esq < dir) {
            int meio = (esq + dir) / 2;
            comparacoes += mergeSort(arr, esq, meio);
            comparacoes += mergeSort(arr, meio + 1, dir);
            comparacoes += merge(arr, esq, meio, dir);
        }
        return comparacoes;
    }

    public static int merge(Show[] arr, int esq, int meio, int dir) {
        int comparacoes = 0;
        int n1 = meio - esq + 1;
        int n2 = dir - meio;

        Show[] L = new Show[n1];
        Show[] R = new Show[n2];

        for (int i = 0; i < n1; i++) L[i] = arr[esq + i];
        for (int j = 0; j < n2; j++) R[j] = arr[meio + 1 + j];

        int i = 0, j = 0, k = esq;
        while (i < n1 && j < n2) {
            comparacoes++;
            int durL = parseDuration(L[i].getDuration());
            int durR = parseDuration(R[j].getDuration());

            if (durL < durR || (durL == durR && L[i].getTitle().compareToIgnoreCase(R[j].getTitle()) <= 0)) {
                arr[k++] = L[i++];
            } else {
                arr[k++] = R[j++];
            }
        }

        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];

        return comparacoes;
    }

    public static int parseDuration(String dur) {
        try {
            return Integer.parseInt(dur.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
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
            String[] campos = c13.splitCSV(linha);
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

    public String getDuration() {
        return duration;
    }
}
