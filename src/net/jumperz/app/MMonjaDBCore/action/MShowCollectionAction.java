package net.jumperz.app.MMonjaDBCore.action;

import java.util.*;
import com.mongodb.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MShowCollectionAction
extends MAbstractAction
{
private Set collSet;
//--------------------------------------------------------------------------------
public MShowCollectionAction()
{
}
//--------------------------------------------------------------------------------
public boolean parse( String action )
{
if( MRegEx.containsIgnoreCase( action, "^show\\s+collections$" ) )
	{
	return true;
	}
return false;
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_showcollections;
}
//--------------------------------------------------------------------------------
public int getActionCondition()
{
return action_cond_db;
}
//--------------------------------------------------------------------------------
public void executeFunction()
throws Exception
{
DB db = dataManager.getDB();
//setContext( db );
collSet = db.getCollectionNames();
}
//--------------------------------------------------------------------------------
public Set getCollSet()
{
return collSet;
}
//--------------------------------------------------------------------------------
}