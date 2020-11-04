package com.example.safenavigation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.safenavigation.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private String name,email,password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_register);
        init();
        if(firebaseAuth.getCurrentUser()!=null)
        {
            startActivity(new Intent(RegisterActivity.this,MainActivity.class));
            finish();
        }

        binding.loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();



            }
        });

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                name=binding.registerNameET.getText().toString();
                email=binding.registerEmailET.getText().toString();
                password=binding.registerPasswordET.getText().toString();
                nullCheckRegistration();




            }
        });


    }

    private void nullCheckRegistration() {
        if(name!=null){
            if(email!=null){
                if(password!=null){

                    register(name,email,password);


                }else {
                    Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(this, "Enter Email Address", Toast.LENGTH_SHORT).show();
            }

        }
        else{
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();

    }

    private void register(final String name, final String email, final String password) {

        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                   // Toast.makeText(RegisterActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                    //database update will be done in future
                    String userId=firebaseAuth.getCurrentUser().getUid();
                    Map<String,Object> userMap=new HashMap<>();
                    userMap.put("name",name);
                    userMap.put("email",email);
                    //userMap.put("password",password);
                    DatabaseReference userReference=databaseReference.child("user").child(userId);
                    userReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(RegisterActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                                finish();
                            }
                        }
                    });

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}
