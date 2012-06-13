package net.jumperz.io;

import java.util.*;
import java.io.*;
import net.jumperz.util.*;

public class MAdvancedLineReader
implements MLineReader
{
private int lastDelimiter;
private BufferedInputStream bis;
private byte[] buffer = new byte[ BUFSIZE ];

public static final int BUFSIZE = 5;
public static final byte BYTE_CR = ( byte )0x0D;
public static final byte BYTE_LF = ( byte )0x0A;
// --------------------------------------------------------------------------------
public String readLine()
throws IOException
{
/*
BufferedReader reader = new BufferedReader( new InputStreamReader( bis, MCharset.CS_ISO_8859_1 ) );
String line = reader.readLine();
if( line == null )
	{
	checkLineDelimiter( line );
	return null;
	}
else
	{
	checkLineDelimiter( line );
	return line;
	}
*/


String line = null;
ByteArrayOutputStream bufStream = null;
while( true )
	{
	int r = bis.read( buffer );
	if( r == -1 )
		{
		if( bufStream != null && bufStream.size() > 0 )
			{
			line = new String( bufStream.toByteArray(), MCharset.CS_ISO_8859_1 );
			}
		else
			{
			line = null;
			}
		checkLineDelimiter( line );
		return line;
		}
	else
		{
		for( int i = 0; i < r; ++i )
			{
			if( buffer[ i ] == BYTE_CR || buffer[ i ] == BYTE_LF )
				{
				if( bufStream == null )
					{
					line = new String( buffer, 0, i, MCharset.CS_ISO_8859_1 );
					}
				else
					{
					bufStream.write( buffer, 0, i );					
					line = new String( bufStream.toByteArray(), MCharset.CS_ISO_8859_1 );				
					}
				checkLineDelimiter( line );
				return line;
				}
			}
		if( bufStream == null )
			{
			bufStream = new ByteArrayOutputStream();
			}
		bufStream.write( buffer, 0, r );
		}
	}
}
// --------------------------------------------------------------------------------
private void checkLineDelimiter( String line )
throws IOException
{
reset();
if( line != null )
	{
	skip( line.length() );
	}
mark();
int i = bis.read();
if( i == 0x0D )
	{
		// CR found
	i = bis.read();
	if( i == 0x0A )
		{
			// CRLF
		mark();
		lastDelimiter = CRLF;
		}
	else
		{
		reset();
		skip( 1 );
		mark();
		lastDelimiter = CR;
		}
	}
else if( i == 0x0A )
	{
		// LF found
	mark();
	lastDelimiter = LF;
	}
else
	{
	lastDelimiter = NULL;
	}
}
// --------------------------------------------------------------------------------
private void reset()
throws IOException
{
bis.reset();
}
// --------------------------------------------------------------------------------
private void skip( long l )
throws IOException
{
bis.skip( l );
}
// --------------------------------------------------------------------------------
private void mark()
throws IOException
{
bis.mark( Integer.MAX_VALUE );
}
// --------------------------------------------------------------------------------
public void setInputStream( InputStream in )
throws IOException
{
bis = new BufferedInputStream( in );

mark();
}
// --------------------------------------------------------------------------------
public String getLastDelimiterString()
{
if( lastDelimiter == CR )
	{
	return "\r";
	}
else if( lastDelimiter == LF )
	{
	return "\n";
	}
else if( lastDelimiter == CRLF )
	{
	return "\r\n";
	}
else
	{
	return "";
	}
}
// --------------------------------------------------------------------------------
public int getLastDelimiterSize()
{
if( lastDelimiter == CR || lastDelimiter == LF )
	{
	return 1;
	}
else if( lastDelimiter == CRLF )
	{
	return 2;
	}
else
	{
	return 0;
	}
}
// --------------------------------------------------------------------------------
public int getLastDelimiter()
{
return lastDelimiter;
}
// --------------------------------------------------------------------------------
}