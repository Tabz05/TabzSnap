package com.tabish.tabzsnap;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UploadPost extends AppCompatActivity {

    FirebaseAuth frbAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db;

    public Uri selectedImage;

    FirebaseStorage storage;
    StorageReference storageReference;

    ImageView imageView;

    EditText postText;

    Button removePostPic;

    Timestamp postUploadedTimeStamp;

    DocumentReference noOfPosts;

    public void getPhoto()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        someActivityResultLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto();
            }
        }
    }

    public void uploadPostPic (View view)
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //checking if permission for gallery has been granted or not
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            getPhoto();
        }
    }

    public void submitPost(View view)
    {
        if (selectedImage != null) {

            db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            String currUsername = (String) document.get("username");

                            Map<String, Object> newPost = new HashMap<>();
                            postUploadedTimeStamp = new Timestamp(new Date());
                            newPost.put("timestamp", postUploadedTimeStamp);
                            newPost.put("owner", currentUser.getUid());
                            newPost.put("ownerName", currUsername);
                            newPost.put("text", postText.getText().toString());

                            noOfPosts.update("total_no_of_posts", FieldValue.increment(1));
                            noOfPosts.update("no_of_posts_active", FieldValue.increment(1));

                            // Code for showing progressDialog while uploading
                            ProgressDialog progressDialog = new ProgressDialog(UploadPost.this);
                            progressDialog.setTitle("Uploading...");
                            progressDialog.show();

                            // Defining the child of storageReference
                            StorageReference ref = storageReference.child("users").child(currentUser.getUid()).child(currentUser.getUid()+postUploadedTimeStamp);

                            // adding listeners on upload
                            // or failure of image
                            ref.putFile(selectedImage).addOnSuccessListener(
                                    new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                        @Override
                                        public void onSuccess(
                                                UploadTask.TaskSnapshot taskSnapshot)
                                        {

                                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    newPost.put("uri", uri.toString());

                                                    db.collection("users").document(currentUser.getUid()).collection("posts").document(currentUser.getUid()+postUploadedTimeStamp)
                                                            .set(newPost)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {


                                                                    db.collection("posts").document(currentUser.getUid()+postUploadedTimeStamp)
                                                                            .set(newPost)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {

                                                                                    // Image uploaded successfully
                                                                                    // Dismiss dialog
                                                                                    progressDialog.dismiss();
                                                                                    Toast.makeText(UploadPost.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();

                                                                                    Intent goToMainActivity = new Intent(getApplicationContext(),MainActivity.class);

                                                                                    startActivity(goToMainActivity);
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(UploadPost.this, e.toString() , Toast.LENGTH_SHORT).show();
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
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {

                                                }
                                            });
                                        }
                                    })

                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {

                                            // Error, Image not uploaded
                                            progressDialog.dismiss();
                                            Toast.makeText(UploadPost.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnProgressListener(
                                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                                // Progress Listener for loading
                                                // percentage on the dialog box
                                                @Override
                                                public void onProgress(
                                                        UploadTask.TaskSnapshot taskSnapshot)
                                                {
                                                    double progress
                                                            = (100.0
                                                            * taskSnapshot.getBytesTransferred()
                                                            / taskSnapshot.getTotalByteCount());
                                                    progressDialog.setMessage("Uploaded " + (int)progress + "%");
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
        else
        {
            Toast.makeText(this,"Please select a photo",Toast.LENGTH_LONG).show();
        }
    }

    public void removePic(View view)
    {
        selectedImage=null;

        imageView.setImageResource(R.drawable.profilepic);
        removePostPic.setVisibility(View.INVISIBLE);
        removePostPic.setEnabled(false);
    }

    public boolean isNetworkAvailable() { // to check if connected to internet
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
        setContentView(R.layout.activity_upload_post);

        frbAuth = FirebaseAuth.getInstance();
        currentUser = frbAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        imageView = findViewById(R.id.postImageView);

        removePostPic = findViewById(R.id.removePostPic);

        postText= findViewById(R.id.postText);

        noOfPosts=db.collection("users").document(currentUser.getUid());
    }

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes

                        Intent data = result.getData();

                        selectedImage = data.getData();

                        Glide.with(getApplicationContext()).asBitmap().load(selectedImage.toString()).into(imageView);

                        removePostPic.setVisibility(View.VISIBLE);
                        removePostPic.setEnabled(true);
                    }
                }
            });
}