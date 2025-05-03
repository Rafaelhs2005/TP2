import java.io.*;
import java.util.*;

public class c18 {
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
        quicksort(vetor, 0, vetor.length - 1);

        for (int i = 0; i < Math.min(10, vetor.length); i++) {
            vetor[i].imprimir();
        }

        in.close();
    }

    public static void quicksort(Show[] array, int esq, int dir) {
        if (esq < dir) {
            int i = esq, j = dir;
            Show pivo = array[(esq + dir) / 2];
            while (i <= j) {
                while (comparar(array[i], pivo) < 0) i++;
                while (comparar(array[j], pivo) > 0) j--;
                if (i <= j) {
                    Show temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                    i++;
                    j--;
                }
            }
            quicksort(array, esq, j);
            quicksort(array, i, dir);
        }
    }

    public static int comparar(Show a, Show b) {
        int cmp = parseDate(a.getDateAdded()).compareTo(parseDate(b.getDateAdded()));
        if (cmp != 0) return cmp;
        return a.getTitle().compareToIgnoreCase(b.getTitle());
    }

    public static Date parseDate(String dateStr) {
        try {
            return new java.text.SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(dateStr);
        } catch (Exception e) {
            return new Date(0); // fallback para data antiga
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
            String[] campos = c18.splitCSV(linha);
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

    public String getDateAdded() {
        return dateAdded;
    }
}
