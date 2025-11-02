#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    char **data;
    int size;
    int capacity;
} StringList;

void initStringList(StringList *list) {
    list->data = NULL;
    list->size = 0;
    list->capacity = 0;
}

void addToStringList(StringList *list, const char *str) {
    if (list->size >= list->capacity) {
        list->capacity = list->capacity == 0 ? 4 : list->capacity * 2;
        list->data = realloc(list->data, list->capacity * sizeof(char *));
    }
    list->data[list->size] = malloc(strlen(str) + 1);
    strcpy(list->data[list->size], str);
    list->size++;
}

void freeStringList(StringList *list) {
    for (int i = 0; i < list->size; i++)
        free(list->data[i]);
    free(list->data);
    list->data = NULL;
    list->size = list->capacity = 0;
}

StringList copyStringList(const StringList *src) {
    StringList dst;
    initStringList(&dst);
    for (int i = 0; i < src->size; i++)
        addToStringList(&dst, src->data[i]);
    return dst;
}

typedef struct {
    char appId[100];
    char name[500];
    char releaseDate[50];
    char estimateOwners[100];
    float price;

    StringList supportedLanguages;

    int metacriticScore;
    float userScore;
    int achievements;

    char publisher[500];
    char developers[500];

    StringList categories;
    StringList genres;
    StringList tags;
} Games;

Games copyGames(const Games *src) {
    Games dst = *src;

    dst.supportedLanguages = copyStringList(&src->supportedLanguages);
    dst.categories        = copyStringList(&src->categories);
    dst.genres            = copyStringList(&src->genres);
    dst.tags              = copyStringList(&src->tags);

    return dst;
}

int contarAspas(const char *s){
    int c = 0;
    while(*s) if(*s++ == '"') c++;
    return c;
}

char *lerLinhaCompleta(FILE *file) {
    char buffer[10000];
    char *result = NULL;
    size_t total = 0;
    int aspas = 0;

    while (fgets(buffer, sizeof(buffer), file)) {
        int len = strlen(buffer);
        aspas += contarAspas(buffer);

        result = realloc(result, total + len + 1);
        if (!result) return NULL;

        if (total == 0)
            strcpy(result, buffer);
        else
            strcat(result, buffer);

        total += len;

        if (aspas % 2 == 0)
            break;
    }
    return result;
}

StringList splitCSV(const char *linha) {
    StringList campos;
    initStringList(&campos);

    char *buffer = malloc(strlen(linha) + 1);
    strcpy(buffer, linha);

    int aspas = 0;
    char *start = buffer;

    for (char *p = buffer; *p; p++) {
        if (*p == '"')
            aspas = !aspas;
        else if (*p == ',' && !aspas) {
            *p = '\0';
            addToStringList(&campos, strlen(start) ? start : "NaN");
            start = p + 1;
        }
    }
    addToStringList(&campos, strlen(start) ? start : "NaN");

    free(buffer);
    return campos;
}

const char *getField(const StringList *campos, int idx) {
    if (!campos || idx < 0 || idx >= campos->size)
        return "NaN";
    return strlen(campos->data[idx]) ? campos->data[idx] : "NaN";
}

StringList parseListField(const char *s) {
    StringList result;
    initStringList(&result);

    if (!s || strcmp(s, "NaN") == 0)
        return result;

    char *temp = malloc(strlen(s) + 1);
    strcpy(temp, s);

    char *start = temp;
    char *end = temp + strlen(temp) - 1;

    if (*start == '[' && *end == ']') {
        *end = '\0';
        start++;
    }

    for (char *p = start; *p; p++)
        if (*p == '\'') *p = ' ';

    char *token = strtok(start, ",");
    while (token) {
        while (isspace((unsigned char)*token)) token++;
        char *e = token + strlen(token) - 1;
        while (e > token && isspace((unsigned char)*e)) *e-- = '\0';
        if (strlen(token))
            addToStringList(&result, token);
        token = strtok(NULL, ",");
    }

    free(temp);
    return result;
}

