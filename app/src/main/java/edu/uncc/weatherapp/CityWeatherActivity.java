package edu.uncc.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

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

public class CityWeatherActivity extends AppCompatActivity {
    String cityKey;
    String city, country;
    ArrayList<ForecastInfo> fiveDays;
    OkHttpClient client;
    String apiKey;
    String headLine, extLink;
    WeatherDAO wDao;
    String temperature;
    SharedPreferences shpr = null;
    String date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        wDao = new WeatherDAO();
        client = new OkHttpClient();
        cityKey = getIntent().getStringExtra("cityKey");
        city = getIntent().getStringExtra("city");
        country = getIntent().getStringExtra("country");
        apiKey = "CdlFqwRE9OXvkpKkNuh9uWOaT8UjTLm8";
        shpr = getSharedPreferences("login",MODE_PRIVATE);

        String finalURL = "http://dataservice.accuweather.com/forecasts/v1/daily/5day/"+cityKey+"?apikey="+apiKey;
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
                try {
                    JSONObject jObj = new JSONObject(data);
                    JSONObject headline = jObj.getJSONObject("Headline");
                    headLine = headline.getString("Text");
                    extLink = headline.getString("MobileLink");
                    fiveDays = new ArrayList<ForecastInfo>();
                    JSONArray dailyArr = jObj.getJSONArray("DailyForecasts");
                    for (int i=0; i<dailyArr.length(); i++){
                        JSONObject faily = dailyArr.getJSONObject(i);
                        ForecastInfo daily = new ForecastInfo();

                        if(i==0){
                            date = faily.getString("Date");
                        }
                        daily.setDate(faily.getString("Date"));
                        String tempMaxSt = faily.getJSONObject("Temperature").getJSONObject("Maximum").getString("Value");
                        String tempMinSt = faily.getJSONObject("Temperature").getJSONObject("Minimum").getString("Value");
                        daily.setTempMax(tempMaxSt);
                        daily.setTempMin(tempMinSt);
                        daily.setDayText(faily.getJSONObject("Day").getString("IconPhrase"));

                        int day = Integer.parseInt(faily.getJSONObject("Day").getString("Icon"));
                        String dayIcon = ""+day;
                        if(day<10){
                            dayIcon = "0"+day;
                        }
                        int night = Integer.parseInt(faily.getJSONObject("Night").getString("Icon"));
                        String nIcon = ""+night;
                        if(night<10){
                            nIcon = "0"+night;
                        }

                        daily.setDayIcon(dayIcon);
                        daily.setNightText(faily.getJSONObject("Night").getString("IconPhrase"));
                        daily.setNightIcon(nIcon);
                        daily.setMobLink(faily.getString("MobileLink"));

                        fiveDays.add(daily);
                    }

                    if(fiveDays.size() > 0){
                        CityWeatherActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setWeatherData(fiveDays.get(0));
                            }
                        });
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    public void setWeatherData(ForecastInfo fInf){

        if(fInf!=null){
            try{
                ((TextView)findViewById(R.id.title)).setText("Daily Forecast for "+city+","+country);
                ((TextView)findViewById(R.id.headLine)).setText(headLine);
                temperature = fInf.getTempMax();
                String dateStr = fInf.getDate().substring(0,19);;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                Date date = sdf.parse(dateStr);
                SimpleDateFormat sdf2 = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
                String today = sdf2.format(date);

                ((TextView)findViewById(R.id.forecastLabel)).setText("Forecast on "+today);
                ((TextView)findViewById(R.id.forecastData)).setText("Temperature "+fInf.getTempMax()+"/"+fInf.getTempMin());

                Picasso.with(this)
                        .load("http://developer.accuweather.com/sites/default/files/"+fInf.getDayIcon()+"-s.png")
                        .into((ImageView)findViewById(R.id.dayImage));
                Picasso.with(this)
                        .load("http://developer.accuweather.com/sites/default/files/"+fInf.getNightIcon()+"-s.png")
                        .into((ImageView)findViewById(R.id.nightImage));
                Log.d("demo",""+fInf.getNightIcon());
                ((TextView)findViewById(R.id.nightPhrase)).setText(fInf.getNightText());
                ((TextView)findViewById(R.id.dayPhrase)).setText(fInf.getNightText());
                ((TextView)findViewById(R.id.moreLink)).setText(Html.fromHtml("<a href='"+fInf.getMobLink()+"'>Click here for more details</a>"));
                ((TextView)findViewById(R.id.extLink)).setText(Html.fromHtml("<a href='"+extLink+"'>Click here for extended forecast</a>"));
                RecyclerView mRecView = (RecyclerView)findViewById(R.id.recyclerView);
                mRecView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false));
                mRecView.setAdapter(new ForecastAdapter(fiveDays));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.city_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.save:
                City c = new City();
                c.setName(city);
                c.setCountry(country);
                c.setFavorite("false");
                c.setKey(cityKey);
                c.setTemperature(temperature);
                c.setDate(date);
                wDao.addCity(c);
                Toast.makeText(this, "City saved in database", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setCurrent:
                SharedPreferences.Editor editor = shpr.edit();
                editor.putString("city_key",cityKey);
                editor.putString("city_name",city);
                editor.putString("country_name",country);
                editor.commit();
                Toast.makeText(this, "City set as current city", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Intent i2 = new Intent(CityWeatherActivity.this,PreferenceActivity.class);
                startActivity(i2);
                break;
        }
        return true;
    }
}
