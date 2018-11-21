package com.cse.osu.pickem;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameOptionsActivity extends AppCompatActivity {

    private Button addPickButton;
    private Button endGameButton;
    private Button editPickButton;

    private EditText teamAEditText;
    private EditText teamBEditText;

    private DatabaseReference pickDatabaseReference;
    private FirebaseAuth auth;
    private Game mGame;
    private AlertDialog addPickDialog;
    private AlertDialog endGameDialog;
    private AlertDialog editPickDialog;
    private Map<Pick, String> pickToPickID;
    private boolean gameTimeExpired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_options);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent creatorIntent = getIntent();
        mGame = creatorIntent.getParcelableExtra("game");
        setTitle(mGame.getFirstTeamName() + " vs. " + mGame.getSecondTeamName());
        TextView titleView = findViewById(R.id.gameTitleView);
        titleView.setText(mGame.getFirstTeamName() + " vs. " + mGame.getSecondTeamName());

        addPickButton = findViewById(R.id.buttonMakePick);
        endGameButton = findViewById(R.id.buttonEndGame);
        editPickButton = findViewById(R.id.buttonEditPick);


        addPickDialog = addPickDialog();
        endGameDialog = endGameDialog();
        editPickDialog = editPickDialog();

        pickDatabaseReference = FirebaseDatabase.getInstance().getReference("picks");
        auth = FirebaseAuth.getInstance();

        pickToPickID = new HashMap<>();
        //Load picks into local data structure for less garbage code below <3
        updatePickMap(pickToPickID);

        addPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGame.isPastLockTime()) {
                    addPickDialog.show();
                } else {
                    Toast.makeText(GameOptionsActivity.this, "Picking for this game is closed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        editPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGame.isPastLockTime()) {
                    editPickDialog.show();
                } else {
                    Toast.makeText(GameOptionsActivity.this, "Picking for this game is closed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        endGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endGameDialog.show();
            }
        });
    }

    protected void updatePickMap(final Map<Pick, String> pickToPickIDIn) {
        pickToPickIDIn.clear();

        pickDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pick pick = snapshot.getValue(Pick.class);
                    if (pick.getGameID().equals(mGame.getGameID())) {
                        String pickKey = dataSnapshot.getRef().getParent().getKey();
                        pickToPickIDIn.put(pick, pickKey);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    protected boolean addPick(final Pick pick) {
        boolean returnValue = true;
        if (!pickAlreadyExistsForCurrentUser()) {
            DatabaseReference newRef = pickDatabaseReference.push();
            String pickKey = newRef.getKey();
            pick.setPickID(pickKey);
            newRef.setValue(pick);
        } else {
            returnValue = false;
        }

        updatePickMap(pickToPickID);

        return returnValue;
    }

    protected boolean editPick(final Pick newPick) {
        pickDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pick workingPick = snapshot.getValue(Pick.class);

                    if (workingPick.getGameID().equals(newPick.getGameID()) && workingPick.getUserID().equals(newPick.getUserID())) {
                        snapshot.getRef().setValue(newPick);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return true;
    }

    protected AlertDialog editPickDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit pick information");
        LayoutInflater inflater = GameOptionsActivity.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_game_editpick, null))
                .setPositiveButton("Edit Pick", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog d = (Dialog) dialog;

                        int teamAScore = Integer.parseInt(((EditText) d.findViewById(R.id.teamA_edit_prediction_text)).getText().toString().trim());
                        int teamBScore = Integer.parseInt(((EditText) d.findViewById(R.id.teamB_edit_prediction_text)).getText().toString().trim());
                        Pick myPick = new Pick(auth.getUid(), mGame.getGameID(), teamAScore, teamBScore);
                        if (editPick(myPick)) {
                            Toast.makeText(GameOptionsActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GameOptionsActivity.this, "Failure! Pick doesn't exist!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //nothing
            }
        });


        return builder.create();
    }

    protected AlertDialog addPickDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter pick information");
        LayoutInflater inflater = GameOptionsActivity.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_game_addpick, null))
                .setPositiveButton("Make Pick", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog d = (Dialog) dialog;

                        int teamAScore = Integer.parseInt(((EditText) d.findViewById(R.id.teamA_prediction_text)).getText().toString().trim());
                        int teamBScore = Integer.parseInt(((EditText) d.findViewById(R.id.teamB_prediction_text)).getText().toString().trim());
                        Pick myPick = new Pick(auth.getUid(), mGame.getGameID(), teamAScore, teamBScore);
                        if (addPick(myPick)) {
                            Toast.makeText(GameOptionsActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GameOptionsActivity.this, "Failed! Pick exists!", Toast.LENGTH_SHORT).show();
                        }


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

    protected boolean pickAlreadyExistsForCurrentUser() {
        boolean returnValue = false;

        for (Map.Entry<Pick, String> testPair : pickToPickID.entrySet()) {
            Pick workingPick = testPair.getKey();
            if (workingPick.getUserID().equals(auth.getUid()) && workingPick.getGameID().equals(mGame.getGameID())) {
                returnValue = true;
            }
        }

        return returnValue;

    }

    protected AlertDialog endGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("End Game?");
        LayoutInflater inflater = GameOptionsActivity.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_game_endgame, null))
                .setPositiveButton("Finalize", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog d = (Dialog) dialog;
                        EditText actualAScore = d.findViewById(R.id.teamA_actualScore_text);
                        EditText actualBScore = d.findViewById(R.id.teamB_actualScore_text);
                        int aScore = Integer.parseInt(actualAScore.getText().toString().trim());
                        int bScore = Integer.parseInt(actualBScore.getText().toString().trim());
                        mGame.fastEndGame(aScore, bScore);
                        finish();
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

}
