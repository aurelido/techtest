package com.aabanegas.payment.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@SuppressWarnings("WeakerAccess")
public class JwtTestTokenGenerator {

    private static final Log LOGGER = LogFactory.getLog(JwtTestTokenGenerator.class);

    private static ResourceLoader resourceLoader = new DefaultResourceLoader();

    private JwtTestTokenGenerator() {
    }

    /**
     * Generate a token.
     *
     * @param args command line arguments, not used
     * @throws KeyStoreException If the keystore can't be read
     */
    public static void main(String[] args) throws KeyStoreException {
        Map<String, String> claims = new HashMap<>();
        claims.put("clientRef", "clientReTestf");

        //Generate token with embedded keystore
        LOGGER.info(generateToken(claims));

        //Generate token with symmetric key
        //LOGGER.info(generateToken(SignatureAlgorithm.HS256, "dGVzdC1rZXk=", claims));

        //Generate token with RS512 algorithm and default key
        //LOGGER.info(generateToken(SignatureAlgorithm.RS512, "classpath:test-keystore.jks", "password", "testKey", claims));
    }

    /**
     * Generate a token from a symmetric key (HS algorithms)
     *
     * @param signatureAlgorithm The signature algorithm
     * @param base64EncodedSecretKey The secret kay, base64 encoded
     * @param claimMap The map of claims
     * @return The generated JWT
     */
    public static String generateToken(SignatureAlgorithm signatureAlgorithm, String base64EncodedSecretKey,
            Map<String, String> claimMap) {
        return createBuilder(claimMap)
                .signWith(signatureAlgorithm, base64EncodedSecretKey)
                .compact();
    }

    /**
     * Generate a JWT using the test keystore and the default algorith (RS256)
     *
     * @param claimMap The map of claims
     * @return The generated JWT
     * @throws KeyStoreException if there is a problem reading the key from the KeyStore
     */
    public static String generateToken(Map<String, ?> claimMap) throws KeyStoreException {
        return generateToken("testKey", claimMap);
    }

    /**
     * Generate a JWT using the test keystore and the default algorith (RS256)
     *
     * @param keyId The keyId to add to the JWT, matching the alias in the keystore
     * @param claimMap The map of claims
     * @return The generated JWT
     * @throws KeyStoreException if there is a problem reading the key from the KeyStore
     */
    public static String generateToken(String keyId, Map<String, ?> claimMap) throws KeyStoreException {
        Key key = getKey("classpath:test-keystore.jks", "password", keyId);
        return generateToken(SignatureAlgorithm.RS256, keyId, key, claimMap);
    }

    /**
     * Generate a JWT from a specified JKS
     *
     * @param signatureAlgorithm The signature algorithm
     * @param file The JKS file location, use "classpath:filename" for files on the classpath
     * @param password The JKS password
     * @param alias The Key alias
     * @param claimMap The map of claims
     * @return The generated JWT
     * @throws KeyStoreException if there is a problem reading the key from the KeyStore
     */
    public static String generateToken(SignatureAlgorithm signatureAlgorithm, String file, String password,
            String alias, Map<String, ?> claimMap) throws KeyStoreException {
        Key key = getKey(file, password, alias);
        return generateToken(signatureAlgorithm, alias, key, claimMap);
    }

    /**
     * Generate a JWT with a specified signature algorithm and Key
     *
     * @param signatureAlgorithm The signature algorithm
     * @param keyId The kid to set in the JWT header
     * @param key The private key to sign the jWT with
     * @param claimMap The map of claims
     * @return The generated JWT
     */
    public static String generateToken(SignatureAlgorithm signatureAlgorithm, String keyId, Key key,
            Map<String, ?> claimMap) {
        return createBuilder(claimMap)
                .setHeaderParam("kid", keyId)
                .signWith(signatureAlgorithm, key)
                .compact();
    }

    /**
     * Read a key from a keystore.
     *
     * @param filename The JKS filename
     * @param password Keystore password
     * @param alias key alias
     * @return The Key, if it exists. Null otherwise.
     * @throws KeyStoreException if there is a problem accessing the specified keystore
     */
    public static Key getKey(String filename, String password, String alias) throws KeyStoreException {
        Key key;
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            Resource resource = resourceLoader.getResource(filename);
            try (InputStream in = resource.getInputStream()) {
                keyStore.load(in, password.toCharArray());
                key = keyStore.getKey(alias, password.toCharArray());
            }
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }

        return key;
    }

    private static JwtBuilder createBuilder(Map<String, ?> claimMap) {
        Claims claims = Jwts.claims();
        claims.putAll(claimMap);
        JwtBuilder jwtBuilder = Jwts.builder().setClaims(claims);

        if (claims.getExpiration() == null) {
            jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)));
        }
        if (claims.getIssuedAt() == null) {
            jwtBuilder.setIssuedAt(new Date());
        }
        if (claims.getSubject() == null) {
            jwtBuilder.setSubject("test");
        }

        return jwtBuilder;
    }
}
