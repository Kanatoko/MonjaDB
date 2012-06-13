package net.jumperz.net.jaxer;

import java.util.*;
import java.io.*;
import java.net.*;
import net.jumperz.net.*;
import net.jumperz.io.*;

public class MJaxerPing
{
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
if( args.length != 1 )
	{
	System.err.println( "Usage: jaxerPing address" );
	return;
	}
MHttpRequest req = new MHttpRequest( "GET / HTTP/1.0\r\nHost: foobar.1.2.3\r\n\r\n" );
MHttpResponse res = new MHttpResponse( "HTTP/1.0 200 OK\r\nConnection: close\r\nX-Foo: bar\r\n\r\n" );

MBuffer mbuf = new MBuffer();
String str = "<html>\r\n<body>\r\n<div id='div1'>foo</div>\r\n<script runat='server'>\r\ndocument.getElementById( 'div1' ).innerHTML='bar';</script>\r\n</body>\r\n</html>\r\n"; 
mbuf.write( str.getBytes() );
res.setBodyBuffer( mbuf );
res.setContentLength();

MJaxerServer js = new MJaxerServer();
js.loadFromStr( args[ 0 ] );
Socket s = js.getConnection();

Map e = new HashMap();

MJaxerFilter jf = new MJaxerFilter( s, req, res, e );
MHttpResponse jaxerResponse = jf.getJaxerResponse();
if( !jf.isError()
&& jaxerResponse.toString().indexOf( "bar</div>" ) > -1
)
	{
	p( "OK" );
	}
else
	{
	p( "Error" );
	}
}
//--------------------------------------------------------------------------------
private static void p( Object o )
{
System.out.println( o );
}
//--------------------------------------------------------------------------------
}