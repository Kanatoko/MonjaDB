package net.jumperz.util;

public final class MTimer
implements MCommand, MSubject1
{
private long interval;
private volatile Thread currentThread;
private int roopCount = -1;
private volatile boolean terminated = false;
private MSubject1 subject = new MSubject1Impl();
//--------------------------------------------------------------------------------
public MTimer()
{
interval = 1000;
}
//-----------------------------------------------------------------------------
public MTimer( long in_interval )
{
interval = in_interval;
}
//-----------------------------------------------------------------------------
public MTimer( long in_interval, int in_roopCount )
{
interval = in_interval;
roopCount = in_roopCount;
}
//-----------------------------------------------------------------------------
public void setRoopCount( int i )
{
roopCount = i;
}
//-----------------------------------------------------------------------------
public final void execute()
{
currentThread = Thread.currentThread();

try
	{
	while( !terminated && roopCount != 0 )
		{
		Thread.sleep( interval );
		if( terminated )
			{
			break;
			}
		notify1();
		if( roopCount > 0 )
			{
			--roopCount;
			}
		}
	}
catch( InterruptedException e )
	{
	if( !terminated )
		{
		e.printStackTrace();		
		}
	}
}
//-----------------------------------------------------------------------------
public final void breakCommand()
{
terminated = true;
if( currentThread != null )
	{
	currentThread.interrupt();
	}
}
//-----------------------------------------------------------------------------
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
