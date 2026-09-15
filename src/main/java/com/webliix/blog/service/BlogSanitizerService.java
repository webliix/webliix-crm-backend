package com.webliix.blog.service;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class BlogSanitizerService {

    private final Safelist safelist;

    public BlogSanitizerService() {
        this.safelist = Safelist.relaxed()
                .addTags("figure", "figcaption", "video", "source", "iframe", "pre", "code", "table", "thead", "tbody", "tr", "th", "td", "hr", "mark", "u", "s")
                .addAttributes(":all", "class", "id", "style")
                .addAttributes("img", "src", "alt", "title", "width", "height", "loading", "srcset", "sizes")
                .addAttributes("video", "src", "poster", "controls", "autoplay", "loop", "muted", "width", "height")
                .addAttributes("source", "src", "type")
                .addAttributes("iframe", "src", "width", "height", "frameborder", "allowfullscreen", "allow")
                .addAttributes("a", "href", "title", "target", "rel")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https", "data")
                .addProtocols("video", "src", "http", "https")
                .addProtocols("iframe", "src", "https");
    }

    public String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.clean(html, safelist);
    }
}
