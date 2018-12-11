package com.cse.osu.pickem;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MainActivity";
    ImageView profilePictureView;
    TextView usernameTextView;
    DrawerLayout drawer;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity: onDestroy() called!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity: onResume() called!");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity: onStart() called!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity: onStop() called!");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity: onPause() called!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity: onCreate() called!");
        setContentView(R.layout.activity_main);

        // Setup Hamburger Menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        profilePictureView = navigationView.getHeaderView(0).findViewById(R.id.nav_picture);
        profilePictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
        usernameTextView = navigationView.getHeaderView(0).findViewById(R.id.nav_username);

        updateProfileInfo();
        // Startup fetchers
        LeagueFetcher leagueFetcher = LeagueFetcher.get();
        LeagueMembersFetcher leagueMembersFetcher = LeagueMembersFetcher.get();
        GameFetcher gameFetcher = GameFetcher.get();

        // Create RecyclerView Fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            setTitle("Home");
            fragment = new HomeFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    protected void updateProfileInfo() {
        FirebaseDatabase.getInstance().getReference("profiles").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Profile tempProfile = snapshot.getValue(Profile.class);
                    if (tempProfile.getUserID().equals(FirebaseAuth.getInstance().getUid())) {
                        // Get and decode profile image
                        String profilePic = tempProfile.getEncodedProfilePicture();
                        if (profilePic != null){
                            byte[] decodedByteArray = android.util.Base64.decode(profilePic, Base64.DEFAULT);
                            profilePictureView.setImageBitmap(BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length));
                        }

                        // Display Username
                        usernameTextView.setText(tempProfile.getUserName());
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            setTitle("Home");
            // Switch to HomeFragment
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = new HomeFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (id == R.id.nav_leagues) {
            setTitle("Leagues");
            // Switch to LeaguesFragment
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = new LeaguesFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (id == R.id.nav_people) {
            setTitle("People");
            // Switch to PeopleFragment
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = new PeopleLeagueFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        } else if (id == R.id.nav_settings) {
            setTitle("Settings");
            // Switch to SettingsFragment
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = new SettingsFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

