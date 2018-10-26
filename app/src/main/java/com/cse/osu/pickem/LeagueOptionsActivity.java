package com.cse.osu.pickem;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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

    private Button addGameButton;
    private Button manageGamesButton;
    private Button renameLeagueButton;
    private Button deleteLeagueButton;

    private DatabaseReference leagueReference;
    private FirebaseAuth auth;
    private League mLeague;

    protected void setupAddGameButton() {
        addGameButton = findViewById(R.id.buttonAddGame);
        addGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    protected void setupRenameLeagueButton() {
        renameLeagueButton = findViewById(R.id.buttonRenameLeague);
        renameLeagueButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               // Show rename dialog
               AlertDialog renameDialog = createRenameDialog();
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
                        Snackbar.make(d.findViewById(android.R.id.content), "Rename Successful", Snackbar.LENGTH_LONG)
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
        leagueReference.addListenerForSingleValueEvent(new ValueEventListener() {
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

        leagueReference = FirebaseDatabase.getInstance().getReference("leagues");
        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_league_options);

        // Wire-up buttons
        setupAddGameButton();
        setupRenameLeagueButton();

        // Display league's name at the top
        TextView leagueNameTextView = findViewById(R.id.textViewLeagueName);
        leagueNameTextView.setText(mLeague.getLeagueName());

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
