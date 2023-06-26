package com.tabish.tabzsnap;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static android.app.Activity.RESULT_OK;

public class NewPostFragment extends Fragment {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private Uri selectedImage;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private ImageView imageView;

    private EditText postText;

    private Button choosePostPic;
    private Button removePostPic;
    private Button submitPostPic;

    private Timestamp postUploadedTimeStamp;

    private DocumentReference noOfPosts;

    private void getPhoto()
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_new_post, container, false);

        if(isAdded()) {

            frbAuth = FirebaseAuth.getInstance();
            currentUser = frbAuth.getCurrentUser();
            db = FirebaseFirestore.getInstance();

            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();

            imageView = view.findViewById(R.id.postImageView);

            choosePostPic = view.findViewById(R.id.choosePostPic);
            removePostPic = view.findViewById(R.id.removePostPic);
            submitPostPic = view.findViewById(R.id.submitPostPic);

            postText = view.findViewById(R.id.postText);

            noOfPosts = db.collection("users").document(currentUser.getUid());

            choosePostPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //checking if permission for gallery has been granted or not
                        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        getPhoto();
                    }
                }
            });

            removePostPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    selectedImage = null;

                    imageView.setImageResource(R.drawable.profilepic);
                    removePostPic.setVisibility(View.INVISIBLE);
                    removePostPic.setEnabled(false);
                }
            });

            submitPostPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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

                                        noOfPosts.update("no_of_posts", FieldValue.increment(1));

                                        // Code for showing progressDialog while uploading
                                        ProgressDialog progressDialog = new ProgressDialog(requireContext());
                                        progressDialog.setTitle("Uploading...");
                                        progressDialog.show();

                                        // Defining the child of storageReference
                                        StorageReference ref = storageReference.child("users").child(currentUser.getUid()).child(currentUser.getUid() + postUploadedTimeStamp);

                                        // adding listeners on upload
                                        // or failure of image
                                        ref.putFile(selectedImage).addOnSuccessListener(
                                                        new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                                            @Override
                                                            public void onSuccess(
                                                                    UploadTask.TaskSnapshot taskSnapshot) {

                                                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                    @Override
                                                                    public void onSuccess(Uri uri) {

                                                                        newPost.put("uri", uri.toString());

                                                                        db.collection("users").document(currentUser.getUid()).collection("posts").document(currentUser.getUid() + postUploadedTimeStamp)
                                                                                .set(newPost)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {


                                                                                        db.collection("posts").document(currentUser.getUid() + postUploadedTimeStamp)
                                                                                                .set(newPost)
                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {

                                                                                                        // Image uploaded successfully
                                                                                                        // Dismiss dialog
                                                                                                        progressDialog.dismiss();
                                                                                                        Toast.makeText(requireContext(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                })
                                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                        Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_SHORT).show();
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
                                                    public void onFailure(@NonNull Exception e) {

                                                        // Error, Image not uploaded
                                                        progressDialog.dismiss();
                                                        Toast.makeText(requireContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnProgressListener(
                                                        new OnProgressListener<UploadTask.TaskSnapshot>() {

                                                            // Progress Listener for loading
                                                            // percentage on the dialog box
                                                            @Override
                                                            public void onProgress(
                                                                    UploadTask.TaskSnapshot taskSnapshot) {
                                                                double progress
                                                                        = (100.0
                                                                        * taskSnapshot.getBytesTransferred()
                                                                        / taskSnapshot.getTotalByteCount());
                                                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
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

                    } else {
                        Toast.makeText(requireContext(), "Please select a photo", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

        return view;
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        // There are no request codes

                        Intent data = result.getData();

                        selectedImage = data.getData();

                        Glide.with(requireContext().getApplicationContext()).asBitmap().load(selectedImage.toString()).into(imageView);

                        removePostPic.setVisibility(View.VISIBLE);
                        removePostPic.setEnabled(true);
                    }

                }
            });
}