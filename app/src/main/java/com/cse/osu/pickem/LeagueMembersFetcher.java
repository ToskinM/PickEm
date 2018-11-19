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
import java.util.List;

public class LeagueMembersFetcher {
    public static final String TAG = "LeagueMembersFetcher";
    private static LeagueMembersFetcher sLeagueMembersFetcher;

    // List of leagues to be sent to recycler view
    private List<LeagueMemberPair> mAllMembers;

    public static LeagueMembersFetcher get() {
        if (sLeagueMembersFetcher == null) {
            sLeagueMembersFetcher = new LeagueMembersFetcher();
        }
        return sLeagueMembersFetcher;
    }

    private LeagueMembersFetcher() {
        this.mAllMembers = new ArrayList<>();
        setupDatabaseListeners();

    }

    // Returns all members of all leagues
    public List<LeagueMemberPair> getAllMembers() {
        return this.mAllMembers;
    }

    // Returns member of a specified league
    public LeagueMemberPair getMember(String leagueID) {
        for (LeagueMemberPair pair : mAllMembers) {
            if (pair.getLeagueID().equals(leagueID)) {
                return pair;
            }
        }
        return null;
    }

    // Returns members of a specified league
    public List<LeagueMemberPair> getMembersOfLeague(String leagueID) {
        List<LeagueMemberPair> membersOfLeague = new ArrayList<>();
        for(LeagueMemberPair pair : mAllMembers) {
            if (pair.getLeagueID().equals(leagueID)) {
                membersOfLeague.add(pair);
            }
        }
        return membersOfLeague;
    }

    private void setupDatabaseListeners(){
        // Get database references
        DatabaseReference leagueMemberDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");

        // League Members listener
        leagueMemberDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mAllMembers.clear();
                Log.d(TAG, dataSnapshot.getChildrenCount() + "");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LeagueMemberPair tempPair = snapshot.getValue(LeagueMemberPair.class);
                    if (!mAllMembers.contains(tempPair)) {
                        mAllMembers.add(tempPair);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
