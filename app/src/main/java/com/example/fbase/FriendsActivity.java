package com.example.fbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class FriendsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private FirebaseUser mUser;
    private FirebaseListAdapter<Friends> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        ListView mList=findViewById(R.id.list_friend);
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid()).child("ChatUser");


        FirebaseListOptions<Friends> options=new FirebaseListOptions.Builder<Friends>()
                .setQuery(mUserRef,Friends.class)
                .setLayout(R.layout.friends)
                .build();


         adapter=new FirebaseListAdapter<Friends>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull final Friends model, int position) {

                ImageView profileImage = v.findViewById(R.id.friend_image);
                TextView profileName = v.findViewById(R.id.friend_name);
                Button message = v.findViewById(R.id.friend_message);
                   final String uid=model.getUid();
                   Picasso.get().load(Uri.parse(model.getProfileImage())).transform(new CircleTransform()).into(profileImage);
                   profileName.setText(model.getProfileName());


                    message.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(FriendsActivity.this, "Welcome Back", Toast.LENGTH_SHORT).show();
                            Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                            chatIntent.putExtra("NewFriend", uid);
                            startActivity(chatIntent);
                        }
                    });
                }

        };

        mList.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
