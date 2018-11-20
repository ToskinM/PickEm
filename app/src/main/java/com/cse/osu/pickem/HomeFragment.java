package com.cse.osu.pickem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    public static final String TAG = "HomeListFragment";
    private RecyclerView mGameRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GameAdapter mAdapter;
    LeagueFetcher mLeagueFetcher;
    GameFetcher mGameFetcher;
    List<Pair<Game, League>> gameLeaguePairs = new ArrayList<>();
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
        mGameRecyclerView.setHasFixedSize(true);
        mGameRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //mGameRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        // Setup Listeners
        setupDatabaseListeners();

        mLeagueFetcher = LeagueFetcher.get();
        mGameFetcher = GameFetcher.get();

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
        gameLeaguePairs.clear();
        List<League> leagues = mLeagueFetcher.getUserLeagues(auth.getUid());
        Log.d(TAG,"" + leagues.size());
        for (League league : leagues){
            List<Game> games = mGameFetcher.getGamesOfLeague(league.getLeagueID());
            Log.d(TAG,"" + games.size());
            for (Game game : games){
                Pair<Game, League> pair = new Pair<>(game, league);
                gameLeaguePairs.add(pair);
            }
        }

        mAdapter = new GameAdapter(gameLeaguePairs);
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
        public void bind(Pair<Game, League> pair) {
            mGame = pair.first;
            mLeague = pair.second;
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
        private List<Pair<Game, League>> mPairs;

        public GameAdapter(List<Pair<Game, League>> pairs) {
            this.mPairs = pairs;
        }

        @Override
        public GameHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new GameHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull GameHolder holder, int position) {
            Pair<Game, League> pair = gameLeaguePairs.get(position);
            holder.bind(pair);
        }

        @Override
        public int getItemCount() {
            return mPairs.size();
        }
    }
}
