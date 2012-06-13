package net.jumperz.util;

import java.io.*;

public final class MStringReceiver
implements MCommand, MSubject1
{
private InputStream inputStream;
private String buffer;
private int state;
private MSubject1 subject = new MSubject1Impl();

public static final int CONNECTED		= 0;
public static final int DISCONNECTED	= 1;
//----------------------------------------------------------------
public MStringReceiver( InputStream in_inputStream )
{
  
inputStream	= in_inputStream;
state		= CONNECTED;
}
//----------------------------------------------------------------
public final void execute()
{
notify1();
try
	{
	BufferedReader in = new BufferedReader( new InputStreamReader( inputStream, MCharset.CS_ISO_8859_1 ) );
	while( true )
		{
		buffer = in.readLine();
		if( buffer == null )
			{
			state = DISCONNECTED;
			break;
			}
		notify1();
		}
	}
catch( Exception e )
	{
	state = DISCONNECTED;
	}

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
public final String getBuffer()
{
return buffer;
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
