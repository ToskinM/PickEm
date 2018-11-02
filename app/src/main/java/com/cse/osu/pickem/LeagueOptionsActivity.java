package com.cse.osu.pickem;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import java.util.HashMap;
import java.util.Map;

public class LeagueOptionsActivity extends AppCompatActivity {

    public static final String TAG = "LeagueOptionsActivity";

    private Button addGameButton;
    private Button manageGamesButton;
    private Button renameLeagueButton;
    private Button deleteLeagueButton;
    private Button backButton;
    private AlertDialog renameDialog;
    private AlertDialog deleteDialog;

    private DatabaseReference leagueDatabaseReference;
    private DatabaseReference leagueMembersDatabaseReference;
    private DatabaseReference gameDatabaseReference;
    private FirebaseAuth auth;
    private League mLeague;

    protected void setupAddGameButton() {
        addGameButton = findViewById(R.id.buttonAddGame);
        addGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGameDialog().show();
            }
        });
    }

    protected AlertDialog addGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter game information");
        LayoutInflater inflater = LeagueOptionsActivity.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_league_addgame, null))
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Dialog d = (Dialog) dialog;
                String teamAText = ((EditText)d.findViewById(R.id.teamA_text)).getText().toString().trim();
                String teamBText = ((EditText)d.findViewById(R.id.teamB_text)).getText().toString().trim();
                String leagueID = mLeague.getLeagueID();
                Log.d("PickEm", "YEET:" + leagueID);

                Game newGame = new Game(teamAText, teamBText, leagueID);
                addGame(newGame);
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Nothing
            }
        });

        return builder.create();
    }

    protected void addGame(Game gameIn) {
        DatabaseReference tempReference = gameDatabaseReference.push();
        gameIn.setGameID(tempReference.getKey());
        gameDatabaseReference.push().setValue(gameIn);
    }

    protected AlertDialog createDeleteLeagueDialog() {
        // Build the confirmation dialog once, ahead of time
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure about that?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        deleteLeague();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing, cancelling delete
                    }
                });
        return builder.create();

        //final AlertDialog deleteConfirmationDialog = builder.create();
        // Wire button to show confirmation
        //deleteLeagueButton = findViewById(R.id.buttonDeleteLeague);
        //deleteLeagueButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        deleteConfirmationDialog.show();
        //    }
        //});
    }

    protected void setupRenameLeagueButton() {
        // Build the confirmation dialog once, ahead of time
        final AlertDialog renameDialog = createRenameDialog();

        // Wire button to show dialog
        renameLeagueButton = findViewById(R.id.buttonRenameLeague);
        renameLeagueButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               renameDialog.show();
           }
        });
    }

    private AlertDialog createRenameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LeagueOptionsActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = LeagueOptionsActivity.this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_league_rename, null))
                // Add action buttons
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;

                        // Get new name for league
                        EditText newNameEditText = d.findViewById(R.id.league_rename);
                        String newName = newNameEditText.getText().toString().trim();

                        // Rename the league
                        renameLeague(mLeague.getLeagueID(), newName);

                        // Tell user league was renamed successfully
                        Snackbar.make(LeagueOptionsActivity.this.findViewById(android.R.id.content), "Rename Successful", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing, cancelling rename
                    }
                });
        return builder.create();
    }

    protected void renameLeague(final String leagueID, final String newName) {
        leagueDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    League snapshotLeague = snapshot.getValue(League.class);

                    //If current user owns the league, and the league being examined is the target,
                    if (snapshotLeague.getLeagueOwnerUID().equals(auth.getUid()) && snapshotLeague.getLeagueID().equals(leagueID)) {

                        //Create a new map to pass into updateChildren()
                        Map<String, Object> childrenMap = new HashMap<>();

                        //Add the league we want to change as the key, and the new League as the value
                        childrenMap.put(snapshotLeague.getLeagueID(), new League(newName, snapshotLeague.getLeagueID(), snapshotLeague.getLeagueOwnerUID()));

                        //Now actually update
                        snapshot.getRef().getParent().updateChildren(childrenMap);
                    }
                }
                TextView leagueNameTextView = findViewById(R.id.textViewLeagueName);
                leagueNameTextView.setText(newName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteLeague() {
        //Need to access firebase, set up listener
        leagueDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //Get the current league on Firebase we are looking at
                    League snapshotLeague = snapshot.getValue(League.class);

                    //If current user owns the league, and the league is the target league,
                    if (snapshotLeague.getLeagueOwnerUID().equals(auth.getUid()) && snapshotLeague.getLeagueID().equals(mLeague.getLeagueID())) {

                        //Delete all of the league member pairs related to the target league
                        deleteLeagueMembers();

                        //Delete league.
                        leagueDatabaseReference.child(mLeague.getLeagueID()).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Log.d(TAG, mLeague.getLeagueID() + " has been deleted.");
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        finish();
    }
    protected void deleteLeagueMembers() {
        leagueMembersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LeagueMemberPair pair = snapshot.getValue(LeagueMemberPair.class);
                    //If the pair being examined is the target
                    if (pair.getLeagueID().equals(mLeague.getLeagueID())) {

                        //Get a reference to the pair, and then delete it.
                        snapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Log.d(TAG, "League members deleted for the League: " + mLeague.getLeagueID());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the league we're working with from intent
        Intent creatorIntent = getIntent();
        mLeague = creatorIntent.getParcelableExtra("league");

        // Get Firebase Database references
        leagueDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");
        leagueMembersDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");
        gameDatabaseReference = FirebaseDatabase.getInstance().getReference("games");
        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_league_options);


        // Create game RecyclerView Fragment
        Log.d(TAG, mLeague.getLeagueID());
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            Bundle bundle = new Bundle();
            bundle.putString("leagueID", mLeague.getLeagueID());
            fragment = new GameListFragment();
            fragment.setArguments(bundle);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        // Setup Dialogs
        setupAddGameButton();
        setupRenameLeagueButton();
        backButton = findViewById(R.id.buttonBackToLeagues);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(LeagueOptionsActivity.this, LeagueActivity.class);
                startActivity(newIntent);
            }
        });
        renameDialog = createRenameDialog();
        deleteDialog = createDeleteLeagueDialog();

        // Display league's name at the top
        TextView leagueNameTextView = findViewById(R.id.textViewLeagueName);
        leagueNameTextView.setText(mLeague.getLeagueName());

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_league_options, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_addGame:
                // Add Game here
            case R.id.action_rename:
                renameDialog.show();
            case R.id.action_delete:
                deleteDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }
}
