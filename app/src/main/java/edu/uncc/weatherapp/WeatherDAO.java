package edu.uncc.weatherapp;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by kalyan on 4/8/2017.
 */

public class WeatherDAO {
    DatabaseReference mainRef = FirebaseDatabase.getInstance().getReference();;
    DatabaseReference cityRef = mainRef.child("cities");;

    public void addCity(City city){
        cityRef.child(city.getKey()).setValue(city);
    }

    public void makeFav(String key, String value){
        cityRef.child(key).child("favorite").setValue(value);
    }

    public ArrayList<City> getAllCities(){
        final ArrayList<City> saved_city_list = new ArrayList<City>();
        cityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()  ) {
                    City c = new City();
                    c = snapshot.getValue(c.getClass());
                    saved_city_list.add(c);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return saved_city_list;
    }
}
