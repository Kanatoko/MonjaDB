package net.jumperz.app.MMonjaDBCore.action;

import java.util.*;
import com.mongodb.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MUseAction
extends MAbstractAction
{
private String action;
private String dbName;
//--------------------------------------------------------------------------------
public MUseAction()
{

}
//--------------------------------------------------------------------------------
public boolean parse( String action )
{
if( MRegEx.containsIgnoreCase( action, "^use\\s+." ) )
	{
	this.action = action;
	return true;
	}
return false;
}
//--------------------------------------------------------------------------------
public String getDBName()
{
return dbName;
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_use;
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
dbName = MRegEx.getMatchIgnoreCase( "^use\\s+(.*)$", action );
/*
debug( dbName );
debug( action );
DB db = dataManager.getMongo().getDB( dbName );
//dataManager.setDB( db );
*/
}
//--------------------------------------------------------------------------------
}