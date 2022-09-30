package com.walczyk.apps.weatherapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherAPI {
    @GET("weather?appid=7f93fb5af536ca9dea1d119e0395bed5&units=metric")
    Call<OpenWeatherMap> getWeatherWithLocation(
            @Query("lat")double lat,
            @Query("lon")double lon
    );
    @GET("weather?appid=7f93fb5af536ca9dea1d119e0395bed5&units=metric")
    Call<OpenWeatherMap> getWeatherWithName(@Query("q")String name);
}
