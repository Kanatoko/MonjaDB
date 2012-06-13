package net.jumperz.app.MMonjaDBCore.action.mj;

import java.util.*;
import com.mongodb.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;
import net.jumperz.app.MMonjaDBCore.action.*;

public class MShowAllDbStatsAction
extends MAbstractAction
{
private List statsList = new ArrayList();
//--------------------------------------------------------------------------------
public MShowAllDbStatsAction()
{
//setContext( dataManager.getMongo() );
}
/*
//--------------------------------------------------------------------------------
public void setContextImpl( Object context )
{
mongo = ( Mongo )context;
}
*/
//--------------------------------------------------------------------------------
public boolean parse( String action )
{
return action.equals( "mj show all db stats" );
}
//--------------------------------------------------------------------------------
public void executeFunction()
throws Exception
{
Mongo mongo = dataManager.getMongo();
List dbNameList = mongo.getDatabaseNames();
for( int i = 0; i < dbNameList.size(); ++i )
	{
	String dbName = ( String )dbNameList.get( i );
	DB db = mongo.getDB( dbName );
	statsList.add( db.getStats().toMap() );
	}
}
//--------------------------------------------------------------------------------
public List getStatsList()
{
return statsList;
}
//--------------------------------------------------------------------------------
public int getActionCondition()
{
return action_cond_connected;
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_mj_all_db_stats;
}
//--------------------------------------------------------------------------------
}
