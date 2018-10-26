package com.cse.osu.pickem;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

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

    protected void setupAddGameButton() {
        addGameButton = findViewById(R.id.buttonRenameLeague);
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
               //On click, open thing and do stuff
           }
        });
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

        leagueReference = FirebaseDatabase.getInstance().getReference("leagues");
        auth = FirebaseAuth.getInstance();
        setupAddGameButton();

        setContentView(R.layout.activity_league_options);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
