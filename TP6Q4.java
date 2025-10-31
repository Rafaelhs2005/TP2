import java.io.*;
import java.util.*;
import java.text.*;

public class TP6Q4 {

    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader("/tmp/games.csv"));
        String header = br.readLine();

        Fila fila = new Fila();
        String linha;

        ArrayList<Games> banco = new ArrayList<>();
        while ((linha = lerLinhaCompleta(br)) != null) {
            Games g = new Games();
            g.ler(linha);
            banco.add(g);
        }
        br.close();

        Scanner in = new Scanner(System.in);

        while (true) {
            String id = in.nextLine();
            if (id.equals("FIM")) break;

            Games g = buscarPorID(banco, id);
            if (g != null) fila.inserir(g);
        }

        int n = Integer.parseInt(in.nextLine());
        for (int i = 0; i < n; i++) {

            String entrada = in.nextLine();
            String[] parts = entrada.split(" ");

            switch (parts[0]) {

                case "I": { // Inserir fim
                    Games g = buscarPorID(banco, parts[1]);
                    if (g != null) fila.inserir(g);
                    break;
                }

                case "R": { // Remover início
                    Games g = fila.remover();
                    if (g != null)
                        System.out.println("(R) " + g.getName());
                    break;
                }

                default:
                    break;
            }
        }

        fila.mostrar();
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

class Celula {
    public Games elemento;
    public Celula prox;
    public Celula(Games elemento) {
        this.elemento = elemento;
        this.prox = null;
    }
}

class Fila {
    Celula primeiro, ultimo;
    int tamanho;

    public Fila() {
        primeiro = new Celula(null);
        ultimo = primeiro;
        tamanho = 0;
    }

    void inserir(Games x) {
        ultimo.prox = new Celula(x);
        ultimo = ultimo.prox;
        tamanho++;
    }

    Games remover() {
        if (primeiro == ultimo) return null;
        Celula tmp = primeiro.prox;
        primeiro.prox = tmp.prox;
        if (tmp == ultimo) ultimo = primeiro;
        tamanho--;
        return tmp.elemento;
    }

    void mostrar() {
        int cont = 0;
        for (Celula i = primeiro.prox; i != null; i = i.prox, cont++) {
            System.out.print("[" + cont + "] ");
            i.elemento.imprimir();
        }
    }
}
