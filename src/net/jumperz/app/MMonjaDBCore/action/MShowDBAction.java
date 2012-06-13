package net.jumperz.app.MMonjaDBCore.action;

import java.util.*;
import com.mongodb.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MShowDBAction
extends MAbstractAction
{
private List dbList;
//private Mongo mongo;
//--------------------------------------------------------------------------------
public MShowDBAction()
{
//setContext( dataManager.getMongo() );
}
//--------------------------------------------------------------------------------
public boolean parse( String action )
{
if( MRegEx.containsIgnoreCase( action, "^show\\s+dbs$" ) )
	{
	return true;
	}
return false;
}
/*
//--------------------------------------------------------------------------------
public void setContextImpl( Object context )
{
mongo = ( Mongo )context;
}
*/
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_showdbs;
}
//--------------------------------------------------------------------------------
public int getActionCondition()
{
return action_cond_connected;
}
//--------------------------------------------------------------------------------
public void executeFunction()
throws Exception
{
dbList = dataManager.getMongo().getDatabaseNames();
}
//--------------------------------------------------------------------------------
public List getDBList()
{
return dbList;
}
//--------------------------------------------------------------------------------
}