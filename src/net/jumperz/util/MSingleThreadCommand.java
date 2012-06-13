package net.jumperz.util;

import net.jumperz.util.*;

public abstract class MSingleThreadCommand
implements MCommand
{
protected volatile boolean terminated = false;
protected Thread myThread;
protected Object mutex;

protected abstract void execute2();
//--------------------------------------------------------------------------------
public void execute()
{
myThread = Thread.currentThread();

while( !terminated )
	{
	execute2();
	
	if( terminated )
		{
		break;
		}

	synchronized( mutex )
		{
		try
			{
			mutex.wait();
			}
		catch( InterruptedException e )
			{
			e.printStackTrace();
			break;
			}
		}
	}

cleanup();
}
// --------------------------------------------------------------------------------
protected void cleanup()
{
}
//--------------------------------------------------------------------------------
}