package com.tabish.tabzsnap;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;

public class FeedFragment extends Fragment {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private SwipeRefreshLayout swipeRefreshLayout;

    private LinearLayout feedLinearLayout;

    private String currUsername;

    private String following_id;

    private ArrayList<String> followingList;

    private String uri;

    private String postText;

    private String owner;

    private String ownerName;

    private TextView welcomeText;

    //to set width and height dynamically in dp
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void showPost()
    {
        db.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                owner = (String) document.get("owner");

                                if(followingList.contains(owner))
                                {
                                    uri = (String) document.get("uri");
                                    ownerName = (String) document.get("ownerName");
                                    postText = (String) document.get("text");

                                    LinearLayout linearLayout = new LinearLayout(requireContext().getApplicationContext());

                                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                                    linearLayout.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.layout_background));
                                    linearLayout.setPadding(40,40,40,40);

                                    LinearLayout.LayoutParams linearLayoutLayoutParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT);

                                    linearLayoutLayoutParams.setMargins(40, 25, 40, 25);

                                    TextView followingName = new TextView(requireContext().getApplicationContext());

                                    followingName.setLayoutParams(new ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    ));

                                    if(owner.equals(currentUser.getUid()))
                                    {
                                        followingName.setText("~"+ownerName+" (You)");
                                    }
                                    else
                                    {
                                        followingName.setText("~"+ownerName);
                                    }

                                    followingName.setTextSize(20);
                                    followingName.setTextColor(getResources().getColor(R.color.black));

                                    linearLayout.addView(followingName);

                                    if(postText.length()>0)
                                    {
                                        TextView textView = new TextView(requireContext().getApplicationContext());

                                        LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams. WRAP_CONTENT ,
                                                LinearLayout.LayoutParams. WRAP_CONTENT );

                                        textViewLayoutParams.setMargins(0,20,0,0);

                                        textView.setText(postText);

                                        textView.setTextSize(20);
                                        textView.setTextColor(getResources().getColor(R.color.black));

                                        linearLayout.addView(textView,textViewLayoutParams);
                                    }

                                    ImageView imageView = new ImageView(requireContext().getApplicationContext());

                                    LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(
                                            dpToPx(275),
                                            dpToPx(275));

                                    imageLayoutParams.setMargins(0,20,0,0);

                                    imageLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

                                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                                    Glide.with(requireContext().getApplicationContext()).load(uri.toString()).into(imageView);

                                    linearLayout.addView(imageView, imageLayoutParams);

                                    feedLinearLayout.addView(linearLayout,linearLayoutLayoutParams);
                                }
                            }

                        } else {
                            //Log.i("Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        if (isAdded()) {

            frbAuth = FirebaseAuth.getInstance();
            currentUser = frbAuth.getCurrentUser();
            db = FirebaseFirestore.getInstance();

            swipeRefreshLayout = view.findViewById(R.id.feedSwipeRefresh);

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    FeedFragment feedFragment = new FeedFragment();
                    ((MainActivity) requireActivity()).switchFragment(feedFragment);

                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            followingList = new ArrayList<String>();

            feedLinearLayout = view.findViewById(R.id.feedLinearLayout);

            welcomeText = view.findViewById(R.id.welcomeText);

            db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            currUsername = (String) document.get("username");
                            followingList.add(currentUser.getUid());

                            welcomeText.setText("Welcome " + currUsername);

                            db.collection("users").document(currentUser.getUid()).collection("Following")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    following_id = (String) document.get("user_id");

                                                    followingList.add(following_id);

                                                }

                                                showPost();

                                            } else {
                                                //Log.i("Error getting documents: ", task.getException());
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


        }

        return view;
    }
}