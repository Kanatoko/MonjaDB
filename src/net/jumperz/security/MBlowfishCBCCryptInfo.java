package net.jumperz.security;

public interface MBlowfishCBCCryptInfo
{
public static final String ALG      = "Blowfish";
public static final String MODE     = "CBC";
public static final String PADDING  = "PKCS5Padding";
public static final int KEYSIZE     = 16;
public static final int IV_LEN      = 8;
}
