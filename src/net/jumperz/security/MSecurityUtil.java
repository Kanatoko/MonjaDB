package net.jumperz.security;

import java.io.*;
import java.security.*;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
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
import net.jumperz.util.*;
import java.util.zip.*;

public class MSecurityUtil
{
public static final String KEYSTORE_TYPE	= "JKS";
public static final String KEYSTORE_ALIAS	= "alias";
public static final String KEY_PASS		= "keyPass";

	//from jp.bitforest.framework1.MWebappUtil.java
private static final String CLIENT_SESSION_EXPIRE_KEY	= "_CsEx_";
private static final String CLIENT_SESSION_KEY		= "_CsSs_";
private static final int CLIENT_SESSION_COOKIE_COUNT	= 15;
private static final int CLIENT_SESSION_COOKIE_LENGTH	= 4000;
public static final int DEFAULT_SESSION_EXPIRE_MIN	= 30;
//--------------------------------------------------------------------------------
public static boolean isValidChain( List chain )
{
	//root, im, leafÇÃèáî‘ÇÃchainÇ≈Ç†ÇÈÇ±Ç∆Ç™èåè
if( chain.size() < 2 )
	{
	return false;
	}

try
	{
	X509Certificate root = null;
	X509Certificate leaf = null;
	List imList = new ArrayList();

	for( int i = 0; i < chain.size(); ++i )
		{
		if( i == 0 )
			{
				//root
			root = ( X509Certificate )chain.get( i );
			}
		else if( i == chain.size() - 1 )
			{
			leaf = ( X509Certificate )chain.get( i );
			}
		else
			{
			imList.add( chain.get( i ) );
			}
		}
	
	KeyStore ks = KeyStore.getInstance( "JKS" );
	ks.load( null, null );
	ks.setCertificateEntry( "root", root );
	
	X509CertSelector target = new X509CertSelector();
	target.setCertificate( leaf );
	
	PKIXBuilderParameters params = new PKIXBuilderParameters( ks, target);
	

	CertStoreParameters intermediates = new CollectionCertStoreParameters( imList );
	params.addCertStore( CertStore.getInstance( "Collection", intermediates ) );
	
	params.setRevocationEnabled( false );
	
	CertPathBuilder builder = CertPathBuilder.getInstance( "PKIX" );
	CertPathBuilderResult result = builder.build( params );
	return true;
	}
catch( Exception e )
	{
	return false;
	}
}
//--------------------------------------------------------------------------------
public static byte[] xor( byte[] b1, byte[] b2 )
{
byte[] buf = new byte[ b1.length ];
for( int i = 0; i < b1.length; ++i )
	{
	buf[ i ] = ( byte )( b1[ i ] ^ b2[ i ] );
	}
return buf;
}
//--------------------------------------------------------------------------------
public static X509TrustManager getDefaultTrustManager()
throws KeyStoreException, NoSuchAlgorithmException
{
TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
tmf.init( ( KeyStore )null );
TrustManager[] tmArray = tmf.getTrustManagers();
return ( X509TrustManager )tmArray[ 0 ];
}
//---------------------------------------------------------------------------------------
public static Certificate[] loadCertificatesFromFile( String fileName )
throws IOException, CertificateException
{
FileInputStream fis = new FileInputStream( fileName );
return loadCertificatesFromStream( fis );
}
//--------------------------------------------------------------------------------
public static X509Certificate loadCertificateFromPem( String pemStr )
throws IOException, CertificateException
{
if( pemStr == null )
	{
	throw new IOException( "pem str is null" );
	}
return ( X509Certificate )loadCertificatesFromStream( MStreamUtil.stringToStream( pemStr ) )[ 0 ];
}
// --------------------------------------------------------------------------------
public static Certificate[] loadCertificatesFromStream( InputStream fis )
throws IOException, CertificateException
{
CertificateFactory cf = CertificateFactory.getInstance( "X.509" );
Collection c = cf.generateCertificates( fis );
int count = c.size();
Certificate[] certArray = new Certificate[ count ];

Iterator p = c.iterator();
int i = 0;
while( p.hasNext() )
	{
	certArray[ i ] = ( Certificate )p.next();
	++i;
	}

return certArray;
}
//---------------------------------------------------------------------------------------
public static Certificate loadCertificateFromFile( String fileName )
throws IOException, CertificateException
{
FileInputStream fis = new FileInputStream( fileName );
BufferedInputStream bis = new BufferedInputStream( fis );
CertificateFactory cf = CertificateFactory.getInstance( "X.509" );
Certificate cert = cf.generateCertificate( bis );
return cert;
}
//---------------------------------------------------------------------------------------
public static PrivateKey loadPrivateKeyFromFile( String fileName, String algorithm )
throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
{
FileInputStream fs = new FileInputStream( fileName );
return loadPrivateKeyFromStream( fs, algorithm );
}
//---------------------------------------------------------------------------------------
public static PrivateKey loadPrivateKeyFromStream( InputStream in, String algorithm )
throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
{
ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
MStreamUtil.connectStream( in, bufStream );
byte[] keyBuffer = bufStream.toByteArray();
KeyFactory keyFactory = KeyFactory.getInstance( algorithm );
PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec( keyBuffer );
PrivateKey privateKey = keyFactory.generatePrivate( keySpec );
return privateKey;
}
//--------------------------------------------------------------------------------
public static PublicKey loadPublicKeyFromStream( InputStream in, String algorithm )
throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
{
ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
MStreamUtil.connectStream( in, bufStream );
byte[] keyBuffer = bufStream.toByteArray();
KeyFactory keyFactory = KeyFactory.getInstance( algorithm );
KeySpec keySpec = new X509EncodedKeySpec( keyBuffer );
PublicKey publicKey = keyFactory.generatePublic( keySpec );
return publicKey;
}
//--------------------------------------------------------------------------------
public static PublicKey loadPublicKeyFromFile( String fileName, String algorithm )
throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
{
FileInputStream fs = new FileInputStream( fileName );
PublicKey publicKey = loadPublicKeyFromStream( fs, algorithm );
fs.close();
return publicKey;
}
//---------------------------------------------------------------------------------------
public static ServerSocketFactory getServerSocketFactory( String certificateFileName, String privateKeyFileName, String algorithm )
throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeySpecException, CertificateException, IOException
{
return MSecurityUtil.getServerSocketFactory( MSecurityUtil.generateKeyStore( certificateFileName, privateKeyFileName, algorithm ) );
}
//---------------------------------------------------------------------------------------
public static ServerSocketFactory getServerSocketFactory( InputStream certificateIn, InputStream privateKeyIn, String algorithm )
throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeySpecException, CertificateException, IOException
{
return MSecurityUtil.getServerSocketFactory( MSecurityUtil.generateKeyStore( certificateIn, privateKeyIn, algorithm ) );
}
//---------------------------------------------------------------------------------------
public static KeyStore generateKeyStore( String certificateFileName, String privateKeyFileName, String algorithm )
throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, KeyStoreException
{
Certificate[] certList		= MSecurityUtil.loadCertificatesFromFile( certificateFileName );
PrivateKey privateKey		= MSecurityUtil.loadPrivateKeyFromFile( privateKeyFileName, algorithm );

KeyStore keyStore = KeyStore.getInstance( KEYSTORE_TYPE );
keyStore.load( null, null );
keyStore.setKeyEntry( KEYSTORE_ALIAS, privateKey, KEY_PASS.toCharArray(), certList );

return keyStore;
}
//---------------------------------------------------------------------------------------
public static KeyStore generateKeyStore( InputStream certificateIn, InputStream privateKeyIn, String algorithm )
throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, KeyStoreException
{
Certificate[] certList		= MSecurityUtil.loadCertificatesFromStream( certificateIn );
PrivateKey privateKey		= MSecurityUtil.loadPrivateKeyFromStream( privateKeyIn, algorithm );

KeyStore keyStore = KeyStore.getInstance( KEYSTORE_TYPE );
keyStore.load( null, null );
keyStore.setKeyEntry( KEYSTORE_ALIAS, privateKey, KEY_PASS.toCharArray(), certList );

return keyStore;
}
// --------------------------------------------------------------------------------
public static void initSslContextForServer( SSLContext ctx, KeyStore keyStore  )
throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException
{
KeyManagerFactory keyManagerFactory = null;
try
	{
	keyManagerFactory = KeyManagerFactory.getInstance( "SunX509" );
	}
catch( java.security.NoSuchAlgorithmException e )
	{
	keyManagerFactory = KeyManagerFactory.getInstance( "IbmX509" );
	}
keyManagerFactory.init( keyStore, MSecurityUtil.KEY_PASS.toCharArray() );
ctx.init( keyManagerFactory.getKeyManagers(), null, null );
}
//---------------------------------------------------------------------------------------
public static SSLSocketFactory getSSLSocketFactory( KeyStore keyStore )
throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException
{
SSLContext ctx = getSslContext();
initSslContextForServer( ctx, keyStore );
return ctx.getSocketFactory();
}
//---------------------------------------------------------------------------------------
public static ServerSocketFactory getServerSocketFactory( KeyStore keyStore )
throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException
{
SSLContext ctx = getSslContext();
initSslContextForServer( ctx, keyStore );
return ctx.getServerSocketFactory();
}
// --------------------------------------------------------------------------------
public static SSLContext getSslContext()
throws NoSuchAlgorithmException
{
SSLContext ctx = null;
try
	{
		//for IBM
	ctx = SSLContext.getInstance( "SSL_TLS", "IBMJSSE2" );
	}
catch( GeneralSecurityException e )
	{
		//for Sun
	ctx = SSLContext.getInstance( "TLS" );	
	}
return ctx;
}
//--------------------------------------------------------------------------------
public static Map getClientSessionImpl( MParameter[] cookies, MBlowfishCBCCipher cipher )
throws IOException, ClassNotFoundException
{
/*
Cookie[] cookies = request.getCookies();
if( cookies == null )
	{
	return new HashMap();
	}
*/

//-----COPY FROM jp.bitforest.framework1.MWebappUtil------->

String[] buffer = new String[ CLIENT_SESSION_COOKIE_COUNT ];
for( int i = 0; i < cookies.length; ++i )
	{
	MParameter cookie = cookies[ i ];
	String name = cookie.getName();
	if( name.indexOf( CLIENT_SESSION_KEY ) == 0 )
		{
		int index = MStringUtil.parseInt( MRegEx.getMatch( "[0-9]{1,}", name ) );
		if( index < CLIENT_SESSION_COOKIE_COUNT )
			{
			buffer[ index ] = cookie.getValue();		
			}
		}
	}

StringBuffer buf = new StringBuffer();
for( int i = 0; i < CLIENT_SESSION_COOKIE_COUNT; ++i )
	{
	String str = buffer[ i ];
	if( str != null )
		{
		buf.append( buffer[ i ] );	
		}
	}

if( buf.length() > 0 )
	{
	byte[] encData = Base64.decode( buf.toString() );
	byte[] plainData = cipher.decrypt( encData );
	ByteArrayInputStream bi = new ByteArrayInputStream( plainData );
	ObjectInputStream oi = new ObjectInputStream( new GZIPInputStream( bi ) );
	Map sessionParameters = ( Map )oi.readObject();

		//check expiration
	try
		{
		long validUntil = Long.parseLong( ( String  )sessionParameters.get( CLIENT_SESSION_EXPIRE_KEY ) );
		if( System.currentTimeMillis() > validUntil )
			{
			System.out.println( "client session expires." );
			return new HashMap();
			}
		}
	catch( Exception e )
		{
		e.printStackTrace();
		return new HashMap();
		}
	
	return sessionParameters;
	}

return new HashMap();
//-----COPY FROM jp.bitforest.framework1.MWebappUtil-------<
}
//--------------------------------------------------------------------------------
public static Map getEncryptedDataFromCookie( MHttpRequest request, MBlowfishCBCCipher cipher )
throws IOException, ClassNotFoundException
{
List cookieList = request.getCookieList();
MParameter[] cookies = new MParameter[ cookieList.size() ];
for( int i = 0; i < cookieList.size(); ++i )
	{
	MParameter cookie = ( MParameter )cookieList.get( i );
	String value = cookie.getValue();
	if( value.matches( "^\".*\"$" ) )
		{
		value = value.substring( 1, value.length() -1 );
		}
	cookie.setValue( value );
	cookies[ i ] = cookie;
	}
return MSecurityUtil.getClientSessionImpl( cookies, cipher );
}
// --------------------------------------------------------------------------------
public static void disableCipherSuites( SSLServerSocket sSocket )
{
disableCipherSuites( sSocket, "DHE" );
disableCipherSuites( sSocket, "SSL_RSA_WITH_DES_CBC_SHA" );
disableCipherSuites( sSocket, "SSL_RSA_EXPORT_WITH_RC4_40_MD5" );
disableCipherSuites( sSocket, "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA" );
}
// --------------------------------------------------------------------------------
public static void disableCipherSuites( SSLServerSocket sSocket, String name )
{
String[] enabledList = sSocket.getEnabledCipherSuites();
List tempList = new ArrayList();
for( int i = 0; i < enabledList.length; ++i )
	{
	String s = enabledList[ i ];
	if( s.toLowerCase().indexOf( name.toLowerCase() ) == -1 )
		{
		tempList.add( s );
		}
	}
String[] newCipherSuites = new String[ tempList.size() ];
for( int i = 0; i < newCipherSuites.length; ++i )
	{
	newCipherSuites[ i ] = ( String )tempList.get( i );
	}
sSocket.setEnabledCipherSuites( newCipherSuites );
}
//--------------------------------------------------------------------------------
public static SocketFactory getBogusSslSocketFactory()
{
return getBogusSslSocketFactory( null );
}
//--------------------------------------------------------------------------------
public static SocketFactory getBogusSslSocketFactory( KeyManager[] kmArray )
{
SSLContext ctx = null;
try
	{
	ctx = getSslContext();
	
	TrustManager[] tmArray = new TrustManager[ 1 ];
	tmArray[ 0 ] = new MBogusX509TrustManager();
	
	ctx.init( kmArray, tmArray, null );
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
return ctx.getSocketFactory();
}
// --------------------------------------------------------------------------------
public static Socket getBogusSslSocketViaProxy( String host, int port, String proxyHost, int proxyPort )
throws IOException
{
return getBogusSslSocketViaProxy( host, port, proxyHost, proxyPort );
}
// --------------------------------------------------------------------------------
public static Socket getBogusSslSocketViaProxy( String host, int port, String proxyHost, int proxyPort, KeyManager[] kmArray )
throws IOException
{
Socket rawSocket = new Socket( proxyHost, proxyPort );
OutputStream rawOut = rawSocket.getOutputStream();
InputStream rawIn = rawSocket.getInputStream();

String connectRequest = "CONNECT " + host + ":" + port + " HTTP/1.0\r\nHost: " + host + ":" + port + "\r\n\r\n";
rawOut.write( connectRequest.getBytes() );

	//receive response
byte[] buf = new byte[ 256 ];
ByteArrayOutputStream byteBuf = new ByteArrayOutputStream( 256 );
while( true )
	{
	int received = rawIn.read( buf );
	if( received == -1 )
		{
		throw new IOException( "Proxy CONNECT failed." );
		}
	byteBuf.write( buf, 0, received );
	String bufStr = byteBuf.toString();
	if( bufStr.endsWith( "\r\n\r\n" ) )
		{
		MHttpResponse response = new MHttpResponse( bufStr );
		if( response.getStatusCode() != 200 )
			{
			throw new IOException( response.getStatusLine() );
			}
		break;
		}
	}

SSLSocketFactory factory = ( SSLSocketFactory )MSecurityUtil.getBogusSslSocketFactory( kmArray );
return factory.createSocket( rawSocket, host, port, true );
}
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
testSslConnect();
System.out.println( "OK." );
}
//--------------------------------------------------------------------------------
public static void p( Object o )
{
System.out.println( o );
}
//--------------------------------------------------------------------------------
public static void testSslConnect()
throws Exception
{
	// 127.0.0.1:443 must be closed
List ipList = Arrays.asList( new Object[]{ "127.0.0.1", "www.gmail.com" } );
Collections.shuffle( ipList );
p( ipList );
Socket s = sslConnect( ipList, 443 );
p( s );
s.close();
}
//--------------------------------------------------------------------------------
public static Socket getBogusSslSocket( String host, int port, KeyManager[] kmArray )
throws IOException
{
return MSecurityUtil.getBogusSslSocketFactory( kmArray ).createSocket( host, port );
}
// --------------------------------------------------------------------------------
public static Socket sslConnect( List hostList, int port )
throws IOException
{
List portList = new ArrayList( hostList.size() );
for( int i = 0; i < hostList.size(); ++i )
	{
	portList.add( port + "" );
	}
return sslConnect( hostList, portList );
}
// --------------------------------------------------------------------------------
public static Socket sslConnect( List hostList, List portList )
throws IOException
{
IOException ex = null;
for( int i = 0; i < hostList.size(); ++i )
	{
	try
		{
		Socket socket = sslConnect( ( String )hostList.get( i ), MStringUtil.parseInt( portList.get( i ) ) );
		return socket;
		}
	catch( IOException e )
		{
		ex = e;
		}
	}
throw ex;
}
// --------------------------------------------------------------------------------
public static Socket sslConnect( String host, int connectPort )
throws IOException
{
return sslConnect( host, connectPort, 30 );
}
// --------------------------------------------------------------------------------
public static Socket sslConnect( String host, int connectPort, int connectTimeOut )
throws IOException
{
Socket socket = null;
IOException ex = null;
SocketAddress sockAddr = new InetSocketAddress( host, connectPort );
for( int i = 0; i < 3; ++i )
	{
	try
		{
		socket = MSecurityUtil.getBogusSslSocketFactory().createSocket();
		socket.connect( sockAddr, connectTimeOut );
		return socket;
		}
	catch( IOException e )
		{
		ex = e;
		MSystemUtil.sleep( 1000 * ( i + 1 ) );
		}
	}
throw ex;
}
//--------------------------------------------------------------------------------
public static Socket getBogusSslSocket( String host, int port )
throws IOException
{
return MSecurityUtil.getBogusSslSocketFactory().createSocket( host, port );
}
//--------------------------------------------------------------------------------
public static void checkServerTrusted( Certificate trusted, Certificate server )
throws Exception
{
checkServerTrusted( new Certificate[]{ trusted }, server );
}
//--------------------------------------------------------------------------------
public static void checkServerTrusted( Certificate[] trusted, Certificate server, List rootCertList )
throws Exception
{
KeyStore ks = KeyStore.getInstance( "JKS" );
ks.load( null, null );
for( int i = 0; i < trusted.length; ++i )
	{
	Certificate cert = trusted[ i ];
	ks.setCertificateEntry( "alias" + i, cert );
	}

for( int i = 0; i < rootCertList.size(); ++i )
	{
	Map data = ( Map )rootCertList.get( i );
	ks.setCertificateEntry( "aliasR" + i, MSecurityUtil.loadCertificateFromPem( ( String )data.get( "pem"  ) ) );
	}

TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
tmf.init( ks );
TrustManager[] tmArray = tmf.getTrustManagers();
TrustManager tm = tmArray[ 0 ];
X509TrustManager x509tm = ( X509TrustManager )tm;
X509Certificate x509 = ( X509Certificate )server;
x509tm.checkServerTrusted( new X509Certificate[]{ x509 }, "RSA" );
}
//--------------------------------------------------------------------------------
public static void checkServerTrusted( Certificate[] trusted, Certificate server )
throws Exception
{
KeyStore ks = KeyStore.getInstance( "JKS" );
ks.load( null, null );
for( int i = 0; i < trusted.length; ++i )
	{
	Certificate cert = trusted[ i ];
	ks.setCertificateEntry( "alias" + i, cert );
	}

TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
tmf.init( ks );
TrustManager[] tmArray = tmf.getTrustManagers();
TrustManager tm = tmArray[ 0 ];
X509TrustManager x509tm = ( X509TrustManager )tm;
X509Certificate x509 = ( X509Certificate )server;
x509tm.checkServerTrusted( new X509Certificate[]{ x509 }, "RSA" );
}
//--------------------------------------------------------------------------------
public static boolean isRoot( X509Certificate cert )
{
return cert.getIssuerDN().equals( cert.getSubjectDN() );
}
//--------------------------------------------------------------------------------
public static List getTrustedPrincipals()
throws Exception
{
List certList = getTrustedCerts();
List principalList = new ArrayList( certList.size() );

for( int i = 0; i < certList.size(); ++i )
	{
	X509Certificate cert = ( X509Certificate )certList.get( i );
	principalList.add( cert.getSubjectX500Principal() );
	}
return principalList;
}
//--------------------------------------------------------------------------------
public static Map getTrustedCertMap()
throws Exception
{
Map map = new HashMap();
List l = getTrustedCerts();
for( int i = 0; i < l.size(); ++i )
	{
	X509Certificate cert = ( X509Certificate )l.get( i );
	map.put( cert.getSubjectDN().toString(), cert );
	}
return map;
}
//--------------------------------------------------------------------------------
public static List getTrustedCerts()
throws Exception
{
List l = new ArrayList();

TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
trustManagerFactory.init( ( KeyStore )null );

TrustManager[] managers = trustManagerFactory.getTrustManagers();
for ( int i = 0; i < managers.length; ++i )
	{
	if ( (managers[ i ] ) instanceof X509TrustManager)
		{
	        X509TrustManager x509TrustManager = ( X509TrustManager )managers[ i ];
		l.addAll( Arrays.asList( x509TrustManager.getAcceptedIssuers() ) );
		}
	}
return l;

/*
SSLSocketFactory sf = ( SSLSocketFactory )SSLSocketFactory.getDefault();

Object object = sf;
Field field = null;
Class clazz = null;

clazz = Class.forName( "com.sun.net.ssl.internal.ssl.SSLSocketFactoryImpl" );
field = clazz.getDeclaredField( "context" );
field.setAccessible( true );
object = field.get( object );

clazz = Class.forName( "com.sun.net.ssl.internal.ssl.DefaultSSLContextImpl" );
field = clazz.getDeclaredField( "defaultTrustManagers" );
field.setAccessible( true );
object = field.get( object );

javax.net.ssl.TrustManager[] managers = ( javax.net.ssl.TrustManager[] )object;
object = managers[ 0 ];

clazz = Class.forName( "com.sun.net.ssl.internal.ssl.X509TrustManagerImpl" );
field = clazz.getDeclaredField( "trustedCerts" );
field.setAccessible( true );
object = field.get( object );

Set set = ( Set )object;
return set;
*/
}
//---------------------------------------------------------------------------------------
public static HostnameVerifier getBogusHostnameVerifier()
{
return new HostnameVerifier()
	{
	public boolean verify( String hostname, SSLSession session )
		{
		return true;
		}
	};
}
//--------------------------------------------------------------------------------

}
