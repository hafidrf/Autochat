package id.co.kamil.autochat;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("KSDJFKASJFI3S8DSJFDH")
                .clientKey("LASDK823JKHR87SDFJSDHF8DFHASFDC")
                .server("https://dash.wabot.id:1337/parse")
                .build()
        );
    }
}
