package com.tabish.tabzsnap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth frbAuth;
    private FirebaseUser currentUser;

    private Fragment feedFragment;

    private BottomNavigationView bottomNavigationView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.menu_bar_logged_in,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {


            case R.id.aboutus:

                Toast.makeText(this, "About Us", Toast.LENGTH_LONG).show();
                Intent goToAboutUs = new Intent(getApplicationContext(), AboutUs.class);
                startActivity(goToAboutUs);

                return true;


            case R.id.signOut:

                frbAuth.signOut();
                Intent goToMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                goToMainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(goToMainActivity);

                return true;

            default:
                return false;
        }
    }

    public void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frbAuth = FirebaseAuth.getInstance();
        currentUser = frbAuth.getCurrentUser();

        if(currentUser==null)
        {
            Intent goToSignIn = new Intent(getApplicationContext(), SignIn.class);
            goToSignIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(goToSignIn);
        }

        bottomNavigationView = findViewById(R.id.bottomNavBar);

        feedFragment = new FeedFragment();

        switchFragment(feedFragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.feed:

                        FeedFragment feedFragment = new FeedFragment();

                        switchFragment(feedFragment);
                        return true;

                    case R.id.myProfile:

                        MyProfileFragment myProfileFragment = new MyProfileFragment();

                        switchFragment(myProfileFragment);
                        return true;

                    case R.id.newPost:

                        NewPostFragment newPostFragment = new NewPostFragment();

                        switchFragment(newPostFragment);

                        return true;

                    case R.id.editProfile:

                        EditProfileFragment editProfileFragment = new EditProfileFragment();

                        switchFragment(editProfileFragment);
                        return true;

                    case R.id.findPeople:

                        UserListFragment userListFragment = new UserListFragment();
                        switchFragment(userListFragment);
                        return true;
                }
                return false;
            }
        });
    }
}