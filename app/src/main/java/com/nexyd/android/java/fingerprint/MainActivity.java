package com.nexyd.android.java.fingerprint;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.nexyd.android.java.fingerprint.interfaces.FingerprintDelegate;

public class MainActivity
    extends AppCompatActivity
    implements FingerprintDelegate
{
    private FingerprintAuthentication fingerprint;
    private TextView errorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        errorMsg = findViewById(R.id.errorText);

        fingerprint = new FingerprintAuthentication(this);
        fingerprint.init(this);

        if (!fingerprint.isHardwareDetected()) {
            errorMsg.setText(R.string.errorMsg);
        } else {
            fingerprint.checkFingerprints();
            fingerprint.initCryptoObject();
            fingerprint.initFingerprintHandler(this);
        }
    }

    @Override
    public void onStatusReceived(boolean fingerprintPassed) {
        if (fingerprintPassed) {
            Intent intent = new Intent(
                this,
                HomeActivity.class);

            startActivity(intent);
        }
    }
}