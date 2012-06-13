package net.jumperz.security;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import net.jumperz.util.*;

public class MRC4Cipher
implements MRC4CryptInfo
{
private Cipher encryptCipher;
private Cipher decryptCipher;
private SecretKeySpec key;
// --------------------------------------------------------------------------------
public MRC4Cipher( String keyStr )
throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException
{
byte[] keyByte = MStringUtil.hexStringToByteArray( keyStr );
key = new SecretKeySpec( keyByte, ALG );

try
	{
	encryptCipher = Cipher.getInstance( ALG  );
	encryptCipher.init( Cipher.ENCRYPT_MODE, key );

	decryptCipher = Cipher.getInstance( ALG );
	decryptCipher.init( Cipher.DECRYPT_MODE, key );
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
}
//----------------------------------------------------------------------------------------------
public synchronized byte[] encrypt( byte[] data )
throws IOException
{
try
	{
	return encryptCipher.doFinal( data );
	}
catch( Exception e )
	{
	e.printStackTrace();
	throw new IOException( e.getMessage() );
	}
}
//----------------------------------------------------------------------------------------------
public synchronized byte[] decrypt( byte[] data )
throws IOException
{
try
	{
	return decryptCipher.doFinal( data );
	}
catch( Exception e )
	{
	e.printStackTrace();
	throw new IOException( e.getMessage() );
	}
}
// --------------------------------------------------------------------------------
public synchronized String decryptToString( byte[] data )
throws IOException
{
byte[] dec = decrypt( data );
return MStringUtil.byteArrayToString( dec );
}
// --------------------------------------------------------------------------------
public synchronized byte[] encryptToBytes( String s )
throws IOException
{
byte[] data = MStringUtil.getBytes( s );
return encrypt( data );
}
// --------------------------------------------------------------------------------
public synchronized String encrypt( String s )
throws IOException
{
return MStringUtil.byteToHexString( encryptToBytes( s ) );
}
// --------------------------------------------------------------------------------
public synchronized String decrypt( String s )
throws IOException
{
	//s must be hex string
byte[] data = MStringUtil.hexStringToByteArray( s );
byte[] dec = decrypt( data );
return MStringUtil.byteArrayToString( dec );
}
// --------------------------------------------------------------------------------

public Cipher getEncryptCipher()
{

return encryptCipher;
}

public Cipher getDecryptCipher()
{

return decryptCipher;
}
}