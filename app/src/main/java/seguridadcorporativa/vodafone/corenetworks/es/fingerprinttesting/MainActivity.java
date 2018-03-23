package seguridadcorporativa.vodafone.corenetworks.es.fingerprinttesting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private FingerprintAuthentication fingerprint;
    private TextView errorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        errorMsg = findViewById(R.id.errorText);

        fingerprint = new FingerprintAuthentication();
        fingerprint.init(this);

        if (!fingerprint.isHardwareDetected()) {
            errorMsg.setText(R.string.errorMsg);
        } else {
            fingerprint.checkFingerprints();
            fingerprint.initCryptoObject();
            fingerprint.initFingerprintHandler(this);
        }
    }
}