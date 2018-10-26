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

public class UserLeagueFetcher {
    public static final String TAG = "LeagueFetcher";
    private static UserLeagueFetcher sUserLeagueFetcher;
    private List<League> mFetchedLeagues;
    private Set<LeagueMemberPair> mFetchedLeagueMembers;
    private FirebaseAuth auth;

    // List of leagues to be sent to recycler view
    private List<League> mLeagues;

    public static UserLeagueFetcher get(Context context, FirebaseAuth auth) {
        if (sUserLeagueFetcher == null) {
            sUserLeagueFetcher = new UserLeagueFetcher(context, auth);
        }
        return sUserLeagueFetcher;
    }

    private UserLeagueFetcher(Context context, final FirebaseAuth auth) {
        this.auth = auth;
        this.mLeagues = new ArrayList<>();
        this.mFetchedLeagueMembers = new HashSet<>();
        this.mFetchedLeagues = new ArrayList<>();
        setupDatabaseListeners();

    }

    public List<League> getLeagues() {
        return this.mLeagues;
    }

    public League getLeague(String leagueID) {
        for (League league : mLeagues) {
            if (league.getLeagueID().equals(leagueID)) {
                return league;
            }
        }
        return null;
    }

    public void updateUsersLeagues(){
        this.mLeagues.clear();
        // Get leagues user is a member of
        for(LeagueMemberPair pair : this.mFetchedLeagueMembers) {
            if (pair.getUID().equals(auth.getUid())){
                for(League league : this.mFetchedLeagues) {
                    if (league.getLeagueID().equals(pair.getLeagueID())){
                        this.mLeagues.add(league);
                    }
                }
            }
        }
    }

    private void setupDatabaseListeners(){
        // Get database references
        DatabaseReference leaguesDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");
        DatabaseReference leagueMemberDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");

        // League Members listener
        leagueMemberDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFetchedLeagueMembers.clear();
                Log.d(TAG, dataSnapshot.getChildrenCount() + "");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LeagueMemberPair tempPair = snapshot.getValue(LeagueMemberPair.class);
                    if (!mFetchedLeagueMembers.contains(tempPair)) {
                        mFetchedLeagueMembers.add(tempPair);
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
                mFetchedLeagues.clear();
                Log.d(TAG, dataSnapshot.getChildrenCount() + "");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    League tempLeague = snapshot.getValue(League.class);
                    if (!mFetchedLeagues.contains(tempLeague)) {
                        mFetchedLeagues.add(tempLeague);
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
