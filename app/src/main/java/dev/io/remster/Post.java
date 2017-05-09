package dev.io.remster;

import android.net.Uri;

/**
 * Created by ninaadpai on 5/3/17.
 */

public class Post {
    String postId;
    String userId;
    String placeName;
    String postCity;
    String postState;
    String postCountry;
    String postLat;
    String postLong;
    String postDesc;
    Uri photo;
    Object timestamp;



    public Post(String postId, String userId, String placeName, String postCity, String postState, String postCountry, String postLat, String postLong, String postDesc, Uri photo, Object timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.placeName = placeName;
        this.postCity = postCity;
        this.postState = postState;
        this.postCountry = postCountry;
        this.postLat = postLat;
        this.postLong = postLong;
        this.postDesc = postDesc;
        this.photo = photo;
        this.timestamp = timestamp;
    }

    public String getPostCity() {
        return postCity;
    }

    public void setPostCity(String postCity) {
        this.postCity = postCity;
    }

    public String getPostState() {
        return postState;
    }

    public void setPostState(String postState) {
        this.postState = postState;
    }

    public String getPostCountry() {
        return postCountry;
    }

    public void setPostCountry(String postCountry) {
        this.postCountry = postCountry;
    }

    public String getPostLat() {
        return postLat;
    }

    public void setPostLat(String postLat) {
        this.postLat = postLat;
    }

    public String getPostLong() {
        return postLong;
    }

    public void setPostLong(String postLong) {
        this.postLong = postLong;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public Uri getPhoto() {
        return photo;
    }

    public void setPhoto(Uri photo) {
        this.photo = photo;
    }

    public String getPostDesc() {
        return postDesc;
    }
    public void setPostDesc(String postDesc) {
        this.postDesc = postDesc;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId='" + postId + '\'' +
                ", userId='" + userId + '\'' +
                ", placeName='" + placeName + '\'' +
                ", postCity='" + postCity + '\'' +
                ", postState='" + postState + '\'' +
                ", postCountry='" + postCountry + '\'' +
                ", postLat='" + postLat + '\'' +
                ", postLong='" + postLong + '\'' +
                ", postDesc='" + postDesc + '\'' +
                ", photo=" + photo +
                ", timestamp=" + timestamp +
                '}';
    }
}
