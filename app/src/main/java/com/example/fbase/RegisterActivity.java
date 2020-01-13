package com.example.fbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText userName,userMail,userPass;
    private Button register;
    private FirebaseAuth mAuth;
    private DatabaseReference mDbRef;
    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refister);
        userName=findViewById(R.id.et_userName);
        userMail=findViewById(R.id.et_userEmail);
        userPass=findViewById(R.id.et_userPassword);
        mAuth=FirebaseAuth.getInstance();
        mProgress=new ProgressDialog(this);
        mDbRef= FirebaseDatabase.getInstance().getReference().child("Users");
        register=findViewById(R.id.btn_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String name=userName.getText().toString();
        String email=userMail.getText().toString();
        String password=userPass.getText().toString();
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
        {
            mProgress.setMessage("Signing in....");
            mProgress.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                   if (task.isSuccessful())
                   {
                       String userId=mAuth.getCurrentUser().getUid();
                       DatabaseReference newUser=mDbRef.child(userId);
                       newUser.child("ProfileName").setValue(name);
                       newUser.child("ProfileImage").setValue("No image Found");
                       Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                       startActivity(intent);}
                }
            });
        }else{
            Toast.makeText(RegisterActivity.this,"Please provide eveer information!!",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {

        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("Do you Want to Exit?");
        builder.setCancelable(true);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Exit to home of your Phone
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }
}
