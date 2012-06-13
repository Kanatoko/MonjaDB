package net.jumperz.util;

import java.io.*;

public final class MByteReceiver
implements MCommand, MSubject1
{
private InputStream inputStream;
private byte[] buffer;
private int dataSize;
private int state;
private MSubject1 subject = new MSubject1Impl();

public static final int CONNECTED	= 0;
public static final int DISCONNECTED	= 1;

private static final int BUFSIZE	= 1024;
//----------------------------------------------------------------
public MByteReceiver( InputStream in_inputStream )
{
inputStream = in_inputStream;
state = CONNECTED;
}
//----------------------------------------------------------------
public final void execute()
{
try
	{
	while( true )
		{
		buffer = new byte[ BUFSIZE ];
		dataSize = inputStream.read( buffer );
		if( dataSize <= 0 )
			{
			break;
			}
		notify1();
		}
	}
catch( Exception e )
	{
	}

state = DISCONNECTED;
notify1();
}
//----------------------------------------------------------------
public final void breakCommand()
{
try
	{
	inputStream.close();
	}
catch( IOException e )
	{
	}
}
//----------------------------------------------------------------
public final byte[] getBuffer()
{
return buffer;
}
//----------------------------------------------------------------
public final int getDataSize()
{
return dataSize;
}
//----------------------------------------------------------------
public final int getState()
{
return state;
}
//----------------------------------------------------------------
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
