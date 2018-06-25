package net.williamott.plasma.classes;


import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PodcastXmlParser {
    // We don't use namespaces
    private static final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    public Subscription parseSubscription(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readSubscriptionFeed(parser);
        } finally {
            in.close();
        }
    }

    private Subscription readSubscriptionFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        Subscription subscription = new Subscription();

        parser.require(XmlPullParser.START_TAG, ns, "rss");
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, ns, "channel");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("title")) {
                subscription.setTrackName(readSubscriptionTitle(parser));
            } else if (name.equals("pubDate")) {
                subscription.setReleaseDate(readSubscriptionPubDate(parser));
            } else if (name.equals("image")) {
                subscription.setArtworkUrl(readSubscriptionImage(parser));
            } else if (name.equals("itunes:author")) {
                subscription.setArtistName(readSubscriptionArtistName(parser));
            } else if (name.equals("atom:link")) {
                subscription.setFeedUrl(readSubscriptionFeedUrl(parser));
            } else {
                skip(parser);
            }
        }

        return subscription;
    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Episode> episodes = new ArrayList<Episode>();

        parser.require(XmlPullParser.START_TAG, ns, "rss");
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, ns, "channel");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("item")) {
                episodes.add(readItem(parser));
            } else {
                skip(parser);
            }
        }

        return episodes;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Episode readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        String title = "";
        String pubDate = "";
        String guid = "";
        String description = "";
        String url = "";
        String albumArtUrl = "";
        String author = "";

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "title":
                    title = readTitle(parser);
                    break;
                case "pubDate":
                    pubDate = readPubDate(parser);
                    break;
                case "guid":
                    guid = readGuid(parser);
                    break;
                case "description":
                    description = readDescription(parser);
                    break;
                case "enclosure":
                    url = readEnclosure(parser);
                    break;
                case "itunes:image":
                    albumArtUrl = readAlbumArtUrl(parser);
                    break;
                case "itunes:author":
                    author = readAuthor(parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        Episode episode = new Episode(title, pubDate, guid, description, url, albumArtUrl, author);
        return episode;
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    private String readSubscriptionTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    private String readSubscriptionPubDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        String pubDate = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        return pubDate;
    }

    private String readSubscriptionImage(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "image");
        String url = "";

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            switch (name) {
                case "url":
                    url = readSubscriptionImageUrl(parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        return url;
    }

    private String readSubscriptionImageUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "url");
        String imageUrl = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "url");
        return imageUrl;
    }

    private String readSubscriptionArtistName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "itunes:author");
        String artistName = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "itunes:author");
        return artistName;
    }

    private String readSubscriptionFeedUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "atom:link");
        String url =  parser.getAttributeValue(null, "href");
        parser.nextTag();
        return url;
    }

    private String readPubDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        String pubDate = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        return pubDate;
    }

    private String readGuid(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "guid");
        String guid = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "guid");
        return guid;
    }

    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return description;
    }

    private String readEnclosure(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "enclosure");
        String url =  parser.getAttributeValue(null, "url");
        parser.nextTag();
        return url;
    }

    private String readAlbumArtUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "itunes:image");
        String albumArtUrl = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "itunes:image");
        return albumArtUrl;
    }

    private String readAuthor(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "itunes:author");
        String author = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "itunes:author");
        return author;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
