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

public class FingerprintAuthentication extends AppCompatActivity {

    private static final String KEY_NAME = "example_key";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;


    public void init(Activity caller) {
        keyguardManager = (KeyguardManager)
            caller.getSystemService(Context.KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager)
            caller.getSystemService(Context.FINGERPRINT_SERVICE);

        if (!keyguardManager.isKeyguardSecure()) {
            Toast.makeText(caller, "Lock screen security " +
                "not enabled in Settings", Toast.LENGTH_LONG).show();

            return;
        }

        if (caller.checkSelfPermission(
            Manifest.permission.USE_FINGERPRINT) !=
            PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(caller, "Fingerprint authentication " +
                "permission not enabled", Toast.LENGTH_LONG).show();

            return;
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            // This happens when no fingerprints are registered.
            Toast.makeText(caller, "Register at least one " +
                "fingerprint in Settings", Toast.LENGTH_LONG).show();

            return;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
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
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;

        } catch (KeyStoreException
                | CertificateException
                | UnrecoverableKeyException
                | IOException
                | NoSuchAlgorithmException
                | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    public void checkFingerprints()
    {
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                "Register at least one fingerprint in Settings",
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
            FingerprintHandler helper = new FingerprintHandler();
            helper.startAuth(fingerprintManager, cryptoObject, caller);
        }
    }

    public boolean isHardwareDetected() {
        return fingerprintManager.isHardwareDetected();
    }
}