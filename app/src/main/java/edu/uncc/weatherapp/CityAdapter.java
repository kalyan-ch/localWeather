package edu.uncc.weatherapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by kalyan on 4/8/2017.
 */

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolderTwo> {

    ArrayList<City> cities;

    WeatherDAO wDao = new WeatherDAO();
    public CityAdapter(ArrayList<City> cities){
        this.cities = cities;
    }

    @Override
    public CityAdapter.ViewHolderTwo onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        CityAdapter.ViewHolderTwo vOne = null;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_cities, null);
        vOne = new CityAdapter.ViewHolderTwo(v);
        return vOne;
    }

    public void onBindViewHolder(CityAdapter.ViewHolderTwo holder, int position){
        View convertView = holder.v;
        final City c = cities.get(position);
        ((TextView)convertView.findViewById(R.id.listCityName)).setText(c.getName()+","+c.getCountry());
        ((TextView)convertView.findViewById(R.id.listTemp)).setText("Temperature: "+c.getTemperature());
        String time = c.getDate().substring(0,19);
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            Date date = sdf.parse(time);
            PrettyTime p = new PrettyTime();
            String w_time = p.format(date);
            ((TextView)convertView.findViewById(R.id.listTime)).setText(w_time);
        }catch (Exception e){
            e.printStackTrace();
        }
        ImageButton ibtn = ((ImageButton)convertView.findViewById(R.id.listFav));
        if("true".equals(c.getFavorite())){
            ibtn.setImageResource(R.mipmap.gold_star);
        }

        ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("true".equals(c.getFavorite())){
                    c.setFavorite("false");
                    wDao.makeFav(c.getKey(),"false");
                }else{
                    c.setFavorite("true");
                    wDao.makeFav(c.getKey(),"true");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return ((null!=cities)? (cities.size()) : (0)) ;
    }


    static class ViewHolderTwo extends RecyclerView.ViewHolder{
        View v;

        public ViewHolderTwo(View v1){
            super(v1);
            this.v = v1;
        }
    }
}