static const char* month_to_num(const char *m3){
    if(!m3) return "00";
    if(!strncasecmp(m3,"Jan",3)) return "01";
    if(!strncasecmp(m3,"Feb",3)) return "02";
    if(!strncasecmp(m3,"Mar",3)) return "03";
    if(!strncasecmp(m3,"Apr",3)) return "04";
    if(!strncasecmp(m3,"May",3)) return "05";
    if(!strncasecmp(m3,"Jun",3)) return "06";
    if(!strncasecmp(m3,"Jul",3)) return "07";
    if(!strncasecmp(m3,"Aug",3)) return "08";
    if(!strncasecmp(m3,"Sep",3)) return "09";
    if(!strncasecmp(m3,"Oct",3)) return "10";
    if(!strncasecmp(m3,"Nov",3)) return "11";
    if(!strncasecmp(m3,"Dec",3)) return "12";
    return "00";
}

static void format_date(char *dest, const char *orig){
    char m3[8]={0}; 
    int day=0, year=0;

    if(sscanf(orig,"%3s %d, %d",m3,&day,&year) == 3){
        snprintf(dest,32,"%02d/%s/%d",day,month_to_num(m3),year);
    } else {
        strncpy(dest, orig, 31);
        dest[31]='\0';
    }
}

void lerGames(Games *g, const char *linha) {
    StringList campos = splitCSV(linha);

    strncpy(g->appId, getField(&campos,0), sizeof(g->appId)-1);
    g->appId[sizeof(g->appId)-1] = '\0';

    strncpy(g->name, getField(&campos,1), sizeof(g->name)-1);
    g->name[sizeof(g->name)-1] = '\0';

    char raw[50];
    strncpy(raw, getField(&campos,2), sizeof(raw)-1);
    raw[sizeof(raw)-1]='\0';
    format_date(g->releaseDate, raw);

    strncpy(g->estimateOwners, getField(&campos,3),sizeof(g->estimateOwners)-1);
    g->estimateOwners[sizeof(g->estimateOwners)-1]='\0';

    const char *pr = getField(&campos,4);
    g->price = strcmp(pr,"NaN") ? atof(pr) : 0.0f;

    g->supportedLanguages = parseListField(getField(&campos,5));
    g->metacriticScore = strcmp(getField(&campos,6),"NaN") ? atoi(getField(&campos,6)) : 0;
    g->userScore       = strcmp(getField(&campos,7),"NaN") ? atof(getField(&campos,7)) : 0;
    g->achievements    = strcmp(getField(&campos,8),"NaN") ? atoi(getField(&campos,8)) : 0;

    strncpy(g->publisher,  getField(&campos,9),  sizeof(g->publisher)-1);
    strncpy(g->developers, getField(&campos,10), sizeof(g->developers)-1);

    g->categories = parseListField(getField(&campos,11));
    g->genres     = parseListField(getField(&campos,12));
    g->tags       = parseListField(getField(&campos,13));

    for(int i=0;i<campos.size;i++)
        free(campos.data[i]);
    free(campos.data);
}

void imprimirGames(const Games *g) {
    printf("=> %s ## %s ## %s ## %s ## %.2f ## ",
           g->appId, g->name, g->releaseDate, g->estimateOwners, g->price);

    printf("[");
    for(int i=0;i<g->supportedLanguages.size;i++){
        printf("%s",g->supportedLanguages.data[i]);
        if(i<g->supportedLanguages.size-1) printf(", ");
    }
    printf("] ## %d ## %.1f ## %d ## [%s] ## [%s] ## [",
           g->metacriticScore, g->userScore, g->achievements, g->publisher, g->developers);

    for(int i=0;i<g->categories.size;i++){
        printf("%s",g->categories.data[i]);
        if(i<g->categories.size-1) printf(", ");
    }
    printf("] ## [");
    for(int i=0;i<g->genres.size;i++){
        printf("%s",g->genres.data[i]);
        if(i<g->genres.size-1) printf(", ");
    }
    printf("] ## [");
    for(int i=0;i<g->tags.size;i++){
        printf("%s",g->tags.data[i]);
        if(i<g->tags.size-1) printf(", ");
    }
    printf("] ##\n");
}

