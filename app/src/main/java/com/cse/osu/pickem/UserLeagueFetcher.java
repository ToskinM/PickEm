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
    private DatabaseReference leaguesDatabaseReference;
    private DatabaseReference leagueMemberDatabaseReference;
    private List<League> loadedLeagues = new ArrayList<>();
    private Set<LeagueMemberPair> loadedLeagueMemberPairs;
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
        updateLeagues();
    }

    public List<League> getLeagues() {
        return mLeagues;
    }

    public League getLeague(String leagueID) {
        for (League league : mLeagues) {
            if (league.getLeagueID().equals(leagueID)) {
                return league;
            }
        }

        return null;
    }

    public void updateLeagues(){
        mLeagues = new ArrayList<>();
        loadedLeagueMemberPairs = new HashSet<LeagueMemberPair>();
        loadedLeagues = new ArrayList<>();

        // Get all members of leagues
        leagueMemberDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");
        leagueMemberDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.getChildrenCount() + "");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LeagueMemberPair tempPair = snapshot.getValue(LeagueMemberPair.class);
                    if (!loadedLeagueMemberPairs.contains(tempPair)) {
                        loadedLeagueMemberPairs.add(tempPair);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        // Get all leagues
        leaguesDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");
        leaguesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.getChildrenCount() + "");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    League tempLeague = snapshot.getValue(League.class);
                    if (!loadedLeagues.contains(tempLeague)) {
                        loadedLeagues.add(tempLeague);
                    }
                }
                // Get leagues user is a member of
                for(LeagueMemberPair pair : loadedLeagueMemberPairs) {
                    if (pair.getUID().equals(auth.getUid())){
                        for(League league : loadedLeagues) {
                            if (league.getLeagueID().equals(pair.getLeagueID())){
                                mLeagues.add(league);
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
