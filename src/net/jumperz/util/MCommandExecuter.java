package net.jumperz.util;

import java.io.*;
import net.jumperz.util.*;
import java.util.*;

public final class MCommandExecuter
extends MSingleThreadCommand
{
private LinkedList commandQueue = new LinkedList();
private int maxQueueCount = MAX_QUEUE_COUNT; // 0 means infinite
private long interval = DEFAULT_INTERVAL;

private static final MCommandExecuter instance = new MCommandExecuter();
private static final long DEFAULT_INTERVAL = 1000; // 1 second
private static final int MAX_QUEUE_COUNT = 50;
//--------------------------------------------------------------------------------
public static MCommandExecuter getInstance()
{
return instance;
}
//--------------------------------------------------------------------------------
public void setInterval( long l )
{
interval = l;
}
//--------------------------------------------------------------------------------
public void setMaxQueueCount( int i )
{
maxQueueCount = i;
}
//--------------------------------------------------------------------------------
public void addCommand( String command )
{
synchronized( commandQueue )
	{
	commandQueue.addLast( command );
	}

synchronized( mutex )
	{
	mutex.notify();
	}
}
//--------------------------------------------------------------------------------
protected void execute2()
{
while( !terminated && !commandQueue.isEmpty() )
	{
	String command = null;
	synchronized( commandQueue )
		{
		if( maxQueueCount > 0
		 && commandQueue.size() >=  maxQueueCount
		  )
			{
			Iterator p = commandQueue.iterator();
			while( p.hasNext() )
				{
				String commandInQueue = ( String )p.next();
				//MLogger.getInstance().Log( "Too many commands in queue : " + commandInQueue );
				}
			commandQueue.clear();
			}
		else
			{
			command = ( String )commandQueue.getFirst();
			commandQueue.removeFirst();
			try
				{
				Runtime.getRuntime().exec( command );
				}
			catch( IOException e )
				{
				e.printStackTrace();
				}
			}
		}
	
	try
		{
		Thread.sleep( interval );
		}
	catch( InterruptedException e )
		{
		e.printStackTrace();
		break;
		}
	}
}
//--------------------------------------------------------------------------------
private MCommandExecuter()
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