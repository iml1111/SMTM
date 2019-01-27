package im.iml.app.smtm;

public class Mangalist {
    private String title;
    private String url;
    private String tags;

    public String getTitle(){return title;}
    public String getUrl(){return url;}
    public String getTags(){return tags;}

    public Mangalist(String title, String url, String tags){
        this.title = title;
        this.url = url;
        this.tags = tags;
    }
}
