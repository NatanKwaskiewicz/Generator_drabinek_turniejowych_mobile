package com.tourney.app.api;

import com.tourney.app.models.Country;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CountriesApi {

    @GET("v3.1/all?fields=name,cca2,flag")
    Call<List<Country>> getCountries();
}