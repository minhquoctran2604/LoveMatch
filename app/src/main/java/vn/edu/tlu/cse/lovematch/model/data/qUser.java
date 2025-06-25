package vn.edu.tlu.cse.lovematch.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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
    private String preferredGender;
    private String religion;
    private String educationLevel;
    private String occupation;
    private String description;
    private boolean locationEnabled;
    private String education;
    private String dob;
    private String location;
    private String interests;
    private String relationship;
    private Map<String, Object> matches;

    public qUser() {}

    public qUser(String uid, String name, String email, String gender, String dateOfBirth, 
                String residence, List<String> photos, double latitude, double longitude, 
                String bio, int age) {
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

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getId() { return uid; } // Alias for getUid() to maintain compatibility
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

    // New methods
    public String getPreferredGender() { return preferredGender; }
    public void setPreferredGender(String preferredGender) { this.preferredGender = preferredGender; }

    public String getReligion() { return religion; }
    public void setReligion(String religion) { this.religion = religion; }

    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isLocationEnabled() { return locationEnabled; }
    public void setLocationEnabled(boolean locationEnabled) { this.locationEnabled = locationEnabled; }

    // New getters and setters
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public Map<String, Object> getMatches() { return matches; }
    public void setMatches(Map<String, Object> matches) { this.matches = matches; }

    // Parcelable implementation
    protected qUser(Parcel in) {
        uid = in.readString();
        name = in.readString();
        email = in.readString();
        gender = in.readString();
        dateOfBirth = in.readString();
        residence = in.readString();
        photos = new ArrayList<>();
        in.readList(photos, String.class.getClassLoader());
        latitude = in.readDouble();
        longitude = in.readDouble();
        bio = in.readString();
        age = in.readInt();
        preferredGender = in.readString();
        religion = in.readString();
        educationLevel = in.readString();
        occupation = in.readString();
        description = in.readString();
        locationEnabled = in.readByte() != 0;
        education = in.readString();
        dob = in.readString();
        location = in.readString();
        interests = in.readString();
        relationship = in.readString();
        matches = new HashMap<>();
        in.readMap(matches, Object.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(gender);
        dest.writeString(dateOfBirth);
        dest.writeString(residence);
        dest.writeList(photos);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(bio);
        dest.writeInt(age);
        dest.writeString(preferredGender);
        dest.writeString(religion);
        dest.writeString(educationLevel);
        dest.writeString(occupation);
        dest.writeString(description);
        dest.writeByte((byte) (locationEnabled ? 1 : 0));
        dest.writeString(education);
        dest.writeString(dob);
        dest.writeString(location);
        dest.writeString(interests);
        dest.writeString(relationship);
        dest.writeMap(matches);
    }

    @Override
    public int describeContents() {
        return 0;
    }

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
}
