package com.cse.osu.pickem;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class LeagueListFragment extends Fragment {
    private RecyclerView mLeagueRecyclerView;
    private LeagueAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_league_list, container, false);

        mLeagueRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mLeagueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        System.out.println("League list fragment created.");

        updateUI();

        return view;
    }

    private void updateUI() {
        LeagueListerSingleton leagueListerSingleton = LeagueListerSingleton.get(getActivity());
        List<League> leagues = leagueListerSingleton.getLeagues();

        mAdapter = new LeagueAdapter(leagues);
        mLeagueRecyclerView.setAdapter(mAdapter);
    }

    private class LeagueHolder extends RecyclerView.ViewHolder {
        private TextView mLeagueNameTextView;
        private League mLeague;

        public LeagueHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_league, parent, false));
            mLeagueNameTextView = (TextView) itemView.findViewById(R.id.league_name);

        }
        public void bind(League league) {
            mLeague = league;
            mLeagueNameTextView.setText(mLeague.getLeagueName());
        }
    }

    private class LeagueAdapter extends RecyclerView.Adapter<LeagueHolder> {
        private List<League> mLeagues;

        public LeagueAdapter(List<League> leagues) {
            mLeagues = leagues;
        }

        @Override
        public LeagueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new LeagueHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(LeagueHolder holder, int position) {
            League league = mLeagues.get(position);
            holder.bind(league);
        }

        @Override
        public int getItemCount() {
            return mLeagues.size();
        }
    }
}
