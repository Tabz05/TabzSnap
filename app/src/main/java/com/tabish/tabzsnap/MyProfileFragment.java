package com.tabish.tabzsnap;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import java.util.ArrayList;
import java.util.Map;

public class MyProfileFragment extends Fragment {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private FirebaseStorage storage;

    private TextView myFollowerText;
    private TextView myFollowingText;

    private TextView usernameText;
    private TextView emailText;
    private TextView myPostText;
    private ImageView myProfilePicture;

    private SwipeRefreshLayout swipeRefreshLayout;

    private LinearLayout myProfilelinearLayout;

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

    int count=-1;

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        if(isAdded()) {

            frbAuth = FirebaseAuth.getInstance();
            currentUser = frbAuth.getCurrentUser();
            db = FirebaseFirestore.getInstance();

            storage = FirebaseStorage.getInstance();

            swipeRefreshLayout = view.findViewById(R.id.myProfileSwipeRefresh);

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    MyProfileFragment myProfileFragment = new MyProfileFragment();
                    ((MainActivity) requireActivity()).switchFragment(myProfileFragment);

                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            noOfPosts = db.collection("users").document(currentUser.getUid());

            uriList = new ArrayList<String>();
            timeStampList = new ArrayList<Timestamp>();

            myFollowerText = view.findViewById(R.id.myFollowerText);
            myFollowingText = view.findViewById(R.id.myFollowingText);

            usernameText = view.findViewById(R.id.usernameText);
            emailText = view.findViewById(R.id.emailText);
            myProfilePicture = view.findViewById(R.id.myProfilePic);
            myPostText = view.findViewById(R.id.myPostText);

            myProfilelinearLayout = view.findViewById(R.id.myProfileLinearLayout);

            db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            Map<String, Object> userDetails = document.getData();
                            username = userDetails.get("username").toString();
                            email = userDetails.get("email").toString();
                            // joinedOn = userDetails.get("joinedOn").toString();
                            no_of_followers = userDetails.get("no_of_followers").toString();
                            no_of_following = userDetails.get("no_of_following").toString();
                            no_of_posts = userDetails.get("no_of_posts").toString();
                            hasProfilePic = userDetails.get("hasProfilePic").toString();

                            usernameText.setText(username);
                            emailText.setText(email);

                            myFollowerText.setText("My Followers (" + no_of_followers.toString() + ")");
                            myFollowingText.setText("My Followings (" + no_of_following.toString() + ")");

                            if (hasProfilePic.equals("true")) {
                                profileUri = userDetails.get("profilePicUri").toString();
                                Glide.with(requireContext().getApplicationContext()).load(profileUri.toString()).into(myProfilePicture);
                            } else {
                                myProfilePicture.setImageResource(R.drawable.profilepic);
                            }

                            myPostText.setText("Your Posts (" + no_of_posts.toString() + ")");

                        } else {
                            //   Log.d(TAG, "No such document");
                        }
                    } else {
                        //Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            db.collection("users").document(currentUser.getUid()).collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    count += 1;

                                    uri = (String) document.get("uri");

                                    uriList.add(uri.toString());

                                    timestamp = (Timestamp) document.get("timestamp");

                                    timeStampList.add(timestamp);

                                    postText = (String) document.get("text");

                                    LinearLayout linearLayout = new LinearLayout(requireContext().getApplicationContext());

                                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                                    linearLayout.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.layout_background));
                                    linearLayout.setPadding(40, 40, 40, 40);

                                    LinearLayout.LayoutParams linearLayoutLayoutParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT);

                                    linearLayoutLayoutParams.setMargins(40, 25, 40, 25);

                                    if (postText.length() > 0) {
                                        TextView textView = new TextView(requireContext().getApplicationContext());

                                        LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT);

                                        textViewLayoutParams.setMargins(0, 0, 0, 20);

                                        textView.setText(postText);

                                        textView.setTextSize(20);
                                        textView.setTextColor(getResources().getColor(R.color.black));

                                        linearLayout.addView(textView, textViewLayoutParams);
                                    }

                                    ImageView imageView = new ImageView(requireContext().getApplicationContext());

                                    LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(
                                            dpToPx(275),
                                            dpToPx(275));

                                    imageLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

                                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                                    Glide.with(requireContext().getApplicationContext()).load(uri.toString()).into(imageView);

                                    linearLayout.addView(imageView, imageLayoutParams);

                                    ImageButton deleteButton = new ImageButton(requireContext().getApplicationContext());

                                    LinearLayout.LayoutParams layoutParamsButton = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT);

                                    layoutParamsButton.setMargins(0, 20, 0, 0);

                                    deleteButton.setImageResource(R.drawable.ic_baseline_delete_forever_24);
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

                                                    db.collection("users").document(currentUser.getUid()).collection("posts").document(currentUser.getUid() + timeStampList.get(delete_button_tag))
                                                            .delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    db.collection("posts").document(currentUser.getUid() + timeStampList.get(delete_button_tag))
                                                                            .delete()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {

                                                                                    Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_LONG).show();

                                                                                    uriList.remove(delete_button_tag);

                                                                                    count-=1;

                                                                                    noOfPosts.update("no_of_posts", FieldValue.increment(-1));

                                                                                    MyProfileFragment myProfileFragment = new MyProfileFragment();
                                                                                    ((MainActivity) requireActivity()).switchFragment(myProfileFragment);
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

                                    linearLayout.addView(deleteButton, layoutParamsButton);

                                    myProfilelinearLayout.addView(linearLayout, linearLayoutLayoutParams);

                                }

                            } else {
                                //Log.i("Error getting documents: ", task.getException());
                            }
                        }
                    });

            myFollowerText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent goToMyFollowers = new Intent(requireContext().getApplicationContext(), MyFollowers.class);
                    goToMyFollowers.putExtra("user_id", currentUser.getUid());

                    startActivity(goToMyFollowers);
                }
            });

            myFollowingText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent goToMyFollowing = new Intent(requireContext().getApplicationContext(), MyFollowing.class);

                    goToMyFollowing.putExtra("user_id", currentUser.getUid());
                    startActivity(goToMyFollowing);
                }
            });

        }

        return view;
    }
}