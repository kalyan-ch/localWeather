package edu.uncc.weatherapp;

/**
 * Created by kalyan on 4/6/2017.
 */

public class WeatherInfo {
    private String datetime,wtext,wIcon,temperature;

    public WeatherInfo(){

    }

    public WeatherInfo(String datetime, String wtext, String wIcon, String temperature) {
        this.datetime = datetime;
        this.wtext = wtext;
        this.wIcon = wIcon;
        this.temperature = temperature;

    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getWtext() {
        return wtext;
    }

    public void setWtext(String wtext) {
        this.wtext = wtext;
    }

    public String getwIcon() {
        return wIcon;
    }

    public void setwIcon(String wIcon) {
        this.wIcon = wIcon;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
}
