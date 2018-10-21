package com.cse.osu.pickem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LeagueActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "PickEm";


    private EditText leagueIDTextField;
    private EditText leagueNameTextField;
    private EditText leagueOutput;
    private Button createLeagueButton;
    private DatabaseReference leaguesDatabaseReference;
    private FirebaseAuth auth;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LeagueActivity: onDestroy() called!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "LeagueActivity: onResume() called!");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "LeagueActivity: onStart() called!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "LeagueActivity: onStop() called!");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "LeagueActivity: onPause() called!");
    }

    private void setUpLeagueCreation(Bundle savedInstanceState) {
        leagueIDTextField = findViewById(R.id.league_name_field);
        leagueNameTextField = findViewById(R.id.league_name_field);

        createLeagueButton = findViewById(R.id.buttonCreateLeague);
        createLeagueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = leagueIDTextField.getText().toString().trim();
                String name = leagueNameTextField.getText().toString().trim();
                League newLeague = new League(name, id, auth.getCurrentUser().getUid());
                leaguesDatabaseReference.child(id).setValue(newLeague);


                Toast.makeText(LeagueActivity.this, newLeague.getLeagueName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LeagueActivity: onCreate() called!");
        setContentView(R.layout.activity_leagues);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        leaguesDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setUpLeagueCreation(savedInstanceState);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.leagues, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
        } else if (id == R.id.nav_leagues) {
            Intent intent = new Intent(this, LeagueActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_people) {
            Intent intent = new Intent(this, People.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



}
