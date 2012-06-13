package net.jumperz.app.MBitDog;

import java.io.*;
import java.util.*;
import net.jumperz.util.*;
import java.text.*;

public class MBitDog
implements MCommand
{
private List loggerList;
private String configFileName;
private InputStream in;
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
if( args.length != 1 )
	{
	System.out.println( "Usage: java net.jumperz.app.MBitDog.MBitDog CONFIG_FILENAME" );
	return;
	}
else
	{
	MBitDog instance = new MBitDog( args[ 0 ] );
	Runtime.getRuntime().addShutdownHook( new MShutdownHook( instance ) );
	instance.execute();
	}
}
// --------------------------------------------------------------------------------
public MBitDog( String s )
throws IOException
{
configFileName = s;
in = System.in;
init();
}
// --------------------------------------------------------------------------------
public MBitDog( String s, InputStream i )
throws IOException
{
configFileName = s;
in = i;
init();
}
// --------------------------------------------------------------------------------
public void init()
throws IOException
{
	// load loggers
loggerList = MLoggerFactory.load( configFileName );
}
// --------------------------------------------------------------------------------
public void breakCommand()
{
/*
cleanUp( false );
MStreamUtil.closeStream( in );
*/
}
// --------------------------------------------------------------------------------
public void execute()
{
try
	{
	execute2();
	}
catch( IOException e )
	{
	e.printStackTrace();
	cleanUp( false );
	}
}
//--------------------------------------------------------------------------------
public void execute2()
throws IOException
{
BufferedReader reader = new BufferedReader( new InputStreamReader( in, MCharset.CS_ISO_8859_1 ) );

	// read, read, read
String line;
while( true )
	{
	line = reader.readLine();
	if( line == null )
		{
		break;
		}
	Iterator p = loggerList.iterator();
	while( p.hasNext() )
		{
		MAbstractLogger logger = ( MAbstractLogger )p.next();
		if( logger.match( line ) )
			{
			logger.log( line );
			if( logger.isEater() )
				{
				break;
				}
			}
		}
	}

cleanUp( true );
}
// --------------------------------------------------------------------------------
public void cleanUp( boolean isNormalShutdown )
{
if( loggerList == null )
	{
	return;
	}

	// cleanup streams
Iterator p = loggerList.iterator();
while( p.hasNext() )
	{
	MAbstractLogger logger = ( MAbstractLogger )p.next();
	logger.cleanUp( isNormalShutdown );
	}
}
//--------------------------------------------------------------------------------
}