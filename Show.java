import java.util.*;
import java.text.*;

public class Show {
    private String showId;
    private String type;
    private String title;
    private String[] director;
    private String[] cast;
    private String country;
    private Date dateAdded;
    private int releaseYear;
    private String rating;
    private String duration;
    private String[] listedIn;

    public Show() {
        super();
    }

    public Show(String showId, String type, String title, String[] director, String[] cast, String country,
                Date dateAdded, int releaseYear, String rating, String duration, String[] listedIn) {
        this.showId = showId;
        this.type = type;
        this.title = title;
        this.director = director;
        this.cast = cast;
        this.country = country;
        this.dateAdded = dateAdded;
        this.releaseYear = releaseYear;
        this.rating = rating;
        this.duration = duration;
        this.listedIn = listedIn;
    }


    public String getShowId() { return showId; }
    public void setShowId(String showId) { this.showId = showId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String[] getDirector() { return director; }
    public void setDirector(String[] director) { this.director = director; }

    public String[] getCast() { return cast; }
    public void setCast(String[] cast) { this.cast = cast; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Date getDateAdded() { return dateAdded; }
    public void setDateAdded(Date dateAdded) { this.dateAdded = dateAdded; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String[] getListedIn() { return listedIn; }
    public void setListedIn(String[] listedIn) { this.listedIn = listedIn; }

    // CLONE
    @Override
    public Show clone() {
        return new Show(
            this.showId,
            this.type,
            this.title,
            this.director != null ? this.director.clone() : null,
            this.cast != null ? this.cast.clone() : null,
            this.country,
            this.dateAdded != null ? (Date) this.dateAdded.clone() : null,
            this.releaseYear,
            this.rating,
            this.duration,
            this.listedIn != null ? this.listedIn.clone() : null
        );
    }

    // IMPRIMIR
    public void imprimir() {
        System.out.println("[" + showId + " ## " + type + " ## " + title + " ## " + String.join(", ", director) + " ## " + Arrays.toString(cast) + " ## " + country + " ## " + dateAdded + " ## " + releaseYear + " ## " + rating + " ## " + duration + " ## " + Arrays.toString(listedIn) + "]");
    }

    // LER (a partir de uma linha CSV já dividida por vírgula)
    public void ler(String linha) {
        try {
            String[] campos = linha.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Divide ignorando vírgulas entre aspas

            this.showId = campos[0].trim();
            this.type = campos[1].trim();
            this.title = campos[2].trim();
            this.director = campos[3].isEmpty() ? new String[]{} : campos[3].split(",\\s*");
            this.cast = campos[4].isEmpty() ? new String[]{} : campos[4].split(",\\s*");
            this.country = campos[5].trim().isEmpty() ? null : campos[5].trim();

            // Data
            if (!campos[6].trim().isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                this.dateAdded = sdf.parse(campos[6].trim());
            }

            this.releaseYear = Integer.parseInt(campos[7].trim());
            this.rating = campos[8].trim();
            this.duration = campos[9].trim();
            this.listedIn = campos[10].isEmpty() ? new String[]{} : campos[10].split(",\\s*");

        } catch (Exception e) {
            System.out.println("Erro ao ler linha: " + e.getMessage());
        }
    }
}



    
