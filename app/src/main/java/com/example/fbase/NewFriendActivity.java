package com.example.fbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NewFriendActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserRef;
    private DatabaseReference mChatUsers;
    private FirebaseListAdapter<Friends> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);
        mAuth=FirebaseAuth.getInstance();
        mCurrentUser=mAuth.getCurrentUser();

        ListView mList=findViewById(R.id.list_new_friend);
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        mChatUsers=mUserRef.child(mCurrentUser.getUid()).child("ChatUser");
        FirebaseListOptions<Friends> options=new FirebaseListOptions.Builder<Friends>()
                .setQuery(mUserRef,Friends.class)
                .setLayout(R.layout.friends)
                .build();

        adapter=new FirebaseListAdapter<Friends>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull final Friends model, int position) {
                final String uid = model.getUid();


                    Log.e("UID", ""+uid);
                    ImageView profileImage = v.findViewById(R.id.friend_image);
                    TextView profileName = v.findViewById(R.id.friend_name);

                    Picasso.get().load(Uri.parse(model.getProfileImage())).transform(new CircleTransform()).into(profileImage);
                    profileName.setText(model.getProfileName());
                    Button message = v.findViewById(R.id.friend_message);


                    message.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Add user to chat List
                            mChatUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(uid))
                                    {
                                        Toast.makeText(NewFriendActivity.this, "Welcome Back", Toast.LENGTH_SHORT).show();

                                    }else{
                                        mChatUsers.child(uid).child("uid").setValue(uid);
                                        mChatUsers.child(uid).child("ProfileName").setValue(model.getProfileName());
                                        mChatUsers.child(uid).child("ProfileImage").setValue(model.getProfileImage());
                                        }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            Intent chatIntent = new Intent(NewFriendActivity.this, ChatActivity.class);
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