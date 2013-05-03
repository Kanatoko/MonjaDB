package net.jumperz.io.multicore;

import net.jumperz.util.*;
import java.io.*;
import java.util.*;

public class MParseCommand
implements MCommand
{
private MParser parser;
private MReader reader;
private MWriter writer;
//--------------------------------------------------------------------------------
public MParseCommand( MParser parser, MReader reader, MWriter writer )
{
this.parser = parser;
this.reader = reader;
this.writer = writer;
}
//--------------------------------------------------------------------------------
public void execute()
{
while( true )
	{
	try
		{
		if( execute2() )
			{
			break;
			}
		}
	catch( IOException e )
		{
		e.printStackTrace();
		break;
		}
	}
}
//--------------------------------------------------------------------------------
private boolean execute2()
throws IOException
{
Map data = reader.getLines();
if( data == null )
	{
	return true;
	}

int index = ( ( Integer )data.get( "index" ) ).intValue();
List list = ( List )data.get( "data" );
for( int i = 0; i < list.size(); ++i )
	{
	String line = ( String )list.get( i );
	list.set( i, parser.parse( line ) );
	}

writer.write( data );

return false;
}
//--------------------------------------------------------------------------------
public void breakCommand()
{

}
//--------------------------------------------------------------------------------
}