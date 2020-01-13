package com.example.fbase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE=55;
    private EditText userName;
    private Button update;
    private DatabaseReference mUserDatabase;
    private StorageReference mUserImageStorage;
    private ImageButton userImage;
    private Uri imageUri=null;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        userName=findViewById(R.id.upd_username);
        update=findViewById(R.id.btn_upd);
        userImage=findViewById(R.id.upd_Image);
        mUserImageStorage= FirebaseStorage.getInstance().getReference().child("Users-Profile-Images");
        mAuth=FirebaseAuth.getInstance();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mProgress=new ProgressDialog(this);

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST_CODE);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress.setMessage("Please wait...");
                mProgress.show();
                final String uid=mAuth.getCurrentUser().getUid();
                final String name=userName.getText().toString();
                if (!TextUtils.isEmpty(name) && imageUri!=null) {
                    final StorageReference filePath=mUserImageStorage.child(imageUri.getLastPathSegment());
                    UploadTask uploadTask=filePath.putFile(imageUri);
                    final Task<Uri> uriTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful())
                            {
                                throw task.getException();
                            }
                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful())
                            {
                                DatabaseReference newUser = mUserDatabase.child(uid);
                                newUser.child("ProfileName").setValue(name);
                                newUser.child("ProfileImage").setValue(imageUri.toString());
                                mProgress.dismiss();
                                finish();
                            }
                        }
                    });
                }
            }
        });
        mProgress.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GALLERY_REQUEST_CODE && resultCode==RESULT_OK)
        {
           Uri imageUri=data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri= result.getUri();
                userImage.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
