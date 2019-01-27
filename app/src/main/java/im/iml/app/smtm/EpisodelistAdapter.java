package im.iml.app.smtm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;

public class EpisodelistAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<Episodelist> data;
    private int layout;

    public EpisodelistAdapter(Context context, int layout, ArrayList<Episodelist> data){
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
        this.layout = layout;
    }

    @Override
    public int getCount(){return data.size();}

    @Override
    public String getItem(int position){
        return data.get(position).getTitle();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false);
        }
        Episodelist item = data.get(position);
        TextView title = convertView.findViewById(R.id.title);
        title.setText(item.getTitle());
        TextView like = convertView.findViewById(R.id.like);
        like.setText(item.getLike());

        return convertView;
    }

}
