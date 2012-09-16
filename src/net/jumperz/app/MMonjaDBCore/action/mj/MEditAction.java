package net.jumperz.app.MMonjaDBCore.action.mj;

import java.text.Collator;
import java.util.*;

import org.bson.types.ObjectId;

import com.mongodb.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.action.MAbstractAction;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MEditAction
extends MAbstractAction
{
private String _idStr;
private Object _idObj;
private String action;
//--------------------------------------------------------------------------------
public MEditAction()
{
}
//--------------------------------------------------------------------------------
public String getAction()
{
return action;
}
//--------------------------------------------------------------------------------
public Object getIdAsObject()
{
return _idObj;
}
//--------------------------------------------------------------------------------
public String getIdAsString()
{
return _idStr;
}
//--------------------------------------------------------------------------------
public boolean parse( String action )
{
this.action = action;
_idStr = MRegEx.getMatch( "mj edit ([-_\\./0-9a-zA-Z]+)$", action );
if( _idStr.length() > 0 )
	{
	try
		{
		_idObj = new ObjectId( _idStr );
		}
	catch( Exception e )
		{
		_idObj = _idStr;
		}
	return true;
	}
else
	{
	return false;
	}
}
//--------------------------------------------------------------------------------
public void executeFunction()
throws Exception
{
}
//--------------------------------------------------------------------------------
public int getActionCondition()
{
return action_cond_collection;
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_mj_edit;
}
//--------------------------------------------------------------------------------
}