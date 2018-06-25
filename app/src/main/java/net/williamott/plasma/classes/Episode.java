package net.williamott.plasma.classes;

public class Episode {
    private int id;
    private String url;
    private String author;
    private String description;
    private String guid;
    private String pubDate;
    private String title;
    private String albumArtUrl;
    private String subscriptionId;
    private String subscriptionTitle;
    private int currentPosition;
    private int userId;
    private int isNew;
    private String downloadLocation;
    private String subscriptionTrackTitle;

    public Episode() {
    }

    public Episode(String title, String pubDate, String guid, String description, String url, String albumArtUrl, String author) {
        this.url = url;
        this.author = author;
        this.description = description;
        this.guid = guid;
        this.pubDate = pubDate;
        this.title = title;
        this.albumArtUrl = albumArtUrl;
    }

    public Episode(String url, String author, String description, String guid, String pubDate, String title, String albumArtUrl, String subscriptionId, String subscriptionTitle, int currentPosition, int userId, int isNew, String downloadLocation, String subscriptionTrackTitle) {
        this.url = url;
        this.author = author;
        this.description = description;
        this.guid = guid;
        this.pubDate = pubDate;
        this.title = title;
        this.albumArtUrl = albumArtUrl;
        this.subscriptionId = subscriptionId;
        this.subscriptionTitle = subscriptionTitle;
        this.currentPosition = currentPosition;
        this.userId = userId;
        this.isNew = isNew;
        this.downloadLocation = downloadLocation;
        this.subscriptionTrackTitle = subscriptionTrackTitle;
    }

    public Episode(int id, String url, String author, String description, String guid, String pubDate, String title, String albumArtUrl, String subscriptionId, String subscriptionTitle, int currentPosition, int userId, int isNew, String downloadLocation, String subscriptionTrackTitle) {
        this.id = id;
        this.url = url;
        this.author = author;
        this.description = description;
        this.guid = guid;
        this.pubDate = pubDate;
        this.title = title;
        this.albumArtUrl = albumArtUrl;
        this.subscriptionId = subscriptionId;
        this.subscriptionTitle = subscriptionTitle;
        this.currentPosition = currentPosition;
        this.userId = userId;
        this.isNew = isNew;
        this.downloadLocation = downloadLocation;
        this.subscriptionTrackTitle = subscriptionTrackTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }

    public void setAlbumArtUrl(String albumArtUrl) {
        this.albumArtUrl = albumArtUrl;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getSubscriptionTitle() {
        return subscriptionTitle;
    }

    public void setSubscriptionTitle(String subscriptionTitle) {
        this.subscriptionTitle = subscriptionTitle;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getIsNew() {
        return isNew;
    }

    public void setIsNew(int isNew) {
        this.isNew = isNew;
    }

    public String getDownloadLocation() {
        return downloadLocation;
    }

    public void setDownloadLocation(String downloadLocation) {
        this.downloadLocation = downloadLocation;
    }

    public String getSubscriptionTrackTitle() {
        return subscriptionTrackTitle;
    }

    public void setSubscriptionTrackTitle(String subscriptionTrackTitle) {
        this.subscriptionTrackTitle = subscriptionTrackTitle;
    }
}
