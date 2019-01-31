package com.katamapps.flashoncall.activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.katamapps.flashoncall.R;
import com.katamapps.flashoncall.adapter.KatamApps_MoreApp_Adapter;
import com.katamapps.flashoncall.classes.Apps_Details;
import com.katamapps.flashoncall.classes.ConnectionDetector;
import com.katamapps.flashoncall.classes.Model_Class;
import com.katamapps.flashoncall.classes.Utils;
import com.katamapps.flashoncall.dialog.CallSettingDialog;
import com.katamapps.flashoncall.dialog.SmsSettingDialog;
import com.katamapps.flashoncall.models.SharePreferenceUtils;
import com.katamapps.flashoncall.recycler_click_listener.ClickListener;
import com.katamapps.flashoncall.recycler_click_listener.RecyclerTouchListener;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;


/**
 * Created by sridhar_v on 12/21/2017.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, RewardedVideoAdListener {


    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private boolean checked = false, checked1 = false;

    private int screen_width, screen_height;
    private SharePreferenceUtils sharePreferenceUtils;
    private RelativeLayout rlCallOnOff, rlSMSOnOff, rlNotificationOnOff;
    private SwitchCompat ivCallOnOff, ivSMSOnOff;
    private Dialog dialog;


    private SharedPreferences preferences;
    private String key;
    private boolean value;

    private ConnectionDetector cd;
    private boolean isInternetPresent;
    private AdView adView;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;


    public static final int RequestPermissionCode = 1;
    private boolean permission = false;

    public static boolean front = false;

    //own ads
    //the URL having the json data
    private static final String JSON_URL = "https://github.com/VattipalliSridhar/News/raw/master/ownads/ownads_link.json";
    //the hero list where we will store all the hero objects after parsing json
    private List<Model_Class> ownads_list, exit_list;
    public static List<Model_Class> more_app_list;
    private RecyclerView more_app_recycler_view;

    //exit screen
    private Dialog customDialog;
    private int dwidth, dheight, dialogWidth, dialogHeight;
    private RelativeLayout exit_top_layout, exit_image_layout, exit_exit_layout, exit_main_layout;
    private ImageView frstImage, secndImage, thirdImage, fourImage, fiveImage, sixImage;
    private TextView frstText, secndText, thirdText, fourText;
    private Button exit_close_button, exit_button;
    private Animation animation;
    private int animation_xml[] = {R.anim.wobble, R.anim.wobble_zoom, R.anim.zoom_in_out, R.anim.exit_anim};
    private Random random = new Random();
    private ArrayList<String> packagesList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        screen_width = this.getResources().getDisplayMetrics().widthPixels;
        screen_height = this.getResources().getDisplayMetrics().heightPixels;

        sharePreferenceUtils = SharePreferenceUtils.getInstance(MainActivity.this);
        if (getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(MainActivity.this, "There is Flash light in your moblie", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "There is no Flash light in your moblie", Toast.LENGTH_LONG).show();
        }

        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {

            if (checkPermission()) {
                //Toast.makeText(MainActivity.this, "All Permissions Granted Successfully", Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }

        } else {


        }
        getSmsApp();

        init_view();
        cd = new ConnectionDetector(this);
        isInternetPresent = cd.isConnectingToInternet();
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);


        more_app_recycler_view = (RecyclerView) findViewById(R.id.more_app_recycler_view);
        LinearLayoutManager mLManager_efct1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        more_app_recycler_view.setLayoutManager(mLManager_efct1);
        more_app_recycler_view.setHasFixedSize(true);

        if (isInternetPresent) {
            ownads_list = new ArrayList<>();
            ownads_list.clear();

            more_app_list = new ArrayList<>();
            more_app_list.clear();

            exit_list = new ArrayList<>();
            exit_list.clear();

            packagesList.clear();
            loadRewardedVideoAd();
            displayAd();
            loadAdsList();


        } else {

        }

        more_app_recycler_view.addOnItemTouchListener(new RecyclerTouchListener(MainActivity.this,
                more_app_recycler_view, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                if (more_app_list != null) {

                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(more_app_list.get(position).getApp_url())));
                    } catch (ActivityNotFoundException e) {
                        String packageName;
                        packageName = more_app_list.get(position).getApp_url().substring(more_app_list.get(position).getApp_url().lastIndexOf('=') + 1);
                        Log.e("TAG", "" + packageName);
                        viewInBrowser(MainActivity.this, "https://play.google.com/store/apps/details?id=" + packageName);
                    }
                } else {

                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


        if (isNotificationServiceEnabled()) {
            preferences.edit().putBoolean("toggle_on_off", preferences.getBoolean("toggle_on_off", true)).commit();
            ivSMSOnOff.setChecked(true);
            sharePreferenceUtils.setSMS(true);
        } else {
            ivSMSOnOff.setChecked(false);
            sharePreferenceUtils.setSMS(false);
        }

        if (hasFlash()) {

            front = true;

            Log.e("ms","f ok");

        } else {

            front = false;
            Log.e("ms","f no");
        }


        Utils.installedApps = getInstalledApps(MainActivity.this);


        if (Utils.installedApps.size() > 0) {
            for (int i = 0; i < Utils.installedApps.size(); i++) {

                key = Utils.installedApps.get(i).getPkg();
                value = Utils.installedApps.get(i).isOnOff();
                preferences.edit().putBoolean(key, value).commit();

            }
        }


    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getString(R.string.rewarded_id),
                new AdRequest.Builder().build());
    }

    private void viewInBrowser(MainActivity mainActivity, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (null != intent.resolveActivity(mainActivity.getPackageManager())) {
            mainActivity.startActivity(intent);
        }
    }


    public SharePreferenceUtils getSharePreferenceUtils() {
        if (this.sharePreferenceUtils != null) {
            return this.sharePreferenceUtils;
        }
        return SharePreferenceUtils.getInstance(this);
    }

    private void init_view() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        rlCallOnOff = (RelativeLayout) findViewById(R.id.rlCallOnOff);
        rlSMSOnOff = (RelativeLayout) findViewById(R.id.rlSMSOnOff);

        rlNotificationOnOff = (RelativeLayout) findViewById(R.id.rlNotificationOnOff);
        rlCallOnOff.setOnClickListener(this);
        rlSMSOnOff.setOnClickListener(this);
        rlNotificationOnOff.setOnClickListener(this);


        ivCallOnOff = (SwitchCompat) findViewById(R.id.ivCallOnOff);
        ivCallOnOff.setOnCheckedChangeListener(this);
        ivCallOnOff.setChecked(this.sharePreferenceUtils.isCall());

        ivSMSOnOff = (SwitchCompat) findViewById(R.id.ivSMSOnOff);
        ivSMSOnOff.setOnCheckedChangeListener(this);
        ivSMSOnOff.setChecked(sharePreferenceUtils.isSMS());


        this.sharePreferenceUtils.getCallOnLength();
        this.sharePreferenceUtils.getCallOffLength();
        this.sharePreferenceUtils.getSMSOnLength();
        this.sharePreferenceUtils.getSMSOffLength();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlCallOnOff:
                if (dialog == null || !dialog.isShowing()) {
                    dialog = new CallSettingDialog(MainActivity.this, 16973937);
                    dialog.show();

                }
                break;

            case R.id.rlSMSOnOff:

                if (dialog == null || !dialog.isShowing()) {
                    dialog = new SmsSettingDialog(MainActivity.this, 16973937);
                    dialog.show();

                }

                break;

            case R.id.rlNotificationOnOff:


                if (isNotificationServiceEnabled()) {
                    sharePreferenceUtils.setSMS(true);
                    preferences.edit().putBoolean("toggle_on_off", preferences.getBoolean("toggle_on_off", true)).commit();
                    startActivity(new Intent(MainActivity.this, Notification_Setting.class));
                    if (isInternetPresent) {
                        if (mRewardedVideoAd.isLoaded()) {
                            mRewardedVideoAd.show();
                        }
                    }

                } else {
                    sharePreferenceUtils.setSMS(false);
                    displayalert1();
                }

                break;

        }

    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.ivSMSOnOff:
                if (isChecked) {

                    if (isNotificationServiceEnabled()) {
                        sharePreferenceUtils.setSMS(true);
                    } else {
                        sharePreferenceUtils.setSMS(false);
                        displayalert();
                    }

                } else {
                    sharePreferenceUtils.setSMS(false);

                }

                break;
            case R.id.ivCallOnOff:
                if (isChecked) {
                    sharePreferenceUtils.setCall(true);
                } else {
                    sharePreferenceUtils.setCall(false);


                }
                break;
        }
    }


    private List<Apps_Details> getInstalledApps(MainActivity mainActivity) {
        List<Apps_Details> res = new ArrayList<Apps_Details>();
        res.clear();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        Intent intent = new Intent(Intent.ACTION_MAIN, null);

        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        List<ResolveInfo> resolveInfoList = MainActivity.this.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfoList) {

            ActivityInfo activityInfo = resolveInfo.activityInfo;

            if (isSystemPackage(resolveInfo)) {


                String appName = activityInfo.applicationInfo.loadLabel(getPackageManager()).toString();

                Drawable icon = activityInfo.applicationInfo.loadIcon(getPackageManager());

                Apps_Details list_APPS = new Apps_Details(appName, icon);
                String key = activityInfo.packageName;
                boolean value = preferences.getBoolean(key, true);
                list_APPS.setOnOff(value);
                list_APPS.setPkg(key);
                if (!key.startsWith("com.android") && !list_APPS.getName().equals(smsPackagename) && !list_APPS.getPkg().equals(getPackageName()) && !list_APPS.getName().equals(getPackageName())) {

                    res.add(list_APPS);
                }
            }

            if (!isSystemPackage(resolveInfo)) {


                String appName = activityInfo.applicationInfo.loadLabel(getPackageManager()).toString();

                Drawable icon = activityInfo.applicationInfo.loadIcon(getPackageManager());

                Apps_Details list_APPS = new Apps_Details(appName, icon);
                String key = activityInfo.packageName;
                boolean value = preferences.getBoolean(key, true);
                list_APPS.setOnOff(value);
                list_APPS.setPkg(key);
                if (!key.startsWith("com.android") && !list_APPS.getPkg().equals(smsPackagename) && !list_APPS.getPkg().equals(getPackageName()) && !list_APPS.getName().equals(getPackageName())) {

                    res.add(list_APPS);
                }
            }


        }

        Collections.sort(res, new MainActivity.Compares());
        return res;
    }

    public boolean isSystemPackage(ResolveInfo resolveInfo) {

        return ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }


    private class Compares implements Comparator<Apps_Details> {
        @Override
        public int compare(Apps_Details o1, Apps_Details o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }


    public static String smsPackagename;

    private void getSmsApp() {
        String defaultSmsPackage;
        if (Build.VERSION.SDK_INT >= 19) {
            defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(MainActivity.this);
        } else {
            defaultSmsPackage = Settings.Secure.getString(getContentResolver(), "sms_default_application");
            PackageManager packageManager = getApplicationContext().getPackageManager();
            defaultSmsPackage = packageManager.resolveActivity(packageManager.getLaunchIntentForPackage(defaultSmsPackage), 0).activityInfo.packageName;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ");
        stringBuilder.append(defaultSmsPackage);
        Log.e("TAG", stringBuilder.toString());
        if (defaultSmsPackage != null) {
            try {
                ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(defaultSmsPackage, 0);
                smsPackagename = applicationInfo.loadLabel(getPackageManager()).toString();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        Log.e("msg3", "  " + smsPackagename);
    }


    private void displayalert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle((CharSequence) "Give Notification Access!!");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Give Notification access to the '");
        stringBuilder.append(getResources().getString(R.string.app_name));
        stringBuilder.append("' first in order to enable flashes for sms.");
        builder.setMessage(stringBuilder.toString());
        builder.setCancelable(false);
        builder.setNegativeButton((CharSequence) "NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                sharePreferenceUtils.setSMS(false);
                ivSMSOnOff.setChecked(false);
                //MainActivity.this.startActivity(new Intent(MainActivity.this, MainActivity.class));
                dialogInterface.cancel();
                //MainActivity.this.finish();

            }
        });
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                checked = true;
                ivSMSOnOff.setChecked(true);
                sharePreferenceUtils.setSMS(true);
            }
        });
        builder.show();
    }

    private void displayalert1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle((CharSequence) "Give Notification Access!!");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Give Notification access to the '");
        stringBuilder.append(getResources().getString(R.string.app_name));
        stringBuilder.append("' first in order to enable flashes for WhatsApp and Facebook and other applications notification.");
        builder.setMessage(stringBuilder.toString());
        builder.setCancelable(false);
        builder.setNegativeButton((CharSequence) "NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                sharePreferenceUtils.setSMS(false);
                //MainActivity.this.startActivity(new Intent(MainActivity.this, MainActivity.class));
                dialogInterface.cancel();
                //MainActivity.this.finish();

            }
        });
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                checked1 = true;
                sharePreferenceUtils.setSMS(true);
            }
        });
        builder.show();
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {

                        CAMERA,
                        READ_PHONE_STATE,
                }, RequestPermissionCode);

    }

    public boolean checkPermission() {

        int ThiredPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int FivethPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);

        return
                ThiredPermissionResult == PackageManager.PERMISSION_GRANTED &&
                        FivethPermissionResult == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean Camera_Permission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean READ_PHONE_STATE_Permission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (Camera_Permission && READ_PHONE_STATE_Permission) {
                        //Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        //Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        permssiondialog();
                    }
                }
                break;
        }
    }


    private void permssiondialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle("App requires Storage permissions to work perfectly..!");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                permission = true;
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Exit",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.more) {
            if (isInternetPresent) {


                try {
                    Intent localIntent = new Intent("android.intent.action.VIEW");
                    localIntent.setData(Uri
                            .parse("market://search?q=pub:katamapps"));
                    MainActivity.this.startActivity(localIntent);
                } catch (Exception e) {
                    viewInBrowser(MainActivity.this, "https://play.google.com/store/apps/developer?id=katamapps");
                }

            } else
                No_Internet_Dialouge();
            return true;
        }
        if (id == R.id.like) {
            if (isInternetPresent) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + getPackageName())));
                } catch (Exception e) {
                    viewInBrowser(MainActivity.this, "https://play.google.com/store/apps/details?id=" + getPackageName());
                }

            } else
                No_Internet_Dialouge();
            return true;
        }
        if (id == R.id.privacy_action) {
            startActivity(new Intent(MainActivity.this, PrivacyActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void No_Internet_Dialouge() {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(
                MainActivity.this);
        mBuilder.setMessage("Sorry No Internet Connection please try again later");
        mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        mBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        mBuilder.create();
        mBuilder.show();

    }


    private void loadAdsList() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);

                            //we have the array named hero inside the object
                            //so here we are getting that json array
                            JSONArray heroArray = obj.getJSONArray("sample");

                            //now looping through all the elements of the json array
                            for (int i = 0; i < heroArray.length(); i++) {
                                //getting the json object of the particular index inside the array
                                JSONObject ownAdsObject = heroArray.getJSONObject(i);

                                //creating a hero object and giving them the values from json object
                                Model_Class modelClass = new Model_Class(ownAdsObject.getString("app_name"), ownAdsObject.getString("app_url"), ownAdsObject.getString("app_icon"));

                                //adding the hero to herolist
                                ownads_list.add(modelClass);
                            }
                            Log.e("msg", "" + ownads_list.size());
                            // Toast.makeText(MainActivity.this,"Ads Loaded",Toast.LENGTH_SHORT).show();

                            if (ownads_list.size() > 0) {
                                ArrayList<Model_Class> tempList = new ArrayList<Model_Class>();

                                packagesList = getInstalledApps();
                                for (int i = 0; i < ownads_list.size(); i++) {
                                    String appname = ownads_list.get(i).getApp_url();
                                    String[] names = appname.split("=");

                                    appname = names[1];

                                    if (!packagesList.contains(appname)) {
                                        tempList.add(ownads_list.get(i));

                                    }
                                }

                                exit_list.clear();
                                exit_list.addAll(tempList);

                                more_app_list.clear();
                                more_app_list.addAll(tempList);
                            }
                            KatamApps_MoreApp_Adapter moreAppsadapter = new KatamApps_MoreApp_Adapter(MainActivity.this, more_app_list);
                            more_app_recycler_view.setAdapter(moreAppsadapter);

                            RelativeLayout more_apps_lay_out = (RelativeLayout) findViewById(R.id.more_apps_lay_out);
                            more_apps_lay_out.setVisibility(View.VISIBLE);

                            more_app_recycler_view.setVisibility(View.VISIBLE);
                            TextView text_ads = (TextView) findViewById(R.id.text_ads);
                            text_ads.setVisibility(View.VISIBLE);
                            text_ads.setText("KatamApps More Apps [Ads]");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);


    }

    private ArrayList<String> getInstalledApps() {
        ArrayList<String> packList = new ArrayList<String>();

        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if ((!isSystemPackage(p))) {
                String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
                String packgname = p.packageName;
                Log.e("       PackageName   ", " info  " + packgname);
                packList.add(packgname);
            }
        }
        return packList;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    private void exit_screen() {
        animation = AnimationUtils.loadAnimation(getApplicationContext(), animation_xml[random.nextInt(4)]);
        //animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.exit_anim);
        customDialog = new Dialog(MainActivity.this);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.exit_screen);

        dwidth = getResources().getDisplayMetrics().widthPixels;
        dheight = getResources().getDisplayMetrics().heightPixels;

        exit_main_layout = (RelativeLayout) customDialog.findViewById(R.id.exit_main_layout);
        exit_main_layout.getLayoutParams().height = (dheight * 75) / 100;
        exit_main_layout.getLayoutParams().width = (dwidth - (dwidth / 10));

        exit_top_layout = (RelativeLayout) customDialog.findViewById(R.id.exit_top_layout);
        exit_top_layout.getLayoutParams().height = (dheight * 10) / 100;

        exit_image_layout = (RelativeLayout) customDialog.findViewById(R.id.exit_image_layout);
        exit_image_layout.getLayoutParams().height = (dheight * 50) / 100;

        exit_exit_layout = (RelativeLayout) customDialog.findViewById(R.id.exit_exit_layout);
        exit_exit_layout.getLayoutParams().height = (dheight * 15) / 100;

        exit_close_button = (Button) customDialog.findViewById(R.id.exit_close_button);
        exit_close_button.getLayoutParams().height = (dwidth) / 10;
        exit_close_button.getLayoutParams().width = (dwidth) / 10;


        frstImage = (ImageView) customDialog.findViewById(R.id.frstImage);
        frstImage.getLayoutParams().width = (int) (screen_width * 25) / 100;
        frstImage.getLayoutParams().height = (int) (screen_width * 25) / 100;

        secndImage = (ImageView) customDialog.findViewById(R.id.secndImage);
        secndImage.getLayoutParams().width = (int) (screen_width * 25) / 100;
        secndImage.getLayoutParams().height = (int) (screen_width * 25) / 100;

        thirdImage = (ImageView) customDialog.findViewById(R.id.thirdImage);
        thirdImage.getLayoutParams().width = (int) (screen_width * 25) / 100;
        thirdImage.getLayoutParams().height = (int) (screen_width * 25) / 100;

        fourImage = (ImageView) customDialog.findViewById(R.id.fourImage);
        fourImage.getLayoutParams().width = (int) (screen_width * 25) / 100;
        fourImage.getLayoutParams().height = (int) (screen_width * 25) / 100;

        fiveImage = (ImageView) customDialog.findViewById(R.id.fiveImage);
        fiveImage.getLayoutParams().width = (int) (screen_width * 25) / 100;
        fiveImage.getLayoutParams().height = (int) (screen_width * 25) / 100;

        sixImage = (ImageView) customDialog.findViewById(R.id.sixImage);
        sixImage.getLayoutParams().width = (int) (screen_width * 25) / 100;
        sixImage.getLayoutParams().height = (int) (screen_width * 25) / 100;

        TextView firstText = (TextView) customDialog
                .findViewById(R.id.frstText);
        TextView secondText = (TextView) customDialog
                .findViewById(R.id.secndText);
        TextView thirdText = (TextView) customDialog
                .findViewById(R.id.thirdText);
        TextView fourthText = (TextView) customDialog
                .findViewById(R.id.fourText);

        TextView fiveText = (TextView) customDialog
                .findViewById(R.id.fiveText);

        TextView sixText = (TextView) customDialog
                .findViewById(R.id.sixText);

        if (exit_list != null && exit_list.size() >= 6) {
            firstText.setText(exit_list.get(0).getApp_name());
            firstText.setSelected(true);
            secondText.setText(exit_list.get(1).getApp_name());
            secondText.setSelected(true);
            thirdText.setText(exit_list.get(2).getApp_name());
            thirdText.setSelected(true);
            fourthText.setText(exit_list.get(3).getApp_name());
            fourthText.setSelected(true);

            fiveText.setText(exit_list.get(4).getApp_name());
            fiveText.setSelected(true);

            sixText.setText(exit_list.get(5).getApp_name());
            sixText.setSelected(true);

        }

        if (exit_list != null && exit_list.size() >= 6) {

            Glide.with(getApplicationContext()).load(exit_list.get(0).getApp_icon())
                    .placeholder(R.drawable.loading_icon).error(R.drawable.loading_icon)
                    .into(frstImage);
            Glide.with(getApplicationContext()).load(exit_list.get(1).getApp_icon())
                    .placeholder(R.drawable.loading_icon).error(R.drawable.loading_icon)
                    .into(secndImage);
            Glide.with(getApplicationContext()).load(exit_list.get(2).getApp_icon())
                    .placeholder(R.drawable.loading_icon).error(R.drawable.loading_icon)
                    .into(thirdImage);
            Glide.with(getApplicationContext()).load(exit_list.get(3).getApp_icon())
                    .placeholder(R.drawable.loading_icon).error(R.drawable.loading_icon)
                    .into(fourImage);

            Glide.with(getApplicationContext()).load(exit_list.get(4).getApp_icon())
                    .placeholder(R.drawable.loading_icon).error(R.drawable.loading_icon)
                    .into(fiveImage);

            Glide.with(getApplicationContext()).load(exit_list.get(5).getApp_icon())
                    .placeholder(R.drawable.loading_icon).error(R.drawable.loading_icon)
                    .into(sixImage);


            frstImage.startAnimation(animation);
            secndImage.startAnimation(animation);
            thirdImage.startAnimation(animation);
            fourImage.startAnimation(animation);
            fiveImage.startAnimation(animation);
            sixImage.startAnimation(animation);

        }
        if (isInternetPresent && exit_list != null && exit_list.size() >= 6) {
            frstImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (exit_list != null && exit_list.size() >= 1)
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(exit_list.get(0).getApp_url())));
                }
            });

            secndImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (exit_list != null && exit_list.size() >= 2)
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(exit_list.get(1).getApp_url())));
                }
            });

            thirdImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (exit_list != null && exit_list.size() >= 3)
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(exit_list.get(2).getApp_url())));
                }
            });

            fourImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (exit_list != null && exit_list.size() >= 4)
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(exit_list.get(3).getApp_url())));
                }
            });

            fiveImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (exit_list != null && exit_list.size() >= 5)
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(exit_list.get(4).getApp_url())));
                }
            });

            sixImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (exit_list != null && exit_list.size() >= 6)
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(exit_list.get(5).getApp_url())));
                }
            });

        }
        exit_close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog.dismiss();
            }
        });

        exit_button = (Button) customDialog.findViewById(R.id.exit_button);
        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //MainActivity.this.finish();
                customDialog.dismiss();
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });


        customDialog.setCancelable(false);
        customDialog.show();
    }

    private void displayAd() {
        try {
            RelativeLayout banner_ads_layout = (RelativeLayout) findViewById(R.id.banner_ads_layout);
            banner_ads_layout.setVisibility(View.VISIBLE);
            adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } catch (Exception e) {

        }
    }


    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        if (isInternetPresent) {
            mRewardedVideoAd.pause(this);
            if (adView != null) {
                adView.pause();
            }
        }

        super.onPause();
    }

    /**
     * Called when returning to the activity
     */

    @Override
    public void onResume() {
        super.onResume();
        if (isInternetPresent) {
            mRewardedVideoAd.resume(this);
            if (adView != null) {
                adView.resume();
            }
        }

        if (checked == true) {
            if (isNotificationServiceEnabled()) {
                ivSMSOnOff.setChecked(true);
                sharePreferenceUtils.setSMS(true);
                checked = false;
            } else {

                ivSMSOnOff.setChecked(false);
                sharePreferenceUtils.setSMS(false);

            }

        } else if (checked1 == true) {
            if (isNotificationServiceEnabled()) {
                ivSMSOnOff.setChecked(true);
                sharePreferenceUtils.setSMS(true);
                checked1 = false;

                preferences.edit().putBoolean("toggle_on_off", preferences.getBoolean("toggle_on_off", true)).commit();
                startActivity(new Intent(MainActivity.this, Notification_Setting.class));

                if (isInternetPresent) {
                    if (mRewardedVideoAd.isLoaded()) {
                        mRewardedVideoAd.show();
                    }
                }

            } else {

                ivSMSOnOff.setChecked(false);
                sharePreferenceUtils.setSMS(false);

            }
        }

    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        if (isInternetPresent) {
            mRewardedVideoAd.destroy(this);
            if (adView != null) {
                adView.destroy();
            }
        }

        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        if (isInternetPresent) {
            exit_screen();

        } else {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    public boolean hasFlash() {
        if (Build.VERSION.SDK_INT > 23) {
            try {
                CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
                return cameraManager.getCameraCharacteristics(cameraManager.getCameraIdList()[1]).get(CameraCharacteristics.FLASH_INFO_AVAILABLE).booleanValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
