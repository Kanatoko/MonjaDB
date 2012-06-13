package net.jumperz.util;

import java.lang.reflect.*;
import java.util.*;
import net.jumperz.util.*;

public class MSyncObserver
implements MObserver2
{
private Map methodMap = new HashMap();
private Object instance;
//--------------------------------------------------------------------------------
public MSyncObserver( Object instance )
{
init( instance );
}
//--------------------------------------------------------------------------------
public void update( Object eventName, Object source )
{
try
	{
	if( methodMap.containsKey( eventName ) )
		{
		Method method = ( Method )methodMap.get( eventName );
		method.invoke( instance, new Object[]{ source } );
		}
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
}
//--------------------------------------------------------------------------------
public void init( Object instance )
{
this.instance = instance;
Class c = instance.getClass();

Method[] methodArray = c.getDeclaredMethods();
for( int i = 0; i < methodArray.length; ++i )
	{
	Method method = methodArray[ i ];
	String methodName = method.getName();
	methodMap.put( methodName, method );
	}
}
//--------------------------------------------------------------------------------
}