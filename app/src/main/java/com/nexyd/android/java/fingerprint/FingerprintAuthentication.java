package com.nexyd.android.java.fingerprint;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.support.v7.app.AppCompatActivity;
import android.app.KeyguardManager;
import android.hardware.fingerprint.FingerprintManager;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyGenParameterSpec;

import com.nexyd.android.java.fingerprint.interfaces.FingerprintDelegate;

import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FingerprintAuthentication
    extends AppCompatActivity
{
    private static final String KEY_NAME = "example_key";
    private FingerprintManager fingerprintManager;
    private KeyStore keyStore;
    private KeyguardManager keyguardManager;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintDelegate delegate;
    private Activity caller;

    public FingerprintAuthentication(FingerprintDelegate delegate) {
        this.delegate = delegate;
    }

    public void init(Activity caller) {
        this.caller = caller;
        keyguardManager = (KeyguardManager)
            caller.getSystemService(Context.KEYGUARD_SERVICE);

        fingerprintManager = (FingerprintManager)
            caller.getSystemService(Context.FINGERPRINT_SERVICE);

        if (!keyguardManager.isKeyguardSecure()) {
            Toast.makeText(caller,
                caller.getString(R.string.lock_screen_security),
                Toast.LENGTH_LONG).show();

            return;
        }

        if (caller.checkSelfPermission(
            Manifest.permission.USE_FINGERPRINT) !=
            PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(caller,
                caller.getString(R.string.permission_not_enabled),
                Toast.LENGTH_LONG).show();

            return;
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            // This happens when no fingerprints are registered.
            Toast.makeText(caller,
                caller.getString(R.string.register_fingerprint),
                Toast.LENGTH_LONG).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance(caller.getString(R.string.keystore));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, caller.getString(R.string.keystore));
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(caller.getString(R.string.key_generator_failed), e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build());

            keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException |
                CertificateException |
                IOException e) {

            throw new RuntimeException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(caller.getString(R.string.cypher_init_failed), e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;

        } catch (KeyPermanentlyInvalidatedException ex) {
            return false;

        } catch (KeyStoreException
                | CertificateException
                | UnrecoverableKeyException
                | IOException
                | NoSuchAlgorithmException
                | InvalidKeyException ex) {

            throw new RuntimeException(caller.getString(R.string.cypher_init_failed), ex);
        }
    }

    public void checkFingerprints() {
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                caller.getString(R.string.register_fingerprint),
                Toast.LENGTH_LONG).show();

            return;
        }

        generateKey();
    }

    public void initCryptoObject() {
        if (cipherInit()) {
            cryptoObject = new FingerprintManager.CryptoObject(cipher);
        }
    }

    public void initFingerprintHandler(Activity caller) {
        if (cipherInit()) {
            cryptoObject = new FingerprintManager.CryptoObject(cipher);
            FingerprintHandler helper = new FingerprintHandler(delegate);
            helper.startAuth(fingerprintManager, cryptoObject, caller);
        }
    }

    public boolean isHardwareDetected() {
        return fingerprintManager.isHardwareDetected();
    }
}