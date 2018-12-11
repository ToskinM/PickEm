package com.cse.osu.pickem;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = "SettingsFragment";
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Log.d(TAG, "onCreateView() called!");

        Button logoutButton = view.findViewById(R.id.button_logOut);
        logoutButton.setOnClickListener(this);

        Button button_myProfile = view.findViewById(R.id.button_myProfile);
        button_myProfile.setOnClickListener(this);

        Button deleteAccountButton = view.findViewById(R.id.button_deleteAccount);
        deleteAccountButton.setOnClickListener(this);
        deleteAccountButton.setBackgroundColor(Color.RED);

        // Get user auth data
        auth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_logOut:
                getActivity().finish();
                auth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                break;

            case R.id.button_deleteAccount:
                deleteUserProfile();
                break;

            case R.id.button_myProfile:
                startActivity(new Intent(getActivity(), ProfileActivity.class));
                break;

            default:
                break;
        }
    }

    private void deleteUserProfile(){
        final DatabaseReference profilesDatabaseReference = FirebaseDatabase.getInstance().getReference("profiles");
        profilesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Profile snapshotProfile = snapshot.getValue(Profile.class);
                    if (snapshotProfile.getUserID().equals(auth.getUid())) {
                        profilesDatabaseReference.child(snapshotProfile.getUserID()).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                FirebaseUser user = auth.getCurrentUser();
                                auth.signOut();
                                user.delete();
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
