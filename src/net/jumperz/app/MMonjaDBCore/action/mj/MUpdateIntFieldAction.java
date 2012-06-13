package net.jumperz.app.MMonjaDBCore.action.mj;

import java.text.Collator;
import java.util.*;

import org.bson.types.ObjectId;

import com.mongodb.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.action.MAbstractAction;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MUpdateIntFieldAction
extends MAbstractAction
{
	//mj update int field test bbb.1 29
private String objectId;
private String action;
//--------------------------------------------------------------------------------
public MUpdateIntFieldAction()
{
}
//--------------------------------------------------------------------------------
public String getAction()
{
return action;
}
//--------------------------------------------------------------------------------
public boolean parse( String action )
{
this.action = action;
String[] array = action.split( "\\s+" );
if( array.length == 8 )
	{
	if( action.indexOf( "mj update int field" ) == 0 )
		{
		return true;	
		}
	}
return false;
}
//--------------------------------------------------------------------------------
public void executeFunction()
throws Exception
{
String[] array = action.split( "\\s+" );
String collName = array[ 4 ];
String oidStr = array[ 5 ];
String fieldName = array[ 6 ];
int value = MStringUtil.parseInt( array[ 7 ] );

ObjectId oid = new ObjectId( oidStr );
DBObject query = new BasicDBObject( "_id", oid );
DBObject update = new BasicDBObject( "$set", new BasicDBObject( fieldName, new Integer( value ) ) );

DB db = MDataManager.getInstance().getDB();

DBCollection coll = db.getCollection( collName );

coll.update( query, update, false, false, WriteConcern.SAFE );
}
//--------------------------------------------------------------------------------
public int getActionCondition()
{
return action_cond_collection;
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_mj_update_int;
}
//--------------------------------------------------------------------------------
}