import java.util.Date;

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

    public Show(String showId, String type, String title, String[] director, String[] cast, String country, Date dateAdded, int releaseYear, String rating, String duration, String[] listedIn) {
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

    public String getShowId() {
        return showId;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getDirector() {
        return director;
    }

    public void setDirector(String[] director) {
        this.director = director;
    }

    public String[] getCast() {
        return cast;
    }

    public void setCast(String[] cast) {
        this.cast = cast;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String[] getListedIn() {
        return listedIn;
    }

    public void setListedIn(String[] listedIn) {
        this.listedIn = listedIn;
    }

    @Override
    public Show clone() {
        Show clonedShow = new Show();
        clonedShow.showId = this.showId;
        clonedShow.type = this.type;
        clonedShow.title = this.title;
        clonedShow.director = this.director;
        clonedShow.cast = this.cast;
        clonedShow.country = this.country;
        clonedShow.dateAdded = this.dateAdded;
        clonedShow.releaseYear = this.releaseYear;
        clonedShow.rating = this.rating;
        clonedShow.duration = this.duration;
        clonedShow.listedIn = this.listedIn;
        return clonedShow;
    }

    public void imprimir() {
        System.out.println("[" + showId + " ## " + type + " ## " + title + " ## " + String.join(", ", director) + " ## " + Arrays.toString(cast) + " ## " + country + " ## " + dateAdded + " ## " + releaseYear + " ## " + rating + " ## " + duration + " ## " + Arrays.toString(listedIn) + "]");
    }
}
    
