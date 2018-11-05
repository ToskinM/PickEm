package com.cse.osu.pickem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PeopleLeagueFragment extends Fragment {
    public static final String TAG = "LeagueListFragment";
    private RecyclerView mPeopleRecyclerView;
    private DatabaseReference leaguesDatabaseReference;
    private LeagueAdapter mAdapter;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people_list, container, false);

        // Get user auth data
        auth = FirebaseAuth.getInstance();

        // Setup recycler view
        mPeopleRecyclerView = view.findViewById(R.id.leaguePeople_recycler_view);
        mPeopleRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Update the list
        updateUI();

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
        UserLeagueFetcher userLeagueFetcher = UserLeagueFetcher.get(getActivity(), auth);
        userLeagueFetcher.updateUsersLeagues();

        // Filter out only the owned leagues
        List<League> leagues = userLeagueFetcher.getLeagues();
        List<League> ownedLeagues = new ArrayList<League>();
        for (League league : leagues) {
            if (league.getLeagueOwnerUID().equals(auth.getUid()))
                ownedLeagues.add(league);
        }

        mAdapter = new LeagueAdapter(ownedLeagues);
        mPeopleRecyclerView.removeAllViews();
        mAdapter.notifyDataSetChanged();
        mPeopleRecyclerView.setAdapter(mAdapter);
    }

    private void setupDatabaseListeners(){
        // Get database references
        DatabaseReference leaguesDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");

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
            // Launch league options activity, sending the League to manage via the intent
            Intent intent = new Intent(getActivity(), PeopleMembersActivity.class);
            intent.putExtra("league", mLeague);
            startActivity(intent);
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
