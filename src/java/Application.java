package com.example.geewy.cygea;

import android.app.*;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewGroupCompat;
import android.support.v7.app.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.SharedPreferences;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import cz.msebera.android.httpclient.client.ClientProtocolException;


public class Application extends ListActivity
{

    private PackageManager packageManager = null;
    private List<ApplicationInfo> applicationInfoList = null;
    private ApplicationAdaptater applicationAdaptater = null;
    private static final String TAG = Application.class.getSimpleName();
    public static String debug = "";

    String packageName = "";
    CharSequence appName = "";
    String apkFile = "";
    private AppCompatDelegate mDelegate;
    public String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);

        packageManager = getPackageManager();

        new LoadApplication().execute();
        setupActionBar();
    }



    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);


        final ApplicationInfo applicationInfo = applicationInfoList.get(position);

        try
        {
            final Intent intent = new Intent(Application.this, HttpUploader.class);
            packageName = applicationInfo.packageName;
            appName = applicationInfo.loadLabel(packageManager);
            apkFile = applicationInfo.sourceDir;

            //creation of the pop-up which will propose the analyze
            final AlertDialog alertDialog = new AlertDialog.Builder(Application.this).create();
            alertDialog.setTitle("Cygea");
            alertDialog.setMessage("Détecteur de Malware Android");
            alertDialog.setIcon(R.drawable.logo);
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Analyser",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new UploadPackageNameDB(Application.this).execute(packageName, (String)appName, apkFile);
                        }
                    });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Annuler",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primary));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.primary));
                }
            });

            alertDialog.show();

        }catch (ActivityNotFoundException e) {
            Toast.makeText(Application.this, e.getMessage(), Toast.LENGTH_LONG).show();

        }catch (Exception e){
            Toast.makeText(Application.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public class UploadPackageNameDB extends AsyncTask<String, Integer, String> {

        String Content;
        String apk_package;
        String apk_name;
        String apkFile_path;

        private Context mContext;
        private int NOTIFICATION_ID = 1;
        private Notification mNotification;
        private NotificationManager mNotificationManager;
        private boolean error = false;

        public UploadPackageNameDB(Context context){
            this.mContext = context;

            //Get the notification manager
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        @Override
        protected void onPreExecute() {
            createNotification("Analyse is in progress","", null);
        }


        @Override
        protected String doInBackground(String... params) {

            apk_package = params[0];
            apk_name = params[1];
            apkFile_path = params[2];

            Log.e("apk", apk_package);
            Log.e("apkname", apk_name);


            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpPost httpPost = new HttpPost("http://server:port/search.py");

                List<NameValuePair> pairList = new ArrayList<NameValuePair>();
                pairList.add(new BasicNameValuePair("package_Name", params[0]));

                httpPost.setEntity(new UrlEncodedFormEntity(pairList));

                HttpResponse httpResponse = httpClient.execute(httpPost);

                Content = EntityUtils.toString(httpResponse.getEntity());

            } catch (UnsupportedEncodingException ue) {
                ue.printStackTrace();
                error = true;
                cancel(true);
            } catch (ClientProtocolException ce) {
                ce.printStackTrace();
                error = true;
                cancel(true);
            } catch (IOException e) {
                e.printStackTrace();
                error = true;
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            createNotification("Error occured during Analyse", Content, null);
        }

        @Override
        protected void onPostExecute(String result) {

            ArrayList<HashMap<String, String>> resultList;
            resultList = new ArrayList<>();

            try {

                JSONObject jsonObj = new JSONObject(Content);

                // Getting JSON Array node
                JSONArray results = jsonObj.getJSONArray("result");

                //retrieve value of hash and packageName of JSON data send
                String hash = results.getJSONObject(0).getString("hash");

                Log.e("hash", apk_package);

                //JSONObject jsonObj = new JSONObject(Content);

                //JSONObject res = jsonObj.getJSONObject("result");

               // String hash = res.getString("hash");
                /*String ann = res.getString("result");
                String separation[] = ann.split(":");
                String separation2[] = separation[1].split(",");

                if(separation2[0].startsWith("[\"-")){
                    writeInFile(Application.this, apk_package +" = 1");
                }

                else{
                    writeInFile(Application.this, apk_package +" = 0");
                }*/

                if (hash.equals("")) {
                    new UploadPackageNameCrawler(Application.this).execute(apk_package, apk_name, apkFile_path);
                }

                else {

                    //writeInFile(Application.this, apk_package);


                    final Intent intent = new Intent(Application.this, ResultActivity.class);
                    intent.putExtra("apk_package", apk_package);
                    Application.debug = apk_package;
                    final PendingIntent pi = PendingIntent.getActivity(Application.this, 0, intent, 0);

                    if (error) {
                        createNotification("Analyse ended abnormally!", Content, null);
                    }
                    else {
                        createNotification("Analyse is complete!","", pi);
                    }
                }

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                error = true;
                cancel(true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Json parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        private void createNotification(String contentTitle, String contentText, PendingIntent pi) {

            //Build the notification using Notification.Builder
            Notification.Builder builder = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.icon)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate(new long[]{1000, 1000})
                    .setContentIntent(pi);

            //Get current notification
            mNotification = builder.getNotification();

            //Show the notification
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }

    }

    public class UploadPackageNameCrawler extends AsyncTask<String, Integer, String> {

        String Content;
        String apk_package;
        String apk_name;
        String apkFile_path;
        private Context mContext;
        private int NOTIFICATION_ID = 1;
        private Notification mNotification;
        private NotificationManager mNotificationManager;
        private boolean error = false;

        public UploadPackageNameCrawler(Context context){
            this.mContext = context;

            //Get the notification manager
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        @Override
        protected void onPreExecute() {
            createNotification("Analyse is in progress","", null);
        }


        @Override
        protected String doInBackground(String... params) {

            apk_package = params[0];
            apk_name = params[1];
            apkFile_path = params[2];

            Log.e("apk2", apk_package);

            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpPost httpPost = new HttpPost("http://server:port/analyze.py");

                List<NameValuePair> pairList = new ArrayList<NameValuePair>();
                pairList.add(new BasicNameValuePair("package_Name", params[0]));

                httpPost.setEntity(new UrlEncodedFormEntity(pairList));

                HttpResponse httpResponse = httpClient.execute(httpPost);

                Content = EntityUtils.toString(httpResponse.getEntity());

            } catch (UnsupportedEncodingException ue) {
                ue.printStackTrace();
                error = true;
                cancel(true);
            } catch (ClientProtocolException ce) {
                ce.printStackTrace();
                error = true;
                cancel(true);
            } catch (IOException e) {
                e.printStackTrace();
                error = true;
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            createNotification("Error occured during Analyse", Content, null);
        }

        @Override
        protected void onPostExecute(String result) {


            Log.e("result", Content);
            Log.e("hash", apk_package);


            if (Content.equals("ERROR: Unable to retrieve app")) {
                new UploadFile().execute(apk_package, apk_name, apkFile_path);
                createNotification("Not stored in the Play Store!", Content, null);
            }

            else {

                Intent intent = new Intent(Application.this, ResultActivity.class);
                intent.putExtra("apk_package", apk_package);
                PendingIntent pi = PendingIntent.getActivity(Application.this, 0, intent, 0);

                if (error) {
                    createNotification("Analyse ended abnormally!", Content, null);
                }
                else {
                    createNotification("Analyse is complete!","", pi);
                }
            }
        }

        private void createNotification(String contentTitle, String contentText, PendingIntent pi) {

            //Build the notification using Notification.Builder
            Notification.Builder builder = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.icon)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate(new long[]{1000, 1000})
                    .setContentIntent(pi);

            //Get current notification
            mNotification = builder.getNotification();

            //Show the notification
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }

    }

    public class UploadFile extends AsyncTask<String, Void, String> {

        String Content;

        @Override
        protected String doInBackground(String... params) {

            String path= params[0];

            String filename = path.substring(path.lastIndexOf("/")+1);
            String url = "http://server:port/upload.py";
            File file = new File(path);

            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);

                InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(file), -1);
                reqEntity.setContentType("binary/octet-stream");
                reqEntity.setContentType("file");
                reqEntity.setChunked(true); // Send in multiple parts if needed
                httpPost.setEntity(reqEntity);
                HttpResponse response = httpClient.execute(httpPost);

                Content = EntityUtils.toString(response.getEntity());

                Log.e("Response", "" + Content);

            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public class UploadPackageNameDBAll extends AsyncTask<String, Integer, String> {

        String Content;
        String apk_package;
        String apk_name;
        String apkFile_path;

        @Override
        protected String doInBackground(String... params) {

            apk_package = params[0];
            apk_name = params[1];
            apkFile_path = params[2];

            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpPost httpPost = new HttpPost("http://server:port/search.py");

                List<NameValuePair> pairList = new ArrayList<NameValuePair>();
                pairList.add(new BasicNameValuePair("package_Name", params[0]));

                httpPost.setEntity(new UrlEncodedFormEntity(pairList));

                HttpResponse httpResponse = httpClient.execute(httpPost);

                Content = EntityUtils.toString(httpResponse.getEntity());

            } catch (UnsupportedEncodingException ue) {
                ue.printStackTrace();
            } catch (ClientProtocolException ce) {
                ce.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {

            ArrayList<HashMap<String, String>> resultList;
            resultList = new ArrayList<>();

            try {

                JSONObject jsonObj = new JSONObject(Content);

                // Getting JSON Array node
                JSONArray results = jsonObj.getJSONArray("result");

                //retrieve value of hash and packageName of JSON data send
                String hash = results.getJSONObject(0).getString("hash");

                //JSONObject jsonObj = new JSONObject(Content);

                //JSONObject res = jsonObj.getJSONObject("result");
                //res.remove("\\\\");

                //String hash = res.getString("hash");
                /*String ann = res.getString("result");
                String separation[] = ann.split(":");
                String separation2[] = separation[1].split(",");

                Log.e("ann :",separation2[0]);

                if(separation2[0].startsWith("[\"-")){
                    writeInFile(Application.this, apk_package +" = 1");
                }

                else{
                    writeInFile(Application.this, apk_package +" = 0");
                }*/

                if (hash.equals("")) {
                    new UploadPackageNameCrawlerAll().execute(apk_package, apk_name, apkFile_path);
                }

                else {

                    final Intent intent = new Intent(Application.this, ResultActivity.class);
                    intent.putExtra("apk_name", apk_name);
                }

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Json parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    public class UploadPackageNameCrawlerAll extends AsyncTask<String, Integer, String> {

        String Content;
        String apk_package;
        String apk_name;
        String apkFile_path;

        @Override
        protected String doInBackground(String... params) {

            apk_package = params[0];
            apk_name = params[1];
            apkFile_path = params[2];

            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpPost httpPost = new HttpPost("http://server:port/analyze.py");

                List<NameValuePair> pairList = new ArrayList<NameValuePair>();
                pairList.add(new BasicNameValuePair("package_Name", params[0]));

                httpPost.setEntity(new UrlEncodedFormEntity(pairList));

                HttpResponse httpResponse = httpClient.execute(httpPost);

                Content = EntityUtils.toString(httpResponse.getEntity());

            } catch (UnsupportedEncodingException ue) {
                ue.printStackTrace();

            } catch (ClientProtocolException ce) {
                ce.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {

            ArrayList<HashMap<String, String>> resultList;
            resultList = new ArrayList<>();

            try {

                JSONObject jsonObj = new JSONObject(Content);

                // Getting JSON Array node
                JSONArray results = jsonObj.getJSONArray("result");

                //retrieve value of hash and packageName of JSON data send
                String hash = results.getJSONObject(0).getString("hash");

                //JSONObject jsonObj = new JSONObject(Content);

                //JSONObject res = jsonObj.getJSONObject("result");
                //res.remove("\\\\");

                //String hash = res.getString("hash");
                /*String ann = res.getString("result");
                String separation[] = ann.split(":");
                String separation2[] = separation[1].split(",");

                if(separation2[0].startsWith("[\"-")){
                    writeInFile(Application.this, apk_package +" = 1");
                }

                else{
                    writeInFile(Application.this, apk_package +" = 0");
                }*/

                if (hash.equals("ERROR: Unable to retrieve " +apk_package)) {
                    new UploadFile().execute(apk_package, apk_name, apkFile_path);
                }

                else {

                    final Intent intent = new Intent(Application.this, ResultActivity.class);
                    intent.putExtra("apk_name", apk_name);
                }

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Json parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list)
    {
        ArrayList<ApplicationInfo> applicationInfoArrayList = new ArrayList<ApplicationInfo>();

        for(ApplicationInfo info : list)
        {
            try
            {
                if(packageManager.getLaunchIntentForPackage(info.packageName) != null)
                {
                    applicationInfoArrayList.add(info);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return  applicationInfoArrayList;
    }

    private class LoadApplication extends AsyncTask<Void, Void, Void>
    {
        private ProgressDialog progressDialog = null;

        @Override
        protected Void doInBackground(Void... params) {
            applicationInfoList = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            applicationAdaptater = new ApplicationAdaptater(Application.this, R.layout.list_row, applicationInfoList);

            return null;
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            setListAdapter(applicationAdaptater);
            progressDialog.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(Application.this, null,
                    "Chargement des infos de l'application...");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_all:
                analyzeAll();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void analyzeAll(){
        String apk_package = "";
        String apk_name = "";
        String apkFile_path = "";


        applicationInfoList = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));

        for (int i = 0; i < applicationInfoList.size(); i++) {

            apk_package = applicationInfoList.get(i).packageName;
            apk_name = (String) applicationInfoList.get(i).loadLabel(packageManager);
            apkFile_path = applicationInfoList.get(i).sourceDir;

            UploadPackageNameDBAll upnca = new UploadPackageNameDBAll();
            upnca.execute(apk_package, apk_name, apkFile_path);

            synchronized (upnca){
                try {
                    upnca.wait(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally
                {
                    new CreateNotification2(Application.this).execute();
                }
            };
        }

        }

    public class CreateNotification2 extends AsyncTask<String, Integer, String> {

        String Content = "";
        int i = 0, malware = 0, benign = 0;
        private Context mContext;
        private int NOTIFICATION_ID = 1;
        private Notification mNotification;
        private NotificationManager mNotificationManager;
        private boolean error = false;

        public CreateNotification2(Context context){
            this.mContext = context;

            //Get the notification manager
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        @Override
        protected void onPreExecute() {
            createNotification("Analyse is in progress","");
        }

        @Override
        protected String doInBackground(String... params) {

            /*  Content = readFile(Application.this);
                char ch[] = new char[Content.length()];

                for(i=0; i < Content.length(); i++){
                    ch[i] = s.charAt(i);

                    if( ((i>0)&&(ch[i]!=' ')&&(ch[i-1]==' ')) || ((ch[0]!=' ')&&(i==0)) ){
                        benign++;
                    }

                    else if( ((i>0)&&(ch[i]!=' ')&&(ch[i-1]==' ')) || ((ch[0]!=' ')&&(i==1)) ){
                        malware++;
                    }
                }
             */

            return null;
        }

        @Override
        protected void onCancelled() {
            createNotification("Error occured during Analyse", Content);
        }

        @Override
        protected void onPostExecute(String result) {
            if (error) {
                createNotification("Analyse ended abnormally!", Content);
            }
            else {
                //createNotification("Analyse is complete!","we have detected " + malware + " malware et " + benign + " seins.");
            }
        }


        private void createNotification(String contentTitle, String contentText) {

            //Build the notification using Notification.Builder
            Notification.Builder builder = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.icon)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate(new long[]{1000, 1000});


            //Get current notification
            mNotification = builder.getNotification();

            //Show the notification
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }
    }


    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    public void writeInFile(Context context, String data){
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;

        try{
            fileOutputStream = context.openFileOutput("db.txt", MODE_PRIVATE);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write(data+"\0");
            outputStreamWriter.flush();

        }catch (Exception e){
            e.printStackTrace();
        }

        finally {
            try{
                outputStreamWriter.close();
                fileOutputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public String readFile(Context context){
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;

        char[] inputBuffer = new char[255];
        String data = null;

        try{
            fileInputStream = context.openFileInput("db.txt");
            inputStreamReader = new InputStreamReader(fileInputStream);
            inputStreamReader.read(inputBuffer);
            data = new String(inputBuffer);



        }catch (Exception e){
            e.printStackTrace();
        }

        finally {
            try {
                inputStreamReader.close();
                fileInputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        return data;
    }


}
