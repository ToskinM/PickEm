package com.cse.osu.pickem;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PeopleMembersActivity extends AppCompatActivity {
    public static final String TAG = "PeopleMembersActivity";

    private DatabaseReference leagueDatabaseReference;
    private DatabaseReference leagueMembersDatabaseReference;
    private FirebaseAuth auth;
    private League mLeague;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_manage);

        // Get the league we're working with from intent
        Intent creatorIntent = getIntent();
        mLeague = creatorIntent.getParcelableExtra("league");

        // Get Firebase Database references
        leagueDatabaseReference = FirebaseDatabase.getInstance().getReference("leagues");
        leagueMembersDatabaseReference = FirebaseDatabase.getInstance().getReference("leagueMembers");
        auth = FirebaseAuth.getInstance();

        // Create league's members RecyclerView Fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            Bundle bundle = new Bundle();
            bundle.putString("leagueID", mLeague.getLeagueID());
            fragment = new PeopleMembersFragment();
            fragment.setArguments(bundle);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
