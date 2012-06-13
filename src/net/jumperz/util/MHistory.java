package net.jumperz.util;

import java.util.*;

public class MHistory
{
private int pos = -1;
private LinkedList list = new LinkedList();
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
MHistory h = new MHistory();
if( !h.isEmpty() ){ throw new Exception(); }
if( !h.atBegin() ){ throw new Exception(); }
if( !h.atEnd() ){ throw new Exception(); }

h.add( "hoge" );
if( !h.current().equals( "hoge" ) ){ throw new Exception(); }

h.add( "fuga" );
if( !h.current().equals( "fuga" ) ){ throw new Exception(); }

h.add( "gyoe" );
if( !h.current().equals( "gyoe" ) ){ throw new Exception(); }

h.back();
if( !h.current().equals( "fuga" ) ){ throw new Exception(); }
if( !h.next().equals( "gyoe" ) ){ throw new Exception(); }
if( !h.prev().equals( "hoge" ) ){ throw new Exception(); }

h.back();
if( !h.current().equals( "hoge" ) ){ throw new Exception(); }
if( !h.next().equals( "fuga" ) ){ throw new Exception(); }
if( h.prev() != null ){ throw new Exception(); }

h.add( "aaa" );
if( !h.current().equals( "aaa" ) ){ throw new Exception(); }
if( !h.prev().equals( "hoge" ) ){ throw new Exception(); }

h.add( "bbb" );
h.add( "bbb" );
if( !h.current().equals( "bbb" ) ){ throw new Exception(); }
if( !h.prev().equals( "aaa" ) ){ throw new Exception(); }

h.back();
if( !h.current().equals( "aaa" ) ){ throw new Exception(); }

h.forward();
if( !h.current().equals( "bbb" ) ){ throw new Exception(); }

p( h );
p( "===OK===" );
}
//--------------------------------------------------------------------------------
public static void p( Object o )
{
System.out.println( o );
}
//--------------------------------------------------------------------------------
public int getPos()
{
return pos;
}
//--------------------------------------------------------------------------------
public String toString()
{
StringBuffer buf = new StringBuffer();
buf.append( list );
buf.append( " pos=" );
buf.append( pos );
buf.append( " " );
if( pos >= 0 )
	{
	buf.append( current() );
	}
return buf.toString();
}
//--------------------------------------------------------------------------------
public boolean isEmpty()
{
return pos == -1;
}
//--------------------------------------------------------------------------------
public List getList()
{
return list;
}
//--------------------------------------------------------------------------------
public synchronized Object current()
{
return list.get( pos );
}
//--------------------------------------------------------------------------------
public boolean atEnd()
{
if( pos == -1 )
	{
	return true;
	}
else
	{
	return ( list.size() == pos + 1 );
	}
}
//--------------------------------------------------------------------------------
public boolean atBegin()
{
if( pos == -1 )
	{
	return true;
	}
else
	{
	return pos == 0;
	}
}
//--------------------------------------------------------------------------------
public synchronized Object prev()
{
if( atBegin() )
	{
	return null;
	}
else
	{
	return list.get( pos - 1 );
	}
}
//--------------------------------------------------------------------------------
public synchronized Object next()
{
if( atEnd() )
	{
	return null;
	}
else
	{
	return list.get( pos + 1 );
	}
}
//--------------------------------------------------------------------------------
public synchronized void forward()
{
if( atEnd() )
	{
	//noop
	}
else
	{
	++pos;
	}
}
//--------------------------------------------------------------------------------
public synchronized void back()
{
if( pos <= 0 )
	{
	//noop
	}
else
	{
	--pos;
	}
}
//--------------------------------------------------------------------------------
public synchronized void add( Object newLocation )
{
Object current = null;
if( list.size() == 0 )
	{
	list.add( newLocation );
	++pos;
	}
else if( list.size() == pos + 1 ) //at end
	{
	current = list.get( pos );
	if( !newLocation.equals( current ) )
		{
		list.add( newLocation );
		++pos;
		}
	}
else
	{
	current = list.get( pos );
	if( !newLocation.equals( current ) )
		{
		LinkedList newList = new LinkedList( list.subList( 0, pos + 1 ) );
		list = newList;
		list.add( newLocation );
		++pos;
		}
	}
}
//--------------------------------------------------------------------------------
}