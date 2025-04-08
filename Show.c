#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

struct Show {
    char showId[100];
    char type[100];
    char title[100];
    char director[10][100];
    char cast[10][100];
    char country[100];
    struct tm dateAdded;
    int releaseYear;
    char rating[100];
    char duration[100];
    char listedIn[10][100];
};

void show_init(struct Show *show) {
    memset(show, 0, sizeof(struct Show));
}

void show_setShowId(struct Show *show, const char *showId) {
    strncpy(show->showId, showId, sizeof(show->showId) - 1);
}

void show_setType(struct Show *show, const char *type) {
    strncpy(show->type, type, sizeof(show->type) - 1);
}

void show_setTitle(struct Show *show, const char *title) {
    strncpy(show->title, title, sizeof(show->title) - 1);
}

void show_setDirector(struct Show *show, const char *director[], int count) {
    for (int i = 0; i < count; i++) {
        strncpy(show->director[i], director[i], sizeof(show->director[i]) - 1);
    }
}

void show_setCast(struct Show *show, const char *cast[], int count) {
    for (int i = 0; i < count; i++) {
        strncpy(show->cast[i], cast[i], sizeof(show->cast[i]) - 1);
    }
}

void show_setCountry(struct Show *show, const char *country) {
    strncpy(show->country, country, sizeof(show->country) - 1);
}

void show_setDateAdded(struct Show *show, int year, int month, int day) {
    memset(&show->dateAdded, 0, sizeof(struct tm));
    show->dateAdded.tm_year = year - 1900;
    show->dateAdded.tm_mon = month - 1;
    show->dateAdded.tm_mday = day;
}

void show_setReleaseYear(struct Show *show, int releaseYear) {
    show->releaseYear = releaseYear;
}

void show_setRating(struct Show *show, const char *rating) {
    strncpy(show->rating, rating, sizeof(show->rating) - 1);
}

void show_setDuration(struct Show *show, const char *duration) {
    strncpy(show->duration, duration, sizeof(show->duration) - 1);
}

void show_setListedIn(struct Show *show, const char *listedIn[], int count) {
    for (int i = 0; i < count; i++) {
        strncpy(show->listedIn[i], listedIn[i], sizeof(show->listedIn[i]) - 1);
    }
}

void show_print(const struct Show *show) {
    printf("[%s ## %s ## %s ## ", show->showId, show->type, show->title);
    for (int i = 0; i < 10; i++) {
        if (show->director[i][0] != '\0') {
            printf("%s, ", show->director[i]);
        }
    }
    printf("## ");
    for (int i = 0; i < 10; i++) {
        if (show->cast[i][0] != '\0') {
            printf("%s, ", show->cast[i]);
        }
    }
    printf("## %s ## %d-%02d-%02d ## %d ## %s ## %s ## ", show->country, show->dateAdded.tm_year + 1900,
           show->dateAdded.tm_mon + 1, show->dateAdded.tm_mday, show->releaseYear, show->rating, show->duration);
    for (int i = 0; i < 10; i++) {
        if (show->listedIn[i][0] != '\0') {
            printf("%s, ", show->listedIn[i]);
        }
    }
    printf("]\n");
}

void show_read(struct Show *show, const char *line) {
    char *lineCopy = strdup(line);
    char *token = strtok(lineCopy, ",");
    int fieldIndex = 0;

    while (token != NULL) {
        switch (fieldIndex) {
            case 0:
                show_setShowId(show, token);
                break;
            case 1:
                show_setType(show, token);
                break;
            case 2:
                show_setTitle(show, token);
                break;
            case 3: {
                char *directorTokens[10];
                int directorCount = 0;
                char *directorToken = strtok(token, ",");
                while (directorToken != NULL) {
                    directorTokens[directorCount++] = directorToken;
                    directorToken = strtok(NULL, ",");
                }
                show_setDirector(show, (const char **)directorTokens, directorCount);
                break;
            }
            case 4: {
                char *castTokens[10];
                int castCount = 0;
                char *castToken = strtok(token, ",");
                while (castToken != NULL) {
                    castTokens[castCount++] = castToken;
                    castToken = strtok(NULL, ",");
                }
                show_setCast(show, (const char **)castTokens, castCount);
                break;
            }
            case 5:
                show_setCountry(show, token);
                break;
            case 6: {
                struct tm dateAdded;
                memset(&dateAdded, 0, sizeof(struct tm));
                strptime(token, "%B %d, %Y", &dateAdded);
                show_setDateAdded(show, dateAdded.tm_year + 1900, dateAdded.tm_mon + 1, dateAdded.tm_mday);
                break;
            }
            case 7:
                show_setReleaseYear(show, atoi(token));
                break;
            case 8:
                show_setRating(show, token);
                break;
            case 9:
                show_setDuration(show, token);
                break;
            case 10: {
                char *listedInTokens[10];
                int listedInCount = 0;
                char *listedInToken = strtok(token, ",");
                while (listedInToken != NULL) {
                    listedInTokens[listedInCount++] = listedInToken;
                    listedInToken = strtok(NULL, ",");
                }
                show_setListedIn(show, (const char **)listedInTokens, listedInCount);
                break;
            }
        }

        token = strtok(NULL, ",");
        fieldIndex++;
    }

    free(lineCopy);
}

int main() {
    struct Show show;
    show_init(&show);

    show_setShowId(&show, "123");
    show_setType(&show, "Movie");
    show_setTitle(&show, "The Matrix");
    const char *directors[] = {"Lana Wachowski", "Lilly Wachowski"};
    show_setDirector(&show, directors, 2);
    const char *cast[] = {"Keanu Reeves", "Carrie-Anne Moss"};
    show_setCast(&show, cast, 2);
    show_setCountry(&show, "USA");
    show_setDateAdded(&show, 1999, 3, 31);
    show_setReleaseYear(&show, 1999);
    show_setRating(&show, "R");
    show_setDuration(&show, "2h 16m");
    const char *listedIn[] = {"Action", "Sci-Fi"};
    show_setListedIn(&show, listedIn, 2);

    show_print(&show);

    return 0;
}
