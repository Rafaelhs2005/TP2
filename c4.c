#include <ctype.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_LINE_LENGTH 1024
#define MAX_FIELDS 20
#define MAX_ARRAY_SIZE 50
#define MAX_ID_LENGTH 20

typedef struct {
  char *showId;
  char *type;
  char *title;
  char **director;
  int directorCount;
  char **cast;
  int castCount;
  char *country;
  char *dateAdded;
  int releaseYear;
  char *rating;
  char *duration;
  char **listedIn;
  int listedInCount;
} Show;

// Função de comparação para qsort
int compareStrings(const void *a, const void *b) {
  return strcmp(*(const char **)a, *(const char **)b);
}

// Função para ordenar um array de strings
void sortStringArray(char **array, int count) {
  if (array != NULL && count > 1) {
    qsort(array, count, sizeof(char *), compareStrings);
  }
}

int countQuotes(const char *line) {
  int count = 0;
  while (*line) {
    if (*line == '"')
      count++;
    line++;
  }
  return count;
}

char *readCompleteLine(FILE *file) {
  char buffer[MAX_LINE_LENGTH];
  char *line = NULL;
  size_t totalLength = 0;
  int quotes = 0;

  do {
    if (fgets(buffer, MAX_LINE_LENGTH, file) == NULL) {
      break;
    }

    quotes += countQuotes(buffer);

    size_t bufferLength = strlen(buffer);
    char *newLine = realloc(line, totalLength + bufferLength + 1);
    if (newLine == NULL) {
      free(line);
      return NULL;
    }

    line = newLine;
    strcpy(line + totalLength, buffer);
    totalLength += bufferLength;

  } while (quotes % 2 != 0);

  if (totalLength > 0 && line[totalLength - 1] == '\n') {
    line[totalLength - 1] = '\0';
  }

  return line;
}

void trim(char *str) {
  int i = 0, j = 0;
  while (isspace((unsigned char)str[i]))
    i++;

  while (str[i]) {
    str[j++] = str[i++];
  }
  str[j] = '\0';

  while (j > 0 && isspace((unsigned char)str[j - 1])) {
    str[--j] = '\0';
  }
}

void splitCSV(const char *line, char **fields, int *fieldCount) {
  bool inQuotes = false;
  char *field = malloc(MAX_LINE_LENGTH);
  int fieldPos = 0;
  *fieldCount = 0;

  for (int i = 0; line[i] != '\0'; i++) {
    if (line[i] == '"') {
      inQuotes = !inQuotes;
    } else if (line[i] == ',' && !inQuotes) {
      field[fieldPos] = '\0';
      trim(field);
      fields[*fieldCount] =
          strcmp(field, "") == 0 ? strdup("NaN") : strdup(field);
      (*fieldCount)++;
      fieldPos = 0;
      free(field);
      field = malloc(MAX_LINE_LENGTH);
    } else {
      field[fieldPos++] = line[i];
    }
  }

  field[fieldPos] = '\0';
  trim(field);
  fields[*fieldCount] = strcmp(field, "") == 0 ? strdup("NaN") : strdup(field);
  (*fieldCount)++;
  free(field);
}

void splitArray(char *str, char ***array, int *count) {
  if (strcmp(str, "NaN") == 0) {
    *array = NULL;
    *count = 0;
    return;
  }

  char **tempArray = malloc(MAX_ARRAY_SIZE * sizeof(char *));
  char *token = strtok(str, ",");
  *count = 0;

  while (token != NULL && *count < MAX_ARRAY_SIZE) {
    trim(token);
    tempArray[*count] = strdup(token);
    (*count)++;
    token = strtok(NULL, ",");
  }

  *array = malloc(*count * sizeof(char *));
  for (int i = 0; i < *count; i++) {
    (*array)[i] = tempArray[i];
  }
  free(tempArray);

  // Ordena o array após a criação
  sortStringArray(*array, *count);
}

void freeShow(Show *show) {
  free(show->showId);
  free(show->type);
  free(show->title);

  for (int i = 0; i < show->directorCount; i++) {
    free(show->director[i]);
  }
  free(show->director);

  for (int i = 0; i < show->castCount; i++) {
    free(show->cast[i]);
  }
  free(show->cast);

  free(show->country);
  free(show->dateAdded);
  free(show->rating);
  free(show->duration);

  for (int i = 0; i < show->listedInCount; i++) {
    free(show->listedIn[i]);
  }
  free(show->listedIn);
}

