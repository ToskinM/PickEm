package com.cse.osu.pickem;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {
    public static final String TAG = "ProfileActivity";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private FirebaseAuth auth;
    private DatabaseReference profileDatabaseReference;

    private Button pictureChangeButton;
    private Button usernameChangeButton;
    private TextView userNameTextView;
    private ImageView profileImageView;

    private String mCurrentPhotoPath;
    private Bitmap profilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Init database refs
        auth = FirebaseAuth.getInstance();
        profileDatabaseReference = FirebaseDatabase.getInstance().getReference("profiles");

        // Wire up views
        profileImageView = findViewById(R.id.imageView_profilePic);
        userNameTextView = findViewById(R.id.textView_userName);
        pictureChangeButton = findViewById(R.id.button_changePicture);
        usernameChangeButton = findViewById(R.id.button_changeUsername);
        setupButtonListeners();

        updateProfileInfo();

    }

    protected void setupButtonListeners() {
        pictureChangeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(ProfileActivity.this, pictureChangeButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu_changeimage, popup.getMenu());
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        switch (id){
                            case R.id.action_takePhoto:
                                dispatchTakePictureIntent();
                            case R.id.action_chooseAvatar:
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        usernameChangeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog renameDialog = createRenameDialog();
                renameDialog.show();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Snackbar.make(ProfileActivity.this.findViewById(android.R.id.content), "Unable to create image file.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        Long tsLong = System.currentTimeMillis()/1000;
        String timeStamp = tsLong.toString();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        if (mCurrentPhotoPath != null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            profileDatabaseReference
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("encodedProflePicture")
                    .setValue(imageEncoded);
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = profileImageView.getWidth();
        int targetH = profileImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        profileImageView.setImageBitmap(bitmap);
        encodeBitmapAndSaveToFirebase(bitmap);
    }

    protected void updateProfileInfo() {
        FirebaseDatabase.getInstance().getReference("profiles").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Profile tempProfile = snapshot.getValue(Profile.class);
                    if (tempProfile.getUserID().equals(auth.getUid())) {
                        // Get and decode profile image
                        byte[] decodedByteArray = android.util.Base64.decode(tempProfile.getEncodedProflePicture(), Base64.DEFAULT);
                        profilePhoto = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                        profileImageView.setImageBitmap(profilePhoto);

                        // Display Username
                        userNameTextView.setText(tempProfile.getUserName());
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private AlertDialog createRenameDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = ProfileActivity.this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_username_change, null))
                // Add action buttons
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog d = (Dialog) dialog;

                        // Get new name for league
                        EditText newNameEditText = d.findViewById(R.id.new_username);
                        String newName = newNameEditText.getText().toString().trim();

                        // Rename the league
                        changeUsername(newName);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing, cancelling rename
                    }
                });
        return builder.create();
    }
    private void changeUsername(String newName){
        profileDatabaseReference
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("userName")
                .setValue(newName);
    }
}
