package net.jumperz.app.MMonjaDBCore.action;

import java.util.*;
import com.mongodb.*;
import com.mongodb.util.*;

import net.jumperz.mongo.MMongoUtil;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MUpdateAction
extends MAbstractAction
{
private String action;
private DB db;
private boolean used = false;
private DBCollection coll;
//--------------------------------------------------------------------------------
public MUpdateAction()
{
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_update;
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
	//MMongoUtil.getListFromAction( MMongoUtil.getArgStrFromAction( action, "update" ), "update" );
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
db.eval( action, null );
}
//--------------------------------------------------------------------------------
}
