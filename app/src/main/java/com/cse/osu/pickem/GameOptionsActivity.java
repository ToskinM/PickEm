package com.cse.osu.pickem;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GameOptionsActivity extends AppCompatActivity {

    private Button addPickButton;
    private DatabaseReference gameDatabaseReference;
    private DatabaseReference pickDatabaseReference;
    private FirebaseAuth auth;
    private Game mGame;
    private AlertDialog addPickDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_options);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addPickButton = findViewById(R.id.buttonMakePick);
        gameDatabaseReference = FirebaseDatabase.getInstance().getReference("games");
        pickDatabaseReference = FirebaseDatabase.getInstance().getReference("picks");
        auth = FirebaseAuth.getInstance();

        addPickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPickDialog.show();
            }
        });

        Intent creatorIntent = getIntent();
        addPickDialog = addPickDialog();
        mGame = creatorIntent.getParcelableExtra("game");
    }

    protected void addPick(Pick pick) {
        pickDatabaseReference.push().setValue(pick);
    }

    protected AlertDialog addPickDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter pick information");
        LayoutInflater inflater = GameOptionsActivity.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_game_addpick, null))
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog d = (Dialog) dialog;

                        if (mGame == null) {
                            Log.d("PickEm", "mGame is null!");
                        }

                        int teamAScore = Integer.parseInt(((EditText)d.findViewById(R.id.teamA_prediction_text)).getText().toString().trim());
                        int teamBScore = Integer.parseInt(((EditText)d.findViewById(R.id.teamB_prediction_text)).getText().toString().trim());
                        Pick myPick = new Pick(auth.getUid(), mGame.getGameID(), teamAScore, teamBScore);
                        addPick(myPick);


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
