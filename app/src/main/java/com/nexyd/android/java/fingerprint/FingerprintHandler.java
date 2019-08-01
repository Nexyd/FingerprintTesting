package com.nexyd.android.java.fingerprint;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.widget.Toast;

import com.nexyd.android.java.fingerprint.interfaces.FingerprintDelegate;

public class FingerprintHandler
    extends FingerprintManager.AuthenticationCallback
{
    private CancellationSignal cancellationSignal;
    private Activity caller;
    private FingerprintDelegate delegate;

    FingerprintHandler(FingerprintDelegate delegate) {
        this.delegate = delegate;
    }

    public void startAuth(FingerprintManager manager,
        FingerprintManager.CryptoObject cryptoObject, Activity caller)
    {
        this.caller = caller;
        cancellationSignal = new CancellationSignal();
        if (caller.checkSelfPermission(
            Manifest.permission.USE_FINGERPRINT) !=
            PackageManager.PERMISSION_GRANTED) {
            return;
        }

        manager.authenticate(cryptoObject, cancellationSignal,
            0, this, null);
    }

    @Override
    public void onAuthenticationError(
        int errMsgId, CharSequence errString) {
        Toast.makeText(caller,
            caller.getString(
                R.string.authentication_error,
                "\n" + errString),

            Toast.LENGTH_LONG).show();

        delegate.onStatusReceived(false);
    }

    @Override
    public void onAuthenticationHelp(
        int helpMsgId, CharSequence helpString) {
        Toast.makeText(caller,
            caller.getString(
                R.string.authentication_help,
                "\n" + helpString),

            Toast.LENGTH_LONG).show();

        delegate.onStatusReceived(false);
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(caller,
            caller.getString(R.string.authentication_failed),
            Toast.LENGTH_LONG).show();

        delegate.onStatusReceived(false);
    }

    @Override
    public void onAuthenticationSucceeded(
        FingerprintManager.AuthenticationResult result) {

        Toast.makeText(caller,
            caller.getString(R.string.authentication_succeeded),
            Toast.LENGTH_LONG).show();

        delegate.onStatusReceived(true);
    }
}