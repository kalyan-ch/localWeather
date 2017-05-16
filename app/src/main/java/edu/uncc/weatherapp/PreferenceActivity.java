package edu.uncc.weatherapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PreferenceActivity extends AppCompatActivity {

    String apiKey = "CdlFqwRE9OXvkpKkNuh9uWOaT8UjTLm8";
    final OkHttpClient client = new OkHttpClient();
    SharedPreferences shpr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        setTitle("Preferences");
        shpr = getSharedPreferences("login",MODE_PRIVATE);
    }

    public void temp(View v){
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final RadioGroup rgp = new RadioGroup(this);
        final RadioButton rbtn = new RadioButton(this);
        rbtn.setText("Celsius");
        rbtn.setId(120);
        final RadioButton rbtn1 = new RadioButton(this);
        rbtn1.setText("Fahrenheit");
        rbtn1.setId(121);
        rgp.addView(rbtn);
        rgp.addView(rbtn1);
        layout.addView(rgp);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Temperature Unit");
        builder.setView(layout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    SharedPreferences.Editor editor = shpr.edit();
                    int id = rgp.getCheckedRadioButtonId();

                    if(id==120){
                        editor.putString("temp_unit","C");
                    }else if(id==121){
                        editor.putString("temp_unit","F");
                    }

                    editor.commit();
                }catch (Exception e){
                    e.printStackTrace();
                }


                Toast.makeText(PreferenceActivity.this, "Temperature Unit changed", Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog mv_alert = builder.create();
        mv_alert.show();
    }

    public void city(View v){
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText cityInput = new EditText(this);
        cityInput.setHint("Enter City");
        final EditText countryInput = new EditText(this);
        countryInput.setHint("Enter Country");

        final SharedPreferences.Editor editor = shpr.edit();
        String key = shpr.getString("city_key","");
        String posText = "Set";
        if(!"".equals(key)){
            cityInput.setText(shpr.getString("city_name",""));
            countryInput.setText(shpr.getString("country_name",""));
            posText = "Update";
        }
        layout.addView(cityInput);
        layout.addView(countryInput);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter City Details");
        builder.setView(layout);

        builder.setPositiveButton(posText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String city = cityInput.getText().toString();
                final String country = countryInput.getText().toString();
                if("".equals(city) || "".equals(country)){
                    Toast.makeText(PreferenceActivity.this, "Please enter a value for city and country", Toast.LENGTH_SHORT).show();
                }else{
                    String finalURL = "http://dataservice.accuweather.com/locations/v1/"+country+"/search?apikey="+apiKey+"&q="+city;
                    Log.d("demo","finalURL "+finalURL);
                    Request request = new Request.Builder()
                            .url(finalURL)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(!response.isSuccessful()){
                                throw new IOException("Unexpected code: "+response);
                            }

                            String data = response.body().string();
                            String cityKey = "";
                            try {
                                JSONArray jArr = new JSONArray(data);
                                JSONObject jObj = jArr.getJSONObject(0);
                                cityKey = jObj.getString("Key");

                                editor.putString("city_key",cityKey);
                                editor.putString("city_name",city);
                                editor.putString("country_name",country);
                                editor.commit();

                                PreferenceActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PreferenceActivity.this, "Current city changed!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }catch (Exception e){
                                Log.d("demo","Exception in json parse");
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog mv_alert = builder.create();
        mv_alert.show();
    }
}
