package com.tabish.tabzsnap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class UserProfile extends AppCompatActivity {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private String user_id;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private TextView userProfileText;
    private ImageView userProfilePicture;

    private Button FollowUnfollow;

    private TextView userPostText;
    private TextView UserFollowers;
    private TextView UserFollowing;

    private String currUsername;

    private boolean follow=false;

    private LinearLayout linearLayout;

    private SwipeRefreshLayout swipeRefreshLayout;

    private String uri;
    private Timestamp timestamp;
    private String postText;

    private String username;
    private String email;
    private String joinedOn;
    private String no_of_followers;
    private String no_of_following;
    private String no_of_posts;
    private String hasProfilePic;
    private String profileUri;

    private DocumentReference noOfFollowers;
    private DocumentReference noOfFollowing;

    public void getUserFollowers(View view)
    {
        Intent goToUserFollowers = new Intent (getApplicationContext(),UserFollowers.class);

        goToUserFollowers.putExtra("user_id",user_id);

        startActivity(goToUserFollowers);
    }

    public void getUserFollowing(View view)
    {
        Intent goToUserFollowing = new Intent (getApplicationContext(),UserFollowing.class);

        goToUserFollowing.putExtra("user_id",user_id);

        startActivity(goToUserFollowing);
    }

    private void checkFollowOrUnfollow()
    {
        db.collection("users").document(user_id).collection("Followers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //   Log.d(TAG, document.getId() + " => " + document.getData());
                                String userId = (String) document.get("user_id");

                                if (userId.equals((currentUser.getUid()).toString())) {
                                    follow = true;

                                    FollowUnfollow.setVisibility(View.VISIBLE);
                                    FollowUnfollow.setText("Unfollow");
                                    FollowUnfollow.setBackgroundColor(getResources().getColor(R.color.red));
                                }
                            }
                            if(follow!=true)
                            {
                                FollowUnfollow.setVisibility(View.VISIBLE);
                                FollowUnfollow.setText("Follow");
                                FollowUnfollow.setBackgroundColor(getResources().getColor(R.color.green));
                            }


                        } else {
                            //Log.i("Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void FollowOrUnfollow(View view)
    {

            if (!follow) {

                noOfFollowers.update("no_of_followers", FieldValue.increment(1));
                noOfFollowing.update("no_of_following", FieldValue.increment(1));

                Map<String, Object> userFollower = new HashMap<>();
                userFollower.put("user_id", currentUser.getUid());
                userFollower.put("username", currUsername);

                Map<String, Object> userFollowing = new HashMap<>();
                userFollowing.put("user_id", user_id);
                userFollowing.put("username", username);

                    db.collection("users").document(user_id).collection("Followers").document(currentUser.getUid())
                            .set(userFollower)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    db.collection("users").document(currentUser.getUid()).collection("Following").document(user_id)
                                            .set(userFollowing)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Toast.makeText(UserProfile.this, username + " followed successfully", Toast.LENGTH_LONG).show();
                                                    follow = true;

                                                    FollowUnfollow.setVisibility(View.VISIBLE);
                                                    FollowUnfollow.setText("Unfollow");
                                                    FollowUnfollow.setBackgroundColor(getResources().getColor(R.color.red));
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });


            } else {

                    noOfFollowers.update("no_of_followers", FieldValue.increment(-1));
                    noOfFollowing.update("no_of_following", FieldValue.increment(-1));


                    db.collection("users").document(user_id).collection("Followers").document(currentUser.getUid()).delete();
                    db.collection("users").document(currentUser.getUid()).collection("Following").document(user_id).delete();


                Toast.makeText(UserProfile.this, username + " unfollowed successfully", Toast.LENGTH_LONG).show();

                follow = false;
                FollowUnfollow.setText("Follow");
                FollowUnfollow.setBackgroundColor(getResources().getColor(R.color.green));
            }


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
        setContentView(R.layout.activity_user_profile);

        frbAuth = FirebaseAuth.getInstance();
        currentUser = frbAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Intent fromUserList = getIntent();

        user_id = fromUserList.getStringExtra("user_id");
        currUsername=fromUserList.getStringExtra("currUsername");

        checkFollowOrUnfollow();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        swipeRefreshLayout=findViewById(R.id.userProfileSwipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Intent goToUserProfile = new Intent (getApplicationContext(),UserProfile.class);

                goToUserProfile.putExtra("user_id",user_id);
                goToUserProfile.putExtra("currUsername",currUsername);

                startActivity(goToUserProfile);

            }
        });

        userProfileText=findViewById(R.id.userProfileText);
        userProfilePicture=findViewById(R.id.userProfilePic);

        userPostText=findViewById(R.id.userPostText);

        linearLayout =  findViewById(R.id.userProfileLinearLayout);

        FollowUnfollow=findViewById(R.id.FollowOrUnfollow);

        UserFollowers=findViewById(R.id.UserFollowers);

        UserFollowing=findViewById(R.id.UserFollowing);

        noOfFollowers=db.collection("users").document(user_id);
        noOfFollowing=db.collection("users").document(currentUser.getUid());

        db.collection("users").document(user_id).
        addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {

                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Map<String, Object> userDetails = snapshot.getData();
                    username = userDetails.get("username").toString();
                    email = userDetails.get("email").toString();
                 //   joinedOn = userDetails.get("joinedOn").toString();
                    no_of_followers= userDetails.get("no_of_followers").toString();
                    no_of_following = userDetails.get("no_of_following").toString();
                    no_of_posts = userDetails.get("no_of_posts_active").toString();
                    hasProfilePic = userDetails.get("hasProfilePic").toString();

                    UserFollowers.setText(username+"'s Followers");
                    UserFollowing.setText(username+"'s Followings");

                    UserFollowers.setEnabled(true);
                    UserFollowing.setEnabled(true);

                    String info= "Username: "+username+ "\nemail: "+email+/*"\nJoined On: "+joinedOn+*/ "\nNo of followers: "+no_of_followers + "\nNo of following: "+no_of_following+"\nNo of posts: "+no_of_posts+"\nProfile picture: ";
                    userProfileText.setText(info);

                    if(hasProfilePic.equals("true"))
                    {
                        profileUri = userDetails.get("profilePicUri").toString();
                        Glide.with(getApplicationContext()).load( profileUri.toString()).into(userProfilePicture);
                    }
                    else
                    {
                        userProfilePicture.setImageResource(R.drawable.profilepic);
                    }

                    if(Long.parseLong(no_of_posts)==0)
                    {
                        userPostText.setText(username+" does not have any posts yet");
                    }
                    else
                    {
                        userPostText.setText("These are "+username+"'s posts");
                    }

                } else {

                }
            }
        });

        db.collection("users").document(user_id).collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:

                                    uri=(String) dc.getDocument().get("uri");

                                    timestamp = (Timestamp)  dc.getDocument().get("timestamp");

                                    postText =(String)  dc.getDocument().get("text");

                                    ImageView imageView = new ImageView(getApplicationContext());

                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams. MATCH_PARENT ,
                                            linearLayout.getWidth()) ;

                                    layoutParams.setMargins( 40 , 20 , 40 , 0 ) ;

                                    Glide.with(getApplicationContext()).load(uri.toString()).into(imageView);

                                    linearLayout.addView(imageView,layoutParams);

                                    TextView textView = new TextView(getApplicationContext());

                                    LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams. WRAP_CONTENT ,
                                            LinearLayout.LayoutParams. WRAP_CONTENT ) ;

                                    textLayoutParams.setMargins( 40 , 10 , 40 , 0 ) ;

                                    textView.setText(postText);

                                    textView.setTextSize(22);
                                    textView.setTextColor(getResources().getColor(R.color.black));
                                    textView.setBackgroundColor(getResources().getColor(R.color.yellow));

                                    linearLayout.addView(textView,textLayoutParams);
                                    break;
                                case MODIFIED:

                                    break;
                                case REMOVED:

                                    break;
                            }
                        }

                    }
                });


    }
}