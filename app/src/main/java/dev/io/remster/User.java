package dev.io.remster;

import java.io.Serializable;

/**
 * Created by ninaadpai on 4/26/17.
 */

public class User implements Serializable {
    String userId, firstName, lastName, emailAddress, photoUrl;
    Object memberSince;

    public User(String userId, String firstName, String lastName, String emailAddress, Object memberSince, String photoUrl) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.memberSince = memberSince;
        this.photoUrl = photoUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Object getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(Object memberSince) {
        this.memberSince = memberSince;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", memberSince=" + memberSince +
                ", photoUrl='" + photoUrl + '\'' +
                '}';
    }
}

