package net.jumperz.app.MMonjaDBCore.action;

import java.util.*;
import com.mongodb.*;
import com.mongodb.util.*;

import net.jumperz.mongo.MMongoUtil;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MRemoveAction
extends MAbstractAction
{
private String action;
private DB db;
private boolean used = false;
private DBCollection coll;
private WriteResult writeResult;
//--------------------------------------------------------------------------------
public MRemoveAction()
{
}
//--------------------------------------------------------------------------------
public WriteResult getResult()
{
return writeResult;
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_remove;
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
db.eval( action, null );
}
//--------------------------------------------------------------------------------
}
