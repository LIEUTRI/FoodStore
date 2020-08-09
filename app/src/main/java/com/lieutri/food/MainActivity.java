package com.lieutri.food;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView txtJson, txtJsonData;
    EditText etName, etEmail;
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
        etEmail = findViewById(R.id.edit_email);
        btnAdd = findViewById(R.id.button_add);
        btnRead = findViewById(R.id.button_read);

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
                URL url = new URL("http://foodstore.ddns.net:8080/demo/add");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", etName.getText().toString());
                jsonObject.put("email", etEmail.getText().toString());
                String data = jsonObject.toString();

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                urlConnection.setRequestProperty("Accept", "text/plain;charset=utf-8");
//                urlConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                urlConnection.setDoOutput(true);
//                urlConnection.setRequestProperty("Cookie", "__test=1e1b75b941f1b69a63d70d7a6aaefdba");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setFixedLengthStreamingMode(data.getBytes().length);
                urlConnection.connect();

                os = new BufferedOutputStream(urlConnection.getOutputStream());
                os.write(data.getBytes());
                os.flush();

                int statusCode = urlConnection.getResponseCode();
                Log.i("statusCode", statusCode+"");
                if (statusCode >= 200 && statusCode < 400){
                    is = urlConnection.getInputStream();
                } else {
                    is = urlConnection.getErrorStream();
                }
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
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
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
                URL url = new URL("http://foodstore.ddns.net:8080/demo/all");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                //connection.setRequestProperty("Cookie", "__test=1e1b75b941f1b69a63d70d7a6aaefdba");
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
                    StringBuilder data = new StringBuilder();
                    JSONArray jArray = new JSONArray(result);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);

                        data.append("\nid: ").append(json_data.getInt("id")).append("\n").append("name: ").append(json_data.getString("name")).append("\n").append("email: ").append(json_data.getString("email"));

                        Log.i("log_tag", "id: " + json_data.getInt("id") +
                                ", name: " + json_data.getString("name") +
                                ", email: " + json_data.getString("email")
                        );
                    }
                    txtJsonData.setText(data.toString());
                } catch (JSONException e) {
                    Log.e("log_tag", "Error parsing data " + e.toString());
                }
            } else {
                Toast.makeText(MainActivity.this, "Cannot receive Json! Try again!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}