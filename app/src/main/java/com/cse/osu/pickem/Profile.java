package com.cse.osu.pickem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

public class Profile implements Parcelable {

    private String userID;
    private String userName;
    private String encodedProfilePicture;

    public Profile(String userID, String userName, String encodedProflePicture) {
        this.userID = userID;
        this.userName = userName;
        this.encodedProfilePicture = encodedProflePicture;
    }
    public Profile(String userID, String userName, Bitmap proflePicture) {
        this.userID = userID;
        this.userName = userName;

        Bitmap tempPic = proflePicture;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tempPic.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        this.encodedProfilePicture = imageEncoded;
    }
    public Profile(String userID, String userName) {
        this.userID = userID;
        this.userName = userName;
        this.encodedProfilePicture = "";
    }

    // Getters and Setters
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEncodedProfilePicture() {
        return encodedProfilePicture;
    }
    public void setEncodedProfilePicture(String encodedProfilePicture) {
        this.encodedProfilePicture = encodedProfilePicture;
    }
    //public void setEncodedProfilePicture(Bitmap profilePicture) {
    //    Bitmap tempPic = profilePicture;
    //    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //    tempPic.compress(Bitmap.CompressFormat.PNG, 100, baos);
    //    String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    //    this.encodedProfilePicture = imageEncoded;
    //}

    public Profile() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Profile)) {
            return false;
        }
        Profile compareProfile = (Profile)o;
        if (this.userID != compareProfile.getUserID() || this.userName != compareProfile.getUserName() || this.encodedProfilePicture != compareProfile.getEncodedProfilePicture()) {
            return false;
        }
        return true;
    }

    // Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(userName);
        dest.writeString(encodedProfilePicture);
    }

    // This is used to regenerate the Profile. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    // Constructor that takes a Parcel and gives you a Profile populated with it's values
    private Profile(Parcel in) {
        this.userID = in.readString();
        this.userName = in.readString();
        this.encodedProfilePicture = in.readString();
    }

    public static String getEncodedProfilePicFromUserID(final String userID){
        // Get database references
        DatabaseReference profileDatabaseReference = FirebaseDatabase.getInstance().getReference("profiles");
        Log.d("PROFILE", profileDatabaseReference.child(userID).getKey());
        return profileDatabaseReference.child(userID).child("encodedProfilePic").getKey();
    }
    public static Bitmap getBitmapFromString(final String encodedBitmap){
        byte[] decodedByteArray = android.util.Base64.decode(encodedBitmap, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }
}
