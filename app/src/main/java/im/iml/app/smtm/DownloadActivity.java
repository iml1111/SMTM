package im.iml.app.smtm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

public class DownloadActivity extends AppCompatActivity {
    ArrayList<Episodelist> download_list = new ArrayList<Episodelist>();
    Document doc;
    MSMimgdownload msMimgdownload = null;
    String imghtml = null;
    Document tag;
    String savePath;
    boolean task_flag = false;
    Bitmap bitmap = null;
    InputStream in = null;
    NotificationManager notificationManager;
    String NOTIFICATION_CHANNEL_ID;
    @SuppressLint("WrongConstant")
    NotificationChannel notificationChannel;
    NotificationCompat.Builder notificationBuilder;

    WebView webView;
    TextView ctext, ttext, txt_result;
    ImageView img_loading, imageView;
    Animation anim;
    Button btn_cancel;
    ImageButton btn_back;
    ProgressBar cprogress,tprogress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        //
        imageView = findViewById(R.id.imageView);
        //
        ctext = findViewById(R.id.ctext);
        ttext = findViewById(R.id.ttext);
        cprogress = findViewById(R.id.cprogress);
        tprogress = findViewById(R.id.tprogress);
        btn_back = findViewById(R.id.imageButton);
        btn_cancel = findViewById(R.id.btn_cancel);
        txt_result = findViewById(R.id.txt_result);
        img_loading = findViewById(R.id.img_android);
        anim = AnimationUtils.loadAnimation(this, R.anim.loading);
        img_loading.setAnimation(anim);
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('body')[0].getElementsByClassName('view-content')[0].innerHTML);");
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavascriptInterface(), "Android");
        AdView mAdView;
        mAdView = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent intent = getIntent();
        processIntent(intent);

        tprogress.setMax(download_list.size());
        tprogress.setProgress(download_list.size());


        msMimgdownload = new MSMimgdownload();
        msMimgdownload.execute();
    }

    public class MyJavascriptInterface {

        @JavascriptInterface
        public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
            imghtml = "<div>" + html + "</div>";
        }
    }

    public class MSMimgdownload extends AsyncTask<String, String, Void> {
        @SuppressLint("WrongConstant")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            task_flag = true;
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
            Intent intent = new Intent(getApplicationContext(), DownloadActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi = PendingIntent
                    .getActivity(getApplicationContext(), 0, intent, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

                // Configure the notification channel.
                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
            notificationBuilder = new NotificationCompat
                    .Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notificationBuilder
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("다운로드 중...")
                        .setPriority(Notification.PRIORITY_MIN)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setContentIntent(pi);
            }
            notificationManager.notify(1, notificationBuilder.build());
        }
        @Override
        protected Void doInBackground(String... Params) {
            for(int i = 0; i< download_list.size();i++) {

                String title = download_list.get(i).getTitle();
                String url = download_list.get(i).getUrl();
                notificationBuilder.setContentTitle("다운로드 중...");
                notificationBuilder.setProgress(download_list.size(), i, false);
                notificationBuilder.setContentText("진행 상황: " + i + " / " + download_list.size());
                notificationBuilder.setAutoCancel(false);
                notificationManager.notify(1, notificationBuilder.build());
                if((!task_flag) || isCancelled()){return null;}
                publishProgress("web_Crawling",
                        title,
                        Integer.toString(i),
                        url);
                if((!task_flag) || isCancelled()){return null;}
                while (true) {
                    if((!task_flag) || isCancelled()){return null;}
                    try {
                        tag = Jsoup.parse(imghtml, "", Parser.xmlParser());
                        break;
                    } catch (NullPointerException e) {
                        continue;
                    } catch (Exception e){
                        publishProgress("error");
                        return null;
                    }
                }
                if((!task_flag) || isCancelled()){return null;}
                publishProgress("AnimStop");
                if((!task_flag) || isCancelled()){return null;}
                savePath = Environment.getExternalStorageDirectory().toString() + "/Download";
                savePath = savePath + "/" + title;
                try {
                    Elements imgtags = tag.select("img");
                    int cnt = 0, tag_length = 0;
                    for (Element imgtag : imgtags) {
                        tag_length++;
                    }
                    if ((!task_flag) || isCancelled()) {
                        return null;
                    }
                    for (Element imgtag : imgtags) {
                        String extend = ".jpg";
                        @SuppressLint("DefaultLocale") String cnt_format = String.format("_%03d", cnt + 1);
                        String localPath = savePath + "/" + title + cnt_format + extend;
                        publishProgress("progress",
                                localPath,
                                Integer.toString(cnt + 1), Integer.toString(tag_length));
                        if ((!task_flag) || isCancelled()) {
                            return null;
                        }
                        try {
                            in = new URL(imgtag.attr("src")).openStream();
                            bitmap = BitmapFactory.decodeStream(in);
                            in.close();
                            publishProgress("inputimg");

                            File dir = new File(savePath);
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            File fileCacheItem = new File(localPath);
                            OutputStream out = null;
                            fileCacheItem.createNewFile();
                            out = new FileOutputStream(fileCacheItem);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            if ((!task_flag) || isCancelled()) {
                                return null;
                            }
                            out.close();

                        } catch (IOException e) {
                            publishProgress("error3");
                            return null;
                        }
                        publishProgress("img_scan", localPath);
                        if ((!task_flag) || isCancelled()) {
                            return null;
                        }
                        cnt++;
                        imghtml = null;
                    }
                }catch (Exception e){
                    publishProgress("error2");
                    return null;
                }

            }

            return null;
        }
        @Override
        protected void onProgressUpdate(String... Params){
            if((!task_flag) || isCancelled()){return;}

            if(Params[0].equals("error")){
                Toast.makeText(getApplicationContext(),"URL 접근 실패. 문제가 지속될 경우, 관리자에 문의바랍니다.",Toast.LENGTH_LONG).show();
            }
            else if(Params[0].equals("error2")){
                Toast.makeText(getApplicationContext(),"페이지 구조 분석 실패. 관리자에게 문의바랍니다.",Toast.LENGTH_LONG).show();
            }
            else if(Params[0].equals("error3")){
                Toast.makeText(getApplicationContext(),"이미지 다운로드 실패. 관리자에게 문의바랍니다.",Toast.LENGTH_LONG).show();
            }
            else if(Params[0].equals("AnimStop")){
                img_loading.setImageBitmap(null);
                btn_back.setEnabled(true);
                btn_cancel.setEnabled(true);
            }
            else if(Params[0].equals("web_Crawling")){
                btn_back.setEnabled(false);
                btn_cancel.setEnabled(false);
                ctext.setText(Params[1]);
                cprogress.setProgress(0);
                ttext.setText("전체 진행도 " + Params[2] + " / " + download_list.size());
                tprogress.setProgress(Integer.parseInt(Params[2]));
                img_loading.setImageResource(R.drawable.loading);
                img_loading.setVisibility(View.VISIBLE);
                imghtml = null;
                if((!task_flag) || isCancelled()){return;}
                webView.loadUrl(Params[3]);
                if((!task_flag) || isCancelled()){return;}
            }
            else if(Params[0].equals("progress")){
                cprogress.setMax(Integer.parseInt(Params[3]));
                cprogress.setProgress(Integer.parseInt(Params[2]));

            }
            else if(Params[0].equals("inputimg")) {
                imageView.setImageBitmap(bitmap);
            }
            else if(Params[0].equals("img_scan")){
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File file = new File(Params[1]);
                if((!task_flag) || isCancelled()){return;}
                intent.setData(Uri.fromFile(file));
                sendBroadcast(intent);
            }
            else{

            }
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ctext.setText("현재 진행중인 만화 없음");
            ttext.setText("전체 진행도 " +download_list.size() + " / " + download_list.size());
            tprogress.setProgress(download_list.size());
            btn_cancel.setText("확인");
            txt_result.setText("다운로드 완료");
            Toast.makeText(getApplicationContext(),"만화 다운로드가 완료되었습니다!",Toast.LENGTH_SHORT).show();
            task_flag = false;
            notificationBuilder.setProgress(0, 0, false);
            notificationBuilder.setContentTitle("다운로드 완료");
            notificationBuilder.setContentText("다운로드가 완료되었습니다.");
            notificationBuilder.setAutoCancel(true);
            notificationManager.notify(/*notification id*/1, notificationBuilder.build());
        }

        @Override
        protected void onCancelled(){
            task_flag = false;
            Toast.makeText(getApplicationContext(),"다운로드가 취소되었습니다.",Toast.LENGTH_SHORT).show();
            notificationManager.cancel(1);
            finish();
            super.onCancelled();
        }
    }


    public void processIntent(Intent intent){
        if(intent != null){
            download_list = intent.getParcelableArrayListExtra("list");
        }
    }

    public void gotoMain(View v){
        task_flag = false;
        ctext.setText("취소 중...");
        ttext.setText("취소 중...");
        txt_result.setText("다운로드 취소 중...");
        try {

            if (msMimgdownload != null && msMimgdownload.getStatus() == AsyncTask.Status.RUNNING){
                msMimgdownload.cancel(true);
            }
            else{
                finish();
            }

        }
        catch (Exception e){ }
    }

    @Override
    protected void onDestroy(){
        notificationManager.cancel(1);
        try {
            if (msMimgdownload != null && msMimgdownload.getStatus() == AsyncTask.Status.RUNNING){
                msMimgdownload.cancel(true);
            }

        }
        catch (Exception e){ }
       super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        task_flag = false;
        ctext.setText("취소 중...");
        ttext.setText("취소 중...");
        txt_result.setText("다운로드 취소 중...");
        try {

            if (msMimgdownload != null && msMimgdownload.getStatus() == AsyncTask.Status.RUNNING){
                msMimgdownload.cancel(true);
            }
            else{
                super.onBackPressed();
            }

        }
        catch (Exception e){ }
    }

}
