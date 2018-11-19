package com.cse.osu.pickem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class LeaderboardFragment extends Fragment {
    public static final String TAG = "HomeListFragment";
    private TableLayout mTableLayout;
    private FirebaseAuth auth;
    private String mLeagueID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        // Get leagueID from launching activity
        if (getArguments() != null){
            mLeagueID = getArguments().getString("leagueID");
        }

        // Get user auth data
        auth = FirebaseAuth.getInstance();

        // Initialize Table
        mTableLayout = view.findViewById(R.id.table_leaderboard);
        buildLeaderoard();

        // Setup Listeners
        setupDatabaseListeners();

        return view;
    }

    private void buildLeaderoard(){
        mTableLayout.removeAllViewsInLayout();
        LeagueMembersFetcher leagueMembersFetcher = LeagueMembersFetcher.get();
        List<LeagueMemberPair> leagueMembers = leagueMembersFetcher.getMembersOfLeague(mLeagueID);
        for (LeagueMemberPair member : leagueMembers){
            addLeaderboardRow(member.getUID(), member.getPoints());
        }
    }

    private void addLeaderboardRow(String username, int score){
        Context context = getActivity();

        // Create row object
        TableRow tableRow = new TableRow(context);

        // Set new table row layout parameters.
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(layoutParams);

        // Add a TextView for Username in the first column.
        TextView usernameTextView = new TextView(context);
        usernameTextView.setText(username);
        tableRow.addView(usernameTextView, 0);

        // Add a TextView for Score in the first column
        TextView scoreTextView = new TextView(context);
        scoreTextView.setText(Integer.toString(score));
        tableRow.addView(scoreTextView, 1);

        // Add row to table
        mTableLayout.addView(tableRow);
    }

    private void setupDatabaseListeners(){
        // Get database references
        DatabaseReference gamesDatabaseReference = FirebaseDatabase.getInstance().getReference("games");
        gamesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
