package com.cse.osu.pickem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserGameFetcher {
    public static final String TAG = "GameFetcher";
    private static UserGameFetcher sUserGameFetcher;
    private List<Game> mFetchedGames;
    private Set<LeagueMemberPair> mFetchedGameMembers;
    private FirebaseAuth auth;
    // List of leagues to be sent to recycler view
    private List<Game> mGames;

    public static UserGameFetcher get(Context context, FirebaseAuth auth) {
        if (sUserGameFetcher == null) {
            sUserGameFetcher = new UserGameFetcher(context, auth);
        }
        return sUserGameFetcher;
    }

    private UserGameFetcher(Context context, final FirebaseAuth auth) {
        this.auth = auth;
        this.mGames = new ArrayList<>();
        this.mFetchedGameMembers = new HashSet<>();
        this.mFetchedGames = new ArrayList<>();
        setupDatabaseListeners();

    }

    public List<Game> getGames() {
        return this.mGames;
    }

    public Game getGame(String leagueID) {
        //for (Game game : mGames) {
            //if (game.getLeagueID().equals(leagueID)) {
            //    return game;
            //}
        //}
        return null;
    }

    public void updateUsersLeagues(){
        this.mGames.clear();
        // Get games of the current league
    }

    private void setupDatabaseListeners(){
        // Get database references
        DatabaseReference leaguesDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");
        DatabaseReference leagueMemberDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");

        // League Members listener
        leagueMemberDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFetchedGameMembers.clear();
                Log.d(TAG, dataSnapshot.getChildrenCount() + "");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LeagueMemberPair tempPair = snapshot.getValue(LeagueMemberPair.class);
                    if (!mFetchedGameMembers.contains(tempPair)) {
                        mFetchedGameMembers.add(tempPair);
                    }
                }
                updateUsersLeagues();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        // Leagues listener
        leaguesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFetchedGames.clear();
                Log.d(TAG, dataSnapshot.getChildrenCount() + "");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    League tempLeague = snapshot.getValue(League.class);
                    if (!mFetchedGames.contains(tempLeague)) {
                        //mFetchedGames.add(tempLeague);
                    }
                }
                updateUsersLeagues();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
