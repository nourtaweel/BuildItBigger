package com.udacity.gradle.builditbigger;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.techpearl.jokes.JokeFactory;
import com.techpearl.jokeshow.JokeDisplayActivity;
import com.udacity.gradle.builditbigger.backend.myApi.MyApi;

import java.io.IOException;

import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity {

    private CountingIdlingResource espressoIdlingResource = new CountingIdlingResource("Server_Call");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void tellJoke(View view) {
       // Toast.makeText(this, JokeFactory.getJoke(), Toast.LENGTH_SHORT).show();
        new EndpointsAsyncTask().execute(new Pair<Context, CountingIdlingResource>(this, espressoIdlingResource));

    }
    class EndpointsAsyncTask extends AsyncTask<Pair<Context, CountingIdlingResource>, Void, String> {
        private MyApi myApiService = null;
        private Context context;
        private CountingIdlingResource mIdlingRes;

        @Override
        protected String doInBackground(Pair<Context, CountingIdlingResource>... params) {
            context = params[0].first;
            mIdlingRes = params[0].second;
            mIdlingRes.increment();
            if(myApiService == null) {  // Only do this once
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // options for running against local devappserver
                        // - 10.0.2.2 is localhost's IP address in Android emulator
                        // - turn off compression when running against local devappserver
                        .setRootUrl("http://10.0.2.2:8080/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                // end options for devappserver

                myApiService = builder.build();
            }

            try {
                //return myApiService.sayHi(name).execute().getData();
                return  myApiService.pullJoke().execute().getData();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
           // Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            mIdlingRes.decrement();
            Intent intent = new Intent(context, JokeDisplayActivity.class);
            intent.putExtra(JokeDisplayActivity.EXTRA_JOKE, result);
            startActivity(intent);
        }
    }

    @VisibleForTesting
    public CountingIdlingResource getEspressoIdlingResource(){
        return espressoIdlingResource;
    }
}
