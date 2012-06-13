package net.jumperz.mongo;

import com.mongodb.*;
import java.util.*;

import net.jumperz.util.MStringUtil;

public class MFindQuery
extends BasicDBObject
implements Cloneable
{
private String origStr;
private String collName;
//--------------------------------------------------------------------------------
public Object clone()
{
MFindQuery fq = new MFindQuery( origStr, new HashMap( this.toMap() ) );
fq.setCollName( collName );
return fq;
}
//--------------------------------------------------------------------------------
public void setCollName( String s )
{
collName = s;
}
//--------------------------------------------------------------------------------
public String getCollName()
{
return collName;
}
//--------------------------------------------------------------------------------
public MFindQuery( String s, Map m )
{
super( m );
origStr = s;
}
//--------------------------------------------------------------------------------
public void setLimitArg( int i )
{
put( "limitArg", new Integer( i ) );
if( getFindArg().size() >= 3 )
	{
	getFindArg().set( 2, new Integer( i ) );
	}
if( i <= 0 )
	{
	getInvokedFunctionNameList().remove( "limit" );	
	}
else
	{
	if( !getInvokedFunctionNameList().contains( "limit" ) )
		{
		getInvokedFunctionNameList().add( "limit" );	
		}
	}
}
//--------------------------------------------------------------------------------
public void setSkipArg( int i )
{
put( "skipArg", new Integer( i ) );
if( getFindArg().size() >= 4 )
	{
	getFindArg().set( 3, new Integer( i ) );
	}
if( i <= 0 )
	{
	getInvokedFunctionNameList().remove( "skip" );	
	}
else
	{
	if( !getInvokedFunctionNameList().contains( "skip" ) )
		{
		getInvokedFunctionNameList().add( "skip" );	
		}
	}
}
//--------------------------------------------------------------------------------
public List getFindArg()
{
return ( List )this.get( "findArg" );
}
//--------------------------------------------------------------------------------
public int getLimitArg()
{
int _limit = _getInt( "limitArg" );
if( _limit == -1 )
	{
	List findArg = getFindArg();
	if( findArg.size() >= 3 )
		{
		_limit = MStringUtil.parseInt( findArg.get( 2 ) );
		}
	}
return _limit;
}
//--------------------------------------------------------------------------------
private int _getInt( Object key )
{
if( this.containsKey( key ) )
	{
	Object value = this.get( key );
	if( value instanceof Double )
		{
		return ( ( Double )value ).intValue();
		}
	else if( value instanceof Integer )
		{
		return ( ( Integer )value ).intValue();		
		}
	else
		{
		return MStringUtil.parseInt( value );
		}
	}
else
	{
	return -1;
	}
}
//--------------------------------------------------------------------------------
public int getSkipArg()
{
int _skip = _getInt( "skipArg" );
if( _skip == -1 )
	{
	List findArg = getFindArg();
	if( findArg.size() == 4 )
		{
		_skip = MStringUtil.parseInt( findArg.get( 3 ) );
		}
	}
return _skip;
}
//--------------------------------------------------------------------------------
public BasicDBObject getSortArg()
{
if( this.containsKey( "sortArg" ) )
	{
	return ( BasicDBObject )get( "sortArg" );
	}
else
	{
	return new BasicDBObject();
	}
}
//--------------------------------------------------------------------------------
public List getInvokedFunctionNameList()
{
return ( List )this.get( "invoked" );
}
//--------------------------------------------------------------------------------
public void skip( int i )
{

}
//--------------------------------------------------------------------------------
}
