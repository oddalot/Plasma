package net.williamott.plasma.classes;

public class Subscription {
    private int id;
    private int trackId;
    private String trackName;
    private String releaseDate;
    private String artworkUrl;
    private String artistName;
    private String feedUrl;
    private int userId;
    private int newEpisodeCount;

    public Subscription() {
    }

    public Subscription(int trackId, String trackName, String releaseDate, String artworkUrl, String artistName, String feedUrl, int userId, int newEpisodeCount) {
        this.trackId = trackId;
        this.trackName = trackName;
        this.releaseDate = releaseDate;
        this.artworkUrl = artworkUrl;
        this.artistName = artistName;
        this.feedUrl = feedUrl;
        this.userId = userId;
        this.newEpisodeCount = newEpisodeCount;
    }

    public Subscription(int id, int trackId, String trackName, String releaseDate, String artworkUrl, String artistName, String feedUrl, int userId, int newEpisodeCount) {
        this.id = id;
        this.trackId = trackId;
        this.trackName = trackName;
        this.releaseDate = releaseDate;
        this.artworkUrl = artworkUrl;
        this.artistName = artistName;
        this.feedUrl = feedUrl;
        this.userId = userId;
        this.newEpisodeCount = newEpisodeCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getNewEpisodeCount() {
        return newEpisodeCount;
    }

    public void setNewEpisodeCount(int newEpisodeCount) {
        this.newEpisodeCount = newEpisodeCount;
    }
}
