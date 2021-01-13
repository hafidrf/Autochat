package id.co.kamil.autochat;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("0cbfcf03-3f98-470a-95be-e9b46fd56e2c")
                .clientKey("0cbfcf03-3f98-470a-95be-e9b46fd56e2c")
                .server("https://dash.wabot.id:1337/parse/")
                .build()
        );
    }
}
