package net.jumperz.util;

import java.sql.*;
import java.util.*;

public class MJSMap
extends TreeMap
implements MJSObject
{
private static final String TAB = "\t";
public static String charset = "Shift_JIS";
public static boolean debug = false;
// --------------------------------------------------------------------------------
public MJSMap( Comparator c )
{
super( c );
}
// --------------------------------------------------------------------------------
public MJSMap()
{
}
// --------------------------------------------------------------------------------
public String toString( int spaceLength  )
{
return toString( spaceLength, charset );
}
// --------------------------------------------------------------------------------
public String toString( int spaceLength, String _charset )
{
StringBuffer spaceBuf = new StringBuffer();
for( int i = 0; i < spaceLength; ++i )
	{
	spaceBuf.append( TAB );
	}
String s = spaceBuf.toString();
StringBuffer buf = new StringBuffer( 256 );
buf.append( "\n" );
buf.append( s );
buf.append( "{" );
Iterator p = this.keySet().iterator();
boolean first = true;
while( p.hasNext() )
	{
	if( !first )
		{
		buf.append( "," );
		}
	buf.append( "\n" );
	buf.append( s );

	String key = ( String )p.next();
	Object value = this.get( key );
	buf.append( "'" );
	buf.append( key );
	buf.append( "'" );
	buf.append( ":" );
	
	if( value instanceof String )
		{
			//timestampと思われる場合はJavaScriptのDateオブジェクトにする
		String valueStr = ( String )value;
		String pattern = "^([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}).*\\+09$";
		String matchStr =  MRegEx.getMatch( pattern, valueStr );
		if( matchStr.length() > 0 )
			{
			Timestamp ts = Timestamp.valueOf( matchStr );
			buf.append( "new Date( '" + ts.toGMTString() + "')" );
			}
		else
			{
			buf.append( "'" );
			if( debug )
				{
				//value = ( ( String )value ).replaceAll( "\\\\", "\\\\u005C" );
				value = ( ( String )value ).replaceAll( "\r", "\\\\u000D" );
				value = ( ( String )value ).replaceAll( "\n", "\\\\u000A" );
				value = ( ( String )value ).replaceAll( "<", "\\\\u003C" );
				value = ( ( String )value ).replaceAll( ">", "\\\\u003E" );
				value = ( ( String )value ).replaceAll( "'", "\\\\u0027" );
				value = ( ( String )value ).replaceAll( "\"", "\\\\u0022" );
				buf.append( value );
				}
			else
				{
				buf.append( MStringUtil.JavaScriptEncode2( ( String )value, _charset ) );		
				}		
			buf.append( "'" );	
			}
		}
	else if( value instanceof Timestamp )
		{
		buf.append( "new Date( '" + ( ( Timestamp )value ).toGMTString() + "')" );
		}
	else if( value instanceof java.util.Date )
		{
		java.util.Date td = ( java.util.Date )value;
		Timestamp ts = new Timestamp( td.getTime() );
		buf.append( "new Date( '" + ts.toGMTString() + "')" );
		}
	else if( value instanceof MJSObject )
		{
		buf.append( ( (  MJSObject )value ) .toString( spaceLength + 1 ) );		
		}
	else
		{
		buf.append( "'" );
		buf.append( value.toString() );
		buf.append( "'" );
		}
	first = false;
	}
buf.append( "\n" );
buf.append( s );
buf.append( "}" );

return buf.toString();
}
// --------------------------------------------------------------------------------
public String toString()
{
return toString( 0 );
}
// --------------------------------------------------------------------------------
}