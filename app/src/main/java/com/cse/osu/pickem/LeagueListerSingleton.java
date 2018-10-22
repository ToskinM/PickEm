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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LeagueListerSingleton {
    private static LeagueListerSingleton sLeagueListerSingleton;
    private DatabaseReference leaguesDatabaseReference;
    private DatabaseReference leagueMemberDatabaseReference;
    private List<League> mLeagues;


    public static LeagueListerSingleton get(Context context) {
        if (sLeagueListerSingleton == null) {
            sLeagueListerSingleton = new LeagueListerSingleton(context);
        }
        return sLeagueListerSingleton;
    }

    private LeagueListerSingleton(Context context) {
        mLeagues = new ArrayList<>();

        leaguesDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");
        leagueMemberDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");
        leaguesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mLeagues.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    mLeagues.add(data.getValue(League.class));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("PickEm", "ON CANCELLED!");
            }
        });
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
}
