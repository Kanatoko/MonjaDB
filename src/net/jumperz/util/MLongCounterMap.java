package net.jumperz.util;

import java.util.*;

public class MLongCounterMap
{
private Map data = new HashMap();
//--------------------------------------------------------------------------------
public synchronized Set keySet()
{
return data.keySet();
}
//--------------------------------------------------------------------------------
public synchronized boolean containsKey( String key )
{
return data.containsKey( key );
}
// --------------------------------------------------------------------------------
public synchronized void add( String key, long l )
{
MLongCounter counter = null;
if( data.containsKey( key ) )
	{
	counter = ( MLongCounter )data.get( key );
	}
else
	{
	counter = new MLongCounter();
	data.put( key, counter );
	}

counter.add( l );
}
// --------------------------------------------------------------------------------
public synchronized void sub( String key, long l )
{
MLongCounter counter = null;
if( data.containsKey( key ) )
	{
	counter = ( MLongCounter )data.get( key );
	}
else
	{
	counter = new MLongCounter();
	data.put( key, counter );
	}

counter.sub( l );
}
// --------------------------------------------------------------------------------
public synchronized long getValue( String key, boolean reset )
{
long value = 0;
MLongCounter counter = null;
if( data.containsKey( key ) )
	{
	counter = ( MLongCounter )data.get( key );
	value = counter.getValue();
	
	if( reset )
		{
		counter.reset();
		}
	}
return value;
}
// --------------------------------------------------------------------------------
public synchronized void setValue( String key, long value )
{
MLongCounter counter = null;
if( data.containsKey( key ) )
	{
	counter = ( MLongCounter )data.get( key );
	}
else
	{
	counter = new MLongCounter();
	data.put( key, counter );
	}

counter.setValue( value );
}
// --------------------------------------------------------------------------------
public synchronized long getValue( String key )
{
return getValue( key, false );
}
// --------------------------------------------------------------------------------
public String toString()
{
return "MLongCounter:" + data ;
}
// --------------------------------------------------------------------------------
}