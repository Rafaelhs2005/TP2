#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// --- ESTRUTURAS ---

typedef struct {
    char **data;
    int size;
    int capacity;
} StringList;

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

typedef struct Celula {
    Games elemento;
    struct Celula *prox;
} Celula;

typedef struct {
    Celula *primeiro;
    Celula *ultimo;
    int tamanho;
} Lista;

// --- STRINGLIST AUX ---

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
    for (int i = 0; i < list->size; i++) {
        free(list->data[i]);
    }
    free(list->data);
    list->data = NULL;
    list->size = 0;
    list->capacity = 0;
}

StringList copyStringList(const StringList *src) {
    StringList dst;
    initStringList(&dst);
    for (int i = 0; i < src->size; i++) {
        addToStringList(&dst, src->data[i]);
    }
    return dst;
}

// --- GAMES AUX ---

Games copyGames(const Games *src) {
    Games dst = *src;
    dst.supportedLanguages = copyStringList(&src->supportedLanguages);
    dst.categories = copyStringList(&src->categories);
    dst.genres = copyStringList(&src->genres);
    dst.tags = copyStringList(&src->tags);
    return dst;
}

// --- FUNÇÕES CSV ---

int contarAspas(const char *linha) {
    int count = 0;
    for (int i = 0; linha[i]; i++) {
        if (linha[i] == '"') count++;
    }
    return count;
}

char *lerLinhaCompleta(FILE *file) {
    char buffer[10000];
    char *result = NULL;
    size_t totalLength = 0;
    int aspas = 0;

    while (fgets(buffer, sizeof(buffer), file)) {
        int len = strlen(buffer);
        aspas += contarAspas(buffer);

        char *newResult = realloc(result, totalLength + len + 1);
        if (!newResult) {
            free(result);
            return NULL;
        }
        result = newResult;

        if (totalLength == 0) {
            strcpy(result, buffer);
        } else {
            strcat(result, buffer);
        }
        totalLength += len;

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
        if (*p == '"') {
            aspas = !aspas;
        } else if (*p == ',' && !aspas) {
            *p = '\0';
            if (strlen(start) == 0) addToStringList(&campos, "NaN");
            else addToStringList(&campos, start);
            start = p + 1;
        }
    }
    if (strlen(start) == 0) addToStringList(&campos, "NaN");
    else addToStringList(&campos, start);

    free(buffer);
    return campos;
}

const char *getField(const StringList *campos, int idx) {
    if (campos == NULL || idx < 0 || idx >= campos->size) return "NaN";
    const char *field = campos->data[idx];
    if (field == NULL || strlen(field) == 0) return "NaN";
    return field;
}

StringList parseListField(const char *s) {
    StringList result;
    initStringList(&result);
    if (s == NULL || strcmp(s, "NaN") == 0) return result;

    char *temp = malloc(strlen(s) + 1);
    strcpy(temp, s);

    char *start = temp;
    char *end = temp + strlen(temp) - 1;
    if (*start == '[' && *end == ']') {
        *end = '\0';
        start++;
    }

    for (char *p = start; *p; p++) if (*p == '\'') *p = ' ';

    char *token = strtok(start, ",");
    while (token != NULL) {
        while (*token && isspace((unsigned char)*token)) token++;
        char *end = token + strlen(token) - 1;
        while (end > token && isspace((unsigned char)*end)) {
            *end = '\0';
            end--;
        }
        if (strlen(token) > 0) addToStringList(&result, token);
        token = strtok(NULL, ",");
    }

    free(temp);
    return result;
}

// --- FORMATAR DATA ---

static const char* month_to_num(const char *m3){
    if(!m3) return "00";
    if(strncasecmp(m3,"Jan",3)==0) return "01";
    if(strncasecmp(m3,"Feb",3)==0) return "02";
    if(strncasecmp(m3,"Mar",3)==0) return "03";
    if(strncasecmp(m3,"Apr",3)==0) return "04";
    if(strncasecmp(m3,"May",3)==0) return "05";
    if(strncasecmp(m3,"Jun",3)==0) return "06";
    if(strncasecmp(m3,"Jul",3)==0) return "07";
    if(strncasecmp(m3,"Aug",3)==0) return "08";
    if(strncasecmp(m3,"Sep",3)==0) return "09";
    if(strncasecmp(m3,"Oct",3)==0) return "10";
    if(strncasecmp(m3,"Nov",3)==0) return "11";
    if(strncasecmp(m3,"Dec",3)==0) return "12";
    return "00";
}

static void format_date(char *dest, const char *orig){
    char m3[8]={0}; int day=0, year=0;
    if(sscanf(orig,"%3s %d, %d",m3,&day,&year)==3){
        snprintf(dest,32,"%02d/%s/%d",day,month_to_num(m3),year);
    } else {
        strncpy(dest,orig,31); dest[31]='\0';
    }
}

