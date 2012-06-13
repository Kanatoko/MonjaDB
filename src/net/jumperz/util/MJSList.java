package net.jumperz.util;

import java.util.*;

public class MJSList
extends ArrayList
implements MJSObject
{
// --------------------------------------------------------------------------------
public String toString( int spaceLength )
{
StringBuffer spaceBuf = new StringBuffer();
for( int i = 0; i < spaceLength; ++i )
	{
	spaceBuf.append( " " );
	}
String s = spaceBuf.toString();

StringBuffer buf = new StringBuffer( 256 );
buf.append( "\n" );
buf.append( s );
buf.append( "[" );
for( int i = 0; i < this.size(); ++i )
	{
	if( i > 0 )
		{
		buf.append( "," );
		}
	buf.append( "\n" );
	buf.append( s );
	Object o = this.get( i );
	if( o instanceof String )
		{
		buf.append( "'" );
		buf.append( MStringUtil.JavaScriptEncode( ( String )o ) );
		buf.append( "'" );
		}
	else if( o instanceof MJSObject )
		{
		buf.append( ( ( MJSObject )o ).toString( spaceLength + 1 ) );
		}
	}
buf.append( "\n" );
buf.append( s );
buf.append( "]" );
return buf.toString();
}
// --------------------------------------------------------------------------------
public String toString()
{
return toString( 0 );
}
// --------------------------------------------------------------------------------
}