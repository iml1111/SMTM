package im.iml.app.smtm;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText search_bar;
    private Button btn_search;
    private static final int MESSAGE_PERMISSION_GRANTED = 101;
    private static final int MESSAGE_PERMISSION_DENIED = 102;
    public MainHandler mainHandler = new MainHandler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        search_bar = findViewById(R.id.search_bar);
        MobileAds.initialize(this, getString(R.string.admob_id));
        AdView mAdView;
        mAdView = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        showPermissionDialog();


    }

    public void Searching(View v){
        if(search_bar.getText().toString().length() <= 1){
            Toast.makeText(this,"2글자 이상 입력해주세요!",Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = new Intent(this, resultActivity.class);
            intent.putExtra("element",search_bar.getText().toString());
            startActivity(intent);
        }
    }

    public void viewMenual(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SMTM Ver " +getString(R.string.app_ver));
        builder.setMessage("Hi, I'M IML!" +
                "\n다운받은 만화는 Download 폴더에 저장되며, 곧바로 갤러리 앱 등을 통해 확인할 수 있습니다." +
                "\n\n여러가지 버그로 인해서 제대로 작동하지 않을 수도 있습니다. 재실행시켜도 계속 문제시, 문의바랍니다.");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { }});
        builder.show();
    }

    public void showPermissionDialog(){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                mainHandler.sendEmptyMessage(MESSAGE_PERMISSION_GRANTED);
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                mainHandler.sendEmptyMessage(MESSAGE_PERMISSION_DENIED);
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    private class MainHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_PERMISSION_GRANTED:
                    break;
                case MESSAGE_PERMISSION_DENIED:
                    finish();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
