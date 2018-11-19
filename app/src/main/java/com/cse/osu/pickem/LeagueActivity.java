package com.cse.osu.pickem;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;

public class LeagueActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "PickEm";

    private EditText leagueIDTextField;
    private EditText leagueNameTextField;
    private FirebaseAuth auth;

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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setUp(savedInstanceState);
    }

    private void setUp(Bundle savedInstanceState) {
        leagueIDTextField = findViewById(R.id.league_id_field);
        leagueNameTextField = findViewById(R.id.league_name_field);

        handleCreateLeagueButton();
        handleJoinLeagueButton();
        handleLeaveLeagueButton();

        // Have yourLeaguesTextView display user's id
        TextView yourLeaguesTextView = findViewById(R.id.yourLeaguesTextView);
        yourLeaguesTextView.setText("Your UID: " + auth.getUid());
    }

    private void handleCreateLeagueButton() {
        // League Creation
        Button createLeagueButton = findViewById(R.id.buttonCreateLeague);
        createLeagueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = leagueIDTextField.getText().toString().trim();
                if (!id.equals("")) {
                    // Cancel process if league with same ID already exists
                    for (League league : LeagueFetcher.get().getAllLeagues()){
                        if (id.equals(league.getLeagueID())){
                            AlertDialog alertDialog = new AlertDialog.Builder(LeagueActivity.this).create();
                            alertDialog.setMessage("League with that ID already exists!");
                            alertDialog.show();
                            return;
                        }
                    }

                    String name = leagueNameTextField.getText().toString().trim();
                    League newLeague = new League(name, id, auth.getUid());
                    newLeague.addToDatabase();
                } else {
                    //Error handling
                    AlertDialog alertDialog = new AlertDialog.Builder(LeagueActivity.this).create();
                    alertDialog.setMessage("League ID cannot be empty!");
                    alertDialog.show();
                }
            }
        });
    }

    private void handleJoinLeagueButton() {
        // League Join
        Button joinLeagueButton = findViewById(R.id.buttonJoinLeague);
        joinLeagueButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                leagueNameTextField.setText("");  // Clear text in league name
                League.addMember(leagueIDTextField.getText().toString().trim(), auth.getUid());
            }
        });
    }

    private void handleLeaveLeagueButton() {
        Button leaveLeagueButton = findViewById(R.id.buttonLeaveLeague);
        leaveLeagueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUserID = auth.getUid();
                String enteredLeagueID = leagueIDTextField.getText().toString().trim();
                League.removeMember(enteredLeagueID, currentUserID);
            }
        });
    }

    // Depreciated, kept for prosperity or later reuse
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
                            League newLeague = new League(leagueName, leagueID, auth.getUid());
                            newLeague.addToDatabase();
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
    // Depreciated, kept for prosperity or later reuse
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

                        League.addMember(leagueID, auth.getUid());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing, cancelling rename
                    }
                });
        return builder.create();
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
            Intent intent = new Intent(this, PeopleActivity.class);
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
