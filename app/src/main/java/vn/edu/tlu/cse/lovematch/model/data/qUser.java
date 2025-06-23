package vn.edu.tlu.cse.lovematch.model.data;

import java.util.List;

public class qUser {
    private String uid;
    private String name;
    private String email;
    private String gender;
    private String dateOfBirth;
    private String residence;
    private List<String> photos;
    private double latitude;
    private double longitude;
    private String bio;
    private int age;

    public qUser() {}

    // Constructor đầy đủ
    public qUser(String uid, String name, String email, String gender, String dateOfBirth, String residence, List<String> photos, double latitude, double longitude, String bio, int age) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.residence = residence;
        this.photos = photos;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bio = bio;
        this.age = age;
    }

    // Getters và Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    // Alias for getUid() to maintain compatibility
    public String getId() { return uid; }
    public void setId(String id) { this.uid = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getResidence() { return residence; }
    public void setResidence(String residence) { this.residence = residence; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}