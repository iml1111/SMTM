package im.iml.app.smtm;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class episodeActivity extends AppCompatActivity {
    String manga_title, manga_url;
    String domain = "https://mangashow.me";
    ArrayList<Episodelist> episode_list = null;
    ArrayList<Episodelist> download_list = null;
    MSMepsearch msMepsearch;
    Document doc;

    TextView top_result;
    ListView result_list;
    ImageView img_loading;
    Animation anim;
    EpisodelistAdapter ep_adapter;
    CheckBox allcheck;
    boolean isdownopen = false, itemchecked = false;
    Animation translateup, translatedown;
    LinearLayout down_layout;
    TextView adds;
    Button btn_down;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);
        top_result = findViewById(R.id.txt_result);
        result_list = findViewById(R.id.episode_list);
        img_loading = findViewById(R.id.img_android);
        anim = AnimationUtils.loadAnimation(this, R.anim.loading);
        img_loading.setAnimation(anim);
        down_layout = findViewById(R.id.download_layout);
        allcheck = findViewById(R.id.allcheck);
        translateup = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate_up);
        translatedown = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate_down);
        btn_down = findViewById(R.id.btn_down);
        AdView mAdView;
        mAdView = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent intent = getIntent();
        processIntent(intent);

        msMepsearch = new MSMepsearch();
        msMepsearch.execute(manga_url);
        allcheck.setOnClickListener(new CheckBox.OnClickListener(){
            @Override
            public void onClick(View v){
                if(allcheck.isChecked()){
                    setAllcheck(true);
                    if(itemchecked){return;}
                    else{
                        itemchecked = true;
                    }
                    down_layout.setVisibility(View.VISIBLE);
                    down_layout.startAnimation(translateup);
                }
                else{
                    setAllcheck(false);
                    if(!itemchecked){return;}
                    else{
                        itemchecked = false;
                    }
                    down_layout.startAnimation(translatedown);
                    down_layout.setVisibility(View.INVISIBLE);
                }
            }
        });
        result_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SparseBooleanArray checked = result_list.getCheckedItemPositions();
                boolean check = false;
                for(int i = 0;i<ep_adapter.getCount();i++){
                    if(checked.get(i)){
                        check = true;
                        break;
                    }
                }
                if(check == itemchecked){
                    return;
                }
                else if(check){
                    itemchecked = check;
                    down_layout.setVisibility(View.VISIBLE);
                    down_layout.startAnimation(translateup);
                }
                else{
                    itemchecked = check;
                    down_layout.startAnimation(translatedown);
                    down_layout.setVisibility(View.INVISIBLE);
                }
            }
        });

        btn_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = result_list.getCheckedItemPositions();
                download_list = new ArrayList<Episodelist>();

                for(int i = ep_adapter.getCount() - 1 ;i >=0 ;i--){
                    if(checked.get(i)){
                        download_list.add(episode_list.get(i));
                    }
                }

                Intent intent = new Intent(getApplicationContext(),DownloadActivity.class);
                intent.putParcelableArrayListExtra("list",download_list);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    public void setAllcheck(boolean value){
        for (int i=0; i<ep_adapter.getCount(); i++) {
            result_list.setItemChecked(i, value) ;
        }
    }

    public void processIntent(Intent intent){
        if(intent != null){
            Bundle bundle = intent.getExtras();
            manga_title = bundle.getString("title");
            manga_url = bundle.getString("url");
        }

    }

    public class MSMepsearch extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            img_loading.setImageResource(R.drawable.loading);
            img_loading.setVisibility(View.VISIBLE);
            if(episode_list == null) {
                episode_list = new ArrayList<>();
            }
        }
        @Override
        protected Void doInBackground(String... Params) {
            String useragent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36";
            try{
                doc = Jsoup.connect(Params[0])
                        .header("User-Agent", useragent).get();
            }catch (IOException e){
                publishProgress("error");
                return null;
            }

            try {
                Elements contents = doc.select("div.slot");
                for (Element con : contents) {
                    Elements obj = con.selectFirst("div.addedAt").selectFirst("div").select("i");
                    String title = con.selectFirst("a").text();
                    String url = con.selectFirst("a").attr("href");
                    String like = obj.eq(0).text();
                    if (url.startsWith("http") == false) {
                        url = domain + url;
                    }
                    Episodelist item = new Episodelist(title, url, "추천 수: " + like);
                    episode_list.add(item);
                }
            }
            catch (Exception e){
                publishProgress("error2");
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(String... Params){
            if(Params[0].equals("error")){
                Toast.makeText(getApplicationContext(),"URL 접근 실패. 문제가 지속될 경우, 관리자에 문의바랍니다.",Toast.LENGTH_LONG).show();
            }
            else if(Params[0].equals("error2")){
                Toast.makeText(getApplicationContext(),"페이지 구조 분석 실패. 관리자에게 문의바랍니다.",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            int total = 0;
            if(episode_list.size() == 0){
                Episodelist item = new Episodelist("만화가 없습니다.","not_result", "not_like");
                total = -1;
                episode_list.add(item);
            }
            ep_adapter = new EpisodelistAdapter(getApplicationContext(), R.layout.episode_list_item, episode_list);
            result_list.setAdapter(ep_adapter);
            ep_adapter.notifyDataSetChanged();
            total = total + episode_list.size();
            top_result.setText(manga_title + " [총 " + total  + "화]");
            img_loading.setImageBitmap(null);
        }

        @Override
        protected void onCancelled(){super.onCancelled();}
    }

    public void gotoMain(View v){
        finish();
    }
    @Override
    protected void  onDestroy(){
        super.onDestroy();
        msMepsearch.cancel(true);
    }

    private class SlidingPageAnimationListener implements Animation.AnimationListener{

        public void onAnimationEnd(Animation animation){
            if(isdownopen){
                isdownopen = false;
            }
            else{
                down_layout.bringToFront();
                isdownopen = true;
            }
        }
        public void onAnimationRepeat(Animation animation) { }
        public void onAnimationStart(Animation animation) { }
    }

}
