package vn.edu.tlu.cse.lovematch.config;  
  
import com.google.firebase.auth.FirebaseAuth;  
import com.google.firebase.database.DatabaseReference;  
import com.google.firebase.database.FirebaseDatabase;  
import com.google.firebase.storage.FirebaseStorage;  
import com.google.firebase.storage.StorageReference;  
  
public class FirebaseConfig {  
    private static FirebaseAuth auth;  
    private static DatabaseReference database;  
    private static StorageReference storage;  
      
    public static FirebaseAuth getAuth() {  
        if (auth == null) {  
            auth = FirebaseAuth.getInstance();  
        }  
        return auth;  
    }  
      
    public static DatabaseReference getDatabase() {  
        if (database == null) {  
            database = FirebaseDatabase.getInstance().getReference();  
        }  
        return database;  
    }  
      
    public static StorageReference getStorage() {  
        if (storage == null) {  
            storage = FirebaseStorage.getInstance().getReference();  
        }  
        return storage;  
    }  
      
    // Helper methods cho các nodes cụ thể  
    public static DatabaseReference getUsersRef() {  
        return getDatabase().child("users");  
    }  
      
    public static DatabaseReference getChatsRef() {  
        return getDatabase().child("chats");  
    }  
      
    public static DatabaseReference getLikesRef() {  
        return getDatabase().child("likes");  
    }  
      
    public static DatabaseReference getMatchesRef() {  
        return getDatabase().child("matches");  
    }  
}

