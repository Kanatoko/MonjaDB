package net.jumperz.util;

import java.util.*;

public final class MThreadPool
implements MObserver1
{
private static int DEFAULT_THREAD_COUNT = 20;
private static int DEFAULT_IDLE_TIME = 30 * 1000; // 30 seconds
private LinkedList waitingThreadList;
private LinkedList runningThreadList;
private LinkedList commandList;
private boolean stopped = false;
private int maxReachedThreadCount = 0;
private volatile MLogger logger = new MNullLogger();
private int maxCount;
private int startCount;
private int maxSleep = DEFAULT_IDLE_TIME;
private int tickCount = 0;
private volatile boolean slowStopped = false;
private Map timeMap = new HashMap();

private volatile long lastCommandWaitTime;

private volatile long commandCount;
private volatile long lastCommandCount; //10 seconds ago
private volatile long[] commandCountHistory = new long[]{ 0,0,0,0,0,0 };
//---------------------------------------------------------------
public MThreadPool( int threadCount )
{
maxCount = threadCount;
startCount = threadCount;

init( threadCount );
}
//---------------------------------------------------------------
public MThreadPool()
{
maxCount = DEFAULT_THREAD_COUNT;
startCount = DEFAULT_THREAD_COUNT;

init( DEFAULT_THREAD_COUNT );
}
// --------------------------------------------------------------------------------
public MThreadPool( int start, int max )
{
maxCount = max;
startCount = start;
init( start );
}
// --------------------------------------------------------------------------------
public int getCommandListCount()
{
return commandList.size();
}
// --------------------------------------------------------------------------------
public int getMaxCount()
{
return maxCount;
}
//--------------------------------------------------------------------------------
public void setLogger( MLogger logger )
{
this.logger = logger;
}
//---------------------------------------------------------------
private void init( int threadCount )
{
waitingThreadList	= new LinkedList();
runningThreadList	= new LinkedList();
commandList		= new LinkedList();

maxReachedThreadCount = threadCount / 2;

	//create worker threads
for( int i = 0; i < threadCount; ++i )
	{
	waitingThreadList.add( new MWorkerThread( this ) );
	}
}
// --------------------------------------------------------------------------------
private void executeCommand()
{
MCommand command = ( MCommand )commandList.getFirst();
commandList.removeFirst();

Long addedTime = ( Long )timeMap.remove( command );
if( addedTime != null )
	{
	lastCommandWaitTime = ( System.currentTimeMillis() ) - addedTime.longValue();	
	}

MWorkerThread workerThread = ( MWorkerThread )waitingThreadList.getLast();
waitingThreadList.removeLast();
runningThreadList.addLast( workerThread );

workerThread.setCommand( command );
workerThread.resumeThread();
}
// --------------------------------------------------------------------------------
public long getLastCommandWaitTime()
{
return lastCommandWaitTime;
}
//---------------------------------------------------------------
private synchronized void doIt()
{
if( commandList.size() == 0 ) //nothing to do
	{
	if( slowStopped ) // you can go to bed now
		{
		if( runningThreadList.size() == 0 ) // no one is working
			{
			stop(); // good night!
			}
		}
	return;
	}

//here we have commands to execute

if( waitingThreadList.size() > 0 ) //we have a thread
  	{
  	executeCommand();
	}
else //we have to decide that whether a new thread need to be created
	{
	if( runningThreadList.size() < maxCount ) //create
		{
		waitingThreadList.addLast( new MWorkerThread( this ) );
	  	executeCommand();
		}
	else
		{
		return; // we already have enough threads
		}
	}

int count = runningThreadList.size();
if( count > maxReachedThreadCount )
	{
	logger.log( "SYS", "maxThreadCount::" + count );
	maxReachedThreadCount = count;
	}
}
//---------------------------------------------------------------
public synchronized void insertCommand( MCommand command )
{
++commandCount;
/*
if( waitingThreadList.size() == 0 )
	{
	waitingThreadList.add( new MWorkerThread( this ) );
	}
*/
commandList.addFirst( command );
doIt();
}
//---------------------------------------------------------------
public synchronized void addCommand( MCommand command )
{
++commandCount;
commandList.addLast( command );
timeMap.put( command, new Long( System.currentTimeMillis() ) );
doIt();
}
//--------------------------------------------------------------------------------
public synchronized boolean forceCommand( MCommand command )
{
if( waitingThreadList.size() > 0 )
	{
	commandList.addFirst( command );
	doIt();
	return true;
	}
else
	{
	return false;
	}
}
//---------------------------------------------------------------
public synchronized void setThreadWait( MWorkerThread workerThread )
{
runningThreadList.remove( workerThread );

if( stopped )
	{
	workerThread.terminate();
	workerThread.resumeThread();
	}
else
	{	
	waitingThreadList.addLast( workerThread );
	doIt();
	}
}
//--------------------------------------------------------------------------------
public synchronized List getLongRunningThreadList( long elapsedTime )
{
List l = new ArrayList();
long now = System.currentTimeMillis();
for( int i = 0; i < runningThreadList.size(); ++i )
	{
	MWorkerThread t = ( MWorkerThread )runningThreadList.get( i );
	long startTime = t.getStartTime();
	if( ( now - startTime ) > elapsedTime )
		{
		l.add( t );
		}
	}
return l;
}
// --------------------------------------------------------------------------------
public String dump()
{
StringBuffer buf = new StringBuffer( 2048 );
buf.append( toString() );
buf.append( "\r\n" );

long[] commandCountHistoryRef = commandCountHistory;
for( int i = 0; i < commandCountHistoryRef.length; ++i )
	{
	buf.append( commandCountHistoryRef[ i ] );
	buf.append( " " );
	}
buf.append( "\r\n" );

synchronized( this )
	{
	for( int i = 0; i < runningThreadList.size(); ++i )
		{
		MWorkerThread t = ( MWorkerThread )runningThreadList.get( i );
		buf.append( t.toString() );
		buf.append( "\r\n" );
		}
	}

return buf.toString();
}
// --------------------------------------------------------------------------------
public synchronized String toString()
{
StringBuffer buf = new StringBuffer( 2048 );
buf.append( "[run/wait/command/total/wtime/max]:" );
buf.append( runningThreadList.size() );
buf.append( "/" );
buf.append( waitingThreadList.size() );
buf.append( "/" );
buf.append( commandList.size() );
buf.append( "/" );
buf.append( commandCount );
buf.append( "/" );
buf.append( lastCommandWaitTime );
buf.append( "/" );
buf.append( maxReachedThreadCount );

return buf.toString();
}
// --------------------------------------------------------------------------------
public void slowStop()
{
slowStopped = true;
doIt();
}
//---------------------------------------------------------------
public synchronized void stop()
{
if( stopped )
	{
	return;
	}

stopped = true;

	//stop waiting threads
while( !waitingThreadList.isEmpty() )
	{
	MWorkerThread workerThread = ( MWorkerThread )waitingThreadList.getFirst();
	waitingThreadList.removeFirst();
	workerThread.terminate();
	workerThread.resumeThread();
	}

	//stop running threads
while( !runningThreadList.isEmpty() )
	{
	MWorkerThread workerThread = ( MWorkerThread )runningThreadList.getFirst();
	runningThreadList.removeFirst();
	workerThread.breakThread();
	}	
}
// --------------------------------------------------------------------------------
private void updateCommandHistory()
{
long[] tmpArray = new long[ commandCountHistory.length ];
int size = commandCountHistory.length;
for( int i = 1; i < size; ++i )
	{
	tmpArray[ i - 1 ] = commandCountHistory[ i ];
	}
tmpArray[ size - 1 ] = commandCount - lastCommandCount;
commandCountHistory = tmpArray; //replace
lastCommandCount = commandCount;
}
// --------------------------------------------------------------------------------
public void update()
{
tickCount++;
if( tickCount == 10 )
	{
	checkElapsedTime();
	updateCommandHistory();
	}
}
// --------------------------------------------------------------------------------
private synchronized boolean hasEnoughThreads()
{
return ( ( runningThreadList.size() + waitingThreadList.size() ) > startCount );
}
// --------------------------------------------------------------------------------
private synchronized void checkElapsedTime()
{
tickCount = 0;
for( int i = 0; i < waitingThreadList.size(); ++i )
	{
	if( !hasEnoughThreads() )
		{
		break;
		}
	MWorkerThread workerThread = ( MWorkerThread )waitingThreadList.get( i );
	long waitTime = workerThread.getWaitTime();
	long now = System.currentTimeMillis();
	if( ( now - waitTime ) > maxSleep )
		{
		workerThread.terminate();
		workerThread.resumeThread();
		waitingThreadList.remove( i );
		--i;
		}
	else
		{
		break;
		}
	}
}
//---------------------------------------------------------------
public int getRunningThreadCount()
{
return runningThreadList.size();
}
//---------------------------------------------------------------
public int getWaitingThreadCount()
{
return waitingThreadList.size();
}
//--------------------------------------------------------------------------------
}
