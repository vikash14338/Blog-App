package com.example.fbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private ImageView mPostImage;
    private TextView mPostDesc, mPostUser;
    private EditText mComment;
    private ImageButton mSendComment;
    private ListView mListView;

    private String userName;
    private DatabaseReference mPostRef;
    private DatabaseReference mCommentRef;
    private String post_Id;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private FirebaseListOptions<Comment> options;
    private FirebaseListAdapter<Comment> adapter;
    private View comHeader;
    private String profileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent comInt = getIntent();
        post_Id = comInt.getStringExtra("post_id");

        initializeUI();
        mListView.addHeaderView(comHeader);

        options = new FirebaseListOptions.Builder<Comment>()
                .setQuery(mCommentRef,Comment.class)
                .setLayout(R.layout.comment)
                .build();

        //comment View Display implementation

        adapter = new FirebaseListAdapter<Comment>(options) {

            @Override
            protected void populateView(@NonNull View v, @NonNull Comment model, int position) {

                View listItemView = v;

                TextView user = listItemView.findViewById(R.id.com_user);
                TextView desc = listItemView.findViewById(R.id.com_description);
                user.setText(model.getUser());
                desc.setText(model.getComment());



            }
        };
        mListView.setAdapter(adapter);

        mPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("userName").getValue() != null || dataSnapshot.child("Description").getValue() != null) {

                    mPostDesc.setText(dataSnapshot.child("Description").getValue().toString());
                    userName=dataSnapshot.child("userName").getValue().toString();
                    mPostUser.setText(userName);
                } else {
                    Toast.makeText(DetailActivity.this, "Something went Wrong", Toast.LENGTH_SHORT).show();
                }
                Picasso.get().load(Uri.parse(dataSnapshot.child("ImageUrl").getValue().toString())).into(mPostImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //retrieve profile name of user
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profileName=dataSnapshot.child("ProfileName").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //add comment to the post
        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String com = mComment.getText().toString();
                mComment.setText("");
                DatabaseReference postComm = mCommentRef.push();


                postComm.child("comment").setValue(com);
                postComm.child("user").setValue(profileName);

            }
        });

    }

    private void initializeUI() {
        comHeader=getLayoutInflater().inflate(R.layout.comment_header,null);
        mPostImage = comHeader.findViewById(R.id.com_image);
        mPostUser = comHeader.findViewById(R.id.com_post_user);
        mPostDesc = comHeader.findViewById(R.id.com_post_desc);
        mComment = comHeader.findViewById(R.id.et_comments);
        mSendComment = comHeader.findViewById(R.id.btn_com_send);
        mListView = findViewById(R.id.list_comment);
        mPostRef = FirebaseDatabase.getInstance().getReference().child("Blog").child(post_Id);
        mCommentRef = mPostRef.child("Comments");
        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());
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