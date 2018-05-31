package com.udacity.gradle.builditbigger;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;


import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.techpearl.jokeshow.JokeDisplayActivity;
import com.udacity.gradle.builditbigger.backend.myApi.MyApi;

import java.io.IOException;



public class MainActivity extends AppCompatActivity implements MainActivityFragment.ButtonClickListener{

    //idling resource for testing
    private CountingIdlingResource espressoIdlingResource =
            new CountingIdlingResource("Server_Call");
    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivityFragment fragment =
                (MainActivityFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment);
        //pass the idling resource to the fragment. It is used in the free app
        fragment.setIdlingRes(espressoIdlingResource);
        mLoadingIndicator = findViewById(R.id.loadingIndicator);

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

    @Override
    public void onButtonClicked() {
        //if the device is connected, load the joke,
        if(isConnected()){
            mLoadingIndicator.setVisibility(View.VISIBLE);
            new EndpointsAsyncTask().execute(new Pair<Context,
                    CountingIdlingResource>(this, espressoIdlingResource));
        }else {
            //if not connected, display a snackbar with a message
            Snackbar snackbar = Snackbar.make(findViewById(R.id.mainLayout),
                    R.string.connectivity_issue_message,
                    Snackbar.LENGTH_LONG);
            snackbar.show();
        }

    }

    /*AsyncTask to pull the joke from the server*/
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
                return  myApiService.pullJoke().execute().getData();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //resume the test
            mIdlingRes.decrement();
            //hide the loading indicator
            mLoadingIndicator.setVisibility(View.GONE);
            //open the Activity from the Android Library with an intent with the joke
            Intent intent = new Intent(context, JokeDisplayActivity.class);
            intent.putExtra(JokeDisplayActivity.EXTRA_JOKE, result);
            startActivity(intent);
        }
    }

    /*returns true when the device is connected, false otherwise*/
    private boolean isConnected(){
        /* Based on code snippet in
         * https://developer.android.com/training/basics/network-ops/managing.html */
        ConnectivityManager connectivityManager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @VisibleForTesting
    public CountingIdlingResource getEspressoIdlingResource(){
        return espressoIdlingResource;
    }
}
