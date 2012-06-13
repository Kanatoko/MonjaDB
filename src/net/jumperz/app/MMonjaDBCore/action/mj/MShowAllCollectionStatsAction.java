package net.jumperz.app.MMonjaDBCore.action.mj;

import java.util.*;
import com.mongodb.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;
import net.jumperz.app.MMonjaDBCore.action.*;

public class MShowAllCollectionStatsAction
extends MAbstractAction
{
private List statsList = new ArrayList();
//--------------------------------------------------------------------------------
public MShowAllCollectionStatsAction()
{
}
//--------------------------------------------------------------------------------
public boolean parse( String action )
{
return action.equals( "mj show all collection stats" );
}
//--------------------------------------------------------------------------------
public void executeFunction()
throws Exception
{
DB db = dataManager.getDB();
Set collNameSet = db.getCollectionNames();
Iterator p = collNameSet.iterator();
while( p.hasNext() )
	{
	String dbName = ( String )p.next();
	DBCollection coll = db.getCollection( dbName );
	statsList.add( coll.getStats() );
	}

//debug( statsList );
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
return event_mj_all_collection_stats;
}
//--------------------------------------------------------------------------------
}
