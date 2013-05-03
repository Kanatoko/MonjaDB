package net.jumperz.net;

import java.util.*;
import java.io.*;
import net.jumperz.util.*;
import net.jumperz.io.*;

/*
 * Low Memory multipart parameter implementation
 */
public class MMultipartParameter2
extends MMultipartParameter
{
private MBuffer mbuffer;
private int headerLength;
private boolean valueIsLazy = false;
//--------------------------------------------------------------------------------
public MMultipartParameter2( MBuffer mbuffer )
throws IOException
{
this.mbuffer = mbuffer;
parseStream( this.mbuffer.getInputStream() );
}
//--------------------------------------------------------------------------------
public MMultipartParameter2( String str )
throws IOException
{
this( new ByteArrayInputStream( str.getBytes( MCharset.CS_ISO_8859_1 ) ) );
}
//--------------------------------------------------------------------------------
public MMultipartParameter2( InputStream in )
throws IOException
{
mbuffer = new MBuffer();
MStreamUtil.connectStream( in, mbuffer, true );
parseStream( this.mbuffer.getInputStream() );
}
//--------------------------------------------------------------------------------
private void parseStream( InputStream in )
throws IOException
{
	//header must be lower than 8KB
int bufSize = 8192;
byte[] buf = new byte[ bufSize ];
int read = in.read( buf );
if( read <= 0 )
	{
	throw new IOException( "Read error." );
	}
String workStr = new String( buf, 0, read, MCharset.CS_ISO_8859_1 );
parse( workStr );
headerLength = workStr.indexOf( "\r\n\r\n" ) + 4;

	//here, 'value' may be a wrong value when data is bigger than 8KB

if( read == bufSize )
	{
	value = "DUMMY"; //reset wrong value
	valueIsLazy = true;
	}
}
//--------------------------------------------------------------------------------
public String getValue()
{
if( valueIsLazy )
	{
	try
		{
		InputStream in = mbuffer.getInputStream();
		in.skip( headerLength );
		return MStreamUtil.streamToString( in );
		}
	catch( Exception e )
		{
		e.printStackTrace();
		return "";
		}
	}
else
	{
	return super.getValue();
	}
}
//--------------------------------------------------------------------------------
public InputStream getValueStream()
{
return mbuffer.getInputStream();
}
//--------------------------------------------------------------------------------
public int getValueSize()
{
return mbuffer.getSize() - headerLength;
}
//--------------------------------------------------------------------------------
public void clear()
{
if( mbuffer != null )
	{
	mbuffer.clear();
	}
}
//--------------------------------------------------------------------------------
}