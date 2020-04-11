package com.example.fbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatActivity extends AppCompatActivity {

    private EditText etMessage;
    private ImageButton btnSend;
    private ListView mList;
    private FirebaseAuth mAuth;
    private DatabaseReference mChatRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mFriendRef;
    private DatabaseReference mFriendChatRef;
    private String friendUid;
    private String senderName;
    private String friendName;
    private FirebaseListOptions<Message> options;
    private FirebaseListAdapter<Message> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent newFriend=getIntent();
        friendUid=newFriend.getStringExtra("NewFriend");
        mFriendRef=FirebaseDatabase.getInstance().getReference().child("Users").child(friendUid);
        etMessage=findViewById(R.id.et_message);
        mList=findViewById(R.id.list_message);
        btnSend=findViewById(R.id.btn_msg_send);
        mAuth=FirebaseAuth.getInstance();
        mUserRef=FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());
        mFriendChatRef=mFriendRef.child("Messages").child(mAuth.getUid());
        mChatRef=mUserRef.child("Messages").child(friendUid);

        //Retrieve sender Name
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                senderName=dataSnapshot.child("ProfileName").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //sending Message to User
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg=etMessage.getText().toString();
                if(!TextUtils.isEmpty(msg)) {
                    DatabaseReference chatRef = mChatRef.push();
                    DatabaseReference frndChat = mFriendChatRef.push();
                    chatRef.child("senderName").setValue(senderName);
                    chatRef.child("message").setValue(msg);
                    frndChat.child("senderName").setValue(senderName);
                    frndChat.child("message").setValue(msg);
                }
                etMessage.setText("");
            }
        });


        //Retrieving Message

        options=new FirebaseListOptions.Builder<Message>()
                .setQuery(mChatRef,Message.class)
                .setLayout(R.layout.message)
                .build();

        adapter=new FirebaseListAdapter<Message>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull Message model, int position) {
                TextView name=v.findViewById(R.id.sender);
                TextView message=v.findViewById(R.id.inboxMessage);
                String user=model.getSenderName();
                GradientDrawable drawable= (GradientDrawable) v.getBackground();
                if(user.equals(senderName))
                {

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
                    params.weight = 1.0f;
                    params.gravity = Gravity.END;
                    drawable.setCornerRadii(new float[] {40, 40,40,40,0,0,40,40});
                    v.setBackground(drawable);



                    name.setLayoutParams(params);
                    message.setLayoutParams(params);
                }else{
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
                    params.weight = 1.0f;
                    params.gravity = Gravity.LEFT;
                    drawable.setCornerRadii(new float[]{40, 40,40,40,40,40,0,0} );
                    v.setBackground(drawable);
                    name.setLayoutParams(params);
                    message.setLayoutParams(params);
                }
                name.setText(model.getSenderName());
                message.setText(model.getMessage());
            }
        };

        mList.setAdapter(adapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveFriendName();
        adapter.startListening();

    }

    private void retrieveFriendName() {
        mFriendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendName=dataSnapshot.child("ProfileName").getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
