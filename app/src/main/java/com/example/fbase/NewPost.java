package com.example.fbase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.PriorityQueue;

public class NewPost extends AppCompatActivity {

    private ImageButton imagePicker;
    private EditText mTitle;
    private EditText mDescription;
    private Uri mImageUri=null;
    private ProgressDialog mprogressBar;
    private Button mPostButton;
    private static final int PICKER_RESULT=55;
    private StorageReference mStorage;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mUserDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        imagePicker=findViewById(R.id.btn_image);
        mTitle=findViewById(R.id.et_title);
        mDescription=findViewById(R.id.et_description);
        mprogressBar=new ProgressDialog(this);
        mDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Blog");
        mPostButton=findViewById(R.id.btn_post);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        mUserDb=FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid());
        mStorage=FirebaseStorage.getInstance().getReference();
        imagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent picker=new Intent(Intent.ACTION_GET_CONTENT);
                picker.setType("image/*");
                startActivityForResult(picker,PICKER_RESULT);

            }
        });

        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });

    }

    private void startPosting() {
        mprogressBar.setMessage("Posting to Blog");
        mprogressBar.show();
        final String title=mTitle.getText().toString();
        final String description=mDescription.getText().toString();
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && mImageUri!=null)
        {

            final StorageReference mRef=mStorage.child("Blog Images").child(mImageUri.getLastPathSegment());
            UploadTask uploadTask = mRef.putFile(mImageUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then( Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return mRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult();
                        final DatabaseReference newPost=mDatabaseRef.push();
                        mUserDb.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                newPost.child("Title").setValue(title);
                                newPost.child("Description").setValue(description);
                                newPost.child("ImageUrl").setValue(downloadUri.toString());
                                newPost.child("UID").setValue(mUser.getUid());
                                newPost.child("userName").setValue(dataSnapshot.child("ProfileName").getValue())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                {
                                                    finish();
                                                }
                                            }
                                        });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                        mprogressBar.dismiss();

                    } else {

                        mprogressBar.dismiss();
                        Toast.makeText(NewPost.this,"Something Went Wrong in Uploading",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else {
            mprogressBar.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICKER_RESULT && resultCode==RESULT_OK){
            mImageUri=data.getData();
            imagePicker.setImageURI(mImageUri);
        }
    }
}
