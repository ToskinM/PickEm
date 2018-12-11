package com.cse.osu.pickem;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LeaguesFragment extends Fragment {
    public static final String TAG = "LeaguesFragment";

    private EditText leagueIDTextField;
    private EditText leagueNameTextField;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leagues, container, false);
        Log.d(TAG, "onCreateView() called!");

        auth = FirebaseAuth.getInstance();

        // Create league RecyclerView Fragment
        FragmentManager fm = getChildFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new LeagueListFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        // Get user auth data
        auth = FirebaseAuth.getInstance();

        setUp(view);

        return view;
    }

    private void setUp(View view) {
        leagueIDTextField = view.findViewById(R.id.league_id_field);
        leagueNameTextField = view.findViewById(R.id.league_name_field);

        handleCreateLeagueButton(view);
        handleJoinLeagueButton(view);
        handleLeaveLeagueButton(view);
    }

    private void handleCreateLeagueButton(View view) {
        // League Creation
        Button createLeagueButton = view.findViewById(R.id.buttonCreateLeague);
        createLeagueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = leagueIDTextField.getText().toString().trim();
                if (!id.equals("")) {
                    // Cancel process if league with same ID already exists
                    for (League league : LeagueFetcher.get().getAllLeagues()){
                        if (id.equals(league.getLeagueID())){
                            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
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
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setMessage("League ID cannot be empty!");
                    alertDialog.show();
                }
            }
        });
    }

    private void handleJoinLeagueButton(View view) {
        // League Join
        Button joinLeagueButton = view.findViewById(R.id.buttonJoinLeague);
        joinLeagueButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                leagueNameTextField.setText("");  // Clear text in league name
                League.addMember(leagueIDTextField.getText().toString().trim(), auth.getUid());
            }
        });
    }

    private void handleLeaveLeagueButton(View view) {
        Button leaveLeagueButton = view.findViewById(R.id.buttonLeaveLeague);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
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
                            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
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
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
