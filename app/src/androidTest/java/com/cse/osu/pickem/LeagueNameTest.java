package com.cse.osu.pickem;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LeagueNameTest {
    private String picture = "";
    private String TEST_FIREBASE_UID = "KbPbloun1GfzwruCWxiuWVrEAHB2";
    private String TEST_FIREBASE_EMAIL = "EspressoBot@andorid.com";
    private String TEST_FIREBASE_PASSWORD = "password";

    @Rule
    public ActivityTestRule<LeagueActivity> mActivityRule =
            new ActivityTestRule<>(LeagueActivity.class);

    // Ensure league name is properly displayed in the League's options.
    @Test
    public void verifyLeagueNameDisplayed() {
        String leagueName = "EspressoLeague-" + new Random().nextInt(1000);
        Intent intent = new Intent(mActivityRule.getActivity(), LeagueOptionsActivity.class);
        intent.putExtra("league", new League(leagueName, "EL-" + new Random().nextInt(1000), TEST_FIREBASE_UID));
        mActivityRule.getActivity().startActivity(intent);

        Espresso.onView(ViewMatchers.withId(R.id.textViewLeagueName)).check(ViewAssertions.matches(ViewMatchers.withText(leagueName)));
    }
}