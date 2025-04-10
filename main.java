import java.io.*;
import java.util.*;

public class main {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("disneyplus.csv"));
        String header = br.readLine(); // pula o cabeçalho

        List<Show> shows = new ArrayList<>();
        String linha;

        while ((linha = lerLinhaCompleta(br)) != null) {
            String[] campos = splitCSV(linha);
            if (campos.length < 12) continue;

            // Usa os 12 primeiros campos
            String[] camposValidos = Arrays.copyOfRange(campos, 0, 12);

            String showId = camposValidos[0];
            String type = camposValidos[1];
            String title = camposValidos[2];
            String[] director = camposValidos[3].equals("NaN") ? new String[0] : camposValidos[3].split(",\\s*");
            String[] cast = camposValidos[4].equals("NaN") ? new String[0] : camposValidos[4].split(",\\s*");
            String country = camposValidos[5];
            String dateAdded = camposValidos[6];
            int releaseYear = camposValidos[7].equals("NaN") ? 0 : Integer.parseInt(camposValidos[7]);
            String rating = camposValidos[8];
            String duration = camposValidos[9];
            String[] listedIn = camposValidos[10].equals("NaN") ? new String[0] : camposValidos[10].split(",\\s*");

            Show show = new Show(showId, type, title, director, cast, country, dateAdded, releaseYear, rating, duration, listedIn);
            shows.add(show);
        }

        br.close();
        //System.out.println("Total de shows carregados: " + shows.size());

        Scanner in = new Scanner(System.in);
        while (true) {
            //stem.out.print("Digite o ID do show para imprimir (ou FIM para sair): ");
            String idBuscado = in.nextLine();

            if (idBuscado.equals("FIM")) break;

            boolean encontrado = false;
            for (Show s : shows) {
                if (s.getShowId().equals(idBuscado)) {
                    s.imprimir();
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                System.out.println("Show com ID \"" + idBuscado + "\" não encontrado.");
            }
        }
        in.close();
    }

    // Função que garante que uma linha esteja completa (número de aspas pares)
    public static String lerLinhaCompleta(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        String linha;
        int aspas = 0;

        while ((linha = br.readLine()) != null) {
            sb.append(linha);
            aspas += contarAspas(linha);

            if (aspas % 2 == 0) break; // linha completa
            sb.append("\n"); // preserva a quebra de linha dentro de um campo
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

    // Mesmo splitCSV que você usava antes
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
