package fr.xephi.authme.security.crypts;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import fr.xephi.authme.AuthMe;

public class IPB3 implements EncryptionMethod {

    @Override
    public String getHash(String password, String salt, String name)
            throws NoSuchAlgorithmException {
        return getMD5(getMD5(salt) + getMD5(password));
    }

    @Override
    public boolean comparePassword(String hash, String password,
            String name) throws NoSuchAlgorithmException {
        String salt = AuthMe.getInstance().database.getNameAuth(name).getSalt();
        return hash.equals(getHash(password, salt, name));
    }

    private static String getMD5(String message)
            throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(message.getBytes());
        byte[] digest = md5.digest();
        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
    }
}