void readShow(Show *show, const char *line) {
  char *fields[MAX_FIELDS];
  int fieldCount;

  splitCSV(line, fields, &fieldCount);

  if (fieldCount < 11) {
    printf("Linha com campos insuficientes\n");
    return;
  }

  show->showId = strdup(fields[0]);
  show->type = strdup(fields[1]);
  show->title = strdup(fields[2]);

  splitArray(fields[3], &show->director, &show->directorCount);
  splitArray(fields[4], &show->cast, &show->castCount);

  show->country =
      strcmp(fields[5], "NaN") == 0 ? strdup("NaN") : strdup(fields[5]);

  if (strcmp(fields[6], "NaN") == 0 || strcmp(fields[6], "") == 0) {
    show->dateAdded = strdup("March 1, 1900");
  } else {
    show->dateAdded = strdup(fields[6]);
  }

  show->releaseYear =
      strcmp(fields[7], "NaN") == 0 || strcmp(fields[7], "") == 0
          ? 0
          : atoi(fields[7]);

  show->rating = strcmp(fields[8], "NaN") == 0 || strcmp(fields[8], "") == 0
                     ? strdup("NaN")
                     : strdup(fields[8]);

  show->duration = strcmp(fields[9], "NaN") == 0 || strcmp(fields[9], "") == 0
                       ? strdup("NaN")
                       : strdup(fields[9]);

  splitArray(fields[10], &show->listedIn, &show->listedInCount);

  for (int i = 0; i < fieldCount; i++) {
    free(fields[i]);
  }
}

void printShow(const Show *show) {
  printf("=> %s ## %s ## %s ## ", show->showId, show->title, show->type);

  if (show->directorCount > 0) {
    for (int i = 0; i < show->directorCount; i++) {
      printf("%s%s", show->director[i],
             i < show->directorCount - 1 ? ", " : "");
    }
  } else {
    printf("NaN");
  }
  printf(" ## ");

  // Campo do elenco (cast) sempre entre colchetes
  printf("[");
  if (show->castCount > 0) {
    for (int i = 0; i < show->castCount; i++) {
      printf("%s%s", show->cast[i], i < show->castCount - 1 ? ", " : "");
    }
  } else {
    printf("NaN");
  }
  printf("]");
  printf(" ## ");

  printf("%s ## %s ## %d ## %s ## %s ## ", show->country, show->dateAdded,
         show->releaseYear, show->rating, show->duration);

  if (show->listedInCount > 0) {
    printf("[");
    for (int i = 0; i < show->listedInCount; i++) {
      printf("%s%s", show->listedIn[i],
             i < show->listedInCount - 1 ? ", " : "");
    }
    printf("]");
  } else {
    printf("NaN");
  }
  printf(" ##\n");
}

// Função de comparação por título
int compareByTitle(const void *a, const void *b) {
  return strcmp((*(Show *)a).title, (*(Show *)b).title);
}

// Busca binária por título
bool buscaBinaria(Show *array, int n, const char *chave) {
  int esq = 0, dir = n - 1;
  while (esq <= dir) {
    int meio = (esq + dir) / 2;
    int cmp = strcmp(array[meio].title, chave);
    if (cmp == 0)
      return true;
    else if (cmp < 0)
      esq = meio + 1;
    else
      dir = meio - 1;
  }
  return false;
}

int main() {
  FILE *file = fopen("/tmp/disneyplus.csv", "r");
  if (file == NULL) {
    perror("Erro ao abrir o arquivo");
    return 1;
  }

  // Pular cabeçalho
  char header[MAX_LINE_LENGTH];
  if (fgets(header, MAX_LINE_LENGTH, file) == NULL) {
    fclose(file);
    printf("Arquivo vazio\n");
    return 1;
  }

  Show *shows = NULL;
  int showCount = 0;
  char *line;

  while ((line = readCompleteLine(file)) != NULL) {
    Show show;
    readShow(&show, line);
    free(line);

    shows = realloc(shows, (showCount + 1) * sizeof(Show));
    shows[showCount++] = show;
  }

  fclose(file);

  // Coleta dos shows buscados por ID
  Show *selecionados = NULL;
  int countSelecionados = 0;

  char idBuscado[MAX_ID_LENGTH];
  while (1) {
    if (fgets(idBuscado, MAX_ID_LENGTH, stdin) == NULL)
      break;
    idBuscado[strcspn(idBuscado, "\n")] = '\0';
    if (strcmp(idBuscado, "FIM") == 0)
      break;

    for (int i = 0; i < showCount; i++) {
      if (strcmp(shows[i].showId, idBuscado) == 0) {
        selecionados =
            realloc(selecionados, (countSelecionados + 1) * sizeof(Show));
        selecionados[countSelecionados++] = shows[i];
        break;
      }
    }
  }

  // Ordenação por título
  qsort(selecionados, countSelecionados, sizeof(Show), compareByTitle);

  // Pesquisa binária por título
  char tituloBusca[1024];
  while (fgets(tituloBusca, sizeof(tituloBusca), stdin) != NULL) {
    tituloBusca[strcspn(tituloBusca, "\n")] = '\0';
    if (strcmp(tituloBusca, "FIM") == 0)
      break;

    if (buscaBinaria(selecionados, countSelecionados, tituloBusca)) {
      printf("SIM\n");
    } else {
      printf("NAO\n");
    }
  }

  // Liberação
  for (int i = 0; i < showCount; i++) {
    freeShow(&shows[i]);
  }
  free(shows);
  free(selecionados);

  return 0;
}
