package net.jumperz.util;

import java.io.*;
import java.util.*;
import java.beans.*;
import java.net.*;

public class MProperties
{
private static final String magic = "d9a3fdfc7ca17c47ed007bed5d2eb873";
private Map keyValueMap = new HashMap();

private static final String INT_ARRAY = "_intArray_";
// --------------------------------------------------------------------------------
public boolean containsKey( String key )
{
return keyValueMap.containsKey( key );
}
//--------------------------------------------------------------------------------
public void load( InputStream in )
throws IOException
{
BufferedReader reader = new BufferedReader( new InputStreamReader( in, MCharset.CS_ISO_8859_1 ) );
String line = null;
try
	{
	while( true )
		{
		line = reader.readLine();
		if( line == null )
			{
			break;
			}
		if( line.indexOf( "#" )  != 0
		 && line.indexOf( "//" ) != 0
		  )
			{
			int index = line.indexOf( "=" );
			if( index != -1 )
				{
				String key = line.substring( 0, index );
				String value = line.substring( index + 1 );
				if( key.indexOf( "_list_" + magic ) == 0 )
					{
					key = key.substring( ( "_list_" + magic ).length() );
					keyValueMap.put( key, loadList( value ) );
					}
				else if( key.indexOf( INT_ARRAY + magic ) == 0 )
					{
					key = key.substring( ( INT_ARRAY + magic ).length() );
					keyValueMap.put( key, loadIntArray( value ) );				
					}
				else
					{				
					keyValueMap.put( key, value );
					}
				}
			}
		}
	}
finally
	{
	reader.close();
	}
}
//--------------------------------------------------------------------------------
private int[] loadIntArray( String value )
throws IOException
{
String[] sArray = value.split( "," );
int[] array = new int[ sArray.length ];
for( int i = 0; i < sArray.length; ++i )
	{
	array[ i ] = MStringUtil.parseInt( sArray[ i ] );
	}
return array;
}
// --------------------------------------------------------------------------------
private List loadList( String value )
throws IOException
{
List l = new ArrayList();

String s = URLDecoder.decode( value, MCharset.CS_ISO_8859_1 );
ByteArrayInputStream in = new ByteArrayInputStream( s.getBytes( MCharset.CS_ISO_8859_1 ) );
XMLDecoder decoder = new XMLDecoder( in );
while( true )
	{
	try
		{
		l.add( decoder.readObject() );		
		}
	catch( ArrayIndexOutOfBoundsException e )
		{
		break;
		}
	}
return l;
}
//--------------------------------------------------------------------------------
private void writeIntArray( Object key, int[] array, OutputStream out )
throws IOException
{
StringBuffer buf = new StringBuffer();
for( int i = 0; i < array.length; ++i )
	{
	if( i > 0 )
		{
		buf.append( ',' );
		}
	buf.append( array[ i ] );
	}
store( INT_ARRAY + magic + key, buf.toString(), out );
}
// --------------------------------------------------------------------------------
public void store( OutputStream out )
throws IOException
{
Iterator p = keyValueMap.keySet().iterator();
Class intArrayClass = ( new int[]{} ).getClass();
Class listClass = List.class;
while( p.hasNext() )
	{
	Object key = p.next();
	Object value = keyValueMap.get( key );
	
	if( intArrayClass.isInstance( value ) )
		{
		int[] array = ( int[] )value;
		writeIntArray( key, ( int[] )value, out );
		}
	else if( listClass.isInstance( value ) )
		{
		List l = ( List )value;
		writeList( key, l, out );
		}
	else
		{
		store( key, value, out );
		}
	}
}
// --------------------------------------------------------------------------------
private void store( Object key, Object value, OutputStream out )
throws IOException
{
out.write( key.toString().getBytes( MCharset.CS_ISO_8859_1 ) );
out.write( "=".getBytes() );
out.write( value.toString().getBytes( MCharset.CS_ISO_8859_1 ) );
out.write( "\n".getBytes() );
}
// --------------------------------------------------------------------------------
private void writeList( Object key, List l, OutputStream out )
throws IOException
{
int count = l.size();
ByteArrayOutputStream buf = new ByteArrayOutputStream();
XMLEncoder e1 = new XMLEncoder( buf );
for( int i = 0; i < count; ++i )
	{
	Object o = l.get( i );
	e1.writeObject( o );
	}
//oos.writeInt( count );
e1.close();

String s = MStreamUtil.streamToString( new ByteArrayInputStream( buf.toByteArray() ) );
s = URLEncoder.encode( s, MCharset.CS_ISO_8859_1 );

store( "_list_" + magic + key, s, out );

/*
ObjectOutputStream oos = new ObjectOutputStream( bufs );
for( int i = 0; i < count; ++i )
	{
	Object o = l.get( i );
	oos.writeObject( o );
	}
//oos.writeInt( count );
oos.close();

String value = MStringUtil.byteToHexString( bufs.toByteArray() );

*/
}
// --------------------------------------------------------------------------------
public void setProperty( String key, List l )
{
keyValueMap.put( key, l );
}
// --------------------------------------------------------------------------------
public List getListProperty( String key )
{
if( keyValueMap.containsKey( key ) )
	{
	try
		{
		return ( List )keyValueMap.get( key );
		}
	catch( Exception e )
		{
		return new ArrayList();
		}
	}
else
	{
	return new ArrayList();
	}
}
//--------------------------------------------------------------------------------
public int[] getIntArrayProperty( String key )
{
return ( int[] )keyValueMap.get( key );
}
// --------------------------------------------------------------------------------
public void setProperty( String key, int[] intArray )
{
keyValueMap.put( key, intArray );
}
// --------------------------------------------------------------------------------
public boolean getBooleanProperty( String key )
{
return MStringUtil.meansTrue( getProperty( key ) );
}
// --------------------------------------------------------------------------------
public boolean getBooleanProperty( String key, boolean defaultValue )
{
if( keyValueMap.containsKey( key ) )
	{
	return getBooleanProperty( key );
	}
else
	{
	return defaultValue;
	}
}
//--------------------------------------------------------------------------------
public String getProperty( String key )
{
if( keyValueMap.containsKey( key ) )
	{
	return ( String )keyValueMap.get( key );
	}
else
	{
	return "";
	}
}
// --------------------------------------------------------------------------------
public int getIntProperty( String key )
{
try
	{
	return Integer.parseInt( ( String )keyValueMap.get( key ) );
	}
catch( NumberFormatException e )
	{
	return -1;
	}
}
// --------------------------------------------------------------------------------
public int getIntProperty( String key, int defaultValue )
{
if( keyValueMap.containsKey( key ) )
	{
	try
		{
		return Integer.parseInt( ( String )keyValueMap.get( key ) );
		}
	catch( NumberFormatException e )
		{
		return defaultValue;
		}
	}
else
	{
	return defaultValue;
	}
}
// --------------------------------------------------------------------------------
public String getProperty( String key, int defaultValue )
{
if( keyValueMap.containsKey( key ) )
	{
	return ( String )keyValueMap.get( key );
	}
else
	{
	return Integer.toString( defaultValue );
	}
}
//--------------------------------------------------------------------------------
public String getProperty( String key, String defaultValue )
{
if( keyValueMap.containsKey( key ) )
	{
	return ( String )keyValueMap.get( key );
	}
else
	{
	return defaultValue;
	}
}
// --------------------------------------------------------------------------------
public void setProperty( String key, String value )
{
keyValueMap.put( key, value );
}
// --------------------------------------------------------------------------------
public void setProperty( String key, int value )
{
keyValueMap.put( key, Integer.toString( value ) );
}
// --------------------------------------------------------------------------------
public void setProperty( String key, boolean value )
{
if( value )
	{
	setProperty( key, "true" );
	}
else
	{
	setProperty( key, "false" );
	}
}
//--------------------------------------------------------------------------------
public Set getKeySet()
{
return keyValueMap.keySet();
}
//--------------------------------------------------------------------------------
public String toString()
{
return keyValueMap.toString();
}
//--------------------------------------------------------------------------------
}