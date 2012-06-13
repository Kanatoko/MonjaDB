package net.jumperz.app.MMonjaDBCore.action;

import java.util.*;
import com.mongodb.*;
import com.mongodb.util.*;

import net.jumperz.mongo.MFindQuery;
import net.jumperz.mongo.MMongoUtil;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MFindAction
extends MAbstractAction
{
private String actionStr;
private DBCursor cursor;
private DB db;
private boolean used = false;
private DBCollection coll;
private MFindQuery findQuery;
//--------------------------------------------------------------------------------
public String getActionStr()
{
return actionStr;
}
//--------------------------------------------------------------------------------
public MFindAction()
{
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_find;
}
//--------------------------------------------------------------------------------
public int getActionCondition()
{
return action_cond_connected;
}
//--------------------------------------------------------------------------------
public DBCollection getCollection()
{
return coll;
}
//--------------------------------------------------------------------------------
public DB getDB()
{
return db;
}
//--------------------------------------------------------------------------------
public boolean parse( String action )
{
try
	{
	this.actionStr = action;
	return true;
	}
catch( Exception e )
	{
	debug( e );
	return false;
	}
}
//--------------------------------------------------------------------------------
public DBCursor getCursor()
{
if( used )
	{
	throw new MContextException( "The cursor has used by another object." );
	}
used = true;
return cursor;
}
//--------------------------------------------------------------------------------
public MFindQuery getFindQuery()
{
return findQuery;
}
//--------------------------------------------------------------------------------
public void executeFunction()
throws Exception
{
db = MDataManager.getInstance().getDB();
String collName = MMongoUtil.getCollNameFromAction( actionStr, "find" );
coll = db.getCollection( collName );

findQuery = MMongoUtil.parseFindQuery( db, actionStr );

BasicDBList findArgList = ( BasicDBList )findQuery.get( "findArg" );

	//check skip & limit
int skip  = (int)findQuery.getSkipArg();
int limit = (int)findQuery.getLimitArg();

if( findArgList.size() == 0 )
	{	//db.test.find()
	cursor = coll.find();
	if( skip > -1 )
		{
		cursor = cursor.skip( skip );
		}
	if( limit > -1 )
		{
		cursor = cursor.limit( limit );
		}
	}
else if( findArgList.size() == 1 )
	{
	DBObject ref = ( DBObject )findArgList.get( 0 );
	cursor = coll.find( ref );
	
	if( skip > -1 )
		{
		cursor = cursor.skip( skip );
		}
	if( limit > -1 )
		{
		cursor = cursor.limit( limit );
		}
	}
else if( findArgList.size() >= 2 )
	{
	DBObject ref = ( DBObject )findArgList.get( 0 );
	DBObject key = ( DBObject )findArgList.get( 1 );
	cursor = coll.find( ref, key );	
	if( skip > -1 )
		{
		cursor = cursor.skip( skip );
		}
	if( limit > -1 )
		{
		cursor = cursor.limit( limit );
		}
	}

if( findQuery.getInvokedFunctionNameList().contains( "sort" ) )
	{
	cursor = cursor.sort( findQuery.getSortArg() );
	}
}
//--------------------------------------------------------------------------------
}
