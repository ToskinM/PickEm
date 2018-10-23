package com.cse.osu.pickem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class LeagueListFragment extends Fragment {
    public static final String TAG = "LeagueListFragment";
    private RecyclerView mLeagueRecyclerView;
    private LeagueAdapter mAdapter;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_league_list, container, false);

        // Get user auth data
        auth = FirebaseAuth.getInstance();

        // Setup recycler view
        mLeagueRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mLeagueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Update the list
        updateUI();

        return view;
    }

    private void updateUI() {
        // Get user's leagues
        UserLeagueFetcher userLeagueFetcher = UserLeagueFetcher.get(getActivity(), auth);
        List<League> leagues = userLeagueFetcher.getLeagues();

        mAdapter = new LeagueAdapter(leagues);
        mLeagueRecyclerView.setAdapter(mAdapter);
    }

    private class LeagueHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mLeagueNameTextView;
        private League mLeague;

        public LeagueHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_league, parent, false));
            itemView.setOnClickListener(this);
            mLeagueNameTextView = (TextView) itemView.findViewById(R.id.league_name);

        }
        public void bind(League league) {
            mLeague = league;
            mLeagueNameTextView.setText(mLeague.getLeagueName());
        }
        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(),
                    "Owned by: " + mLeague.getLeagueOwnerUID(), Toast.LENGTH_SHORT)
                    .show();
            PopupMenu popupMenu = new PopupMenu(getActivity(), getView());
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu_leagues, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.action_delete_league) {
                        Toast.makeText(getActivity(),
                                "Not yet Implemented", Toast.LENGTH_SHORT)
                                .show();
                    } else if (id == R.id.action_delete_league) {
                    }
                    return true;
                }
            });
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
