package com.tabish.tabzsnap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

public class UserFollowers extends AppCompatActivity {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private TextView UserFollowersText;

    private String user_id;
    private String follower_id;

    private String followerUsername;
    private String currUsername;

    private String username;

    private ListView listViewUserFollowers;

    private ArrayList<String> followersList;

    private ArrayAdapter<String> arrayAdapter;

    private void getUserFollowers()
    {
        db.collection("users").document(user_id).collection("Followers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                followerUsername=(String) document.get("username");

                                if(followerUsername.equals(currUsername))
                                {
                                    followerUsername=currUsername+" (You)";
                                }
                                followersList.add(followerUsername);
                            }
                            arrayAdapter.notifyDataSetChanged();
                        } else {
                            //Log.i("Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_followers);

        frbAuth = FirebaseAuth.getInstance();
        currentUser = frbAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Intent fromUserProfile = getIntent();

        user_id = fromUserProfile.getStringExtra("user_id");

        UserFollowersText=findViewById(R.id.UserFollowersText);

        followersList= new ArrayList<String>();

        listViewUserFollowers=findViewById(R.id.listViewUserFollowers);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,followersList);

        listViewUserFollowers.setAdapter(arrayAdapter);

        DocumentReference DocRef = db.collection("users").document(currentUser.getUid());

        DocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists())
                    {
                        currUsername = (String) document.get("username");

                        db.collection("users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists())
                                    {
                                        username=(String) document.get("username");
                                        UserFollowersText.setText(username+"'s Followers");

                                        getUserFollowers();

                                    } else {
                                        //   Log.d(TAG, "No such document");
                                    }
                                } else {
                                    //Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });

                    } else {
                        //   Log.d(TAG, "No such document");
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        listViewUserFollowers.setOnItemClickListener(new AdapterView.OnItemClickListener() { //when an item of listview is clicked
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                db.collection("users")
                        .whereEqualTo("username", followersList.get(i).toString())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        follower_id=(String) document.getId();

                                        Intent goToUserProfile = new Intent (getApplicationContext(),UserProfile.class);

                                        goToUserProfile.putExtra("user_id",follower_id);

                                        startActivity(goToUserProfile);
                                    }
                                } else {

                                }
                            }
                        });
            }
        });
    }
}