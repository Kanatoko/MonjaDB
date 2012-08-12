package net.jumperz.util;

import java.util.*;

public class MObjectPool
{
private volatile LinkedList objectQueue = new LinkedList();
private volatile LinkedList threadQueue = new LinkedList();
//--------------------------------------------------------------------------------
public void addToPool( Object o )
{
objectQueue.addLast( o );
}
//--------------------------------------------------------------------------------
public Object getObject()
{
while( true )
	{
	synchronized( objectQueue )
		{
		if( !objectQueue.isEmpty() )
			{
			Object o = objectQueue.getFirst();
			objectQueue.removeFirst();
			return o;
			}
		}

	Thread currentThread = Thread.currentThread();
	
	synchronized( threadQueue )
		{
		threadQueue.addLast( currentThread );
		}
	
	synchronized( currentThread )
		{
		try
			{
			currentThread.wait();
			}
		catch( InterruptedException e )
			{
			e.printStackTrace();
			return null;
			}
		}
	}
}
//--------------------------------------------------------------------------------
public void returnObject( Object o )
{
synchronized( objectQueue )
	{
	objectQueue.addLast( o );
	}

Thread wakenedThread = null;
synchronized( threadQueue )
	{
	if( threadQueue.isEmpty() )
		{
		return;
		}
	wakenedThread = ( Thread )threadQueue.getFirst();
	threadQueue.removeFirst();
	}

synchronized( wakenedThread )
	{
	wakenedThread.notify();
	}
}
//--------------------------------------------------------------------------------
}