#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAX_LINE 10000
#define MAX_FIELDS 20
#define MAX_FIELD 1024
#define MAX_GAMES 30000

typedef struct {
    char app_id[64];
    char title[512];
    char date_added[32];
    int release_year;
    double price;
    char languages[1024];
    int metascore;
    double user_score;
    int reviews;
    char publishers[512];
    char developers[512];
    char features[1024];
    char genres[512];
    char tags[2048];
} Game;

// --- Funções auxiliares ---
static void rtrim(char *s) {
    int i = (int)strlen(s)-1;
    while(i>=0 && (s[i]=='\n' || s[i]=='\r' || s[i]==' ')) s[i--]='\0';
}

static int count_quotes(const char *s) {
    int c=0; for(;*s;s++) if(*s=='"') c++; return c;
}

static char* read_record(FILE *f, char *dest, size_t dest_size) {
    dest[0]='\0';
    char buf[1024]; int quotes=0; size_t len=0;
    while(fgets(buf,sizeof(buf),f)) {
        size_t blen=strlen(buf);
        if(len+blen>=dest_size) blen=dest_size-len-1;
        strncat(dest, buf, blen);
        len+=blen;
        quotes+=count_quotes(buf);
        if(quotes%2==0) break;
    }
    if(len==0) return NULL;
    rtrim(dest);
    return dest;
}

static int split_csv(const char *line, char campos[][MAX_FIELD], int max_fields) {
    int field=0,pos=0,in_quotes=0;
    if(max_fields<=0) return 0;
    campos[0][0]='\0';
    for(size_t i=0;line[i];i++){
        char c=line[i];
        if(c=='"'){in_quotes=!in_quotes; continue;}
        if(c==',' && !in_quotes){
            campos[field][pos]='\0';
            field++;
            if(field>=max_fields) return max_fields;
            pos=0;
            campos[field][0]='\0';
        }else{
            if(pos<MAX_FIELD-1) campos[field][pos++]=c;
        }
    }
    campos[field][pos]='\0';
    return field+1;
}

static void remove_brackets(char *s) {
    size_t len=strlen(s);
    if(len>=2 && s[0]=='[' && s[len-1]==']') {
        memmove(s,s+1,len-2);
        s[len-2]='\0';
    }
    for(char *p=s;*p;p++) if(*p=='\'') *p=' ';
}

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
    char m3[8]={0}; int day=0; int year=0;
    if(sscanf(orig,"%3s %d, %d",m3,&day,&year)==3){
        snprintf(dest,32,"%02d/%s/%d",day,month_to_num(m3),year);
    }else{
        strncpy(dest,orig,31); dest[31]='\0';
    }
}

static void clean_list(char *s){
    char tmp[MAX_FIELD]; int j=0;
    for(int i=0;s[i];i++){
        if(s[i]==' ' && (i==0 || s[i-1]==',' || s[i+1]==',')) continue;
        if(s[i]==','){tmp[j++]=','; tmp[j++]=' ';} 
        else tmp[j++]=s[i];
    }
    tmp[j]='\0'; strcpy(s,tmp);
}

void parse_game(Game *g,const char *record){
    char fields[MAX_FIELDS][MAX_FIELD];
    for(int i=0;i<MAX_FIELDS;i++) fields[i][0]='\0';
    int n=split_csv(record,fields,MAX_FIELDS);

    strncpy(g->app_id,(n>0?fields[0]:""),63); g->app_id[63]='\0';
    strncpy(g->title,(n>1?fields[1]:""),511); g->title[511]='\0';
    format_date(g->date_added,(n>2?fields[2]:""));
    g->release_year=(n>3?atoi(fields[3]):0);
    g->price=(n>4?atof(fields[4]):0.0);
    strncpy(g->languages,(n>5?fields[5]:""),1023); remove_brackets(g->languages); clean_list(g->languages);
    g->metascore=(n>6?atoi(fields[6]):0);
    g->user_score=(n>7?atof(fields[7]):0.0);
    g->reviews=(n>8?atoi(fields[8]):0);
    strncpy(g->publishers,(n>9?fields[9]:""),511); remove_brackets(g->publishers);
    strncpy(g->developers,(n>10?fields[10]:""),511); remove_brackets(g->developers);
    strncpy(g->features,(n>11?fields[11]:""),1023); remove_brackets(g->features); clean_list(g->features);
    strncpy(g->genres,(n>12?fields[12]:""),511); remove_brackets(g->genres); clean_list(g->genres);
    strncpy(g->tags,(n>13?fields[13]:""),2047); remove_brackets(g->tags); clean_list(g->tags);
}

void print_game(Game *g){
    printf("=> %s ## %s ## %s ## %d ## %.2f ## [%s] ## %d ## %.1f ## %d ## [%s] ## [%s] ## [%s] ## [%s] ## [%s] ##\n",
        g->app_id,g->title,g->date_added,g->release_year,g->price,
        g->languages,g->metascore,g->user_score,g->reviews,
        g->publishers,g->developers,g->features,g->genres,g->tags);
}

// --- Main ---
int main(){
    FILE *f=fopen("/tmp/games.csv","r");
    if(!f){printf("Erro ao abrir CSV\n"); return 1;}
    char record[MAX_LINE];

    fgets(record,sizeof(record),f); 
    Game *games=malloc(sizeof(Game)*MAX_GAMES);
    size_t count=0;

    while(read_record(f,record,sizeof(record))){
        if(count>=MAX_GAMES) break;
        parse_game(&games[count++],record);
    }
    fclose(f);

    char input[128];
    while(fgets(input,sizeof(input),stdin)){
        rtrim(input);
        if(strcmp(input,"FIM")==0) break;
        int found=0;
        for(size_t i=0;i<count;i++){
            if(strcmp(games[i].app_id,input)==0){
                print_game(&games[i]);
                found=1; break;
            }
        }
        if(!found) printf("Game com ID \"%s\" não encontrado.\n",input);
    }

    free(games);
    return 0;
}
