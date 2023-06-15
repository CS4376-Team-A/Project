package SearchEngine.Services;

import SearchEngine.Interfaces.IScraperService;
import SearchEngine.Models.Index;
import SearchEngine.Repositories.IndexRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ScraperService implements IScraperService {
    private IndexRepository indexRepository;

    public ScraperService(IndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }
    public void scrape(String url, int maxDepth) {
        scrape(url, maxDepth, 0);
    }

    private void scrape(String url, int maxDepth, int currentDepth) {
        if (currentDepth > maxDepth) {
            return; // Terminate recursion if depth limit is reached
        }

        if (indexRepository.doesUrlExist(url) || !isIncludedUrl(url)) {
            return; // Skip if URL already exists or doesn't meet inclusion criteria
        }

        try {
            Document document = Jsoup.connect(url).get();
            String title = document.title();
            String description = extractMetaData(document, "description");
            String keywords = extractMetaData(document, "keywords");
            System.out.println("URL: " + url);
            System.out.println("Title: " + title);
            System.out.println("Description: " + description);
            System.out.println("Keywords: " + keywords);
            Index index = new Index(url, title, description, keywords);
            indexRepository.saveIndex(index);

            Elements linksOnPage = document.select("a[href]");

            for (Element page : linksOnPage) {
                String link = page.attr("abs:href");
                scrape(link, maxDepth, currentDepth + 1); // Recursively scrape child URLs with increased depth
            }
        } catch (IOException e) {
            System.err.println("For '" + url + "': " + e.getMessage());
        }
    }

    private boolean isIncludedUrl(String url) {
        return url.startsWith("http") || url.startsWith("https");
    }

    String extractMetaData(Document document, String property) {
        String tag = null;
        String cssQuery = "meta[property='og:" + property + "']";
        Elements elements = document.select(cssQuery);

        if (elements != null && elements.size() >= 1) {
            tag = elements.first().attr("content");
        }
        return tag;
    }
}
