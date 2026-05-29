package com.tourney.app.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CountriesClient {

    private static CountriesClient instance;

    private final CountriesApi api;

    private CountriesClient() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://restcountries.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(CountriesApi.class);
    }

    public static synchronized CountriesClient getInstance() {

        if (instance == null) {
            instance = new CountriesClient();
        }

        return instance;
    }

    public CountriesApi getApi() {
        return api;
    }
}