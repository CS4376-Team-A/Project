package SearchEngine.Models;

public class Index {
    public String url;
    public String title;
    public String description;
    public String keywords;

    public Index(String url, String title, String description, String keywords) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
    }
}
