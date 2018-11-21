package com.cse.osu.pickem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private Button registerButton;
    private Button loginButton;
    private EditText email;
    private EditText username;
    private EditText password;

    private FirebaseAuth auth;
    private DatabaseReference profilesDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        auth = FirebaseAuth.getInstance();
        profilesDatabaseReference = FirebaseDatabase.getInstance().getReference("profiles");

        if (auth.getCurrentUser() != null) {
            checkForUserProfile();
            finish();
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        registerButton = findViewById(R.id.buttonSubmit);
        loginButton = findViewById(R.id.buttonLogIn);

        email = findViewById(R.id.emailEditText);
        username = findViewById(R.id.usernameText);
        password = findViewById(R.id.emailPasswordText);

        registerButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v == registerButton) {
            registerUser();
        } else if (v == loginButton) {
            loginUser();
        }
    }

    public void loginUser() {
        String emailText = email.getText().toString().trim();

        if (emailText.isEmpty()) {
            //Email is empty!
            Toast.makeText(this, "Email empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.getText().toString().trim().length() == 0) {
            //Password is empty!
            Toast.makeText(this, "Email empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(emailText, password.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkForUserProfile();
                            finish();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Incorrect Email or Password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void registerUser() {
        String emailText = email.getText().toString().trim();

        if (emailText.isEmpty()) {
            //Email is empty!
            Toast.makeText(this, "Email empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.getText().toString().trim().length() == 0) {
            //Password is empty!
            Toast.makeText(this, "Password empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(emailText, password.getText().toString().trim()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    createUserProfile();
                    Toast.makeText(LoginActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        throw task.getException();

                    } catch (FirebaseAuthWeakPasswordException e) {
                        Toast.makeText(LoginActivity.this, "Password too weak!", Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        Toast.makeText(LoginActivity.this, "Malformed email!", Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthUserCollisionException e) {
                        Toast.makeText(LoginActivity.this, "User exists already!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Unknown error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void checkForUserProfile() {
        profilesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean inDatabase = false;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Profile tempProfile = snapshot.getValue(Profile.class);
                    if (tempProfile.getUserID().equals(auth.getUid())) {
                        inDatabase = true;
                        break;
                    }
                }
                if (!inDatabase){
                    createUserProfile();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void createUserProfile() {
        String usernameString = username.getText().toString().trim();
        if (usernameString.equals(""))
            usernameString = auth.getUid();
        Profile newUserProfile = new Profile(auth.getUid(), usernameString);
        profilesDatabaseReference
                .child(auth.getUid())
                .setValue(newUserProfile);
    }
}
