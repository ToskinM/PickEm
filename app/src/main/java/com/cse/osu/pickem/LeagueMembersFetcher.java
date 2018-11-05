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

public class LeagueMembersFetcher {
    public static final String TAG = "LeagueMembersFetcher";
    private static LeagueMembersFetcher sLeagueMembersFetcher;
    private Set<LeagueMemberPair> mFetchedLeagueMembers;

    // List of leagues to be sent to recycler view
    private List<LeagueMemberPair> mMembers;

    public static LeagueMembersFetcher get(Context context, FirebaseAuth auth) {
        if (sLeagueMembersFetcher == null) {
            sLeagueMembersFetcher = new LeagueMembersFetcher(context, auth);
        }
        return sLeagueMembersFetcher;
    }

    private LeagueMembersFetcher(Context context, final FirebaseAuth auth) {
        this.mMembers = new ArrayList<>();
        this.mFetchedLeagueMembers = new HashSet<>();
        setupDatabaseListeners();

    }

    public List<LeagueMemberPair> getMembers() {
        return this.mMembers;
    }

    public LeagueMemberPair getMember(String leagueID) {
        for (LeagueMemberPair pair : mMembers) {
            if (pair.getLeagueID().equals(leagueID)) {
                return pair;
            }
        }
        return null;
    }

    public void updateLeagueMembers(String leagueID){
        this.mMembers.clear();
        for(LeagueMemberPair pair : this.mFetchedLeagueMembers) {
            if (pair.getLeagueID().equals(leagueID)) {
                this.mMembers.add(pair);
            }
        }
    }

    private void setupDatabaseListeners(){
        // Get database references
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
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
