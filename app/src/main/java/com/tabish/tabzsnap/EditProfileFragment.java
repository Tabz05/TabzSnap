package com.tabish.tabzsnap;

import android.Manifest;
import android.app.Activity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private Uri selectedImage;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Button removePic;

    private Button chooseProfilePicEdit;
    private Button removeProfilePicEdit;
    private Button submitEditProfile;

    private ImageView imageView3;

    private boolean hasProfilePic;

    private String profilePicUri;

    private void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        someActivityResultLauncher.launch(intent);
    }

    //requesting permission

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto();
            }
        }
    }

    /*public void updateUsername()
    {
        db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //   Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        currUsername = (String) document.get("username");

                        db.collection("usernames").get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult())
                                            {
                                                existing_username=(String) document.getId();

                                                if(existing_username.equals(newUsername.getText().toString()))
                                                {
                                                    found=1;
                                                }
                                            }

                                            if(found==0)
                                            {
                                                new_username =newUsername.getText().toString();
                                                Map<String, Object> newUsername = new HashMap<>();
                                                newUsername.put("username",new_username);

                                                db.collection("users").document(currentUser.getUid())
                                                        .update(newUsername)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                Map<String, Object> nameUpdate = new HashMap<>();
                                                                nameUpdate.put(new_username,currentUser.getUid());

                                                                db.collection("usernames").document(new_username)
                                                                        .set(nameUpdate)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                db.collection("usernames").document(currUsername).delete();


                                                                                db.collectionGroup("Followers").whereEqualTo("username", currUsername).get()
                                                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                            @Override
                                                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                                                                    document.update(newUsername)
                                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onSuccess(Void aVoid) {

                                                                                                                }
                                                                                                            })
                                                                                                   .addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception e) {

                                                                                                    }
                                                                                                });
                                                                                                }
                                                                                            }
                                                                                        });



                                                                                Toast.makeText(EditProfile.this, "Username changed!!", Toast.LENGTH_SHORT).show();

                                                                                if (selectedImage != null)
                                                                                {
                                                                                    updateImage();
                                                                                }
                                                                                else
                                                                                {
                                                                                    Intent goToMainActivity = new Intent(getApplicationContext(),MainActivity.class);

                                                                                    startActivity(goToMainActivity);
                                                                                }

                                                                            }

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
                                                            public void onFailure(@NonNull Exception e) {

                                                            }
                                                        });


                                            }
                                            else
                                            {
                                                Toast.makeText(EditProfile.this, "Username is already in use", Toast.LENGTH_SHORT).show();
                                            }


                                        } else {
                                            //Log.i("Error getting documents: ", task.getException());
                                        }

                    }
                                });


                    }
                    else {
                        //   Log.d(TAG, "No such document");
                    }


            } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }

        }
        });
    }*/

    private void updateImage()
    {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        // Defining the child of storageReference
        StorageReference ref = storageReference.child("users").child(currentUser.getUid()).child("profilePic");

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

                                        Map<String, Object> userProfilePic = new HashMap<>();
                                        userProfilePic.put("hasProfilePic", true);
                                        userProfilePic.put("profilePicUri", uri.toString());

                                        db.collection("users").document(currentUser.getUid())
                                                .update(userProfilePic)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        // Image uploaded successfully
                                                        // Dismiss dialog
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getContext(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
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
                        //Toast.makeText(EditProfile.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        if(isAdded()) {

            frbAuth = FirebaseAuth.getInstance();
            currentUser = frbAuth.getCurrentUser();
            db = FirebaseFirestore.getInstance();

            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();

            imageView3 = view.findViewById(R.id.imageView3);
            removePic = view.findViewById(R.id.removeProfilePicEdit);

            chooseProfilePicEdit = view.findViewById(R.id.chooseProfilePicEdit);
            removeProfilePicEdit = view.findViewById(R.id.removeProfilePicEdit);
            submitEditProfile = view.findViewById(R.id.submitEditProfile);

            DocumentReference docRef = db.collection("users").document(currentUser.getUid());

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //   Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            String currUsername = (String) document.get("username");
                            hasProfilePic = (boolean) document.get("hasProfilePic");
                            profilePicUri = (String) document.get("profilePicUri");

                            if (hasProfilePic) {
                                Glide.with(requireContext().getApplicationContext()).load(profilePicUri.toString()).into(imageView3);

                                removePic.setVisibility(View.VISIBLE);
                                removePic.setEnabled(true);

                            } else {
                                imageView3.setImageResource(R.drawable.profilepic);
                            }

                            chooseProfilePicEdit.setEnabled(true);
                            chooseProfilePicEdit.setVisibility(View.VISIBLE);

                        } else {
                            //   Log.d(TAG, "No such document");
                        }
                    } else {
                        //Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            chooseProfilePicEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //checking if permission for gallery has been granted or not
                        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        getPhoto();
                    }
                }
            });

            removeProfilePicEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    submitEditProfile.setEnabled(false);
                    submitEditProfile.setVisibility(View.INVISIBLE);

                    if (!hasProfilePic) {
                        imageView3.setImageResource(R.drawable.profilepic);
                        removePic.setVisibility(View.INVISIBLE);
                        removePic.setEnabled(false);

                        Toast.makeText(requireContext(), "pic removed", Toast.LENGTH_LONG).show();
                    }

                    if (hasProfilePic) {
                        hasProfilePic = false;

                        Map<String, Object> userProfilePic = new HashMap<>();
                        userProfilePic.put("hasProfilePic", false);
                        userProfilePic.put("profilePicUri", "");

                        db.collection("users").document(currentUser.getUid())
                                .update(userProfilePic)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        StorageReference storageReference = storage.getReferenceFromUrl(profilePicUri.toString());
                                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Toast.makeText(requireContext(), "your profile picture has been removed", Toast.LENGTH_LONG).show();

                                                imageView3.setImageResource(R.drawable.profilepic);
                                                removePic.setVisibility(View.INVISIBLE);
                                                removePic.setEnabled(false);

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
                    }
                }
            });

            submitEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                /* if(!(newUsername.getText().toString().isEmpty()))
        {
           updateUsername();
        }*/

                    //if((newUsername.getText().toString().isEmpty()) && selectedImage != null)
                    //  {

                    updateImage();

                    // }
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
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes

                        Intent data = result.getData();

                        selectedImage = data.getData();

                        Glide.with(requireContext().getApplicationContext()).asBitmap().load(selectedImage.toString()).into(imageView3);

                        removePic.setVisibility(View.VISIBLE);
                        removePic.setEnabled(true);

                        submitEditProfile.setEnabled(true);
                        submitEditProfile.setVisibility(View.VISIBLE);
                    }
                }
            });
}