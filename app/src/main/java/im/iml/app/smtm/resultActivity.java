package im.iml.app.smtm;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class resultActivity extends AppCompatActivity {
    String element = "";
    String domain = "https://mangashow.me";
    private String search_url = "https://mangashow.me/bbs/search.php?url=https%3A%2F%2Fmangashow.me%2Fbbs%2Fsearch.php&stx=";
    private int page = 0;
    Document doc;
    ArrayList<Mangalist> manga_list = null;
    boolean lastitemVisibleFlag = false;
    int listview_pos;

    ImageView img_loading;
    Animation anim;
    ListView result_list;
    MangalistAdapter ml_adapter;
    TextView top_result;
    MSMsearch msMsearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        result_list = findViewById(R.id.manga_list);
        top_result = findViewById(R.id.txt_result);
        img_loading = findViewById(R.id.img_android);
        anim = AnimationUtils.loadAnimation(this, R.anim.loading);
        img_loading.setAnimation(anim);
        AdView mAdView;
        mAdView = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent intent = getIntent();
        processIntent(intent);


        msMsearch = new MSMsearch();
        msMsearch.execute(search_url, element, Integer.toString(page));

        result_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (manga_list.get(position).getUrl() == "not_result") {
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), episodeActivity.class);
                intent.putExtra("title",manga_list.get(position).getTitle());
                intent.putExtra("url",manga_list.get(position).getUrl());
                startActivity(intent);
            }
        });
        result_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastitemVisibleFlag) {
                    if(manga_list.size() == 1 && manga_list.get(0).getUrl() == "not_result"){
                        return;
                    }
                    page++;
                    msMsearch = new MSMsearch();
                    msMsearch.execute(search_url, element, Integer.toString(page));
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
            }
        });

    }


    public void processIntent(Intent intent){
        if(intent != null){
            Bundle bundle = intent.getExtras();
            element = bundle.getString("element");
        }
    }

    public void gotoMain(View v){
        finish();
    }

    public class MSMsearch extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            img_loading.setImageResource(R.drawable.loading);
            img_loading.setVisibility(View.VISIBLE);
            if(manga_list == null) {
                manga_list = new ArrayList<>();
            }
            listview_pos = result_list.getFirstVisiblePosition();

        }

        @Override
        protected Void doInBackground(String... Params){
            String useragent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36";
            try {
                doc = Jsoup.connect(Params[0] + Params[1] + "&page=" + Params[2]).header("User-Agent", useragent).get();
            }
            catch (Exception e){
                publishProgress("error");
                return null;
            }

            try {
                Elements contents = doc.select("div.post-content.text-center");
                for (Element con : contents) {
                    Element manga_info = con.selectFirst("div.manga-subject");
                    String title = manga_info.text();
                    String link = manga_info.selectFirst("a").attr("href");
                    if (link.startsWith("http") == false) {
                        link = domain + link;
                    }
                    String tag = con.selectFirst("div.tags").text();
                    if (tag.trim().equals(null) || tag.trim().equals("")) {
                        tag = "없음";
                    }
                    Mangalist item = new Mangalist(title, link, "태그: " + tag);
                    manga_list.add(item);
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
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            if(manga_list.size() == 0){
                Mangalist item = new Mangalist("결과가 없습니다.","not_result", "no_result");
                manga_list.add(item);
            }
            ml_adapter = new MangalistAdapter(getApplicationContext(), R.layout.manga_list_item, manga_list);
            result_list.setAdapter(ml_adapter);
            ml_adapter.notifyDataSetChanged();

            top_result.setText("검색 결과: " + '"' + element  + '"');
            result_list.setSelection(listview_pos);
            img_loading.setImageBitmap(null);

        }

        @Override
        protected void onCancelled(){
            super.onCancelled();
        }

    }
    @Override
    protected void  onDestroy(){
        super.onDestroy();
        msMsearch.cancel(true);
    }

}
