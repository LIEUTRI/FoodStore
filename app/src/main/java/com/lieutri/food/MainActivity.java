package com.lieutri.food;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView txtJson, txtJsonData;
    EditText etName, etSex, etBirthyear;
    Button btnAdd, btnRead;

    java.net.CookieManager msCookieManager = new java.net.CookieManager();
    static final String COOKIES_HEADER = "Set-Cookie";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtJson = findViewById(R.id.txtJson);
        txtJsonData = findViewById(R.id.txtJsonData);
        etName = findViewById(R.id.edit_name);
        etSex = findViewById(R.id.edit_sex);
        etBirthyear = findViewById(R.id.edit_birthyear);
        btnAdd = findViewById(R.id.button_add);
        btnRead = findViewById(R.id.button_read);

//        CookieManager cookieManager = new CookieManager();
//
//        HttpCookie cookie = new HttpCookie("lang", "vi");
//        cookie.setDomain("dev.lieutri.ml");
//        cookie.setPath("/");
//        cookie.setVersion(0);
//        try {
//            msCookieManager.getCookieStore().add(new URI("http://dev.lieutri.ml/connectdb.php/"), cookie);
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//        CookieHandler.setDefault(msCookieManager);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendData sendData = new SendData();
                sendData.execute("start");
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetData getData = new GetData();
                getData.execute();
            }
        });
    }

    private class SendData extends AsyncTask<String,String,String> {

        BufferedReader reader = null;
        OutputStream os;
        InputStream is;
        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            HttpURLConnection urlConnection = null;
            //http post
            try {
                URL url = new URL("http://dev.lieutri.ml/adddata.php");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", etName.getText().toString());
                jsonObject.put("sex", etSex.getText().toString());
                jsonObject.put("birthyear", etBirthyear.getText().toString());
                String data = jsonObject.toString();

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                urlConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Cookie", "__test=1e1b75b941f1b69a63d70d7a6aaefdba");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setFixedLengthStreamingMode(data.getBytes().length);
                urlConnection.connect();

//                Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
//                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
//                if (cookiesHeader != null) {
//                    for (String cookie : cookiesHeader) {
//                        msCookieManager.getCookieStore().add(new URI("http://dev.lieutri.ml/adddata.php"), HttpCookie.parse(cookie).get(0));
//                    }
//                }
//
//                if (msCookieManager.getCookieStore().getCookies().size() > 0) {
//                    // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
//                    urlConnection.setRequestProperty("Cookie", TextUtils.join(";",  msCookieManager.getCookieStore().getCookies()));
//                    Log.i("cookie", TextUtils.join(";",  msCookieManager.getCookieStore().getCookies()));
//                }

                os = new BufferedOutputStream(urlConnection.getOutputStream());
                os.write(data.getBytes());
                os.flush();

                is = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d("Response: ", ">>> " + line);
                }
                return buffer.toString();

            } catch (Exception e) {
                Log.e("log_tag", "Error converting result " + e.toString());
            } finally {
                try {
                    os.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                urlConnection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject json = new JSONObject(s);
                Toast.makeText(MainActivity.this, json.getString("result"), Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetData extends AsyncTask<String, String, String> {

        ProgressDialog pd;
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL("http://dev.lieutri.ml/readdata.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Cookie", "__test=1e1b75b941f1b69a63d70d7a6aaefdba");
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d("Response: ", "> " + line);

                }
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            txtJson.setText(result);
            //parse json data
            if (result != null){
                try {
                    JSONArray jArray = new JSONArray(result);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);

                        txtJsonData.append("\nid: " + json_data.getInt("id") + "\n" + "name: " + json_data.getString("name")
                                + "\n" + "sex: " + json_data.getString("sex") + "\n" + "birth year: " + json_data.getString("birthyear"));

                        Log.i("log_tag", "id: " + json_data.getInt("id") +
                                ", name: " + json_data.getString("name") +
                                ", sex: " + json_data.getInt("sex") +
                                ", birthyear: " + json_data.getInt("birthyear")
                        );
                    }
                } catch (JSONException e) {
                    Log.e("log_tag", "Error parsing data " + e.toString());
                }
            } else {
                Toast.makeText(MainActivity.this, "Cannot receive Json! Try again!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}