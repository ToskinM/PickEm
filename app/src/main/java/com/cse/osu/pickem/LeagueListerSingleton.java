package com.cse.osu.pickem;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeagueListerSingleton {
    private static LeagueListerSingleton sLeagueListerSingleton;

    private List<League> mLeagues;


    public static LeagueListerSingleton get(Context context) {
        if (sLeagueListerSingleton == null) {
            sLeagueListerSingleton = new LeagueListerSingleton(context);
        }
        return sLeagueListerSingleton;
    }

    private LeagueListerSingleton(Context context) {
        mLeagues = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            League league = new League();
            league.setLeagueName("League #" + i);
            mLeagues.add(league);
        }
    }

    public List<League> getLeagues() {
        return mLeagues;
    }

    public League getCrime(UUID id) {
        for (League league : mLeagues) {
            if (league.getLeagueID().equals(id)) {
                return league;
            }
        }

        return null;
    }
}
