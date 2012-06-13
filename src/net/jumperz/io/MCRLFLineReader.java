package net.jumperz.io;

import java.io.*;
import net.jumperz.util.*;

public class MCRLFLineReader
implements MLineReader
{
private static final int BUFSIZE	= 1024;
private static final byte CR		= ( byte )0x0D;
private static final byte LF		= ( byte )0x0A;

private byte[] buffer	= new byte[ BUFSIZE ];
private boolean isEnd;
private boolean CRFound;
private int start;
private int dataLen;
private int lineLen;
private int r;
private ByteArrayOutputStream bufferStream;
private InputStream is;

//private static int count;
//private static int bufCount;
/*
//--------------------------------------------------------------------------------
public MCRLFLineReader( InputStream in_is )
{
//System.err.println( count + " : " + bufCount );
//++count;
is = in_is;
}*/
// --------------------------------------------------------------------------------
public String getLastDelimiterString()
{
return "\r\n";
}
// --------------------------------------------------------------------------------
public void setInputStream( InputStream i )
{
is = i;
}
//--------------------------------------------------------------------------------
private ByteArrayOutputStream bufferStream()
{
if( bufferStream == null )
	{
	//++bufCount;
	bufferStream  = new ByteArrayOutputStream( BUFSIZE );
	}
return bufferStream;
}
// --------------------------------------------------------------------------------
public int getLastDelimiter()
{
return CRLF;
}
// --------------------------------------------------------------------------------
public int getLastDelimiterSize()
{
return 2;
}
//--------------------------------------------------------------------------------
public String readLine()
throws IOException
{
if( isEnd == true )
	{
	return null;
	}

while( true )
	{
	if( dataLen > 0 )
		{
		if( CRFound )
			{
			CRFound = false;
			if( buffer[ start ] == LF )
				{
				++start;
				--dataLen;
				String str = bufferStream().toString( MCharset.CS_ISO_8859_1 );
				bufferStream().reset();
				checkCRLF( str );
				return str;
				}
			else
				{
				bufferStream().write( CR );
				}
			}
		else
			{
				// search CRLF
			boolean CRLFFound = false;
			for( int i = 0; i < dataLen - 1; ++i )
				{
				byte b1 = buffer[ start + i ];
				byte b2 = buffer[ start + ( i + 1 ) ];
				if( b1 == CR
				 && b2 == LF
				  )
					{
					CRLFFound = true;
					lineLen = i;
					break;
					}
				}
			
			if( CRLFFound )
				{
				String str = null;
				if( bufferStream == null
				 || bufferStream().size() == 0
				  )
					{
					str = new String( buffer, start, lineLen, MCharset.CS_ISO_8859_1 );
					}
				else
					{
					bufferStream().write( buffer, start, lineLen );
					str = bufferStream().toString( MCharset.CS_ISO_8859_1 );
					bufferStream().reset();
					}
				start	+= ( lineLen + 2 );
				dataLen -= ( lineLen + 2 );
				checkCRLF( str );
				return str;
				}
			else
				{
				if( buffer[ start + dataLen - 1 ] == CR )
					{
					CRFound = true;
					bufferStream().write( buffer, start, dataLen - 1 );
					}
				else
					{
					bufferStream().write( buffer, start, dataLen );
					}

				start	= 0;
				dataLen	= 0;
				}
			}
		}
	else
		{
		while( true )
			{
			r = is.read( buffer );
			if( r != 0 )
				{
				break;
				}
			}
		if( r > 0 )
			{
			start	= 0;
			dataLen	= r;
			}
		else if( r == -1 )
			{
			isEnd = true;
			if( CRFound )
				{
				bufferStream().write( CR );
				String str = bufferStream().toString( MCharset.CS_ISO_8859_1 );
				checkCRLF( str );
				return str;
				}
			else
				{
				if( bufferStream == null
				 || bufferStream().size() == 0
				  )
					{
					return null;
					}
				else
					{
					String str = bufferStream().toString( MCharset.CS_ISO_8859_1 );
					checkCRLF( str );
					return str;
					}
				}
			}
		}
	}
}
//--------------------------------------------------------------------------------
private void checkCRLF( String str )
throws IOException
{
if( str.indexOf( "\r" ) > -1
 || str.indexOf( "\n" ) > -1
  )
	{
	throw new IOException( "Single CR or LF found" );
	}
}
//--------------------------------------------------------------------------------
}