void freeGames(Games *g) {
    freeStringList(&g->supportedLanguages);
    freeStringList(&g->categories);
    freeStringList(&g->genres);
    freeStringList(&g->tags);
}

typedef struct Cel {
    Games elemento;
    struct Cel *prox;
} Cel;

typedef struct {
    Cel *topo;
    int size;
} Pilha;

void iniciarPilha(Pilha *p) {
    p->topo = NULL;
    p->size = 0;
}

void push(Pilha *p, Games x) {
    Cel *tmp = malloc(sizeof(Cel));
    tmp->elemento = x;
    tmp->prox = p->topo;
    p->topo = tmp;
    p->size++;
}

Games pop(Pilha *p) {
    Games vazio;
    memset(&vazio, 0, sizeof(Games));

    if (!p->topo) return vazio;

    Cel *tmp = p->topo;
    Games resp = tmp->elemento;
    p->topo = tmp->prox;
    free(tmp);
    p->size--;
    return resp;
}

void mostrarPilha(Pilha *p) {
    Cel *i = p->topo;
    int idx = 0;
    while (i) {
        printf("[%d] ", idx++);
        imprimirGames(&i->elemento);
        i = i->prox;
    }
}

void liberarPilha(Pilha *p) {
    while (p->topo) {
        Games g = pop(p);
        freeGames(&g);
    }
}

Games *buscarPorID(Games *banco, int tam, const char *id) {
    for(int i=0;i<tam;i++){
        if(!strcmp(banco[i].appId,id)) return &banco[i];
    }
    return NULL;
}

int main() {

    FILE *file = fopen("/tmp/games.csv","r");
    if(!file){
        printf("Erro ao abrir arquivo!\n");
        return 1;
    }

    char header[10000];
    fgets(header, sizeof(header), file);

    Games *banco = malloc(10000*sizeof(Games));
    int bancoSize = 0, cap = 10000;

    char *linha;
    while( (linha = lerLinhaCompleta(file)) != NULL ){
        if(bancoSize >= cap){
            cap *= 2;
            banco = realloc(banco, cap*sizeof(Games));
        }
        lerGames(&banco[bancoSize], linha);
        bancoSize++;
        free(linha);
    }
    fclose(file);

    Pilha pilha;
    iniciarPilha(&pilha);

    char entrada[100];
    while(1){
        fgets(entrada, sizeof(entrada), stdin);
        entrada[strcspn(entrada,"\n")] = 0;
        if(!strcmp("FIM",entrada)) break;

        Games *g = buscarPorID(banco, bancoSize, entrada);
        if(g) push(&pilha, copyGames(g));
    }

    int n;
    fgets(entrada,sizeof(entrada),stdin);
    sscanf(entrada,"%d",&n);

    for(int i=0;i<n;i++){
        fgets(entrada,sizeof(entrada),stdin);
        entrada[strcspn(entrada,"\n")] = 0;

        char comando[10], param[100];
        if(sscanf(entrada,"%s %s", comando, param) >= 1){
            
            if(!strcmp(comando,"I")){
                Games *g = buscarPorID(banco, bancoSize, param);
                if(g) push(&pilha, copyGames(g));

            } else if(!strcmp(comando,"R")){
                Games g = pop(&pilha);
                printf("(R) %s\n", g.name);
                freeGames(&g);
            }
        }
    }

    mostrarPilha(&pilha);
    liberarPilha(&pilha);

    for(int i=0;i<bancoSize;i++)
        freeGames(&banco[i]);
    free(banco);

    return 0;
}
