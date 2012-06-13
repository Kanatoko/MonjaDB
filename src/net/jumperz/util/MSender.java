package net.jumperz.util;

import java.io.*;

public class MSender
//extends MSubject
implements MCommand, MSubject1
{
private OutputStream outputStream;
private byte[] buffer;
private int state;

public static final int SUCCESS	= 0;
public static final int DISCONNECTED	= 1;

private MSubject1 subject = new MSubject1Impl();
//----------------------------------------------------------------
public MSender( OutputStream in_outputStream, byte[] IN_buffer )
{
outputStream = in_outputStream;
buffer = IN_buffer;
state = SUCCESS;
}
//----------------------------------------------------------------
public void execute()
{
try
	{
	outputStream.write( buffer );
	}
catch( IOException e )
	{
	state = DISCONNECTED;
	}
notify1();
}
//----------------------------------------------------------------
public void breakCommand()
{
try
	{
	outputStream.close();
	}
catch( IOException e )
	{
	}	
}
//----------------------------------------------------------------
public int getState()
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
