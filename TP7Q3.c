#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_LINE 5000
#define MAX_FIELD 200

typedef struct {
  char appId[50];
  char name[200];
} Games;

typedef struct No {
  Games elemento;
  struct No *esq, *dir;
  int altura;
} No;

No *novoNo(Games g) {
  No *n = (No *)malloc(sizeof(No));
  n->elemento = g;
  n->esq = n->dir = NULL;
  n->altura = 1;
  return n;
}

int altura(No *n) {
  if (!n)
    return 0;
  return n->altura;
}

int max(int a, int b) { return (a > b) ? a : b; }

int fatorBalanceamento(No *n) {
  if (!n)
    return 0;
  return altura(n->esq) - altura(n->dir);
}

No *rotacaoDireita(No *y) {
  No *x = y->esq;
  No *T2 = x->dir;

  x->dir = y;
  y->esq = T2;

  y->altura = max(altura(y->esq), altura(y->dir)) + 1;
  x->altura = max(altura(x->esq), altura(x->dir)) + 1;

  return x;
}

No *rotacaoEsquerda(No *x) {
  No *y = x->dir;
  No *T2 = y->esq;

  y->esq = x;
  x->dir = T2;

  x->altura = max(altura(x->esq), altura(x->dir)) + 1;
  y->altura = max(altura(y->esq), altura(y->dir)) + 1;

  return y;
}

int cmp(const char *a, const char *b) { return strcmp(a, b); }

No *inserirAVL(No *raiz, Games g) {

  if (raiz == NULL)
    return novoNo(g);

  if (cmp(g.name, raiz->elemento.name) < 0)
    raiz->esq = inserirAVL(raiz->esq, g);

  else if (cmp(g.name, raiz->elemento.name) > 0)
    raiz->dir = inserirAVL(raiz->dir, g);

  else // nomes iguais → não insere
    return raiz;

  raiz->altura = 1 + max(altura(raiz->esq), altura(raiz->dir));
  int fb = fatorBalanceamento(raiz);

  // Desbalanceamentos
  // LL
  if (fb > 1 && cmp(g.name, raiz->esq->elemento.name) < 0)
    return rotacaoDireita(raiz);

  // RR
  if (fb < -1 && cmp(g.name, raiz->dir->elemento.name) > 0)
    return rotacaoEsquerda(raiz);

  // LR
  if (fb > 1 && cmp(g.name, raiz->esq->elemento.name) > 0) {
    raiz->esq = rotacaoEsquerda(raiz->esq);
    return rotacaoDireita(raiz);
  }

  // RL
  if (fb < -1 && cmp(g.name, raiz->dir->elemento.name) < 0) {
    raiz->dir = rotacaoDireita(raiz->dir);
    return rotacaoEsquerda(raiz);
  }

  return raiz;
}

int pesquisar(No *raiz, const char *nome) {
  printf("%s: =>", nome);

  while (raiz != NULL) {
    if (cmp(nome, raiz->elemento.name) == 0) {
      printf("SIM\n");
      return 1;
    }

    if (cmp(nome, raiz->elemento.name) < 0) {
      printf("esq ");
      raiz = raiz->esq;
    } else {
      printf("dir ");
      raiz = raiz->dir;
    }
  }

  printf("NAO\n");
  return 0;
}

Games buscarPorID(Games *banco, int n, const char *id) {
  Games vazio;
  vazio.appId[0] = '\0';

  for (int i = 0; i < n; i++) {
    if (strcmp(banco[i].appId, id) == 0)
      return banco[i];
  }
  return vazio;
}

void lerCSV(char *linha, Games *g) {
  char *token = strtok(linha, ",");
  int coluna = 0;

  while (token != NULL) {
    if (coluna == 0)
      strcpy(g->appId, token);
    else if (coluna == 1)
      strcpy(g->name, token);

    token = strtok(NULL, ",");
    coluna++;
  }
}

int main() {

  FILE *f = fopen("/tmp/games.csv", "r");
  if (!f) {
    printf("Erro ao abrir CSV\n");
    return 0;
  }

  char linha[MAX_LINE];
  fgets(linha, MAX_LINE, f);

  Games banco[20000];
  int n = 0;

  while (fgets(linha, MAX_LINE, f)) {
    Games g;
    lerCSV(linha, &g);
    banco[n++] = g;
  }
  fclose(f);

  No *raiz = NULL;

  char entrada[MAX_FIELD];

  while (1) {
    scanf("%s", entrada);
    if (strcmp(entrada, "FIM") == 0)
      break;

    Games g = buscarPorID(banco, n, entrada);
    if (g.appId[0] != '\0')
      raiz = inserirAVL(raiz, g);
  }

  getchar();
  while (1) {
    fgets(entrada, MAX_FIELD, stdin);
    entrada[strcspn(entrada, "\n")] = '\0';

    if (strcmp(entrada, "FIM") == 0)
      break;

    pesquisar(raiz, entrada);
  }

  return 0;
}
