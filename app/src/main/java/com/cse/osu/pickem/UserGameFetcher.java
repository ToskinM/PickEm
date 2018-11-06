package com.cse.osu.pickem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserGameFetcher {
    public static final String TAG = "GameFetcher";
    private static UserGameFetcher sUserGameFetcher;
    private List<Game> mFetchedGames;

    public static UserGameFetcher get(Context context) {
        if (sUserGameFetcher == null) {
            sUserGameFetcher = new UserGameFetcher(context);
        }
        return sUserGameFetcher;
    }

    private UserGameFetcher(Context context) {
        this.mFetchedGames = new ArrayList<>();
        setupDatabaseListeners();
    }

    public List<Game> getGames(String leagueID) {
        List<Game> leagueGames = new ArrayList<>();
        for(Game game : mFetchedGames) {
            if (game.getLeagueID().equals(leagueID)) {
                leagueGames.add(game);
            }
        }
        return leagueGames;
    }
    public List<Game> getAllGames() {
        return mFetchedGames;
    }

    private void setupDatabaseListeners(){
        // Get database references
        DatabaseReference gamesDatabaseReference = FirebaseDatabase.getInstance().getReference("games");

        // maintain an updated list of all games
        gamesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFetchedGames.clear();
                Log.d(TAG, dataSnapshot.getChildrenCount() + "");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Game tempGame = snapshot.getValue(Game.class);
                    if (!mFetchedGames.contains(tempGame)) {
                        mFetchedGames.add(tempGame);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
}
