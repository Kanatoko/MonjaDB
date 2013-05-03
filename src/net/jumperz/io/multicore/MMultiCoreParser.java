package net.jumperz.io.multicore;

import java.io.*;

import net.jumperz.util.MStreamUtil;
import net.jumperz.util.MThreadPool;

public class MMultiCoreParser
{
private InputStream in;
private OutputStream out;
private Class parserClass;
private String charset;
private MReader reader;
private MWriter writer;
private int threadCount = 4;
//--------------------------------------------------------------------------------
public MMultiCoreParser( InputStream in, OutputStream out, Class parserClass, String charset )
throws IOException
{
this.in = in;
this.out = out;
this.parserClass = parserClass;
this.charset = charset;

init();
}
//--------------------------------------------------------------------------------
public MMultiCoreParser( Class parserClass )
throws IOException
{
this.in = System.in;
this.out = System.out;
this.parserClass = parserClass;
this.charset = "ISO-8859-1";

init();
}
//--------------------------------------------------------------------------------
public void setThreadCount( int i )
{
threadCount = i;
}
//--------------------------------------------------------------------------------
public void setBatchSize( int i )
{
if( i < 10 )
	{
	//too small. igored
	return;
	}
reader.setBatchSize( i );
}
//--------------------------------------------------------------------------------
private void init()
throws IOException
{
reader = new MReader( in, charset );
writer = new MWriter( out, charset );
}
//--------------------------------------------------------------------------------
public void execute()
{
try
	{
	MThreadPool tp = new MThreadPool( threadCount );
	for( int i = 0; i < threadCount; ++i )
		{
		tp.addCommand( new MParseCommand( ( MParser )parserClass.newInstance(), reader, writer ) );
		}
	tp.slowStop();
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
}
//--------------------------------------------------------------------------------
}