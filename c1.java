import java.io.File;
import java.util.Scanner;

public class c1 {
    public static void main(String[] args) throws Exception {
        final int NUM_CAMPOS = 12;
        String[] campos = new String[NUM_CAMPOS];
        Scanner sc = new Scanner(new File("./disneyplus.csv"));

        // Pula o cabeçalho
        sc.nextLine();

        while (sc.hasNextLine()) {
            String linha = sc.nextLine();
            int campoIndex = 0;
            StringBuilder campoAtual = new StringBuilder();
            boolean dentroDeAspas = false;

            for (int i = 0; i < linha.length(); i++) {
                char c = linha.charAt(i);

                if (c == '"') {
                    dentroDeAspas = !dentroDeAspas; // alterna estado
                } else if (c == ',' && !dentroDeAspas) {
                    // fim de campo
                    campos[campoIndex++] = campoAtual.toString().isEmpty() ? "NaN" : campoAtual.toString();
                    campoAtual.setLength(0); // limpa StringBuilder
                } else {
                    campoAtual.append(c);
                }
            }

            // adiciona o último campo
            if (campoIndex < NUM_CAMPOS) {
                campos[campoIndex] = campoAtual.toString().isEmpty() ? "NaN" : campoAtual.toString();
            }

            // Exibe os dados extraídos
            for (int i = 0; i < NUM_CAMPOS; i++) {
                System.out.printf("Campo %02d: %s%n", i + 1, campos[i]);
            }

            System.out.println("=".repeat(60)); // separador entre linhas
        }

        sc.close();
    }
}
