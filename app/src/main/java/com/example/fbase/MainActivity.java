package com.example.fbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseRef,mUserDb;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    PostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView =findViewById(R.id.post_display);
        mAuth=FirebaseAuth.getInstance();
        mUserDb=FirebaseDatabase.getInstance().getReference().child("Users");
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser()==null)
                {
                    Intent LoginIntent=new Intent(MainActivity.this,LoginActivity.class);
                    LoginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(LoginIntent);

                }
            }
        };
        mDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Blog");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<post> options =
                new FirebaseRecyclerOptions.Builder<post>()
                        .setQuery(mDatabaseRef, post.class)
                        .build();
        adapter=new PostAdapter(options,this);
        mRecyclerView.setAdapter(adapter);



    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUser();
        mAuth.addAuthStateListener(mAuthListener);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_add)
        {
            startActivity(new Intent(MainActivity.this,NewPost.class));
        }
        if (item.getItemId()==R.id.logout)
        {
            mAuth.signOut();
        }
        return super.onOptionsItemSelected(item);
    }
    private void checkUser() {

        if (mAuth.getCurrentUser()!=null) {
            final String userId = mAuth.getCurrentUser().getUid();
            mUserDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(userId)) {
                        Intent update = new Intent(MainActivity.this, ProfileActivity.class);
                        update.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(update);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
}
