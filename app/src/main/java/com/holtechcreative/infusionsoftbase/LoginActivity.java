package com.holtechcreative.infusionsoftbase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.holtechcreative.infusionsoftbase.api.APIClient;
import com.holtechcreative.infusionsoftbase.api.ServiceGenerator;
import com.holtechcreative.infusionsoftbase.api.objects.AccessToken;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    Button loginButton;

    public static final String API_LOGIN_URL = "https://signin.infusionsoft.com/app/oauth/authorize";
    public static final String API_OAUTH_CLIENTID = "e575b4amyazamj2zxzsrfxvf";
    public static final String API_OAUTH_CLIENTSECRET = "mnfzhv5g4z";
    public static final String API_OAUTH_REDIRECT = "com.holtechcreative.infusionsoftbase://isoauth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d("Inside LoginActivity", "We're inside!");
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(API_LOGIN_URL + "?client_id=" + API_OAUTH_CLIENTID + "&redirect_uri=" + API_OAUTH_REDIRECT + "&response_type=code&scope=full"));
                // This flag is set to prevent the browser with the login form from showing in the history stack
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri uri = getIntent().getData();
        if(uri != null && uri.toString().startsWith(API_OAUTH_REDIRECT)) {
            String code = uri.getQueryParameter("code");
            if(code != null) {
                // TODO We can probably do something with this code! Show the user that we are logging them in

                final SharedPreferences prefs = this.getSharedPreferences(
                        BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

                APIClient client = ServiceGenerator.createService(APIClient.class);
                Call<AccessToken> call = client.getNewAccessToken(code, API_OAUTH_CLIENTID,
                        API_OAUTH_CLIENTSECRET, API_OAUTH_REDIRECT,
                        "authorization_code");
                call.enqueue(new Callback<AccessToken>() {
                    @Override
                    public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                        int statusCode = response.code();
                        if(statusCode == 200) {
                            Log.d("In Login onResponse", response.body().toString());
                            AccessToken token = response.body();
                            prefs.edit().putBoolean("oauth.loggedin", true).apply();
                            prefs.edit().putString("oauth.accesstoken", token.getAccessToken()).apply();
                            prefs.edit().putString("oauth.refreshtoken", token.getRefreshToken()).apply();
                            prefs.edit().putString("oauth.tokentype", token.getTokenType()).apply();

                            // TODO Show the user they are logged in
                        } else {
                            // TODO Handle errors on a failed response
                        }
                    }

                    @Override
                    public void onFailure(Call<AccessToken> call, Throwable t) {
                        // TODO Handle failure
                    }
                });
            } else {
                // TODO Handle a missing code in the redirect URI
            }
        }
    }
}
