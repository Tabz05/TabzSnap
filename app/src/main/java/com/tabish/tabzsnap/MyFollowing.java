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

public class MyFollowing extends AppCompatActivity {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private ListView MyFollowingListView;

    private String currUsername;

    private String user_id;
    private String username;

    private ArrayList<String> followingList;

    private ArrayAdapter<String> arrayAdapter;

    private void getFollowingList()
    {
        db.collection("users").document(currentUser.getUid()).collection("Following")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                username=(String) document.get("username");
                                followingList.add(username);
                            }
                            arrayAdapter.notifyDataSetChanged();
                        } else {
                            //Log.i("Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private boolean isNetworkAvailable() { // to check if connected to internet
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
        {
            connected = false;
        }

        return connected;
    }

    public void homeButton(View view)
    {
        if (currentUser != null)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
            }
            else
            {
                if (isNetworkAvailable()) {
                    Intent goToHome = new Intent(getApplicationContext(), MainActivity.class);

                    startActivity(goToHome);
                } else {
                    Toast.makeText(this, "No internet access", Toast.LENGTH_LONG).show();
                }
            }
        }
        else
        {
            Intent goToHome = new Intent(getApplicationContext(), MainActivity.class);

            startActivity(goToHome);
        }
    }

    public void myFeedButton(View view)
    {
        if (currentUser != null)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
            } else {
                if (isNetworkAvailable()) {
                    Intent goToFeed = new Intent(getApplicationContext(), UserFeed.class);
                    startActivity(goToFeed);

                } else {
                    Toast.makeText(this, "No internet access", Toast.LENGTH_LONG).show();
                }

            }
        }
        else
        {
            Toast.makeText(this, "Sign In", Toast.LENGTH_LONG).show();
            Intent goToSignIn = new Intent(getApplicationContext(), SignIn.class);
            startActivity(goToSignIn);
        }
    }

    public void myProfileButton(View view)
    {
        if(currentUser!=null)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
            } else {
                if (isNetworkAvailable()) {
                    Toast.makeText(this, "Your Profile", Toast.LENGTH_SHORT).show();
                    Intent goToMyProfile = new Intent(getApplicationContext(), MyProfile.class);
                    startActivity(goToMyProfile);

                } else {
                    Toast.makeText(this, "No internet access", Toast.LENGTH_LONG).show();
                }

            }
        }
        else
        {
            Toast.makeText(this, "Sign In", Toast.LENGTH_LONG).show();
            Intent goToSignIn = new Intent(getApplicationContext(), SignIn.class);
            startActivity(goToSignIn);
        }
    }

    public void findPeopleButton(View view)
    {
        if(currentUser!=null)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
            } else {
                if (isNetworkAvailable()) {
                    Toast.makeText(this, "User List", Toast.LENGTH_SHORT).show();
                    Intent goToUserList = new Intent(getApplicationContext(), UserList.class);

                    startActivity(goToUserList);
                } else {
                    Toast.makeText(this, "No internet access", Toast.LENGTH_LONG).show();
                }

            }
        }
        else
        {
            Toast.makeText(this, "Sign In", Toast.LENGTH_LONG).show();
            Intent goToSignIn = new Intent(getApplicationContext(), SignIn.class);
            startActivity(goToSignIn);
        }
    }

    public void newPostButton(View view)
    {
        if (currentUser != null)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
            }
            else
            {
                if (isNetworkAvailable()) {
                    Intent goToUploadPost = new Intent(getApplicationContext(), UploadPost.class);

                    startActivity(goToUploadPost);
                } else {
                    Toast.makeText(this, "No internet access", Toast.LENGTH_LONG).show();
                }
            }
        }
        else
        {
            Toast.makeText(this, "Sign in to upload posts", Toast.LENGTH_LONG).show();
            Intent goToSignIn = new Intent(getApplicationContext(), SignIn.class);
            startActivity(goToSignIn);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_following);

        frbAuth = FirebaseAuth.getInstance();
        currentUser = frbAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        followingList = new ArrayList<String>();

        MyFollowingListView=findViewById(R.id.MyFollowingListView);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,followingList);

        MyFollowingListView.setAdapter(arrayAdapter);

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

                        getFollowingList();
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


        MyFollowingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { //when an item of listview is clicked
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                db.collection("users")
                        .whereEqualTo("username", followingList.get(i).toString())
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