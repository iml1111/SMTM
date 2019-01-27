package im.iml.app.smtm;

import android.os.Parcel;
import android.os.Parcelable;

public class Episodelist implements Parcelable {
    private String title;
    private String url;
    private String like;

    public String getTitle(){return title;}
    public String getUrl(){return url;}
    public String getLike(){return like;}

    public Episodelist(String title, String url, String like){
        this.title = title;
        this.url = url;
        this.like = like;
    }

    public Episodelist(Parcel in){
        this.title = in.readString();
        this.url = in.readString();
        this.like = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString((this.title));
        dest.writeString((this.url));
        dest.writeString((this.like));
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        @Override
        public Episodelist createFromParcel(Parcel in){
            return new Episodelist(in);
        }

        @Override
        public Episodelist[] newArray(int size){
            return new Episodelist[size];
        }
    };
}
