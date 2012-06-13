package net.jumperz.util;

import java.io.*;

public final class MStreamConnector
implements MCommand, MSubject1
{
private int state;
private InputStream inputStream;
private OutputStream outputStream;
private MSubject1 subject = new MSubject1Impl();

private static final int DEFAULT_BUFSIZE = 4096;

public static final int RECEIVED	= 0;
public static final int SENT		= 1;
public static final int CLOSED		= 2;

private int received;
private byte[] buffer;
private int bufSize = DEFAULT_BUFSIZE;
private int totalSize = 0;
//-------------------------------------------------------------------------------------------
public MStreamConnector( InputStream in_inputStream, OutputStream in_outputStream )
{
inputStream	= in_inputStream;
outputStream	= in_outputStream;
}
// --------------------------------------------------------------------------------
public int getReceived()
{
return received;
}
// --------------------------------------------------------------------------------
public byte[] getBuffer()
{
return buffer;
}
// --------------------------------------------------------------------------------
public void setBufSize( int i )
{
if( i > 0 )
	{
	bufSize = i;
	}
}
//--------------------------------------------------------------------------------
public int getTotalSize()
{
return totalSize;
}
//-------------------------------------------------------------------------------------------
public void execute()
{
try
	{
	received = 0;
	buffer = new byte[ bufSize ];
	while( true )
		{
		received = inputStream.read( buffer );
		if( received <= 0 )
			{
			break;
			}
		state = RECEIVED;
		notify1();
		
		outputStream.write( buffer, 0, received );
		totalSize += received;
		state = SENT;
		notify1();
		}
	}
catch( IOException e )
	{
	}

closeStreams();
state = CLOSED;
notify1();
}
//-------------------------------------------------------------------------------------------
public void breakCommand()
{
closeStreams();
}
//-------------------------------------------------------------------------------------------
private void closeStreams()
{
try
	{
	inputStream.close();
	}
catch( IOException e )
	{
	e.printStackTrace();
	}
try
	{
	outputStream.close();
	}
catch( IOException e )
	{
	e.printStackTrace();
	}
}
//-------------------------------------------------------------------------------------------
public int getState()
{
return state;
}
//-------------------------------------------------------------------------------------------
public void notify1()
{
subject.notify1();
}
//----------------------------------------------------------------
public void register1( MObserver1 observer )
{
subject.register1( observer );
}
//----------------------------------------------------------------
public void removeObservers1()
{
subject.removeObservers1();
}
//----------------------------------------------------------------
public void removeObserver1( MObserver1 observer )
{
subject.removeObserver1( observer );
}
//----------------------------------------------------------------
}