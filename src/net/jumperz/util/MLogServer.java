package net.jumperz.util;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

import net.jumperz.app.MBitDog.*;
import net.jumperz.util.*;

public class MLogServer
extends MSingleThreadCommand
implements MLogger
{
public static final int log_debug	= 0;
public static final int log_info	= 1;
public static final int log_warn	= 2;
public static String[] logLevelStr = new String[]{ "DEBUG","INFO","WARN" };

private static MLogServer instance = new MLogServer();

private OutputStream out;
private LinkedList messageQueue = new LinkedList();
private boolean debug = false;
private boolean simpleMode = true;
private volatile List ignoredClassNameList = new ArrayList();
private PrintStream simpleOut = System.out;
// --------------------------------------------------------------------------------
public static MLogServer getInstance()
{
return instance;
}
// --------------------------------------------------------------------------------
private MLogServer()
{
//singleton
mutex = this;
ignoredClassNameList.add(  "MLogServer" );
ignoredClassNameList.add( "MAbstractLogAgent" );
}
//--------------------------------------------------------------------------------
public synchronized void addIgnoredClassName( String className )
{
List newList = new ArrayList();
newList.addAll( ignoredClassNameList );
newList.add( className );
ignoredClassNameList = newList;
}
// --------------------------------------------------------------------------------
public void setDebug( boolean b )
{
debug = b;
}
// --------------------------------------------------------------------------------
public void setSimple( boolean b )
{
simpleMode = b;
}
// --------------------------------------------------------------------------------
public void init( String bitDogFileName, MThreadPool threadPool )
throws IOException
{
simpleMode = false;

	//BitDogを開始
out = new PipedOutputStream();
InputStream in = new PipedInputStream( ( PipedOutputStream )out );
MBitDog bitDog = new MBitDog( bitDogFileName, in );
threadPool.addCommand( bitDog );

	//LogServerを開始
threadPool.addCommand( this );
}
// --------------------------------------------------------------------------------
protected void cleanup()
{
execute2();
MStreamUtil.closeStream( out );
}
// --------------------------------------------------------------------------------
public void shutdown()
{
	//この関数はインスタンスのメインスレッドとは別のスレッドから呼び出されることに注意

if( terminated )
	{
	return;
	}

terminated = true;
synchronized( this )
	{
	this.notify();
	}

	//このインスタンスのメインスレッドが溜まっているログを書き込む間待機する
	//このshutdown()が呼び出されたタイミング以降はlog()の機能は保証しなくていい
	//しかしshutdown()以前に届いていたメッセージは必ず出力するようにする

/*
	//ここでwhileループでmessageQueueを監視してもいいかも
MSystemUtil.sleep( 1000 );

	//残っているメッセージをすべて吐き出す
synchronized( messageQueue )
	{
	execute2();
	}
MStreamUtil.closeStream( out );

terminated = true;
synchronized( this )
	{
	this.notify();
	}
*/
}
// --------------------------------------------------------------------------------
public void execute2()
{
if( simpleMode )
	{
	return;
	}

while( true )
	{
	String message = null;
	
		//キューからメッセージを取り出す
		//messageQueueをロックするので、時間がかかる可能性のあるストリームへの書き込みはロックを開放してから行う
	synchronized( messageQueue )
		{
		if( messageQueue.isEmpty() )
			{
			break;
			}
		else
			{
			message = ( String )messageQueue.getFirst();
			messageQueue.removeFirst();		
			}
		}
	
	try
		{
			//書き込み
		out.write( message.getBytes( MCharset.CS_ISO_8859_1 ) );
		out.write( 0x0A );		
		}
	catch( IOException e )
		{
		e.printStackTrace();
		MStreamUtil.closeStream( out );
		break;
		}
	}
}
// --------------------------------------------------------------------------------
public void breakCommand()
{
}
// --------------------------------------------------------------------------------
public void log( String prefix, String message )
{
log( "", log_info , prefix, message );
}
// --------------------------------------------------------------------------------
public void log( String message )
{
log( "", log_info , "", message );
}
// --------------------------------------------------------------------------------
public void log( String className, int logLevel, String prefix, Object messageObject )
{
log( className, logLevel, prefix, messageObject, new java.util.Date(), getStackTraceElement( null ) );
}
//--------------------------------------------------------------------------------
public void log( String className, int logLevel, String prefix, Object messageObject, String callerClassName )
{
log( className, logLevel, prefix, messageObject, new java.util.Date(), getStackTraceElement( callerClassName ) );
}
//--------------------------------------------------------------------------------
private StackTraceElement getStackTraceElement( String callerClassName )
{
StackTraceElement[] array = ( new Exception() ).getStackTrace();
StackTraceElement ste = null;
for( int i = 0; i < array.length; ++i )
	{
	ste = array[ i ];
	String className = ste.getClassName();
	
	boolean shouldBeIgnored = false;
	for( int k = 0; k < ignoredClassNameList.size(); ++k )
		{
		String ignoredClassName = ( String )ignoredClassNameList.get( k );
		if( className.indexOf( ignoredClassName ) != -1 )
			{
			shouldBeIgnored = true;
			}
		}
	
	if( !shouldBeIgnored )
		{
		if( callerClassName == null )
			{
			break;
			}
		else
			{
			if( className.indexOf( callerClassName ) == -1 )
				{
				break;
				}
			}
		}
	}
return ste;
}
// --------------------------------------------------------------------------------
public void log( String className, int logLevel, String prefix, Object messageObject, java.util.Date now, StackTraceElement ste )
{
if( messageObject instanceof String )
	{
	String s = ( String )messageObject;
	if( s.indexOf( '\r' ) > -1 || s.indexOf( '\n' ) > -1 )
		{
		String[] array = s.split( "[\\r\\n]{1,2}" );
		log( className, logLevel, prefix, array, now, ste );
		return;
		}
	}
else if( messageObject instanceof String[] )
	{
	log( className, logLevel, prefix, "[", now, ste );		
	String[] array = ( String[] )messageObject;
	for( int i = 0; i < array.length; ++i )
		{
		log( className, logLevel, prefix, array[ i ], now, ste );
		}
	log( className, logLevel, prefix, "]", now, ste );		
	return;
	}
else if( messageObject instanceof List )
	{
	List list = ( List )messageObject;
	log( className, logLevel, prefix, "[", now, ste );		
	for( int i = 0; i < list.size(); ++i )
		{
		log( className, logLevel, prefix, list.get( i ), now, ste );
		}
	log( className, logLevel, prefix, "]", now, ste );		
	return;
	}
else if( messageObject instanceof Map )
	{
	Map map = ( Map )messageObject;
	Iterator p = map.keySet().iterator();
	log( className, logLevel, prefix, "{", now, ste );		
	while( p.hasNext() )
		{
		Object key = p.next();
		log( className, logLevel, prefix, key + ":" + map.get( key ), now, ste );		
		}
	log( className, logLevel, prefix, "}", now, ste );		
	return;
	}
else if( messageObject instanceof Throwable )
	{
	String[] strArray = MStringUtil.throwableToStrArray( ( Throwable )messageObject );
	log( className, logLevel, prefix, strArray, now, ste );		
	}
else
	{
		//Object
	String s;
	if( messageObject == null )
		{
		s = "null";
		}
	else
		{
		s = messageObject.toString();
		}
	log( className, logLevel, prefix, s, now, ste );	
	return;
	}

StringBuffer buf = new StringBuffer( 256 );
buf.append( ":(" );
buf.append( ste.getFileName() );
buf.append( ":" );
buf.append( ste.getLineNumber() );
buf.append( "):" );
buf.append( className );
buf.append( ":" );
buf.append( logLevelStr[ logLevel ] );
buf.append( ":" );
buf.append( prefix );
buf.append( ":" );

Thread ct = Thread.currentThread();
if( ct instanceof MWorkerThread )
	{
	MWorkerThread wt = ( MWorkerThread)ct;
	buf.append( wt.getSuperThreadString() );
	buf.append( ":" );
	}

buf.append( messageObject );

String message = buf.toString();

if( !simpleMode )
	{
	synchronized( messageQueue )
		{
		messageQueue.addLast( message );
		}
	}

synchronized( mutex )
	{
	if( !simpleMode )
		{
		mutex.notify();	
		}
	if( debug || simpleMode )
		{
		simpleOut.println( now + message );
		}
	}
}
//--------------------------------------------------------------------------------
public void setSimpleOut( PrintStream _out )
{
simpleOut = _out;
}
// --------------------------------------------------------------------------------
}