package edu.uncc.weatherapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by kalyan on 4/8/2017.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolderOne> {
    ArrayList<ForecastInfo> fiveDays = null;


    public ForecastAdapter(ArrayList<ForecastInfo> pods_list){
        this.fiveDays = pods_list;

    }

    @Override
    public ViewHolderOne onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        ViewHolderOne vOne = null;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, null);
        vOne = new ViewHolderOne(v);
        return vOne;
    }

    @Override
    public void onBindViewHolder(ViewHolderOne holder, int position) {
        if(fiveDays != null){
            ForecastInfo fInf = fiveDays.get(position);
            View v = holder.v;
            try{
                String dateStr = fInf.getDate().substring(0,19);;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                Date date = sdf.parse(dateStr);
                SimpleDateFormat sdf2 = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
                String today = sdf2.format(date);

                ((TextView)v.findViewById(R.id.cardText)).setText(today);
                Picasso.with(v.getContext())
                        .load("http://developer.accuweather.com/sites/default/files/"+fInf.getDayIcon()+"-s.png")
                        .into((ImageView)v.findViewById(R.id.cardImg));
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public int getItemCount() {
        return ((null!=fiveDays)? (fiveDays.size()) : (0)) ;
    }

    static class ViewHolderOne extends RecyclerView.ViewHolder{
        View v;

        public ViewHolderOne(View v1){
            super(v1);
            this.v = v1;
        }
    }
}