// --- FUNÇÕES GAMES ---

void lerGames(Games *g, const char *linha) {
    StringList campos = splitCSV(linha);

    strncpy(g->appId, getField(&campos, 0), sizeof(g->appId)-1);
    g->appId[sizeof(g->appId)-1] = '\0';
    
    strncpy(g->name, getField(&campos, 1), sizeof(g->name)-1);
    g->name[sizeof(g->name)-1] = '\0';

    char rawDate[50];
    strncpy(rawDate, getField(&campos, 2), sizeof(rawDate)-1);
    rawDate[sizeof(rawDate)-1] = '\0';
    format_date(g->releaseDate, rawDate);

    strncpy(g->estimateOwners, getField(&campos, 3), sizeof(g->estimateOwners)-1);
    g->estimateOwners[sizeof(g->estimateOwners)-1] = '\0';

    const char *priceStr = getField(&campos, 4);
    g->price = strcmp(priceStr, "NaN")==0 ? 0.0f : atof(priceStr);

    g->supportedLanguages = parseListField(getField(&campos, 5));
    g->metacriticScore = strcmp(getField(&campos, 6),"NaN")==0 ? 0 : atoi(getField(&campos,6));
    g->userScore = strcmp(getField(&campos,7),"NaN")==0 ? 0.0f : atof(getField(&campos,7));
    g->achievements = strcmp(getField(&campos,8),"NaN")==0 ? 0 : atoi(getField(&campos,8));

    strncpy(g->publisher,getField(&campos,9),sizeof(g->publisher)-1);
    g->publisher[sizeof(g->publisher)-1]='\0';
    
    strncpy(g->developers,getField(&campos,10),sizeof(g->developers)-1);
    g->developers[sizeof(g->developers)-1]='\0';

    g->categories = parseListField(getField(&campos,11));
    g->genres = parseListField(getField(&campos,12));
    g->tags = parseListField(getField(&campos,13));

    for(int i=0;i<campos.size;i++) free(campos.data[i]);
    free(campos.data);
}

