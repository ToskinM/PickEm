package com.cse.osu.pickem;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class PeopleMembersFragment extends Fragment {
    public static final String TAG = "PeopleMembersFragment";
    private RecyclerView mPeopleRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MemberAdapter mAdapter;
    private FirebaseAuth auth;
    private String mLeagueID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people_list, container, false);

        // Get leagueID from launching activity
        if (getArguments() != null){
            mLeagueID = getArguments().getString("leagueID");
        }

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
        mPeopleRecyclerView = view.findViewById(R.id.leaguePeople_recycler_view);
        mPeopleRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Update the list
        updateUI();

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
        LeagueMembersFetcher leagueMembersFetcher = LeagueMembersFetcher.get(getActivity());

        // Filter out only the owned leagues
        List<LeagueMemberPair> members = leagueMembersFetcher.getMembersOfLeague(mLeagueID);

        mAdapter = new MemberAdapter(members);
        mPeopleRecyclerView.removeAllViews();
        mAdapter.notifyDataSetChanged();
        mPeopleRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }


    private void setupDatabaseListeners(){
        // Get database references
        DatabaseReference leagueMembersDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");

        // Leagues listener
        leagueMembersDatabaseReference.addValueEventListener(new ValueEventListener() {
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
    private class MemberHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mUsernameTextView;
        private TextView mScoreTextView;
        private LeagueMemberPair mMember;
        private String memberProfilePic;
        private String memberUsername;
        private AlertDialog memberDialog;

        public MemberHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_people, parent, false));
            itemView.setOnClickListener(this);
            mUsernameTextView = itemView.findViewById(R.id.user_name);
            mScoreTextView = itemView.findViewById(R.id.user_score);
        }
        public void bind(LeagueMemberPair pair) {
            mMember = pair;
            setUsernameFromUID(pair.getUID());
            mScoreTextView.setText(Integer.toString(pair.getPoints()));
        }
        @Override
        public void onClick(View view) {
            memberDialog = createMemberDialog();
            memberDialog.show();
        }

        private void setUsernameFromUID(final String uid){
            // Get database reference
            DatabaseReference profilesDatabaseReference = FirebaseDatabase.getInstance().getReference("profiles");
            profilesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Profile profile = snapshot.getValue(Profile.class);
                        if (profile.getUserID().equals(uid)) {
                            memberUsername = profile.getUserName();
                            mUsernameTextView.setText(memberUsername);
                            memberProfilePic = profile.getEncodedProflePicture();
                            return;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        private AlertDialog createMemberDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_member_options, null);
            builder.setView(dialogView);

            TextView usernameTextView = dialogView.findViewById(R.id.textView_userName);
            usernameTextView.setText(memberUsername);

            ImageView profileImageView = dialogView.findViewById(R.id.imageView_profilePic);

            if (memberProfilePic != null && !memberProfilePic.equals(""))
                profileImageView.setImageBitmap(Profile.getBitmapFromString(memberProfilePic));

            Button removeButton = dialogView.findViewById(R.id.button_remove);
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    League.removeMemberFromLeague(mLeagueID, mMember.getUID());
                }
            });

            return builder.create();
        }
    }

    //// Links leagues to holders in the recyclerView
    private class MemberAdapter extends RecyclerView.Adapter<MemberHolder> {
        private List<LeagueMemberPair> mMembers;

        public MemberAdapter(List<LeagueMemberPair> members) {
            mMembers = members;
        }

        @Override
        public MemberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new MemberHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberHolder holder, int position) {
            LeagueMemberPair pair = mMembers.get(position);
            holder.bind(pair);
        }

        @Override
        public int getItemCount() {
            return mMembers.size();
        }
    }
}
