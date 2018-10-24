package com.cse.osu.pickem;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class LeagueListFragment extends Fragment {
    public static final String TAG = "LeagueListFragment";
    private RecyclerView mLeagueRecyclerView;
    private DatabaseReference leaguesDatabaseReference;
    private LeagueAdapter mAdapter;
    private FirebaseAuth auth;
    private TextView leagueRenameTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_league_list, container, false);

        // Get user auth data
        auth = FirebaseAuth.getInstance();

        // Setup recycler view
        mLeagueRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mLeagueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Update the list
        leaguesDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");
        leaguesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateUI();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

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
        UserLeagueFetcher userLeagueFetcher = UserLeagueFetcher.get(getActivity(), auth);
        userLeagueFetcher.updateLeagues();
        List<League> leagues = userLeagueFetcher.getLeagues();

        mAdapter = new LeagueAdapter(leagues);
        mLeagueRecyclerView.removeAllViews();
        mAdapter.notifyDataSetChanged();
        mLeagueRecyclerView.setAdapter(mAdapter);
    }


    private class LeagueHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mLeagueNameTextView;
        private League mLeague;

        public LeagueHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_league, parent, false));
            itemView.setOnClickListener(this);
            mLeagueNameTextView = (TextView) itemView.findViewById(R.id.league_name);

        }
        public void bind(League league) {
            mLeague = league;
            mLeagueNameTextView.setText(mLeague.getLeagueName());
        }
        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(),
                    "Owned by: " + mLeague.getLeagueOwnerUID(), Toast.LENGTH_SHORT)
                    .show();
            AlertDialog leagueActionsDialog = createLeagueActionsDialog(mLeague);
            leagueActionsDialog.show();
        }

        private AlertDialog createLeagueActionsDialog(final League league) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String[] items = {"Rename", "Delete", "Cancel"};
            builder.setTitle(league.getLeagueName())
                    .setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0){
                                AlertDialog renameDialog = createRenameDialog(league);
                                renameDialog.show();
                            }
                        }
                    });
            return builder.create();
        }
        private AlertDialog createRenameDialog(League league) {
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
                            EditText newNameEditText = d.findViewById(R.id.league_rename);
                            String newName = newNameEditText.getText().toString().trim();
                            Toast.makeText(getActivity(),
                                    newName, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //LoginDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }

    private class LeagueAdapter extends RecyclerView.Adapter<LeagueHolder> {
        private List<League> mLeagues;

        public LeagueAdapter(List<League> leagues) {
            mLeagues = leagues;
        }

        @Override
        public LeagueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new LeagueHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(LeagueHolder holder, int position) {
            League league = mLeagues.get(position);
            holder.bind(league);
        }

        @Override
        public int getItemCount() {
            return mLeagues.size();
        }
    }
}
