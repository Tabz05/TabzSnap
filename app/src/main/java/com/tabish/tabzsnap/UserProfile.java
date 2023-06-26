package com.tabish.tabzsnap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.Map;

public class UserProfile extends AppCompatActivity {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private String user_id;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private ImageView userProfilePicture;

    private Button FollowUnfollow;

    private TextView userUsernameText;
    private TextView userEmailText;
    private TextView userPostText;
    private TextView UserFollowers;
    private TextView UserFollowing;

    private String currUsername;

    private boolean follow=false;

    private LinearLayout userProfileLinearLayout;

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

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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

        FollowUnfollow=findViewById(R.id.FollowOrUnfollow);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        userProfilePicture=findViewById(R.id.userProfilePic);

        userUsernameText = findViewById(R.id.userUsernameText);
        userEmailText = findViewById(R.id.userEmailText);
        userPostText=findViewById(R.id.userPostText);

        userProfileLinearLayout =  findViewById(R.id.userProfileLinearLayout);

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
                            no_of_posts = userDetails.get("no_of_posts").toString();
                            hasProfilePic = userDetails.get("hasProfilePic").toString();

                            userUsernameText.setText(username);
                            userEmailText.setText(email);

                            UserFollowers.setText(username+"'s Followers ("+no_of_followers.toString()+")");
                            UserFollowing.setText(username+"'s Followings ("+no_of_following.toString()+")");

                            UserFollowers.setEnabled(true);
                            UserFollowing.setEnabled(true);

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
                                userPostText.setText(username+"'s posts ("+no_of_posts.toString()+")");
                            }

                        } else {

                        }
                    }
        });


        db.collection("users").document(user_id).collection("Followers")
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

                                    String userId = (String) dc.getDocument().get("user_id");

                                    if (userId.equals((currentUser.getUid()).toString())) {
                                        follow = true;

                                        FollowUnfollow.setVisibility(View.VISIBLE);
                                        FollowUnfollow.setText("Unfollow");
                                        //FollowUnfollow.setBackgroundColor(getResources().getColor(R.color.red));
                                    }

                                    break;
                                case MODIFIED:

                                    break;
                                case REMOVED:

                                    break;
                            }
                        }

                        if(!follow)
                        {
                            FollowUnfollow.setVisibility(View.VISIBLE);
                            FollowUnfollow.setText("Follow");
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

                                    LinearLayout linearLayout = new LinearLayout(getApplicationContext());

                                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                                    linearLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.layout_background));
                                    linearLayout.setPadding(40,40,40,40);

                                    LinearLayout.LayoutParams linearLayoutLayoutParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT);

                                    linearLayoutLayoutParams.setMargins(40, 25, 40, 25);

                                    if(postText.length()>0)
                                    {
                                        TextView textView = new TextView(getApplicationContext());

                                        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams. WRAP_CONTENT ,
                                                LinearLayout.LayoutParams. WRAP_CONTENT ) ;

                                        textLayoutParams.setMargins( 0 , 0 , 0 , 20 ) ;

                                        textView.setText(postText);

                                        textView.setTextSize(20);
                                        textView.setTextColor(getResources().getColor(R.color.black));

                                        linearLayout.addView(textView,textLayoutParams);
                                    }

                                    ImageView imageView = new ImageView(getApplicationContext());

                                    LinearLayout.LayoutParams imageViewLayoutParams = new LinearLayout.LayoutParams(
                                            dpToPx(275),
                                            dpToPx(275));

                                    imageViewLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

                                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                                    Glide.with(getApplicationContext()).load(uri.toString()).into(imageView);

                                    linearLayout.addView(imageView,imageViewLayoutParams);

                                    userProfileLinearLayout.addView(linearLayout,linearLayoutLayoutParams);

                                    break;
                                case MODIFIED:

                                    break;
                                case REMOVED:

                                    break;
                            }
                        }

                    }
                });

        FollowUnfollow.setEnabled(true);
        FollowUnfollow.setVisibility(View.VISIBLE);

        FollowUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                    follow = false;

                    Toast.makeText(UserProfile.this, username + " unfollowed successfully", Toast.LENGTH_LONG).show();

                    noOfFollowers.update("no_of_followers", FieldValue.increment(-1));
                    noOfFollowing.update("no_of_following", FieldValue.increment(-1));

                    db.collection("users").document(user_id).collection("Followers").document(currentUser.getUid()).delete();
                    db.collection("users").document(currentUser.getUid()).collection("Following").document(user_id).delete();
                }
            }
        });

        UserFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent goToUserFollowers = new Intent (getApplicationContext(),UserFollowers.class);

                goToUserFollowers.putExtra("user_id",user_id);

                startActivity(goToUserFollowers);
            }
        });

        UserFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent goToUserFollowing = new Intent (getApplicationContext(),UserFollowing.class);

                goToUserFollowing.putExtra("user_id",user_id);

                startActivity(goToUserFollowing);
            }
        });

    }
}