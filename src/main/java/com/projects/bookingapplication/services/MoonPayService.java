package com.projects.bookingapplication.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class MoonPayService {

    // ⚠️ IMPORTANT: These should be configured in application.properties or environment variables
    @Value("${moonpay.api.publishable-key}")
    private String moonpayApiKey;

    @Value("${moonpay.api.secret-key}")
    private String moonpaySecretKey;

    @Value("${moonpay.wallet-address}")
    private String moonpayWalletAddress;

    private static final String BASE_URL = "https://buy.moonpay.com";

    // ⭐️ MOBILE INTEGRATION: This is the Deep Link the mobile app must register
    // to handle the post-payment redirection. MoonPay will redirect here on success/failure.
    // Ensure the mobile app registers this exact scheme and path.
    private static final String MOBILE_REDIRECT_URL = "yourappscheme://moonpay/complete";
    // You should replace 'yourappscheme' with your app's actual scheme (e.g., 'bookingapp').

    /**
     * Generates the securely signed URL for the MoonPay widget,
     * including the redirectURL for mobile deep linking.
     */
    public String generateSignedMoonpayUrl(
            BigDecimal depositAmount,
            Long externalRefId,
            String userEmail
    ) {
        String baseFiat = "EUR";
        String baseCrypto = "USDC";

        String params = String.format(
                "?apiKey=%s" +
                        "&currencyCode=%s" +
                        "&baseCurrencyCode=%s" +
                        "&baseCurrencyAmount=%s" +
                        "&externalTransactionId=%s" +
                        "&lockWalletAddress=true" +
                        "&walletAddress=%s" +
                        "&email=%s" +
                        "&redirectURL=%s", // ⭐️ Added redirectURL for mobile callback
                moonpayApiKey,
                baseCrypto.toLowerCase(),
                baseFiat.toLowerCase(),
                depositAmount.toPlainString(),
                externalRefId.toString(),
                moonpayWalletAddress,
                urlEncode(userEmail),
                urlEncode(MOBILE_REDIRECT_URL) // URL-encoded Deep Link
        );

        try {
            String signature = generateHmacSha256(params, moonpaySecretKey);

            return BASE_URL + params + "&signature=" + urlEncode(signature);
        } catch (Exception e) {
            throw new RuntimeException("Error generating MoonPay signature", e);
        }
    }

    /**
     * ⭐️ Webhook Security Method: Verifies the HMAC-SHA256 signature from the
     * Moonpay-Signature-V2 header against the payload and the Secret Key.
     */
    public boolean verifyWebhookSignature(String payload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isEmpty()) {
            return false;
        }

        try {
            // 1. Calculate the expected signature from the payload and secret key
            String expectedSignature = generateHmacSha256(payload, moonpaySecretKey);

            // 2. The header format is 't=<timestamp>,s=<signature>'. We extract the signature part 's='.
            String[] parts = signatureHeader.split(",");
            String signature = null;

            for (String part : parts) {
                if (part.trim().startsWith("s=")) {
                    signature = part.trim().substring(2);
                    break;
                }
            }

            if (signature == null) {
                return false;
            }

            // 3. Compare the calculated signature with the signature from the header
            return expectedSignature.equals(signature);

        } catch (Exception e) {
            System.err.println("Error during MoonPay webhook signature verification: " + e.getMessage());
            return false;
        }
    }


    /**
     * Helper method to generate the HMAC-SHA256 signature (used for both URL signing and Webhook verification).
     */
    private String generateHmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // MoonPay uses Base64 encoding for the signature.
        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}