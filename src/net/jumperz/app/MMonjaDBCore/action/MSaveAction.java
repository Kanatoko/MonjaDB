package net.jumperz.app.MMonjaDBCore.action;

import java.util.*;
import com.mongodb.*;
import com.mongodb.util.*;

import net.jumperz.mongo.MMongoUtil;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MSaveAction
extends MAbstractAction
{
private String action;
private DB db;
private boolean used = false;
private DBCollection coll;
//--------------------------------------------------------------------------------
public MSaveAction()
{
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_save;
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
	this.action = action;
	return true;
	}
catch( Exception e )
	{
	debug( e );
	return false;
	}
}
//-----------------------------------------------------------------------------
public void executeFunction()
throws Exception
{
db = MDataManager.getInstance().getDB();

try
	{
	db.eval( action, null );
	}
catch( MongoException e )
	{
	MEventManager.getInstance().fireErrorEvent( e );
	}
}
//--------------------------------------------------------------------------------
}
