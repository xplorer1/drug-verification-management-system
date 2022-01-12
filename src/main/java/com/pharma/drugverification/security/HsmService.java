package com.pharma.drugverification.security;

import com.pharma.drugverification.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

/**
 * HSM Service - Simulates Hardware Security Module operations
 * In production, this would interface with a real HSM via PKCS#11
 * For now, we use HMAC-SHA256 as a placeholder for RSA signatures
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HsmService {

    private final ApplicationProperties applicationProperties;
    private static final int CURRENT_KEY_VERSION = 1;

    /**
     * Generate a crypto-tail signature for a serialized unit
     * In production: Uses HSM's private key to sign the data
     * Current implementation: Uses HMAC-SHA256 as simulation
     */
    public String generateCryptoTail(String serialNumber, String gtin, String batchNumber) {
        try {
            String data = serialNumber + ":" + gtin + ":" + batchNumber;

            // In production, this would use HSM PKCS#11 interface
            // For now, simulate with HMAC using configured secret
            String secret = applicationProperties.getJwt().getSecret();
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);

            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String cryptoTail = Base64.getEncoder().encodeToString(signature);

            log.debug("Generated crypto-tail for serial: {}", serialNumber);
            return cryptoTail;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to generate crypto-tail", e);
            throw new RuntimeException("Cryptographic operation failed", e);
        }
    }

    /**
     * Verify a crypto-tail signature
     * In production: Uses HSM's public key to verify
     */
    public boolean verifyCryptoTail(String serialNumber, String gtin, String batchNumber, String cryptoTail) {
        try {
            String expectedCryptoTail = generateCryptoTail(serialNumber, gtin, batchNumber);
            return expectedCryptoTail.equals(cryptoTail);
        } catch (Exception e) {
            log.error("Failed to verify crypto-tail", e);
            return false;
        }
    }

    /**
     * Generate Data Matrix barcode content
     * Format: GS1 Digital Link standard
     */
    public String generateDataMatrix(String gtin, String serialNumber, String batchNumber, String expirationDate) {
        // GS1 Digital Link format: (01)GTIN(21)SERIAL(10)BATCH(17)EXPDATE
        return String.format("(01)%s(21)%s(10)%s(17)%s",
                gtin, serialNumber, batchNumber, expirationDate);
    }

    public int getCurrentKeyVersion() {
        return CURRENT_KEY_VERSION;
    }

    /**
     * Generate a unique serial number
     * In production, this might use HSM's random number generator
     */
    public String generateSerialNumber() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 100000);
        return String.format("SN%d%05d", timestamp, random);
    }
}
