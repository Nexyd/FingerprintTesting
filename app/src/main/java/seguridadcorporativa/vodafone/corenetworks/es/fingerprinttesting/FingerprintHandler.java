package seguridadcorporativa.vodafone.corenetworks.es.fingerprinttesting;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.widget.Toast;

public class FingerprintHandler extends
    FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Activity caller;

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
            "Authentication error\n" + errString,
            Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationHelp(
        int helpMsgId, CharSequence helpString) {
        Toast.makeText(caller,
            "Authentication help\n" + helpString,
            Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(caller,
            "Authentication failed.",
            Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(
        FingerprintManager.AuthenticationResult result) {

        Toast.makeText(caller,
            "Authentication succeeded.",
            Toast.LENGTH_LONG).show();
    }
}