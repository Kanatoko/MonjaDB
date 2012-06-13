package net.jumperz.util;

public final class MWorkerThread
extends Thread
{
private MCommand command;
private volatile boolean suspended;
private volatile boolean terminated;
private MThreadPool threadPool;
private volatile long waitTime;
private volatile long startTime = -1;
//--------------------------------------------------------------------
public MWorkerThread( MThreadPool in_threadPool )
{
threadPool	= in_threadPool;
terminated	= false;
suspended	= true;
start();
}
// --------------------------------------------------------------------------------
public long getStartTime()
{
return startTime;
}
//--------------------------------------------------------------------------------
public MCommand getCommand()
{
return command;
}
//--------------------------------------------------------------------
public final void run()
{
while( !terminated )
	{
	synchronized( this )
		{
		while( suspended )
			{
			try
				{
				wait();
				}
			catch( Exception e )
				{
				e.printStackTrace();
				//break;
				}
			}
		}
	
	if( terminated )
		{
		break;
		}

	try
		{
		startTime = System.currentTimeMillis();
		command.execute();
		}
	catch( Throwable e )
		{
		 // execute() throws no Exception
		System.err.println( "Caught a throwable. The Command is : " + command );
		e.printStackTrace();
		command.breakCommand();
		}

	command = null;
	suspended = true;
	waitTime = System.currentTimeMillis();
	startTime = -1;
	threadPool.setThreadWait( this );
	}
}
//--------------------------------------------------------------------
public final void setCommand( MCommand in_Command )
{
command = in_Command;
}
//--------------------------------------------------------------------
public final void terminate()
{
terminated = true;
}
//--------------------------------------------------------------------
public final synchronized void resumeThread()
{
suspended = false;
notify();
}
//--------------------------------------------------------------------
public final void breakThread()
{
if( command != null )
	{
	try
		{
		command.breakCommand();	
		}
	catch( Exception e )
		{
		e.printStackTrace();
		}
	}
}
//--------------------------------------------------------------------
public long getWaitTime()
{
return waitTime;
}
//--------------------------------------------------------------------------------
public String getSuperThreadString()
{
return super.toString();
}
// --------------------------------------------------------------------------------
public String toString()
{
if( command == null )
	{
	return "MWorkerThread:idle:" + super.toString();
	}
else
	{
	if( startTime == -1 )
		{
		return "MWorkerThread:" + command + ":" + super.toString();
		}
	else
		{
		return "MWorkerThread:" + ( System.currentTimeMillis() - startTime ) + ":" + command + ":" + super.toString();
		}
	}
}
// --------------------------------------------------------------------------------

}
