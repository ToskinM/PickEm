package com.cse.osu.pickem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GameListFragment extends Fragment {
    public static final String TAG = "GameListFragment";
    private RecyclerView mGameRecyclerView;
    private GameAdapter mAdapter;
    private String mLeagueID;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_list, container, false);

        // Get leagueID from launching activity
        if (getArguments() != null){
            mLeagueID = getArguments().getString("leagueID");
        }
        Log.d(TAG, mLeagueID);

        // Setup recycler view
        mGameRecyclerView = view.findViewById(R.id.game_recycler_view);
        mGameRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Setup Listeners
        setupDatabaseListeners();

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
        List<Game> games = userGameFetcher.getGames(mLeagueID);

        mAdapter = new GameAdapter(games);
        mGameRecyclerView.removeAllViews();
        mAdapter.notifyDataSetChanged();
        mGameRecyclerView.setAdapter(mAdapter);
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

    //// A "container" of recyclerView that holds a list item (a league)
    private class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mGameTextView;
        private Game mGame;

        public GameHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_game, parent, false));
            itemView.setOnClickListener(this);
            mGameTextView = itemView.findViewById(R.id.game_name);

        }
        public void bind(Game game) {
            mGame = game;
            mGameTextView.setText(mGame.getFirstTeamName() + " vs. " + mGame.getSecondTeamName());
        }

        // When you click a game in the list
        @Override
        public void onClick(View view) {

            //Launch league options activity, sending the League to manage via the intent
            Intent intent = new Intent(getActivity(), GameOptionsActivity.class);
            intent.putExtra("game", mGame);
            startActivity(intent);
        }
    }

    //// Links leagues to holders in the recyclerView
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
            holder.bind(game);
        }

        @Override
        public int getItemCount() {
            return mGames.size();
        }
    }
}
