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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Map;

public class MyProfile extends AppCompatActivity {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private TextView myProfileText;
    private ImageView myProfilePicture;
    private TextView myPostText;

    private SwipeRefreshLayout swipeRefreshLayout;

    private LinearLayout linearLayout;

    private String uri;
    private Timestamp timestamp;
    private String postText;

    private String uri_to_delete;
    private int delete_button_tag;

    private DocumentReference noOfPosts;

    private String username;
    private String email;
    private String joinedOn;
    private String no_of_followers;
    private String no_of_following;
    private String no_of_posts;
    private String hasProfilePic;
    private String profileUri;

    private ArrayList<String> uriList;
    private ArrayList<Timestamp> timeStampList;

    private int count=-1;

    public void getMyFollowers(View view)
    {
        Intent goToMyFollowers = new Intent (getApplicationContext(),MyFollowers.class);

        goToMyFollowers.putExtra("user_id",currentUser.getUid());

        startActivity(goToMyFollowers);
    }

    public void getMyFollowing(View view)
    {
        Intent goToMyFollowing = new Intent (getApplicationContext(),MyFollowing.class);

        goToMyFollowing.putExtra("user_id",currentUser.getUid());

        startActivity(goToMyFollowing);
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
        setContentView(R.layout.activity_my_profile);

        frbAuth = FirebaseAuth.getInstance();
        currentUser = frbAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        swipeRefreshLayout=findViewById(R.id.myProfileSwipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent goToMyProfile = new Intent(getApplicationContext(), MyProfile.class);
                startActivity(goToMyProfile);
            }
        });

        noOfPosts = db.collection("users").document(currentUser.getUid());

        uriList= new ArrayList<String>();
        timeStampList = new ArrayList<Timestamp>();

        myProfileText = findViewById(R.id.MyProfileText);
        myProfilePicture = findViewById(R.id.imageView2);
        myPostText = findViewById(R.id.myPostText);

        linearLayout = findViewById(R.id.myProfileLinearLayout);

        db.collection("users").document(currentUser.getUid()).
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
                           // joinedOn = userDetails.get("joinedOn").toString();
                            no_of_followers = userDetails.get("no_of_followers").toString();
                            no_of_following = userDetails.get("no_of_following").toString();
                            no_of_posts = userDetails.get("no_of_posts_active").toString();
                            hasProfilePic = userDetails.get("hasProfilePic").toString();

                            String info = "Username: " + username + "\nemail: " + email + /*"\nJoined On: " + joinedOn +*/ "\nNo of followers: " + no_of_followers + "\nNo of following: " + no_of_following + "\nNo of posts: " + no_of_posts + "\nProfile picture: ";
                            myProfileText.setText(info);

                            if (hasProfilePic.equals("true")) {
                                profileUri = userDetails.get("profilePicUri").toString();
                                Glide.with(getApplicationContext()).load(profileUri.toString()).into(myProfilePicture);
                            } else {
                                myProfilePicture.setImageResource(R.drawable.profilepic);
                            }

                            if(Long.parseLong(no_of_posts)==0)
                            {
                                myPostText.setText("You don't have any posts yet");
                            }
                            else
                            {
                                myPostText.setText("These are your posts");
                            }

                        } else {

                        }
                    }
                });

        db.collection("users").document(currentUser.getUid()).collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
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

                                    count+=1;

                                    uri=(String)dc.getDocument().get("uri");

                                    uriList.add(uri.toString());

                                    timestamp = (Timestamp)dc.getDocument().get("timestamp");

                                    timeStampList.add(timestamp);

                                    postText =(String)dc.getDocument().get("text");

                                    ImageView imageView = new ImageView(getApplicationContext());

                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams. MATCH_PARENT ,
                                            linearLayout.getWidth());

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

                                    ImageButton deleteButton = new ImageButton(getApplicationContext());

                                    LinearLayout.LayoutParams layoutParamsButton = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams. WRAP_CONTENT ,
                                            LinearLayout.LayoutParams. WRAP_CONTENT ) ;

                                    layoutParamsButton.setMargins( 40 , 5 , 40 , 0 ) ;

                                    deleteButton.setImageResource(R.drawable.deleteicon);
                                    deleteButton.setBackgroundColor(getResources().getColor(R.color.white));

                                    deleteButton.setTag(count);

                                    deleteButton.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {

                                            delete_button_tag = (int) v.getTag();

                                            uri_to_delete = uriList.get(delete_button_tag);

                                            StorageReference storageReference = storage.getReferenceFromUrl(uri_to_delete.toString());
                                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {


                                                    db.collection("users").document(currentUser.getUid()).collection("posts").document(currentUser.getUid()+timeStampList.get(delete_button_tag))
                                                            .delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    db.collection("posts").document(currentUser.getUid()+timeStampList.get(delete_button_tag))
                                                                            .delete()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {

                                                                                    Toast.makeText(MyProfile.this,"Deleted successfully",Toast.LENGTH_LONG).show();

                                                                                    noOfPosts.update("no_of_posts_active", FieldValue.increment(-1));

                                                                                    Intent goToMyProfile = new Intent(getApplicationContext(),MyProfile.class);
                                                                                    startActivity(goToMyProfile);
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {

                                                                                }
                                                                            });

                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {

                                                                }
                                                            });
                                                }
                                            });

                                        }
                                    });

                                    linearLayout.addView(deleteButton,layoutParamsButton);
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

    @Override //to prevent going back when back button is clicked
    public void onBackPressed()
    {

    }
}