package jfxsearchengine.db;

public class Index {
	private String url;
	private String title;
    private String description;
    private String keywords;
    private String dateCreated;
    private int id;

    public Index(String url, String title, String description, String keywords) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
    }
    
    public Index(int id, String url, String title, String description, String keywords, String date) {
        this.id = id;
    	this.url = url;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.dateCreated = date;
    }
    
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
    
    public String toString() {
    	return "(\'"+this.url+"\', \'"+this.title+"\', \'"+this.description+"\', \'"+this.keywords+"\')";
    }
}
