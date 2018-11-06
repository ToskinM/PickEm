package com.cse.osu.pickem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Asserts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.common.internal.Asserts.*;

public class HomeFragment extends Fragment {
    public static final String TAG = "HomeListFragment";
    private RecyclerView mGameRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GameAdapter mAdapter;
    UserLeagueFetcher userLeagueFetcher;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_list, container, false);

        // Get user auth data
        auth = FirebaseAuth.getInstance();

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                updateUI();
            }
        });

        // Setup recycler view
        mGameRecyclerView = view.findViewById(R.id.game_recycler_view);
        mGameRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //mGameRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        // Setup Listeners
        setupDatabaseListeners();

        userLeagueFetcher = UserLeagueFetcher.get(getActivity(), auth);

        updateUI();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void updateUI() {
        // Get user's owned leagues
        UserGameFetcher userGameFetcher = UserGameFetcher.get(getActivity());
        List<Game> games = userGameFetcher.getAllGames();

        //List<League> leagues = userLeagueFetcher.getLeagues();

        mAdapter = new GameAdapter(games);
        mGameRecyclerView.removeAllViews();
        mAdapter.notifyDataSetChanged();
        mGameRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void setupDatabaseListeners(){
        // Get database references
        DatabaseReference gamesDatabaseReference = FirebaseDatabase.getInstance().getReference("games");
        gamesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateUI();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //// A "container" of recyclerView that holds a list item
    private class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mGameTextView;
        private TextView mLeagueTextView;
        private Game mGame;
        private League mLeague;

        public GameHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_gamehome, parent, false));
            itemView.setOnClickListener(this);
            mGameTextView = itemView.findViewById(R.id.game_name);
            mLeagueTextView = itemView.findViewById(R.id.league_name);

        }
        public void bind(Game game, League league) {
            mGame = game;
            mLeague = league;
            if (mGame != null)
                mGameTextView.setText(mGame.getFirstTeamName() + " vs. " + mGame.getSecondTeamName());
            if (mLeague != null)
                mLeagueTextView.setText(mLeague.getLeagueName());
        }

        // When you click a game in the list
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), GameOptionsActivity.class);
            intent.putExtra("game", mGame);
            startActivity(intent);
        }
    }

    //// Links games to holders in the recyclerView
    private class GameAdapter extends RecyclerView.Adapter<GameHolder> {
        private List<Game> mGames;

        public GameAdapter(List<Game> games) {
            mGames = games;
        }

        @Override
        public GameHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new GameHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull GameHolder holder, int position) {
            Game game = mGames.get(position);
            League league = userLeagueFetcher.getLeague(game.getLeagueID());
            holder.bind(game, league);
        }

        @Override
        public int getItemCount() {
            return mGames.size();
        }
    }
}
