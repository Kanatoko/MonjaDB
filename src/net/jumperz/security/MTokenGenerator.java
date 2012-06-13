package net.jumperz.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import net.jumperz.util.MStringUtil;

public class MTokenGenerator
{
private SecureRandom random;

public static final String ALG = "SHA1PRNG";
public static final int BYTE_LEN = 16;

private volatile static MTokenGenerator instance;

/*
static
{
try
	{
	instance = new MTokenGenerator();
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
}
*/
// --------------------------------------------------------------------------------
public static String getToken2( int len )
{
if( instance == null )
	{
	instance = new MTokenGenerator();
	}
return instance.getToken( len );
}
// --------------------------------------------------------------------------------
public static String getToken2()
{
if( instance == null )
	{
	instance = new MTokenGenerator();
	}
return instance.getToken();
}
// --------------------------------------------------------------------------------
public MTokenGenerator()
{
try
	{
	random = SecureRandom.getInstance( ALG );
	byte[] seed = random.generateSeed( BYTE_LEN );
	random.setSeed( seed );
	}
catch( NoSuchAlgorithmException e )
	{
	e.printStackTrace();
	}
}
// --------------------------------------------------------------------------------
public synchronized String getToken( int len )
{
byte[] buf = new byte[ len ];
synchronized( random )
	{
	random.nextBytes( buf );
	}
return MStringUtil.byteToHexString( buf );
}
// --------------------------------------------------------------------------------
public synchronized String getToken()
{
return getToken( BYTE_LEN );
}
// --------------------------------------------------------------------------------
}