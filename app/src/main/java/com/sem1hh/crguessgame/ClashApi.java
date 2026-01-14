package com.sem1hh.crguessgame;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ClashApi {
    // Parantez içini boş bıraktık, linki MainActivity'den yollayacağız
    @GET
    Call<List<ClashCard>> getCards(@Url String fullUrl);
}