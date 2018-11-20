package com.cse.osu.pickem;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileUsernameTest {
    private String username = "";
    private String TEST_FIREBASE_UID = "KbPbloun1GfzwruCWxiuWVrEAHB2";
    private String TEST_FIREBASE_EMAIL = "EspressoBot@andorid.com";
    private String TEST_FIREBASE_PASSWORD = "password";

    @Rule
    public ActivityTestRule<ProfileActivity> mActivityRule =
            new ActivityTestRule<>(ProfileActivity.class);

    // Ensure username displays correctly before and after name change.
    @Test
    public void verifyUsernameChange() {
        getUsername();
        while (username.equals("")){
            // Wait until starting username is fetched from Database
        }

        // Check starting username is displayed
        Espresso.onView(ViewMatchers.withId(R.id.textView_userName)).check(ViewAssertions.matches(ViewMatchers.withText(username)));

        // Perform rename
        Espresso.onView(ViewMatchers.withId(R.id.button_changeUsername)).perform(ViewActions.click());

        // In rename AlertDialog, enter a random new username and click rename button
        String newUsername = ("EspressoTest-" + new Random().nextInt(1000)).trim();
        Espresso.onView(ViewMatchers.withId(R.id.new_username)).perform(ViewActions.typeText(newUsername));
        Espresso.onView(ViewMatchers.withId(android.R.id.button1)).perform(ViewActions.click());

        // Get username from database again, it should be the new username
        username = "";
        getUsername();
        while (username.equals("")){
            // Wait until new username is fetched from Database
        }

        // Check new username is displayed
        Espresso.onView(ViewMatchers.withId(R.id.textView_userName)).check(ViewAssertions.matches(ViewMatchers.withText(newUsername)));
    }

    private void getUsername(){
        FirebaseDatabase.getInstance().getReference("profiles").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Profile tempProfile = snapshot.getValue(Profile.class);
                    if (tempProfile.getUserID().equals(FirebaseAuth.getInstance().getUid())) {
                        username = tempProfile.getUserName();
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}