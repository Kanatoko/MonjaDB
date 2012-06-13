package net.jumperz.net;

import java.io.*;
import net.jumperz.util.*;
import java.util.*;
import java.net.*;

public final class MSocketCloser
extends MSingleThreadCommand
{
private LinkedList socketQueue = new LinkedList();

private static final MSocketCloser instance = new MSocketCloser();
//--------------------------------------------------------------------------------
public static MSocketCloser getInstance()
{
return instance;
}
//--------------------------------------------------------------------------------
public void addSocket( Socket socket )
{
synchronized( socketQueue )
	{
	socketQueue.addLast( socket );
	}

synchronized( mutex )
	{
	mutex.notify();
	}
}
//--------------------------------------------------------------------------------
protected void execute2()
{
while( !socketQueue.isEmpty() )
	{
	Socket socket = null;
	synchronized( socketQueue )
		{
		socket = ( Socket )socketQueue.getFirst();
		socketQueue.removeFirst();
		}
	try
		{
		if( !socket.isClosed() )
			{
			socket.close();
			}
		}
	catch( IOException e )
		{
		e.printStackTrace();
		}
	}
}
//--------------------------------------------------------------------------------
private MSocketCloser()
{
mutex = this;
}
//--------------------------------------------------------------------------------
public void breakCommand()
{
terminated = true;

synchronized( this )
	{
	notify();
	}
}
//--------------------------------------------------------------------------------
}