package com.example.expensemanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPass;
    private Button btnLogin;
    private TextView mForgetPassword;
    private TextView mSignupHere;

    private ProgressDialog mDialog;

    //for sms
    private static final int SMS_PERMISSION_CODE = 100;

    //Firebase
    private FirebaseAuth mAuth;

    //broadcast sms
    private BroadcastReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
        }

        mDialog=new ProgressDialog(this);
        loginDetails();
        checkAndRequestPermission();
    }

    private void checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)){
                showPermissionExplanationDialog();
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
            }
        } else {
            // Permission already granted
            initializeSmsReceiver();
        }
    }

    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("SMS permission is needed")
                .setMessage("This app requires SMS permission to automatically detect and update your expenses.")
                .setPositiveButton("Allow", (dialog, which) ->
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void initializeSmsReceiver() {
        Toast.makeText(this, "SMS Receiver Initialized.", Toast.LENGTH_SHORT).show();
        smsReceiver = new smsReceiver();
        registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeSmsReceiver(); // **Updated**: Initialize the SMS receiver when the permission is granted.
            } else {
                Toast.makeText(this, "SMS Permission Denied. Please enable it from settings.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loginDetails(){
        mEmail = findViewById(R.id.email_login);
        mPass = findViewById(R.id.password_login);
        btnLogin = findViewById(R.id.btn_login);
        mForgetPassword = findViewById(R.id.forgot_password);
        mSignupHere = findViewById(R.id.signup_req);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getText().toString().trim();
                String pass = mPass.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email Required...");
                    return;
                }
                if(TextUtils.isEmpty(pass)){
                    mPass.setError("Password Required...");
                    return;
                }
                mDialog.setMessage("Processing...");
                mDialog.show();

                mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            mDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                            Toast.makeText(getApplicationContext(), "Login Successful...", Toast.LENGTH_SHORT).show();
                        }else{
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Login Failed...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // Registration Activity
        mSignupHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),RegistrationActivity.class));
            }
        });

        //Reset Password Activity

        mForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ResetActivity.class));
            }
        });
    }
}