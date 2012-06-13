package net.jumperz.util;

import java.util.*;

public class MAsyncCommandManager
implements MObserver2
{
private MThreadPool threadPool;
private List commandList = new ArrayList();
private Map resultMap = new HashMap();
private Object mutex = new Object();
private boolean usingExternalThreadPool = false;
//--------------------------------------------------------------------------------
public MAsyncCommandManager()
{
}
//--------------------------------------------------------------------------------
public MAsyncCommandManager( MThreadPool tp, MCommand2 command )
{
threadPool = tp;
addCommand( command );
}
//--------------------------------------------------------------------------------
public MAsyncCommandManager( MThreadPool tp, List _commandlist )
{
threadPool = tp;
addCommand( _commandlist );
}
//--------------------------------------------------------------------------------
public MAsyncCommandManager( MCommand2 command )
{
threadPool = new MThreadPool( 1 );
addCommand( command );
}
//--------------------------------------------------------------------------------
public MAsyncCommandManager( int threadCount, List _commandList )
{
threadPool = new MThreadPool( threadCount );
addCommand( _commandList );
}
//--------------------------------------------------------------------------------
public MAsyncCommandManager( int threadCount )
{
threadPool = new MThreadPool( threadCount );
}
//--------------------------------------------------------------------------------
public MAsyncCommandManager( MThreadPool tp )
{
usingExternalThreadPool = true;
threadPool = tp;
}
//--------------------------------------------------------------------------------
public MThreadPool getThreadPool()
{
return threadPool;
}
//--------------------------------------------------------------------------------
public void breakCommands()
{
synchronized( mutex )
	{
	for( int i = 0; i < commandList.size(); ++i )
		{
		MCommand command = ( MCommand )commandList.get( i );
		command.breakCommand();
		}
	}
}
//--------------------------------------------------------------------------------
public Object getFastestAndBreakCommands( long timeout ) //main thread
throws InterruptedException
{
Object result = getFastest( timeout );
breakCommands();
return result;
}
//--------------------------------------------------------------------------------
public Object getFastestAndStopThreadPool( long timeout ) //main thread
throws InterruptedException
{
if( usingExternalThreadPool )
	{
	throw new IllegalStateException( "External Thread Pool." );
	}

Object result = getFastest( timeout );
threadPool.stop();
return result;
}
//--------------------------------------------------------------------------------
public Object getFastest( long timeout ) //main thread
throws InterruptedException
{
long start = System.currentTimeMillis();
synchronized( mutex )
	{
	while( true )
		{
		if( resultMap.size() > 0 )
			{
			Iterator p = resultMap.keySet().iterator();
			if( p.hasNext() )
				{
				return resultMap.get( p.next() );
				}
			}
		else
			{
			long _timeout = getTimeout( start, timeout );
			if( _timeout > 0 )
				{
				mutex.wait( _timeout );
				}
			else
				{
				return null;
				}
			}
		}
	
	}
}
//--------------------------------------------------------------------------------
public Object getByCommand( MCommand2 command, long timeout ) //main thread
throws InterruptedException
{
long start = System.currentTimeMillis();
synchronized( mutex )
	{
	while( true )
		{
		if( resultMap.containsKey( command ) )
			{
			return resultMap.get( command );
			}
		else
			{
			long _timeout = getTimeout( start, timeout );
			if( _timeout > 0 )
				{
				mutex.wait( _timeout );
				}
			else
				{
				return null;
				}
			}
		}
	}
}
//--------------------------------------------------------------------------------
private long getTimeout( long start, long timeout )
{
long now = System.currentTimeMillis();
if( ( now - start ) >= ( timeout - 50 ) )
	{
	return -1;
	}
else
	{
	return ( timeout - ( now - start ) );
	}
}
//--------------------------------------------------------------------------------
public Object getByCommand( MCommand2 command ) //main thread
throws InterruptedException
{
synchronized( mutex )
	{
	while( true )
		{
		if( resultMap.containsKey( command ) )
			{
			return resultMap.get( command );
			}
		else
			{
			//log( resultMap.size() + ":" + commandList.size() );
			//log( "waiting..." );
			mutex.wait();
			//log( "woke up!" );
			}
		}
	
	}
}
//--------------------------------------------------------------------------------
public Object getFastestAndStopThreadPool()
throws InterruptedException
{
if( usingExternalThreadPool )
	{
	throw new IllegalStateException( "External Thread Pool." );
	}

Object result = getFastest();
threadPool.stop();
return result;
}
//--------------------------------------------------------------------------------
public Object getFastest() //main thread
throws InterruptedException
{
synchronized( mutex )
	{
	while( true )
		{
		if( resultMap.size() > 0 )
			{
			Iterator p = resultMap.keySet().iterator();
			if( p.hasNext() )
				{
				return resultMap.get( p.next() );
				}
			}
		else
			{
			mutex.wait();
			}
		}
	
	}
}
//--------------------------------------------------------------------------------
public Map getSomeAndStopThreadPool( long timeout ) //main thread
throws InterruptedException
{
if( usingExternalThreadPool )
	{
	throw new IllegalStateException( "External Thread Pool." );
	}

Map result = getSome( timeout );
threadPool.stop();
return result;
}
//--------------------------------------------------------------------------------
public Map getSome( long timeout ) //main thread
throws InterruptedException
{
long start = System.currentTimeMillis();
synchronized( mutex )
	{
	while( true )
		{
		if( commandList.size() == 0 )
			{
			return new HashMap( resultMap );
			}
		else
			{
			long _timeout = getTimeout( start, timeout );
			if( _timeout > 0 )
				{
				mutex.wait( _timeout );			
				}
			else
				{
				return new HashMap( resultMap );
				}
			}
		}
	
	}
}
//--------------------------------------------------------------------------------
public Map getAll() //main thread
throws InterruptedException
{
synchronized( mutex )
	{
	while( true )
		{
		if( commandList.size() == 0 )
			{
			return new HashMap( resultMap );
			}
		else
			{
			mutex.wait();
			}
		}
	}
}
//--------------------------------------------------------------------------------
public void addCommand( List _list ) // main thread
{
for( int i = 0; i < _list.size(); ++i )
	{
	MCommand2 command = ( MCommand2 )_list.get( i );
	command.register2( this );
	synchronized( mutex )
		{
		commandList.add( command );
		}
	if( threadPool != null )
		{
		threadPool.addCommand( command );
		}
	}
}
//--------------------------------------------------------------------------------
public void addCommand( MCommand2 command ) // main thread
{
command.register2( this );
synchronized( mutex )
	{
	commandList.add( command );
	}
if( threadPool != null )
	{
	threadPool.addCommand( command );
	}
}
//--------------------------------------------------------------------------------
public void update( Object event, Object source ) //command thread
{
synchronized( mutex )
	{
	resultMap.put( source, ( ( MCommand2 )source ).getResult() ); // put result
	commandList.remove( source ); //remove command from list
	mutex.notify();
	}
}
//--------------------------------------------------------------------------------
}