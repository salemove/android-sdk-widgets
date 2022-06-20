package com.glia.widgets.view.configuration.chat;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RemoteUiConfigApi {

    @GET("{cache_id}")
    Call<ChatStyle> getChatConfig(@Path("cache_id") String cacheId);

}
