package net.jumperz.io;

import java.util.*;
import java.io.*;
import net.jumperz.util.MStreamUtil;

/*
 * - NOT multi-thread ready
 * - cleanup() must be called ( especially for large data )
 */
public final class MBuffer
extends OutputStream
{
private static Set tmpFileSet = new HashSet();
//private static Set streamSet = new HashSet();
private static final int DEFAULT_MAX_MEM_SIZE = 1024 * 1024 * 10; //10MByte
private static final int BUFSIZE = 2048;
private static int staticMaxMemSize = DEFAULT_MAX_MEM_SIZE;
public static boolean debug = false;

private int totalSize = 0;
private OutputStream activeStream;
private OutputStream fileStream;
private ByteArrayOutputStream byteStream;
private boolean isSmall;
private boolean fileMode = false;
private File tmpFile;
private File file;
private int maxMemSize;
private boolean closed = false;
private boolean isNull = false;
// --------------------------------------------------------------------------------
public static void setStaticMaxMemSize( int i )
{
staticMaxMemSize = i;
}
/*
//--------------------------------------------------------------------------------
public static void deleteTmpFiles()
{
Iterator p = tmpFileSet.iterator();
while( p.hasNext() )
	{
	File tmpFile = ( File )p.next();
	tmpFile.delete();
	}
}
// --------------------------------------------------------------------------------
public static void closeStreams()
{
Iterator p = streamSet.iterator();
while( p.hasNext() )
	{
	OutputStream s = ( OutputStream )p.next();
	MStreamUtil.closeStream( s );
	}
}
*/
// --------------------------------------------------------------------------------
public MBuffer( int size )
{
maxMemSize = size;
init();
}
//----------------------------------------------------------------------------------
public MBuffer()
{
maxMemSize = staticMaxMemSize;
init();
}
// --------------------------------------------------------------------------------
public void write( int i )
throws IOException
{
activeStream.write( i );
totalSize++;
}
// --------------------------------------------------------------------------------
public boolean isClosed()
{
return closed;
}
//--------------------------------------------------------------------------------
public void close()
{
closed = true;
MStreamUtil.closeStream( activeStream );
}
//----------------------------------------------------------------------------------
private void init()
{
/*
if( bufStream != null )
	{
	try
		{
		bufStream.close();
		}
	catch( IOException e )
		{
		e.printStackTrace();
		}
	}

if( tmpFile != null )
	{
	tmpFile.delete();
	}
*/

tmpFile		= null;
isSmall		= true;
byteStream	= new ByteArrayOutputStream( BUFSIZE );
activeStream	= byteStream;
}
// --------------------------------------------------------------------------------
public void setNull()
{
isSmall = false;
isNull = true;
activeStream = new MNullOutputStream();
}
//----------------------------------------------------------------------------------
public void write( byte[] buffer, int len )
throws IOException
{
write( buffer, 0, len );
}
// --------------------------------------------------------------------------------
public int getSize()
{
return totalSize;
}
// --------------------------------------------------------------------------------
public void write( byte[] buffer, int offset, int len )
throws IOException
{
if( isSmall
 && totalSize < maxMemSize
 && ( totalSize + len ) >= maxMemSize
  )
	{
	changeStreamToTmpFile();
	}

activeStream.write( buffer, offset, len );
totalSize += len;
}
// --------------------------------------------------------------------------------
public byte[] getBytes()
throws IOException
{
return MStreamUtil.streamToBytes( getInputStream() );
}
//----------------------------------------------------------------------------------
public void write( byte[] buffer )
throws IOException
{
int len = buffer.length;
write( buffer, len );
}
//----------------------------------------------------------------------------------
private void changeStreamToTmpFile()
throws IOException
{
isSmall = false;

	// copy data
tmpFile = File.createTempFile( "mbuffer_", ".buf" );
file = tmpFile;
synchronized( MBuffer.tmpFileSet )
	{
	MBuffer.tmpFileSet.add( tmpFile );
	}
tmpFile.deleteOnExit();

fileStream = new FileOutputStream( tmpFile );
byteStream.writeTo( fileStream );
byteStream = null;

activeStream = fileStream;

if( debug )
	{
	new Exception( "create:" + tmpFile.getAbsolutePath() ).printStackTrace();
	}
}
//----------------------------------------------------------------------------------
public InputStream getInputStream()
{
InputStream inputStream = null;
if( isSmall )
	{
	byte[] buffer = byteStream.toByteArray();
	inputStream = new ByteArrayInputStream( buffer );
	}
else if( isNull )
	{
	byte[] buffer = new byte[]{};
	inputStream = new ByteArrayInputStream( buffer );
	}
else
	{
	try
		{
		inputStream = new FileInputStream( file ); 
		}
	catch( FileNotFoundException e )
		{
		e.printStackTrace();
		}
	}
return inputStream;
}
//--------------------------------------------------------------------------------
public void clear()
{
try
	{
	if( !closed )
		{
		close();
		}
	
	deleteTmpFile();
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
}
//--------------------------------------------------------------------------------
private void deleteTmpFile()
{
if( !isSmall )
	{
	if( debug )
		{
		new Exception( "delete:" + tmpFile.getAbsolutePath() ).printStackTrace();
		}

	MStreamUtil.closeStream( fileStream );
	if( tmpFile != null )
		{
		synchronized( tmpFileSet )
			{
			tmpFileSet.remove( tmpFile );
			}

		tmpFile.delete();
		}
	}
}
// --------------------------------------------------------------------------------
public void setFile( File f )
throws IOException
{
file = f;
fileMode = true;
isSmall = false;
activeStream = new FileOutputStream( f );
}
//----------------------------------------------------------------------------------
}