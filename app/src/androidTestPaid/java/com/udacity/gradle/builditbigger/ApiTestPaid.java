package com.udacity.gradle.builditbigger;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;
/*Tests the paid version of the App*/
@RunWith(AndroidJUnit4.class)
public class ApiTestPaid {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);
    CountingIdlingResource mIdlingResource;
    @Before
    public void registerIdlingResource(){
        mIdlingResource = mActivityRule.getActivity().getEspressoIdlingResource();
        Espresso.registerIdlingResources(mIdlingResource);
    }
    @Test
    public void asyncTaskTest(){
        //click the button
        onView(withId(R.id.button)).perform(click());
        //check the joke is visible
        onView(withId(R.id.textView)).check(matches(not(withText(""))));
    }
    @After
    public void unregisterIdlingResource(){
        if(mIdlingResource != null){
            Espresso.unregisterIdlingResources(mIdlingResource);
        }

    }
}
