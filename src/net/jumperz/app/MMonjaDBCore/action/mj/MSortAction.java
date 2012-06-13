package net.jumperz.app.MMonjaDBCore.action.mj;

import java.text.Collator;
import java.util.*;
import com.mongodb.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.action.MAbstractAction;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MSortAction
extends MAbstractAction
{
private String action;
//--------------------------------------------------------------------------------
public MSortAction()
{
}
//--------------------------------------------------------------------------------
public boolean parse( String action )
{
this.action = action;
if( action.matches( "mj sort by .*") )
	{
	return true;
	}
else
	{
	return false;
	}
}
//--------------------------------------------------------------------------------
private double toDouble( Object value )
{
if( value instanceof Integer )
	{
	Integer intval = ( Integer )value;
	return ( new Double( intval.intValue() ) ).doubleValue();
	}
else if( value instanceof Double )
	{
	return ( ( Double )value ).doubleValue();
	}
else
	{
	if( value.toString().matches( "^[0-9\\.]+$" ) )
		{
		return Double.parseDouble( value.toString() );
		}
	else
		{
		return 0;
		}
	}
}
//--------------------------------------------------------------------------------
public void executeFunction()
throws Exception
{
final String columnName = MRegEx.getMatch( "mj sort by ([^ ]+) ", action );
final int _sortOrder = MStringUtil.parseInt( MRegEx.getMatch( ".* (1|-1)$", action ) );

Comparator c = new Comparator(){/**************/
public int compare( Object o1, Object o2 )
{
Map map1 = ( Map )o1;
Map map2 = ( Map )o2;

Object value1 = map1.get( columnName );
Object value2 = map2.get( columnName );

if( value1 == null && value2 == null )
	{
	return 0;
	}
else if( value1 == null )
	{
	return -1 * _sortOrder;
	}
else if( value2 == null )
	{
	return 1 * _sortOrder;
	}

//debug( value1.getClass() );
//debug( value2.getClass() );


if( value1 == null || value2 == null )
	{
	return 0;
	}

if( ( value1 instanceof Integer || value1 instanceof Double )
 && ( value2 instanceof Integer || value2 instanceof Double ) 
  )
	{
	double double1 = toDouble( value1 );
	double double2 = toDouble( value2 );
	if( double1 > double2 )
		{
		return 1 * _sortOrder;
		}
	else if( double1 == double2 )
		{
		return 0;
		}
	else
		{
		return -1 * _sortOrder;
		}
	}

String str1 = value1.toString();
String str2 = value2.toString();
Collator collator = Collator.getInstance( Locale.getDefault() );
return collator.compare( str1, str2 ) * _sortOrder;
}
public boolean equals( Object o1, Object o2 )
{
return o1.equals( o2 );
}
};/***************/

Collections.sort( dataManager.getDocumentDataList(), c );
}
//--------------------------------------------------------------------------------
public int getActionCondition()
{
return action_cond_collection;
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_mj_sort;
}
//--------------------------------------------------------------------------------
}