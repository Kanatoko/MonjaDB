package net.jumperz.security;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.*;
import javax.net.*;
import javax.net.ssl.*;
import java.net.*;
import java.util.*;
import net.jumperz.net.*;
import net.jumperz.util.MStreamUtil;
import net.jumperz.util.MStringUtil;
import java.lang.reflect.*;
import java.security.cert.X509Certificate;
import org.bouncycastle.openssl.PEMWriter;

public class MBCUtil
{
//--------------------------------------------------------------------------------
public static String genIdFromCert( X509Certificate cert )
{
String pemStr = toPem( cert );
return MStringUtil.getMd5Hash( pemStr ) + "-" + pemStr.length();
}
//--------------------------------------------------------------------------------
public static String toPem( X509Certificate cert )
{
ByteArrayOutputStream bo = null;
try
	{
	bo = new ByteArrayOutputStream();
	PEMWriter writer = new PEMWriter( new OutputStreamWriter( bo ) );
	writer.writeObject( cert );
	writer.flush();
	writer.close();
	byte[] _data = bo.toByteArray();
	String pemStr = new String( _data );
	return pemStr;
	}
catch( Exception e )
	{
	e.printStackTrace();
	return "";
	}
finally
	{
	MStreamUtil.closeStream( bo );
	}
}
}
