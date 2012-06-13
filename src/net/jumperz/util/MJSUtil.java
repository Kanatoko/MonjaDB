package net.jumperz.util;

import java.util.*;
import javax.script.*;
import net.jumperz.net.*;

public class MJSUtil
{
//--------------------------------------------------------------------------------
public static ScriptEngine getEngine()
{
ScriptEngineManager manager = new ScriptEngineManager();
ScriptEngine engine = manager.getEngineByName( "js" );
return engine;
}
//--------------------------------------------------------------------------------
public static Map getMap( Object o, Object key1 )
{
Map map = ( Map )o;
if( map == null )
	{
	return new HashMap();
	}
else
	{
	if( map.containsKey( key1 ) )
		{
		return ( Map )map.get( key1 );	
		}
	else
		{
		return new HashMap();
		}
	}
}
//--------------------------------------------------------------------------------
public static Map getMap( Object o, Object key1, Object key2 )
{
Map map = getMap( o, key1 );
return ( Map )map.get( key2 );
}
//--------------------------------------------------------------------------------
public static List getList( Object o, Object key1, Object key2 )
{
return ( List )getMap( o, key1 ).get( key2 );
}
//--------------------------------------------------------------------------------
public static List getList( Object o, Object key1 )
{
Map map = ( Map )o;
return ( List )map.get( key1 );
}
// --------------------------------------------------------------------------------
public static int getInt( Object o, Object key1, Object key2 )
{
return ( ( Integer )get( o, key1, key2 ) ).intValue();
}
// --------------------------------------------------------------------------------
public static int getInt( Object o, Object key1, Object key2, Object key3 )
{
return ( ( Integer )get( o, key1, key2, key3 ) ).intValue();
}
// --------------------------------------------------------------------------------
public static String getString( Object o, Object key1 )
{
Map m = ( Map )o;
if( m == null )
	{
	return "";
	}
else
	{
	if( m.containsKey( key1 ) )
		{
		return ( String )m.get( key1 );
		}
	else
		{
		return "";
		}	
	}
}
// --------------------------------------------------------------------------------
public static String getString( Object o, Object key1, Object key2 )
{
String str = ( String )get( o, key1, key2 );
if( str == null )
	{
	str = "";
	}
return str;
}
// --------------------------------------------------------------------------------
public static String getString( Object o, Object key1, Object key2, Object key3 )
{
return ( String )get( o, key1, key2, key3 );
}
// --------------------------------------------------------------------------------
public static Object get( Object o, Object key1, Object key2, Object key3 )
{
return ( ( Map )get( o, key1, key2 ) ).get( key3 );
}
// --------------------------------------------------------------------------------
public static Object get( Object o, Object key1, Object key2 )
{
try
	{
	return ( ( Map )( ( ( Map )o ).get( key1 ) ) ).get( key2 );
	}
catch( NullPointerException e )
	{
	e.printStackTrace();
	System.out.println( o + " " + key1 + " " + key2 );
	return null;
	}
}
//--------------------------------------------------------------------------------
}