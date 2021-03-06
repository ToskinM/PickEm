package com.cse.osu.pickem;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import java.util.Map;

public class LeagueListFragment extends Fragment {
    public static final String TAG = "LeagueListFragment";
    private RecyclerView mLeagueRecyclerView;
    private DatabaseReference leaguesDatabaseReference;
    private LeagueAdapter mAdapter;
    private FirebaseAuth auth;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void renameLeague(final String leagueID, final String newName) {
        leaguesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_league_list, container, false);

        // Get user auth data
        auth = FirebaseAuth.getInstance();

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                updateUI();
            }
        });

        // Setup recycler view
        mLeagueRecyclerView = view.findViewById(R.id.league_recycler_view);
        mLeagueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Update the list
        //updateUI();

        // Setup Listeners
        setupDatabaseListeners();

        return view;
    }

        @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void updateUI() {
        // Get user's owned leagues
        LeagueFetcher leagueFetcher = LeagueFetcher.get();
        List<League> leagues = leagueFetcher.getUserLeagues(auth.getUid());

        mAdapter = new LeagueAdapter(leagues);
        mLeagueRecyclerView.removeAllViews();
        mAdapter.notifyDataSetChanged();
        mLeagueRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }


    private void deleteLeague(final String leagueID) {
        //Need to access firebase, set up listener
        leaguesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //Get the current league on firebase we are looking at
                    League snapshotLeague = snapshot.getValue(League.class);

                    //If current user owns the league, and the league is the target league,
                    if (snapshotLeague.getLeagueOwnerUID().equals(auth.getUid()) && snapshotLeague.getLeagueID().equals(leagueID)) {

                        //Delete all of the league member pairs related to the target league
                        deleteLeagueMembers(snapshotLeague.getLeagueID());

                        //Delete league.
                        leaguesDatabaseReference.child(leagueID).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Log.d(TAG, leagueID + " has been deleted.");
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
    protected void deleteLeagueMembers(final String leagueID) {
        FirebaseDatabase.getInstance().getReference("leagueMembers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LeagueMemberPair pair = snapshot.getValue(LeagueMemberPair.class);
                    //If the pair being examined is the target
                    if (pair.getLeagueID().equals(leagueID)) {

                        //Get a reference to the pair, and then delete it.
                        snapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Log.d(TAG, "League members deleted for the League: " + leagueID);
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

    private void setupDatabaseListeners(){
        // Get database references
        DatabaseReference leaguesDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");
        DatabaseReference leagueMemberDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");

        // League Members listener
        leagueMemberDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateUI();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        // Leagues listener
        leaguesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateUI();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //// A "container" of recyclerView that holds a list item (a league)
    private class LeagueHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mLeagueNameTextView;
        private League mLeague;

        public LeagueHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_league, parent, false));
            itemView.setOnClickListener(this);
            mLeagueNameTextView = itemView.findViewById(R.id.league_name);

        }
        public void bind(League league) {
            mLeague = league;
            mLeagueNameTextView.setText(mLeague.getLeagueName());
        }
        @Override
        public void onClick(View view) {
            //// Show league options popup dialog
            //Toast.makeText(getActivity(),
            //        "Owned by: " + mLeague.getLeagueOwnerUID(), Toast.LENGTH_SHORT)
            //        .show();
            //AlertDialog leagueActionsDialog = createLeagueActionsDialog();
            //leagueActionsDialog.show();

            // Launch league options activity, sending the League to manage via the intent
            Intent intent = new Intent(getActivity(), LeagueOptionsActivity.class);
            intent.putExtra("league", mLeague);
            startActivity(intent);
        }

        private AlertDialog createLeagueActionsDialog() {
            Context context = getActivity();
            if (context != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                String[] items = {"Rename", "Delete", "Cancel"};
                builder.setTitle(mLeague.getLeagueName())
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    AlertDialog renameDialog = createRenameDialog();
                                    renameDialog.show();
                                } else if (which == 1){
                                    AlertDialog deleteDialog = createDeleteConfirmationDialogue();
                                    deleteDialog.show();
                                }
                            }
                        });
                return builder.create();
            } else {
                // Make this better
                return null;
            }
        }

        private AlertDialog createRenameDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
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
                            Toast.makeText(getActivity(),
                                    "League successfully renamed to: " + newName, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Do nothing, cancelling rename
                        }
                    });
            return builder.create();
        }

        private AlertDialog createDeleteConfirmationDialogue() {
            Context context = getActivity();
            if (context != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Are you sure about that?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                deleteLeague(mLeague.getLeagueID());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Do nothing, cancelling rename
                            }
                        });
                return builder.create();
            } else {
                // Make this better
                return null;
            }
        }
    }

    //// Links leagues to holders in the recyclerView
    private class LeagueAdapter extends RecyclerView.Adapter<LeagueHolder> {
        private List<League> mLeagues;

        public LeagueAdapter(List<League> leagues) {
            mLeagues = leagues;
        }

        @Override
        public LeagueHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new LeagueHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull LeagueHolder holder, int position) {
            League league = mLeagues.get(position);
            holder.bind(league);
        }

        @Override
        public int getItemCount() {
            return mLeagues.size();
        }
    }
}
