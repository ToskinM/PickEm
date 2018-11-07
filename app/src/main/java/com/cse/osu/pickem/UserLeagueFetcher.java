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

public class UserLeagueFetcher {
    public static final String TAG = "LeagueFetcher";
    private static UserLeagueFetcher sUserLeagueFetcher;

    private List<League> mAllLeagues;
    private List<LeagueMemberPair> mAllLeagueMembers;

    public static UserLeagueFetcher get(Context context) {
        if (sUserLeagueFetcher == null) {
            sUserLeagueFetcher = new UserLeagueFetcher(context);
        }
        return sUserLeagueFetcher;
    }

    private UserLeagueFetcher(Context context) {
        this.mAllLeagues = new ArrayList<>();
        this.mAllLeagueMembers = new ArrayList<>();
        setupDatabaseListeners();

    }

    // returns all leagues in DB
    public List<League> getAllLeagues() {
        return this.mAllLeagues;
    }

    // Get all leagues user is member of
    public List<League> getUserLeagues(String userID) {
        List<League> userLeagues = new ArrayList<>();
        for(LeagueMemberPair pair : this.mAllLeagueMembers) {
            if (pair.getUID().equals(userID)){
                for(League league : this.mAllLeagues) {
                    if (league.getLeagueID().equals(pair.getLeagueID())){
                        userLeagues.add(league);
                    }
                }
            }
        }
        return userLeagues;
    }

    private void setupDatabaseListeners(){
        // Get database references
        DatabaseReference leaguesDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");
        DatabaseReference leagueMemberDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");

        // League Members listener
        leagueMemberDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mAllLeagueMembers.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LeagueMemberPair tempPair = snapshot.getValue(LeagueMemberPair.class);
                    if (!mAllLeagueMembers.contains(tempPair)) {
                        mAllLeagueMembers.add(tempPair);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        // Leagues listener
        leaguesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mAllLeagues.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    League tempLeague = snapshot.getValue(League.class);
                    if (!mAllLeagues.contains(tempLeague)) {
                        mAllLeagues.add(tempLeague);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
