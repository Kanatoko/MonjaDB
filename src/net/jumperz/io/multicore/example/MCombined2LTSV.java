package net.jumperz.io.multicore.example;

import net.jumperz.io.multicore.*;

public class MCombined2LTSV
implements MParser
{
//--------------------------------------------------------------------------------
public String parse( String line )
{
StringBuffer buf = new StringBuffer( 512 );

int index;

	//host
index = line.indexOf( ' ' );
buf.append( "host:" );
buf.append( line.substring( 0, index ) );
buf.append( '\t' );
line = line.substring( index + 1 );

	//ident
index = line.indexOf( ' ' );
buf.append( "ident:" );
buf.append( line.substring( 0, index ) );
buf.append( '\t' );
line = line.substring( index + 1 );

	//user
index = line.indexOf( ' ' );
buf.append( "user:" );
buf.append( line.substring( 0, index ) );
buf.append( '\t' );
line = line.substring( index + 1 );

	//time
index = line.indexOf( ']' );
buf.append( "time:" );
buf.append( line.substring( 0, index + 1 ) );
buf.append( '\t' );
line = line.substring( index + 3 ); //eat space and double quote

	//req
index = getNextIndex( line );
buf.append( "req:" );
buf.append( line.substring( 0, index ) );
buf.append( '\t' );
line = line.substring( index + 2 );

	//status
index = line.indexOf( ' ' );
buf.append( "status:" );
buf.append( line.substring( 0, index ) );
buf.append( '\t' );
line = line.substring( index + 1 );

	//size
index = line.indexOf( ' ' );
if( index == -1 )
	{
		//common
	buf.append( "size:" );
	buf.append( line );
	}
else
	{
		//combined
	
		//size
	index = line.indexOf( ' ' );
	buf.append( "size:" );
	buf.append( line.substring( 0, index ) );
	buf.append( '\t' );
	line = line.substring( index + 2 );
	
		//referer
	index = getNextIndex( line );
	buf.append( "referer:" );
	buf.append( line.substring( 0, index ) );
	buf.append( '\t' );
	line = line.substring( index + 3 );

		//ua
	index = getNextIndex( line );
	buf.append( "ua:" );
	buf.append( line.substring( 0, index ) );
	//line = line.substring( index + 1 );
	}

return buf.toString();
}
//--------------------------------------------------------------------------------
public static int getNextIndex( String line )
{
boolean escape = false;
char c;
for( int index = 0;; ++index )
	{
	c = line.charAt( index );
	if( escape )
		{
		escape = false;
		}
	else
		{
		if( c == '\\' )
			{
			escape = true;
			}
		if( c == '"' )
			{
			return index;
			}
		}
	}
}
//--------------------------------------------------------------------------------
}