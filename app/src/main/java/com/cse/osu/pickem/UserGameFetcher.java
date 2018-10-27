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
    private String mLeagueID;
    // List of leagues to be sent to recycler view
    private List<Game> mLeagueGames;

    public static UserGameFetcher get(Context context, String leagueID) {
        if (sUserGameFetcher == null) {
            sUserGameFetcher = new UserGameFetcher(context, leagueID);
        }
        return sUserGameFetcher;
    }

    private UserGameFetcher(Context context, final String leagueID) {
        this.mLeagueID = leagueID;
        this.mFetchedGames = new ArrayList<>();
        setupDatabaseListeners();

    }

    public List<Game> getGames() {
        return this.mLeagueGames;
    }


    private void setupDatabaseListeners(){
        // Get database references
        DatabaseReference gamesDatabaseReference = FirebaseDatabase.getInstance().getReference("games");

        // League Members listener
        gamesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFetchedGames.clear();
                Log.d(TAG, dataSnapshot.getChildrenCount() + "");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Game tempGame = snapshot.getValue(Game.class);
                    if (tempGame.getLeagueID().equals(mLeagueID)) {
                        mLeagueGames.add(tempGame);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
}
