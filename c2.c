#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#define MAX_SHOWS 10000
#define MAX_LINE 1000
#define MAX_FIELD 200

typedef struct {
    char showId[MAX_FIELD];
    char type[MAX_FIELD];
    char title[MAX_FIELD];
    char director[10][MAX_FIELD];
    int directorCount;
    char cast[20][MAX_FIELD];
    int castCount;
    char country[MAX_FIELD];
    char dateAdded[MAX_FIELD];
    int releaseYear;
    char rating[MAX_FIELD];
    char duration[MAX_FIELD];
    char listedIn[10][MAX_FIELD];
    int listedCount;
} Show;

int contarAspas(const char* linha) {
    int count = 0;
    for (int i = 0; linha[i]; i++)
        if (linha[i] == '"') count++;
    return count;
}

char* lerLinhaCompleta(FILE* fp, char* buffer) {
    char temp[MAX_LINE];
    buffer[0] = '\0';
    int aspas = 0;

    while (fgets(temp, MAX_LINE, fp)) {
        strcat(buffer, temp);
        aspas += contarAspas(temp);
        if (aspas % 2 == 0) break;
    }

    return strlen(buffer) == 0 ? NULL : buffer;
}

int splitCSV(const char* linha, char campos[][MAX_FIELD]) {
    int campoIndex = 0, i = 0, j = 0;
    bool aspas = false;
    char buffer[MAX_FIELD] = "";

    while (linha[i]) {
        if (linha[i] == '"') {
            aspas = !aspas;
        } else if (linha[i] == ',' && !aspas) {
            buffer[j] = '\0';
            strcpy(campos[campoIndex++], j == 0 ? "NaN" : buffer);
            j = 0;
        } else {
            buffer[j++] = linha[i];
        }
        i++;
    }

    buffer[j] = '\0';
    strcpy(campos[campoIndex++], j == 0 ? "NaN" : buffer);

    return campoIndex;
}

void trim(char* str) {
    int len = strlen(str);
    while (len > 0 && (str[len-1] == '\n' || str[len-1] == '\r'))
        str[--len] = '\0';
}

void parseArray(char* campo, char arr[][MAX_FIELD], int* count) {
    *count = 0;
    if (strcmp(campo, "NaN") == 0) return;

    char* token = strtok(campo, ",");
    while (token != NULL && *count < 20) {
        while (*token == ' ') token++;
        strcpy(arr[(*count)++], token);
        token = strtok(NULL, ",");
    }
}

void lerShow(Show* s, const char* linha) {
    char campos[11][MAX_FIELD];
    char linhaCopia[MAX_LINE];
    strcpy(linhaCopia, linha);

    int qtd = splitCSV(linhaCopia, campos);
    if (qtd < 11) {
        printf("Erro: Linha com campos insuficientes\n");
        return;
    }

    strcpy(s->showId, campos[0]);
    strcpy(s->type, campos[1]);
    strcpy(s->title, campos[2]);

    parseArray(campos[3], s->director, &s->directorCount);
    parseArray(campos[4], s->cast, &s->castCount);

    strcpy(s->country, strcmp(campos[5], "NaN") == 0 ? "NaN" : campos[5]);
    strcpy(s->dateAdded, strcmp(campos[6], "NaN") == 0 ? "March 1, 1900" : campos[6]);

    s->releaseYear = strcmp(campos[7], "NaN") == 0 ? 0 : atoi(campos[7]);
    strcpy(s->rating, campos[8]);
    strcpy(s->duration, campos[9]);

    parseArray(campos[10], s->listedIn, &s->listedCount);
}

void imprimirShow(const Show* s) {
    printf("=> %s ## %s ## %s ## ", s->showId, s->title, s->type);

    if (s->directorCount > 0) {
        for (int i = 0; i < s->directorCount; i++) {
            printf("%s", s->director[i]);
            if (i < s->directorCount - 1) printf(", ");
        }
    } else {
        printf("NaN");
    }
    printf(" ## ");

    if (s->castCount > 0) {
        printf("[");
        for (int i = 0; i < s->castCount; i++) {
            printf("%s", s->cast[i]);
            if (i < s->castCount - 1) printf(", ");
        }
        printf("]");
    } else {
        printf("NaN");
    }
    printf(" ## ");

    printf("%s ## %s ## %d ## %s ## %s ## ", s->country, s->dateAdded, s->releaseYear, s->rating, s->duration);

    if (s->listedCount > 0) {
        printf("[");
        for (int i = 0; i < s->listedCount; i++) {
            printf("%s", s->listedIn[i]);
            if (i < s->listedCount - 1) printf(", ");
        }
        printf("]");
    } else {
        printf("NaN");
    }
    printf(" ##\n");
}

int main() {
    FILE* fp = fopen("/tmp/disneyplus.csv", "r");
    if (!fp) {
        printf("Erro ao abrir o arquivo.\n");
        return 1;
    }

    char linha[MAX_LINE];
    fgets(linha, MAX_LINE, fp); // Pula cabeçalho

    Show shows[MAX_SHOWS];
    int count = 0;

    while (lerLinhaCompleta(fp, linha) != NULL && count < MAX_SHOWS) {
        lerShow(&shows[count++], linha);
    }
    fclose(fp);

    char input[MAX_FIELD];
    while (true) {
        fgets(input, MAX_FIELD, stdin);
        trim(input);
        if (strcmp(input, "FIM") == 0) break;

        bool encontrado = false;
        for (int i = 0; i < count; i++) {
            if (strcmp(shows[i].showId, input) == 0) {
                imprimirShow(&shows[i]);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            printf("Show com ID \"%s\" não encontrado.\n", input);
        }
    }

    return 0;
}
