package com.example.fbase;

public class Friends {
    private String ProfileImage;
    private String ProfileName;
    private String uid;
    public Friends(){}



    public Friends(String profileImage, String profileName, String uid1) {
        ProfileImage = profileImage;
        ProfileName = profileName;
        uid=uid1;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public String getProfileName() {
        return ProfileName;
    }

    public void setProfileName(String profileName) {
        ProfileName = profileName;
    }
}
