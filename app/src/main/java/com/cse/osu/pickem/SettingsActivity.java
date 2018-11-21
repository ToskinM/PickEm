package com.cse.osu.pickem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.graphics.Color;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static final String TAG = "PickEm";
    private Button logoutButton;
    private Button deleteAccountButton;
    private Button button_myProfile;
    private FirebaseAuth auth;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SettingsActivity: onDestroy() called!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "SettingsActivity: onResume() called!");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "SettingsActivity: onStart() called!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "SettingsActivity: onStop() called!");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "SettingsActivity: onPause() called!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "SettingsActivity: onCreate() called!");
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        logoutButton = findViewById(R.id.button_logOut);
        logoutButton.setOnClickListener(this);

        button_myProfile = findViewById(R.id.button_myProfile);
        button_myProfile.setOnClickListener(this);

        deleteAccountButton = findViewById(R.id.button_deleteAccount);
        deleteAccountButton.setOnClickListener(this);
        deleteAccountButton.setBackgroundColor(Color.RED);

        auth = FirebaseAuth.getInstance();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_leagues) {
            Intent intent = new Intent(this, LeagueActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_people) {
            Intent intent = new Intent(this, PeopleActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) throws NullPointerException{
        if (v == logoutButton) {
            finish();
            auth.signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        } else if (v == deleteAccountButton) {
            deleteUserProfile();
        } else if (v == button_myProfile) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }
    }

    private void deleteUserProfile(){
        final DatabaseReference profilesDatabaseReference = FirebaseDatabase.getInstance().getReference("profiles");
        profilesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Profile snapshotProfile = snapshot.getValue(Profile.class);
                    if (snapshotProfile.getUserID().equals(auth.getUid())) {
                        profilesDatabaseReference.child(snapshotProfile.getUserID()).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                FirebaseUser user = auth.getCurrentUser();
                                auth.signOut();
                                user.delete();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}