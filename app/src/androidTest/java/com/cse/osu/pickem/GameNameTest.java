package com.cse.osu.pickem;

import android.content.Intent;

import org.junit.Rule;
import org.junit.Test;

import java.util.Date;
import java.util.Random;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

public class GameNameTest {
    private String picture = "";
    private String TEST_FIREBASE_UID = "KbPbloun1GfzwruCWxiuWVrEAHB2";
    private String TEST_FIREBASE_EMAIL = "EspressoBot@andorid.com";
    private String TEST_FIREBASE_PASSWORD = "password";

    @Rule
    public ActivityTestRule<HomeActivity> mActivityRule =
            new ActivityTestRule<>(HomeActivity.class);

    // Ensure league name is properly displayed in the League's options.
    @Test
    public void verifyGameNameDisplayed() {
        //TODO parcelable extra not working in GameOptionsActivity??
        String espressoFirstName = "EspressoTeamA-" + new Random().nextInt(1000);
        String espressoSecondName = "EspressoTeamB-" + new Random().nextInt(1000);
        String espressoLeagueID = "EspressoLeagueID-" + new Random().nextInt(1000);
        Intent intent = new Intent(mActivityRule.getActivity(), GameOptionsActivity.class);
        Game game = new Game(espressoFirstName, espressoSecondName, espressoLeagueID);
        game.setLockTime(new Date(new Date().getTime() + 10000));
        intent.putExtra("game", game);
        mActivityRule.getActivity().startActivity(intent);

        String correctString = espressoFirstName + " vs. " + espressoSecondName;
        Espresso.onView(ViewMatchers.withId(R.id.gameTitleView)).check(ViewAssertions.matches(ViewMatchers.withText(correctString)));
    }
}
