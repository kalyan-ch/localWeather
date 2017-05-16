package edu.uncc.weatherapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    SharedPreferences shpr = null;
    final OkHttpClient client = new OkHttpClient();
    String apiKey = "CdlFqwRE9OXvkpKkNuh9uWOaT8UjTLm8";
    WeatherInfo currWInf = null;
    String city, country;
    RelativeLayout rl = null;
    WeatherDAO wDao;
    ArrayList<City> cities =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean dFlag = true;
        setContentView(R.layout.activity_main);
        rl = (RelativeLayout) findViewById(R.id.weather_layout);
        shpr = getSharedPreferences("login",MODE_PRIVATE);
        String key = shpr.getString("city_key","");
        wDao = new WeatherDAO();
        Log.d("demo","key: "+key);
        if(!"".equals(key)){
            city = shpr.getString("city_name","");
            country = shpr.getString("country_name","");
            getCurrentCond();
        }else{
            rl.removeAllViews();
            LayoutInflater lInf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = lInf.inflate(R.layout.button_text,rl,false);
            rl.addView(view);
        }
        cities = wDao.getAllCities();
        if(cities!=null){
            if(cities.size()>0){
                RecyclerView lv = (RecyclerView)findViewById(R.id.listVCity);
                lv.setLayoutManager(new LinearLayoutManager(this));
                lv.setAdapter(new CityAdapter(cities));
            }else{
                dFlag = true;
            }
        }else{
            dFlag = true;
        }
        if(!dFlag){
            ((TextView)findViewById(R.id.savedCities)).setText("There are no cities to display. Search the city from the search box and save");
        }else{
            ((TextView)findViewById(R.id.savedCities)).setText("Saved Cities");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.mainSettings){
            Intent i2 = new Intent(MainActivity.this,PreferenceActivity.class);
            startActivity(i2);
        }
        return true;
    }

    public void getCity(View v){

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText cityInput = new EditText(this);
        cityInput.setHint("Enter City");
        final EditText countryInput = new EditText(this);
        countryInput.setHint("Enter Country");
        layout.addView(cityInput);
        layout.addView(countryInput);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter City Details");
        builder.setView(layout);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                city = cityInput.getText().toString();
                country = countryInput.getText().toString();
                if("".equals(city) || "".equals(country)){
                    Toast.makeText(MainActivity.this, "Please enter a value for city and country", Toast.LENGTH_SHORT).show();
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
                                SharedPreferences.Editor editor = shpr.edit();
                                editor.putString("city_key",cityKey);
                                editor.putString("city_name",city);
                                editor.putString("country_name",country);
                                editor.commit();
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getCurrentCond();
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

    public void getCurrentCond(){
        String currentKey = shpr.getString("city_key","");
        Toast.makeText(MainActivity.this, "Current City details saved", Toast.LENGTH_SHORT).show();
        if(currentKey!=null){
            if( !("".equals(currentKey)) ){
                String subURl = "http://dataservice.accuweather.com/currentconditions/v1/"+currentKey+"?apikey="+apiKey;
                Request request = new Request.Builder().url(subURl).build();
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
                        try {
                            JSONArray jArr = new JSONArray(data);
                            JSONObject jobj = jArr.getJSONObject(0);

                            String datetime = jobj.getString("LocalObservationDateTime");
                            String wtext = jobj.getString("WeatherText");
                            String weIcon = "";
                            int wIcon = Integer.parseInt(jobj.getString("WeatherIcon"));
                            if(wIcon<10){
                                weIcon = "0"+wIcon;
                            }else{
                                weIcon = ""+wIcon;
                            }
                            JSONObject tempObj = jobj.getJSONObject("Temperature");

                            String tempVal = tempObj.getJSONObject("Metric").getString("Value");
                            String unit = tempObj.getJSONObject("Metric").getString("Unit");
                            String temperature = tempVal+"°"+unit;

                            currWInf = new WeatherInfo(datetime,wtext,weIcon,temperature);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    displayCurrWeather();
                                }
                            });

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });
            }
        }
    }

    public void displayCurrWeather(){
        if(currWInf!=null){
            try{

                rl.removeAllViews();
                LayoutInflater lInf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = lInf.inflate(R.layout.weather_info,rl,false);
                String time = currWInf.getDatetime().substring(0,19);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                Date date = sdf.parse(time);
                PrettyTime p = new PrettyTime();
                String w_time = p.format(date);

                ((TextView)view.findViewById(R.id.w_time)).setText("Updated "+w_time);
                ((TextView)view.findViewById(R.id.w_text)).setText(currWInf.getWtext());
                ((TextView)view.findViewById(R.id.w_city_cntry)).setText(city+","+country);
                ((TextView)view.findViewById(R.id.w_temp)).setText("Temperature: "+currWInf.getTemperature());

                String imageUrl = "http://developer.accuweather.com/sites/default/files/"+currWInf.getwIcon()+"-s.png";
                Log.d("demo","img: "+imageUrl);
                Picasso.with(view.getContext())
                        .load(imageUrl)
                        .into((ImageView)view.findViewById(R.id.w_icon));
                rl.addView(view);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void searchCity(View v){
        final String cityText = ((TextView)findViewById(R.id.cityEditText)).getText().toString();
        final String countryText = ((TextView)findViewById(R.id.countryEditText)).getText().toString();
        if(!"".equals(cityText) && !"".equals(countryText)){
            String finalURL = "http://dataservice.accuweather.com/locations/v1/"+countryText+"/search?apikey="+apiKey+"&q="+cityText;
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
                    try{
                        JSONArray jArr = new JSONArray(data);
                        JSONObject jObj = jArr.getJSONObject(0);
                        final String cityKey = jObj.getString("Key");
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                takeToNew(cityKey, cityText, countryText);
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }else{
            Toast.makeText(this, "Please enter values for city and country", Toast.LENGTH_SHORT).show();
        }
    }

    public void takeToNew(String cityKey, String cityText, String countryText){
        Intent i1 = new Intent(MainActivity.this,CityWeatherActivity.class);
        i1.putExtra("cityKey",cityKey);
        i1.putExtra("city",cityText);
        i1.putExtra("country",countryText);
        startActivity(i1);
    }

}
