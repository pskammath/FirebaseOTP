package com.peeyoosh.fbotplib;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class fbotplib {

    private static final long OTP_EXPIRE_SEC = 60 * 5;

    String phoneNumber;
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String mVerificationCode;
    Activity mActivity;
    boolean codeSend = false;
    boolean invalidOTP = false;
    private OnOTPSendListner otpSendListner;
    private OnOTPVerifyListner otpVerifyListner;

    public fbotplib(final Activity activity) {

        auth = FirebaseAuth.getInstance();
        mActivity = activity;
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                otpSendListner.onOTPError();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mVerificationCode = s;
                codeSend = true;
                otpSendListner.onOTPSend();
            }

        };
    }

    public void sendOTP(String countryCode, String phoneNum,OnOTPSendListner otpSendListner) {
        this.otpSendListner = otpSendListner;
        phoneNumber = countryCode + phoneNum;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(this.phoneNumber, OTP_EXPIRE_SEC, TimeUnit.SECONDS, mActivity, mCallbacks);
    }

    public void verifyOTP(String codeReceived, OnOTPVerifyListner otpVerifyListner) {
        this.otpVerifyListner = otpVerifyListner;
        if (mVerificationCode != null)
            verifyPhoneNumber(mVerificationCode, codeReceived);
    }

    public boolean isCodeSent() {
        return codeSend;
    }

    public boolean isInvalidOTP() { return  invalidOTP; }

    private void verifyPhoneNumber(String mVerificationCod, String otp) {
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationCod, otp);
        signInWithPhone(phoneAuthCredential);
    }

    private void signInWithPhone(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    otpVerifyListner.onOTPVerified();
                    Log.e("Task Result:", "Success");
                    invalidOTP = false;
                } else {
                    otpVerifyListner.onOTPError();
                    Log.e("Task Result:", "Failed");
                    invalidOTP = true;
                }
            }
        });
    }

    public interface OnOTPSendListner {
        void onOTPSend();

        void onOTPError();
    }

    public interface OnOTPVerifyListner {

        void onOTPVerified();

        void onOTPError();
    }
}
