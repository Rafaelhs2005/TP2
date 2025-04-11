#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#define MAX_LINE 8192
#define MAX_FIELDS 12
#define MAX_ITEMS 50
#define MAX_STR 256

typedef struct {
    char showId[MAX_STR];
    char type[MAX_STR];
    char title[MAX_STR];
    char director[MAX_ITEMS][MAX_STR];
    int directorCount;
    char cast[MAX_ITEMS][MAX_STR];
    int castCount;
    char country[MAX_STR];
    char dateAdded[MAX_STR];
    int releaseYear;
    char rating[MAX_STR];
    char duration[MAX_STR];
    char listedIn[MAX_ITEMS][MAX_STR];
    int listedInCount;
} Show;

void trim(char *str) {
    char *end;
    while (*str == ' ' || *str == '\t' || *str == '\n' || *str == '\r') str++;
    if (*str == 0) return;
    end = str + strlen(str) - 1;
    while (end > str && (*end == ' ' || *end == '\t' || *end == '\n' || *end == '\r')) end--;
    *(end + 1) = 0;
}

int contarAspas(const char *linha) {
    int count = 0;
    for (int i = 0; linha[i] != '\0'; i++) {
        if (linha[i] == '"') count++;
    }
    return count;
}

char* lerLinhaCompleta(FILE *fp, char *buffer) {
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

void splitCSV(char *linha, char campos[MAX_FIELDS][MAX_LINE]) {
    int i = 0, j = 0, k = 0;
    bool dentroAspas = false;
    for (; linha[i] != '\0'; i++) {
        if (linha[i] == '"') {
            dentroAspas = !dentroAspas;
        } else if (linha[i] == ',' && !dentroAspas) {
            campos[j][k] = '\0';
            j++;
            k = 0;
        } else {
            if (k < MAX_LINE - 1) campos[j][k++] = linha[i];
        }
    }
    campos[j][k] = '\0';
    for (int x = j + 1; x < MAX_FIELDS; x++) campos[x][0] = '\0';
}

int compararStrings(const void *a, const void *b) {
    const char *sa = *(const char **)a;
    const char *sb = *(const char **)b;
    return strcmp(sa, sb);
}

void separarLista(char *campo, char destino[MAX_ITEMS][MAX_STR], int *count) {
    if (strcmp(campo, "NaN") == 0) {
        strncpy(destino[0], "NaN", MAX_STR);
        *count = 1;
        return;
    }

    char *token = strtok(campo, ",");
    *count = 0;
    while (token && *count < MAX_ITEMS) {
        trim(token);
        strncpy(destino[*count], token, MAX_STR - 1);
        destino[*count][MAX_STR - 1] = '\0';
        (*count)++;
        token = strtok(NULL, ",");
    }

    char *ordenar[MAX_ITEMS];
    for (int i = 0; i < *count; i++) ordenar[i] = destino[i];
    qsort(ordenar, *count, sizeof(char*), compararStrings);
    for (int i = 0; i < *count; i++) strncpy(destino[i], ordenar[i], MAX_STR);
}

void lerShow(Show *s, char campos[MAX_FIELDS][MAX_LINE]) {
    strncpy(s->showId, campos[0][0] ? campos[0] : "NaN", MAX_STR);
    strncpy(s->type, campos[1][0] ? campos[1] : "NaN", MAX_STR);
    strncpy(s->title, campos[2][0] ? campos[2] : "NaN", MAX_STR);

    separarLista(campos[3][0] ? campos[3] : "NaN", s->director, &s->directorCount);
    separarLista(campos[4][0] ? campos[4] : "NaN", s->cast, &s->castCount);

    strncpy(s->country, campos[5][0] ? campos[5] : "NaN", MAX_STR);
    strncpy(s->dateAdded, campos[6][0] ? campos[6] : "March 1, 1900", MAX_STR);
    s->releaseYear = campos[7][0] ? atoi(campos[7]) : 0;
    strncpy(s->rating, campos[8][0] ? campos[8] : "NaN", MAX_STR);
    strncpy(s->duration, campos[9][0] ? campos[9] : "NaN", MAX_STR);
    separarLista(campos[10][0] ? campos[10] : "NaN", s->listedIn, &s->listedInCount);
}

void imprimirShow(const Show *s) {
    printf("=> %s ## %s ## %s ## ", s->showId, s->title, s->type);
    for (int i = 0; i < s->directorCount; i++) {
        printf("%s%s", s->director[i], (i < s->directorCount - 1 ? ", " : ""));
    }
    printf(" ## [");
    for (int i = 0; i < s->castCount; i++) {
        printf("%s%s", s->cast[i], (i < s->castCount - 1 ? ", " : ""));
    }
    printf("] ## %s ## %s ## %d ## %s ## %s ## [", s->country, s->dateAdded, s->releaseYear, s->rating, s->duration);
    for (int i = 0; i < s->listedInCount; i++) {
        printf("%s%s", s->listedIn[i], (i < s->listedInCount - 1 ? ", " : ""));
    }
    printf("] ##\n");
}

int main() {
    FILE *fp = fopen("/tmp/disneyplus.csv", "r");
    if (!fp) {
        perror("Erro ao abrir o arquivo");
        return 1;
    }

    char linhaCompleta[MAX_LINE];
    char campos[MAX_FIELDS][MAX_LINE];
    Show shows[10000];
    int totalShows = 0;

    fgets(linhaCompleta, MAX_LINE, fp);
    while (lerLinhaCompleta(fp, linhaCompleta)) {
        splitCSV(linhaCompleta, campos);
        lerShow(&shows[totalShows++], campos);
    }
    fclose(fp);

    char entrada[MAX_STR];
    printf("Digite um ID de show (ou FIM para sair):\n");
    while (scanf("%255s", entrada) == 1) {
        if (strcmp(entrada, "FIM") == 0) break;

        bool encontrado = false;
        for (int i = 0; i < totalShows; i++) {
            if (strcmp(shows[i].showId, entrada) == 0) {
                imprimirShow(&shows[i]);
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            printf("Show com ID \"%s\" nao encontrado.\n", entrada);
        }
        printf("\nDigite outro ID de show (ou FIM para sair):\n");
    }

    return 0;
}