void imprimirGames(const Games *g) {
    printf("=> %s ## %s ## %s ## ", g->appId, g->name, g->releaseDate);
    printf("%s ## %.2f ## [", g->estimateOwners, g->price);

    for(int i=0;i<g->supportedLanguages.size;i++){
        printf("%s",g->supportedLanguages.data[i]);
        if(i<g->supportedLanguages.size-1) printf(", ");
    }
    printf("] ## ");

    printf("%d ## %.1f ## %d ## [%s] ## [%s] ## [", g->metacriticScore, g->userScore,
           g->achievements, g->publisher, g->developers);

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

// --- LISTA ENCADEADA ---

void iniciarLista(Lista *lista) {
    lista->primeiro = NULL;
    lista->ultimo = NULL;
    lista->tamanho = 0;
}

void inserirInicio(Lista *lista, Games x) {
    Celula *tmp = malloc(sizeof(Celula));
    tmp->elemento = x;
    tmp->prox = lista->primeiro;
    lista->primeiro = tmp;
    if(lista->ultimo==NULL) lista->ultimo = tmp;
    lista->tamanho++;
}

void inserirFim(Lista *lista, Games x) {
    Celula *tmp = malloc(sizeof(Celula));
    tmp->elemento = x;
    tmp->prox = NULL;
    if(lista->ultimo==NULL){
        lista->primeiro=tmp; lista->ultimo=tmp;
    } else {
        lista->ultimo->prox=tmp; lista->ultimo=tmp;
    }
    lista->tamanho++;
}

void inserir(Lista *lista, Games x, int pos) {
    if(pos<0 || pos>lista->tamanho){ printf("Posição inválida!\n"); return; }
    if(pos==0){ inserirInicio(lista,x); return; }
    if(pos==lista->tamanho){ inserirFim(lista,x); return; }

    Celula *i=lista->primeiro;
    for(int j=0;j<pos-1;j++) i=i->prox;

    Celula *tmp=malloc(sizeof(Celula));
    tmp->elemento=x; tmp->prox=i->prox; i->prox=tmp;
    lista->tamanho++;
}

Games removerInicio(Lista *lista) {
    if(lista->primeiro==NULL){ Games vazio; memset(&vazio,0,sizeof(Games)); return vazio; }
    Celula *tmp=lista->primeiro;
    Games resp=tmp->elemento;
    lista->primeiro=tmp->prox;
    if(lista->primeiro==NULL) lista->ultimo=NULL;
    free(tmp); lista->tamanho--;
    return resp;
}

Games removerFim(Lista *lista) {
    if(lista->primeiro==NULL){ Games vazio; memset(&vazio,0,sizeof(Games)); return vazio; }
    if(lista->primeiro==lista->ultimo){
        Games resp=lista->primeiro->elemento;
        free(lista->primeiro); lista->primeiro=lista->ultimo=NULL; lista->tamanho--;
        return resp;
    }
    Celula *i=lista->primeiro;
    while(i->prox!=lista->ultimo) i=i->prox;
    Games resp=lista->ultimo->elemento;
    free(lista->ultimo); lista->ultimo=i; i->prox=NULL; lista->tamanho--;
    return resp;
}

Games remover(Lista *lista,int pos){
    if(pos<0 || pos>=lista->tamanho){ Games vazio; memset(&vazio,0,sizeof(Games)); return vazio; }
    if(pos==0) return removerInicio(lista);
    if(pos==lista->tamanho-1) return removerFim(lista);

    Celula *i=lista->primeiro;
    for(int j=0;j<pos-1;j++) i=i->prox;
    Celula *tmp=i->prox;
    Games resp=tmp->elemento;
    i->prox=tmp->prox; free(tmp); lista->tamanho--;
    return resp;
}

void mostrarLista(Lista *lista){
    Celula *i=lista->primeiro; int cont=0;
    while(i!=NULL){
        printf("[%d] ", cont++);
        imprimirGames(&i->elemento);
        i=i->prox;
    }
}

void liberarLista(Lista *lista){
    Celula *atual=lista->primeiro;
    while(atual!=NULL){
        Celula *prox=atual->prox;
        freeGames(&atual->elemento);
        free(atual);
        atual=prox;
    }
    lista->primeiro=lista->ultimo=NULL; lista->tamanho=0;
}

// --- BUSCA ---

Games *buscarPorID(Games *banco,int bancoSize,const char *id){
    for(int i=0;i<bancoSize;i++){
        if(strcmp(banco[i].appId,id)==0) return &banco[i];
    }
    return NULL;
}

// --- MAIN ---

int main(){
    FILE *file=fopen("/tmp/games.csv","r");
    if(!file){ printf("Erro ao abrir arquivo!\n"); return 1; }

    char header[10000];
    fgets(header,sizeof(header),file);

    Games *banco=malloc(10000*sizeof(Games));
    int bancoSize=0, bancoCapacity=10000;

    char *linha;
    while((linha=lerLinhaCompleta(file))!=NULL){
        if(bancoSize>=bancoCapacity){ bancoCapacity*=2; banco=realloc(banco,bancoCapacity*sizeof(Games)); }
        lerGames(&banco[bancoSize],linha);
        bancoSize++;
        free(linha);
    }
    fclose(file);

    Lista lista;
    iniciarLista(&lista);

    char entrada[100];
    while(1){
        fgets(entrada,sizeof(entrada),stdin);
        entrada[strcspn(entrada,"\n")]=0;
        if(strcmp(entrada,"FIM")==0) break;

        Games *g=buscarPorID(banco,bancoSize,entrada);
        if(g!=NULL) inserirFim(&lista,copyGames(g));
    }

    int n;
    fgets(entrada,sizeof(entrada),stdin);
    sscanf(entrada,"%d",&n);

    for(int i=0;i<n;i++){
        fgets(entrada,sizeof(entrada),stdin);
        entrada[strcspn(entrada,"\n")]=0;

        char comando[10], param1[100], param2[100];
        if(sscanf(entrada,"%s %s %s",comando,param1,param2)>=1){
            if(strcmp(comando,"II")==0){
                Games *g=buscarPorID(banco,bancoSize,param1);
                if(g!=NULL) inserirInicio(&lista,copyGames(g));
            } else if(strcmp(comando,"IF")==0){
                Games *g=buscarPorID(banco,bancoSize,param1);
                if(g!=NULL) inserirFim(&lista,copyGames(g));
            } else if(strcmp(comando,"I*")==0){
                int pos=atoi(param1);
                Games *g=buscarPorID(banco,bancoSize,param2);
                if(g!=NULL) inserir(&lista,copyGames(g),pos);
            } else if(strcmp(comando,"RI")==0){
                Games g=removerInicio(&lista);
                printf("(R) %s\n",g.name);
                freeGames(&g);
            } else if(strcmp(comando,"RF")==0){
                Games g=removerFim(&lista);
                printf("(R) %s\n",g.name);
                freeGames(&g);
            } else if(strcmp(comando,"R*")==0){
                int pos=atoi(param1);
                Games g=remover(&lista,pos);
                printf("(R) %s\n",g.name);
                freeGames(&g);
            }
        }
    }

    mostrarLista(&lista);
    liberarLista(&lista);

    for(int i=0;i<bancoSize;i++) freeGames(&banco[i]);
    free(banco);

    return 0;
}
