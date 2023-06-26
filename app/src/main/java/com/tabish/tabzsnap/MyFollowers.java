package com.tabish.tabzsnap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
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

public class MyFollowers extends AppCompatActivity {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private ListView MyFollowersListView;

    private String currUsername;

    private String user_id;
    private String username;

    private ArrayList<String> followerList;

    private ArrayAdapter<String> arrayAdapter;

    private void getFollowerList()
    {
        db.collection("users").document(currentUser.getUid()).collection("Followers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                username=(String) document.get("username");
                                followerList.add(username);
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
        setContentView(R.layout.activity_my_followers);

        frbAuth = FirebaseAuth.getInstance();
        currentUser = frbAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        followerList = new ArrayList<String>();

        MyFollowersListView=findViewById(R.id.MyFollowersListView);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,followerList);

        MyFollowersListView.setAdapter(arrayAdapter);

        DocumentReference DocRef = db.collection("users").document(currentUser.getUid());

        DocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists())
                    {
                        currUsername = (String) document.get("username");

                        getFollowerList();
                    }
                    else
                    {
                        //   Log.d(TAG, "No such document");
                    }
                }
                else
                {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


        MyFollowersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { //when an item of listview is clicked
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                db.collection("users")
                        .whereEqualTo("username", followerList.get(i).toString())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        user_id=(String) document.getId();

                                        Intent goToUserProfile = new Intent (getApplicationContext(),UserProfile.class);

                                        goToUserProfile.putExtra("user_id",user_id);

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