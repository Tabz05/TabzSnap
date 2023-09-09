package com.tabish.tabzsnap;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class UserListFragment extends Fragment {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private ListView listViewUser;

    private String currUsername;

    private String User_Id;
    private String username;

    private ArrayList<String> usersList;
    private ArrayList<String> followingList;

    private ArrayAdapter<String> arrayAdapter;

    private void getUserList()
    {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                username=(String) document.get("username");

                                if(!username.equals(currUsername) && !followingList.contains(username))
                                {
                                    usersList.add(username);
                                }
                            }

                            arrayAdapter.notifyDataSetChanged();
                        } else {
                            //Log.i("Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        if(isAdded()) {

            frbAuth = FirebaseAuth.getInstance();
            currentUser = frbAuth.getCurrentUser();
            db = FirebaseFirestore.getInstance();

            followingList = new ArrayList<String>();

            listViewUser = view.findViewById(R.id.listViewUser);

            usersList = new ArrayList<String>();

            arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, usersList);

            listViewUser.setAdapter(arrayAdapter);

            db.collection("users").document(currentUser.getUid()).collection("Following").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String following = (String) document.get("username");
                                    followingList.add(following);
                                }

                                DocumentReference DocRef = db.collection("users").document(currentUser.getUid());

                                DocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {

                                                currUsername = (String) document.get("username");

                                                getUserList();

                                            } else {
                                                //   Log.d(TAG, "No such document");
                                            }
                                        } else {
                                            //Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });

                            } else {
                                //Log.i("Error getting documents: ", task.getException());
                            }
                        }
                    });

            listViewUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    db.collection("users")
                            .whereEqualTo("username", usersList.get(i).toString())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {

                                            User_Id = (String) document.getId();

                                            Intent goToUserProfile = new Intent(requireContext().getApplicationContext(), UserProfile.class);

                                            goToUserProfile.putExtra("user_id", User_Id);
                                            goToUserProfile.putExtra("currUsername", currUsername);

                                            startActivity(goToUserProfile);
                                        }
                                    } else {

                                    }
                                }
                            });
                }
            });

        }

        return view;
    }
}