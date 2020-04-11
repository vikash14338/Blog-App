package com.example.fbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private FirebaseRecyclerAdapter adapter;
    private FirebaseRecyclerOptions<post> options;
    private boolean mPostLike=false;
    private DatabaseReference mLikeRef= FirebaseDatabase.getInstance().getReference().child("Likes");

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
        options = new FirebaseRecyclerOptions.Builder<post>()
                        .setQuery(mDatabaseRef, post.class)
                        .build();
        createRecyclerAdapter();
        mRecyclerView.setAdapter(adapter);



    }

    private void createRecyclerAdapter() {

        adapter=new FirebaseRecyclerAdapter<post,BlogViewHolder>(options) {

            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new BlogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_display,
                        parent,false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final BlogViewHolder holder, int position, @NonNull final post model) {


                final String post_key=getRef(position).getKey();
                holder.title.setText(model.getTitle());
                holder.description.setText(model.getDescription());
                Picasso.get().load(model.getImageUrl()).into(holder.postImage);
                holder.userName.setText("~"+model.getUserName());
                Log.e("image uri :",""+model.getImageUrl());

                holder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Toast.makeText(MainActivity.this,""+mAuth.getCurrentUser().getUid(),Toast.LENGTH_SHORT).show();

                    }
                });

                mLikeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(post_key).hasChild(mAuth.getUid()))
                        {
                            holder.likeBtn.setImageResource(R.drawable.image_like_red);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                holder.likeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPostLike = true;

                        mLikeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (mPostLike) {

                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getUid())) {

                                        mLikeRef.child(post_key).child(mAuth.getUid()).removeValue();
                                        holder.likeBtn.setImageResource(R.drawable.image_like_grey);
                                    } else {
                                        mLikeRef.child(post_key).child(mAuth.getUid()).setValue("random");
                                        holder.likeBtn.setImageResource(R.drawable.image_like_red);
                                    }
                                    mPostLike=false;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });

                holder.comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent comIntent=new Intent(MainActivity.this,DetailActivity.class);
                        comIntent.putExtra("post_id",post_key);
                        startActivity(comIntent);
                    }
                });
                //Implementation of Post is done here
                holder.shareBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent share=new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");
                        //share.putExtra(Intent.EXTRA_SUBJECT, ""+model.getTitle());
                        share.putExtra(Intent.EXTRA_TEXT,model.getDescription());
                        share.putExtra(Intent.EXTRA_TEXT,model.getTitle()+"\n"+model.getDescription()+"\n"+model.getImageUrl());
                        startActivity(Intent.createChooser(share,"Share Via"));
                    }
                });
            }

        };
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
        if (item.getItemId()==R.id.friend)
        {
            startActivity(new Intent(MainActivity.this,NewFriendActivity.class));
        }
        if (item.getItemId()==R.id.chat){
            startActivity(new Intent(MainActivity.this,FriendsActivity.class));
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

    public static class BlogViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView title,description,userName;
        ImageView postImage;
        ImageButton likeBtn,shareBtn;
        Button comment;
        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            title=itemView.findViewById(R.id.post_title);
            description=itemView.findViewById(R.id.post_desc);
            postImage=itemView.findViewById(R.id.post_image);
            userName=itemView.findViewById(R.id.post_user);
            likeBtn=itemView.findViewById(R.id.btn_like);
            comment=itemView.findViewById(R.id.btn_comment);
            shareBtn=itemView.findViewById(R.id.btn_share);
        }
    }
}
