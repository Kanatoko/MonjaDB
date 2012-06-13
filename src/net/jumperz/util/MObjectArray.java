package net.jumperz.util;

import java.util.*;

public class MObjectArray
{
List list = new ArrayList();
//--------------------------------------------------------------------------------
public MObjectArray()
{
}
//--------------------------------------------------------------------------------
public MObjectArray( long l )
{
add( l );
}
//--------------------------------------------------------------------------------
public MObjectArray( Object o )
{
list.add( o );
}
// --------------------------------------------------------------------------------
public void removeNulls()
{
for( int i = 0; i < list.size(); ++i )
	{
	Object o = list.get( i );
	if( o == null )
		{
		list.set( i, "" );
		}
	}
}
//--------------------------------------------------------------------------------
public void add( Object o )
{
list.add( o );
}
// --------------------------------------------------------------------------------
public void add( char c )
{
list.add( new Character( c ) );
}
//--------------------------------------------------------------------------------
public void add( int i )
{
list.add( new Integer( i ) );
}
//--------------------------------------------------------------------------------
public void add( long l )
{
list.add( new Long( l ) );
}
//--------------------------------------------------------------------------------
public void add( boolean b )
{
list.add( new Boolean( b ) );
}
//--------------------------------------------------------------------------------
public List toList()
{
return list;
}
// --------------------------------------------------------------------------------
public String toString()
{
StringBuffer buf = new StringBuffer( 256 );
for( int i = 0; i < list.size(); ++i )
	{
	buf.append( list.get( i ) );
	buf.append( "," );
	}
return buf.toString();
}
//--------------------------------------------------------------------------------
public String toQues()
{
if( list.size() == 0 )
	{
	return "";
	}

StringBuffer strBuf = new StringBuffer();
strBuf.append( "?" );

for( int i = 1; i < list.size(); ++i )
	{
	strBuf.append( ",?" );
	}

return strBuf.toString();
}
//--------------------------------------------------------------------------------
}