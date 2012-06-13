package net.jumperz.security;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import net.jumperz.util.*;

public class MBlowfishCBCCipher
implements MBlowfishCBCCryptInfo
{
private Cipher encryptCipher;
private Cipher decryptCipher;
private SecretKeySpec key;
// --------------------------------------------------------------------------------
public MBlowfishCBCCipher( String keyStr )
throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException
{
byte[] keyByte = MStringUtil.hexStringToByteArray( keyStr );
key = new SecretKeySpec( keyByte, ALG );

encryptCipher = Cipher.getInstance( ALG + "/" + MODE + "/" + PADDING );
decryptCipher = Cipher.getInstance( ALG + "/" + MODE + "/" + PADDING  );
}
//----------------------------------------------------------------------------------------------
public synchronized byte[] encrypt( byte[] data )
throws IOException
{
try
	{
	encryptCipher.init( Cipher.ENCRYPT_MODE, key );
	byte[] encIv = encryptCipher.getIV();
	
	ByteArrayOutputStream bo = new ByteArrayOutputStream( data.length + 256 );
	bo.write( encIv );
	bo.write( encryptCipher.doFinal( data ) );
	return bo.toByteArray();
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
		//iv
	byte[] iv = new byte[ IV_LEN ];
	for( int i = 0; i < IV_LEN; ++i )
		{
		iv[ i ] = data[ i ];
		}
	IvParameterSpec dps = new IvParameterSpec( iv );
	decryptCipher.init( Cipher.DECRYPT_MODE, key, dps );

	byte[] encData = new byte[ data.length - IV_LEN ];
	for( int i = 0; i < encData.length; ++i )
		{
		encData[ i ] = data[ i + IV_LEN ];
		}
	
	return decryptCipher.doFinal( encData );
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
}