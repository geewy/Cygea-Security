package com.example.geewy.cygea;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.client.ClientProtocolException;

public class HttpUploader extends AppCompatActivity{

    private static final String TAG = Application.class.getSimpleName();
    TextView textView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_uploader);
        textView = (TextView)findViewById(R.id.content);

        Intent intent = getIntent();
        String name = "";
        String packageName = "";
        String pathFile = "";

        if (intent != null) {
            name = intent.getStringExtra("apk_name");
            packageName = intent.getStringExtra("apk_package");
            pathFile = intent.getStringExtra("apk_file");
        }

        new UploadPackageNameDB().execute(packageName, name, pathFile);
    }


    public class UploadPackageNameDB extends AsyncTask<String, Integer, String> {

        String Content;
        String apk_package;
        String apk_name;
        String apkFile_path;
        final ProgressDialog progDailog = new ProgressDialog(HttpUploader.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog.setIcon(R.drawable.icon);
            progDailog.setTitle("Analyze");
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setIcon(R.drawable.icon);
            progDailog.setCancelable(true);
            progDailog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            apk_package = params[0];
            apk_name = params[1];
            apkFile_path = params[2];

            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpPost httpPost = new HttpPost("your_ip_address/search.py"); /* send to the server for search in database */

                List<NameValuePair> pairList = new ArrayList<NameValuePair>();
                pairList.add(new BasicNameValuePair("package_Name", params[0]));

                httpPost.setEntity(new UrlEncodedFormEntity(pairList));

                HttpResponse httpResponse = httpClient.execute(httpPost);

                Content = EntityUtils.toString(httpResponse.getEntity());
                Log.e("response", Content);

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

            super.onPostExecute(result);
            progDailog.dismiss();

            ArrayList<HashMap<String, String>> resultList;
            resultList = new ArrayList<>();

            try {
                JSONObject jsonObj = new JSONObject(Content);

                // Getting JSON Array node
                JSONArray results = jsonObj.getJSONArray("result");

                //retrieve value of hash and packageName of JSON data send
                String hash = results.getJSONObject(0).getString("hash");

                if (hash.equals("")) {
                    Toast.makeText(getApplicationContext(), apk_name+" not stored in our database, please wait",Toast.LENGTH_LONG).show();
                    textView.setText(apk_name+" not stored in our database, please wait");
                    new UploadPackageNameCrawler().execute(apk_package, apk_name, apkFile_path);
                }

                else {

                    final Intent intent = new Intent(HttpUploader.this, ResultActivity.class);
                    intent.putExtra("apk_name", apk_name);

                    startActivity(intent);
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

    public class UploadPackageNameCrawler extends AsyncTask<String, Integer, String> {

        String Content;
        String apk_package;
        String apk_name;
        String apkFile_path;
        final ProgressDialog progDailog = new ProgressDialog(HttpUploader.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog.setIcon(R.drawable.icon);
            progDailog.setTitle("Analyze");
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            apk_package = params[0];
            apk_name = params[1];
            apkFile_path = params[2];

            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpPost httpPost = new HttpPost("your_ip_address/analyze.py"); /* send to the server for analyse */

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

            super.onPostExecute(result);
            progDailog.dismiss();

            ArrayList<HashMap<String, String>> resultList;
            resultList = new ArrayList<>();

            try {
                JSONObject jsonObj = new JSONObject(Content);

                // Getting JSON Array node
                JSONArray results = jsonObj.getJSONArray("result");

                //retrieve value of hash and packageName of JSON data send
                String hash = results.getJSONObject(0).getString("hash");

                if (hash.equals("ERROR: Unable to retrieve " +apk_package)) {
                    Toast.makeText(getApplicationContext(), apk_name+" not stored in the Play Store", Toast.LENGTH_LONG).show();
                    textView.setText(apk_name+" not stored in the Play Store");
                    new UploadFile().execute(apkFile_path);
                }

                else {
                    final Intent intent = new Intent(HttpUploader.this, ResultActivity.class);
                    intent.putExtra("apk_name", apk_name);

                    startActivity(intent);
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

    public class UploadFile extends AsyncTask<String, Void, String> {

        String Content;

        @Override
        protected String doInBackground(String... params) {

            String path= params[0];

            String filename = path.substring(path.lastIndexOf("/")+1);
            String url = "your_ip_address/upload.py"; /* send the apk file to the server */
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
}
