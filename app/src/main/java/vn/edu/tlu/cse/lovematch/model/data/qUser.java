package vn.edu.tlu.cse.lovematch.model.data;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class qUser implements Parcelable {
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
    private boolean locationEnabled;

    public qUser() {}

    // Constructor đầy đủ
    public qUser(String uid, String name, String email, String gender, String dateOfBirth, String residence,
                 List<String> photos, double latitude, double longitude, String bio, int age, boolean locationEnabled) {
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
        this.locationEnabled = locationEnabled;
    }

    // Constructor from Parcel
    protected qUser(Parcel in) {
        uid = in.readString();
        name = in.readString();
        email = in.readString();
        gender = in.readString();
        dateOfBirth = in.readString();
        residence = in.readString();
        photos = in.createStringArrayList();
        latitude = in.readDouble();
        longitude = in.readDouble();
        bio = in.readString();
        age = in.readInt();
        locationEnabled = in.readByte() != 0;
    }

    // Getters và Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

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

    public boolean isLocationEnabled() { return locationEnabled; }
    public void setLocationEnabled(boolean locationEnabled) { this.locationEnabled = locationEnabled; }

    // Parcelable implementation
    public static final Creator<qUser> CREATOR = new Creator<qUser>() {
        @Override
        public qUser createFromParcel(Parcel in) {
            return new qUser(in);
        }

        @Override
        public qUser[] newArray(int size) {
            return new qUser[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(gender);
        dest.writeString(dateOfBirth);
        dest.writeString(residence);
        dest.writeStringList(photos);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(bio);
        dest.writeInt(age);
        dest.writeByte((byte) (locationEnabled ? 1 : 0));
    }
}