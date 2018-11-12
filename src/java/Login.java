package com.example.geewy.cygea;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Connection;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import butterknife.ButterKnife;
import butterknife.Bind;

public class Login extends AppCompatActivity {

    private static final String TAG = Login.class.getSimpleName();
    private static final int REQUEST_SIGNUP = 0;
    java.sql.Connection con;

    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_login) Button _loginButton;
    @Bind(R.id.link_signup) TextView _signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), Signup.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login(){
        Log.d(TAG, "login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(Login.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        checkLogin CheckLogin = new checkLogin();
                        CheckLogin.execute(email, password);
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                /* By default we just finish the Activity and log them in automatically */
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public class checkLogin extends AsyncTask<String, String, String>{

        String z = "";
        boolean success = false;

        @Override
        protected String doInBackground(String... params){
            String email = params[0];
            String passwd = params[1];

            if(email.equals("") || passwd.equals("")){
                z = "Please enter an Email and Password";
            }

            else{
                try{
                    con = connectionClass("sami", "qJsHmkSlBaD", "os.path.jocm(BASE_DIR, 'db.sqlite3'", "ip");

                    if (con == null)
                    {
                        z = "Check Your Internet Access!";
                    }

                    else{
                        String query = "select * from login where email ='" + email.toString() + "'and password = '"+ passwd.toString() +"'";
                        Statement statement = con.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);

                        if(resultSet.next()){
                            z = "Login successful";
                            success=true;
                            con.close();
                        }

                        else{
                            z = "Invalid Credentials!";
                            success = false;
                        }
                    }
                }catch (Exception e){
                    success = false;
                    z = e.getMessage();
                }
            }
            return z;
        }
    }

    @SuppressLint("NewApi")
    public java.sql.Connection connectionClass(String email, String passwd, String db, String server){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        java.sql.Connection connection = null;
        String connectionURL = null;

        try{
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            connectionURL = "jdbc:jtds:sqlserver://" + server + db + ";email=" + email+ ";password=" + passwd + ";";
            connection = DriverManager.getConnection(connectionURL);
        }catch (SQLException se){
            Log.e("error here 1 : ", se.getMessage());
        }catch (ClassNotFoundException ce){
            Log.e("error here 2 : ", ce.getMessage());
        }catch (Exception e){
            Log.e("error here 3 : ", e.getMessage());
        }

        return connection;
    }
}
