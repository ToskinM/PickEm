package com.cse.osu.pickem;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private Button leaveLeagueButton;
    private DatabaseReference leaguesDatabaseReference;
    private DatabaseReference leagueMemberDatabaseReference;
    private FirebaseAuth auth;

    private List<League> loadedLeagues = new ArrayList<>();
    private Set<LeagueMemberPair> loadedLeagueMemberPairs = new HashSet<LeagueMemberPair>();


    private void handleCreateLeagueButton() {
        // League Creation
        createLeagueButton = findViewById(R.id.buttonCreateLeague);
        createLeagueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = leagueIDTextField.getText().toString().trim();
                if (!id.equals("")) {
                    String name = leagueNameTextField.getText().toString().trim();
                    //Set up new league, and add it to firebase
                    League newLeague = new League(name, id, auth.getCurrentUser().getUid());
                    leaguesDatabaseReference.child(id).setValue(newLeague);
                    Toast.makeText(LeagueActivity.this, newLeague.getLeagueName(), Toast.LENGTH_SHORT).show();
                } else {
                    //Error handling
                    AlertDialog alertDialog = new AlertDialog.Builder(LeagueActivity.this).create();
                    alertDialog.setMessage("LeagueID cannot be empty!");
                    alertDialog.show();
                }
            }
        });
    }
    private AlertDialog createCreateLeagueDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LeagueActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = LeagueActivity.this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_create_league, null))
                .setTitle("Create League")
                // Add action buttons
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;

                        // Get league ID/Name of league to create
                        EditText leagueIDEditText = d.findViewById(R.id.league_id);
                        String leagueID = leagueIDEditText.getText().toString().trim();
                        EditText leagueNameEditText = d.findViewById(R.id.league_name);
                        String leagueName = leagueNameEditText.getText().toString().trim();

                        if (!leagueID.equals("")) {
                            //Set up new league, and add it to firebase
                            League newLeague = new League(leagueName, leagueID, auth.getCurrentUser().getUid());
                            leaguesDatabaseReference.child(leagueID).setValue(newLeague);
                        } else {
                            //Error handling
                            AlertDialog alertDialog = new AlertDialog.Builder(LeagueActivity.this).create();
                            alertDialog.setMessage("LeagueID cannot be empty.");
                            alertDialog.show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing, cancelling rename
                    }
                });
        return builder.create();
    }

    private void handleJoinLeagueButton() {
        // League Join
        joinLeagueButton = findViewById(R.id.buttonJoinLeague);
        joinLeagueButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                leagueNameTextField.setText(""); //Clear text in league name
                LeagueMemberPair pairToAdd = new LeagueMemberPair(auth.getUid(), leagueIDTextField.getText().toString().trim());
                boolean okToAdd = false;

                //Ensure league exists
                for (League league : loadedLeagues) {
                    if (league.getLeagueID().equals(leagueIDTextField.getText().toString().trim())) {
                        okToAdd = true;
                    }
                }
                //Ensure user isn't already a member of the league
                for (LeagueMemberPair testPair : loadedLeagueMemberPairs) {
                    if (testPair.equals(pairToAdd)) {
                        Log.d("PickEm", "Already exists in thing");
                        okToAdd = false;
                    }
                }
                //Passes all tests, add new league pair
                if (okToAdd) {
                    leagueMemberDatabaseReference.push().setValue(pairToAdd);
                }
            }
        });
    }
    private AlertDialog createJoinLeagueDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LeagueActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = LeagueActivity.this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_join_league, null))
                .setTitle("Join League")
                // Add action buttons
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;

                        // Get league ID of league to join
                        EditText leagueIDEditText = d.findViewById(R.id.league_id);
                        String leagueID = leagueIDEditText.getText().toString().trim();

                        LeagueMemberPair pairToAdd = new LeagueMemberPair(auth.getUid(), leagueID);
                        boolean okToAdd = false;

                        //Ensure league exists
                        for (League league : loadedLeagues) {
                            if (league.getLeagueID().equals(leagueID)) {
                                okToAdd = true;
                            }
                        }

                        //Ensure user isn't already a member of the league
                        for (LeagueMemberPair testPair : loadedLeagueMemberPairs) {
                            if (testPair.equals(pairToAdd)) {
                                Log.d("PickEm", "Already exists in thing");
                                okToAdd = false;
                            }
                        }

                        //Passes all tests, add new league pair
                        if (okToAdd) {
                            leagueMemberDatabaseReference.push().setValue(pairToAdd);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing, cancelling rename
                    }
                });
        return builder.create();
    }

    private void handleLeaveLeagueButton() {
        leaveLeagueButton = findViewById(R.id.buttonLeaveLeague);
        leaveLeagueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                leagueMemberDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String currentUserID = auth.getUid();
                        String enteredLeagueID = leagueIDTextField.getText().toString().trim();
                        LeagueMemberPair currentPair = new LeagueMemberPair(currentUserID, enteredLeagueID);

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            //Get info about the league on firebase currently being examined
                            String snapshotUserID = snapshot.getValue(LeagueMemberPair.class).getUID();
                            String snapshotLeagueID = snapshot.getValue(LeagueMemberPair.class).getLeagueID();

                            //Create actual league member pair for firebase league
                            LeagueMemberPair snapshotPair = new LeagueMemberPair(snapshotUserID, snapshotLeagueID);

                            //If the current pair (userId/text field) matches the pair on firebase,
                            if (snapshotPair.equals(currentPair)) {

                                //Remove the league member pair, leaving the league.
                                snapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        Toast.makeText(LeagueActivity.this, "Removed pair!", Toast.LENGTH_SHORT).show();
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
        });
    }



    private void setUp(Bundle savedInstanceState) {
        leagueIDTextField = findViewById(R.id.league_id_field);
        leagueNameTextField = findViewById(R.id.league_name_field);

        handleCreateLeagueButton();
        handleJoinLeagueButton();
        handleLeaveLeagueButton();

        // Have yourLeaguesTextView display user's id
        yourLeaguesTextView = findViewById(R.id.yourLeaguesTextView);
        yourLeaguesTextView.setText("Your UID: " + auth.getUid());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LeagueActivity: onCreate() called!");
        setContentView(R.layout.activity_leagues);
        Toolbar toolbar = findViewById(R.id.toolbar);
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


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setUp(savedInstanceState);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.leagues, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_create:
                AlertDialog createDialog = createCreateLeagueDialog();
                createDialog.show();
            case R.id.action_join:
                AlertDialog joinDialog = createJoinLeagueDialog();
                joinDialog.show();
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



}
