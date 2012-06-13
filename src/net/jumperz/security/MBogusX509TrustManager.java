package net.jumperz.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

public class MBogusX509TrustManager
implements X509TrustManager
{
//--------------------------------------------------------------------------------
public X509Certificate[] getAcceptedIssuers()
{
return null;
}
//--------------------------------------------------------------------------------
public void checkClientTrusted( X509Certificate[] arg0, String arg1 )
throws CertificateException
{
}
//--------------------------------------------------------------------------------
public void checkServerTrusted( X509Certificate[] certs, String arg1 )
throws CertificateException
{
}
//--------------------------------------------------------------------------------	
}