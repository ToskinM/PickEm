package com.cse.osu.pickem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LeagueActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "PickEm";


    private EditText leagueIDTextField;
    private EditText leagueNameTextField;
    private EditText leagueOutput;
    private TextView yourLeaguesTextView;
    private Button createLeagueButton;
    private Button joinLeagueButton;
    private DatabaseReference leaguesDatabaseReference;
    private DatabaseReference leagueMemberDatabaseReference;
    private FirebaseAuth auth;

    private List<League> loadedLeagues = new ArrayList<>();
    private Set<LeagueMemberPair> loadedLeagueMemberPairs = new HashSet<LeagueMemberPair>();


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

    private void setUp(Bundle savedInstanceState) {
        leagueIDTextField = findViewById(R.id.league_id_field);
        leagueNameTextField = findViewById(R.id.league_name_field);

        // League Creation
        createLeagueButton = findViewById(R.id.buttonCreateLeague);
        createLeagueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = leagueIDTextField.getText().toString().trim();
                if (!id.equals("")) {
                    String name = leagueNameTextField.getText().toString().trim();
                    League newLeague = new League(name, id, auth.getCurrentUser().getUid());
                    leaguesDatabaseReference.child(id).setValue(newLeague);
                    Toast.makeText(LeagueActivity.this, newLeague.getLeagueName(), Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(LeagueActivity.this).create();
                    alertDialog.setMessage("LeagueID cannot be empty!");
                    alertDialog.show();
                }
            }
        });

        // League Join
        joinLeagueButton = findViewById(R.id.buttonJoinLeague);
        joinLeagueButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                leagueNameTextField.setText(""); //Clear text in league name
                LeagueMemberPair pairToAdd = new LeagueMemberPair(auth.getUid(), leagueIDTextField.getText().toString().trim());
                boolean okToAdd = false;

                for (League league : loadedLeagues) {
                    if (league.getLeagueID().equals(leagueIDTextField.getText().toString().trim())) {
                        okToAdd = true;
                    }
                }

                for (LeagueMemberPair testPair : loadedLeagueMemberPairs) {
                    if (testPair.equals(pairToAdd)) {
                        Log.d("PickEm", "Already exists in thing");
                        okToAdd = false;
                    }
                }

                if (okToAdd) {
                    leagueMemberDatabaseReference.push().setValue(pairToAdd);
                }
            }
        });

        // Have yourLeaguesTextView display user's id
        yourLeaguesTextView = findViewById(R.id.yourLeaguesTextView);
        yourLeaguesTextView.setText("Your UID: " + auth.getUid());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LeagueActivity: onCreate() called!");
        setContentView(R.layout.activity_leagues);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();

        // Create league RecyclerView Fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new LeagueListFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        leaguesDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");
        leagueMemberDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");


        leagueMemberDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    loadedLeagueMemberPairs.clear();
                    LeagueMemberPair tempPair = snapshot.getValue(LeagueMemberPair.class);
                    if (!loadedLeagueMemberPairs.contains(tempPair)) {
                        loadedLeagueMemberPairs.add(tempPair);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        leaguesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                loadedLeagues.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    loadedLeagues.add(data.getValue(League.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("PickEm", "ON CANCELLED!");
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        setUp(savedInstanceState);
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
        // automatically handle clicks on the HomeActivity/Up button, so long
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
            Intent intent = new Intent(this, HomeActivity.class);
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
