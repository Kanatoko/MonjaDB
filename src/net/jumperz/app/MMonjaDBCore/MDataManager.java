package net.jumperz.app.MMonjaDBCore;

import java.io.IOException;
import java.util.*;

import org.bson.types.ObjectId;

import net.jumperz.app.MMonjaDBCore.action.*;
import net.jumperz.app.MMonjaDBCore.action.mj.MEditAction;
import net.jumperz.app.MMonjaDBCore.event.*;
//import net.jumperz.app.MMonjaDBCore.exception.MConnectedToWrongHostException;
import net.jumperz.mongo.MFindQuery;
import net.jumperz.mongo.MMongoUtil;
import net.jumperz.util.*;
import com.mongodb.*;

public class MDataManager
extends MAbstractLogAgent
implements MObserver2, MConstants
{
private static  MDataManager instance = new MDataManager();

private volatile Mongo mongo;
private volatile DB db;
private volatile String collName = "";
//private volatile List dbNameList;
private volatile List documentDataList;
private volatile Map documentDataMap;
private volatile List columnNameList;
private volatile Object document;
//private volatile String lastFindActionString;
private volatile String lastEditActionString;
private volatile MFindAction lastFindAction;
private volatile MConnectAction connectAction;
private int sortOrder = sort_order_default;
private MThreadPool threadPool = new MThreadPool( 5 );
private MThreadPool actionThreadPool = new MThreadPool( 1 );
private MProperties prop;
private boolean numberIntEnabled = false;
private MHistory findHistory = new MHistory();
//private int batchSize = default_batch_size;
private int maxFindResults = default_max_results;
private boolean maybeHasMoreResults;

	//copy paste
private volatile List copiedDocumentList = new ArrayList();
private volatile String copiedCollName;

//--------------------------------------------------------------------------------
public String getCopiedCollName()
{
return copiedCollName;
}
//--------------------------------------------------------------------------------
public void setCopiedCollName( String copiedCollName )
{
this.copiedCollName = copiedCollName;
}
//--------------------------------------------------------------------------------
public List getCopiedDocumentList()
{
return copiedDocumentList;
}
//--------------------------------------------------------------------------------
public boolean connectedToDifferentHost( MAbstractAction _action )
{
return ( isConnected() && !connectedToSameHost( _action ) );
}
//--------------------------------------------------------------------------------
public MConnectAction getConnectAction()
{
return connectAction;
}
//--------------------------------------------------------------------------------
public boolean connectedToSameHost( MAbstractAction _action )
{
if( connectAction == null )
	{
	return false;
	}
else
	{
	return connectAction.equals( _action );
	}
}
//--------------------------------------------------------------------------------
public void setCopiedDocumentList( List copiedDocumentList )
{
this.copiedDocumentList = copiedDocumentList;
}
/*
//--------------------------------------------------------------------------------
public void setBatchSize( int i )
{
batchSize = i;
}
*/
//--------------------------------------------------------------------------------
public void setProp( MProperties p )
{
prop = p;
}
//--------------------------------------------------------------------------------
public MFindAction getLastFindAction()
{
return lastFindAction;
}
//--------------------------------------------------------------------------------
public String getLastFindActionString()
{
return lastFindAction.getActionStr();
}
//--------------------------------------------------------------------------------
public String getCollName()
{
return collName;
}
//--------------------------------------------------------------------------------
public int getMaxResults()
{
return maxFindResults;
}
//--------------------------------------------------------------------------------
public int getSortOrder()
{
	//sort order
if( sortOrder == sort_order_asc )
	{
	sortOrder = sort_order_desc;
	}
else
	{
	sortOrder = sort_order_asc;
	}
return sortOrder;
}
//--------------------------------------------------------------------------------
public List getColumnNameList()
{
return columnNameList;
}
//--------------------------------------------------------------------------------
public List getDocumentDataList()
{
return documentDataList;
}
//--------------------------------------------------------------------------------
public Map getDocumentDataMap()
{
return documentDataMap;
}
//--------------------------------------------------------------------------------
public MProperties getProp()
{
return prop;
}
//--------------------------------------------------------------------------------
public static MDataManager getInstance()
{
return instance;
}
//--------------------------------------------------------------------------------
public MThreadPool getActionThreadPool()
{
return actionThreadPool;
}
//--------------------------------------------------------------------------------
public boolean isConnected()
{
return mongo != null;
}
//--------------------------------------------------------------------------------
public synchronized DB getDB()
{
return db;
}
//--------------------------------------------------------------------------------
public synchronized Mongo getMongo()
{
return mongo;
}
//--------------------------------------------------------------------------------
public synchronized void setDB( DB db )
{
this.db = db;
}
//--------------------------------------------------------------------------------
private MDataManager()
{
instance = this;
}
//--------------------------------------------------------------------------------
public MThreadPool getThreadPool()
{
return threadPool;
}
//--------------------------------------------------------------------------------
private synchronized void onConnect( MConnectAction action )
{
this.connectAction = action;

mongo = action.getMongo();
db = action.getDB();

threadPool.addCommand( new MCommand() {	public void execute(){ //-----------------
checkNumberInt();
} public void breakCommand(){}	} ); //------------
}
//--------------------------------------------------------------------------------
public boolean numberIntEnabled()
{
return numberIntEnabled;
}
//--------------------------------------------------------------------------------
private void checkNumberInt()
{
try
	{
	db.eval( "NumberInt(1)", null );
	numberIntEnabled = true;
	}
catch( Exception e )
	{
	//e.printStackTrace();
	}
}
//--------------------------------------------------------------------------------
private synchronized void onShowDbs( MShowDBAction action )
{
//dbNameList = action.getDBList();
}
//--------------------------------------------------------------------------------
private synchronized void onUse( MUseAction action )
{
db = mongo.getDB( action.getDBName() );
}
//--------------------------------------------------------------------------------
public void stopThreadPools()
{
threadPool.slowStop();
actionThreadPool.slowStop();
}
//--------------------------------------------------------------------------------
public String getLastEditActionString()
{
return lastEditActionString;
}
//--------------------------------------------------------------------------------
public MHistory getFindHistory()
{
return findHistory;
}
//--------------------------------------------------------------------------------
private void onFind( MFindAction action )
throws IOException
{
//dbName = action.getDB().getName();
collName = action.getCollection().getName();

	//reset data
documentDataList = new ArrayList();
documentDataMap = new HashMap();
document = null;
sortOrder = sort_order_default;
lastFindAction = action;
findHistory.add( action.getActionStr() );

MFindQuery findQuery = MMongoUtil.parseFindQuery( db, action.getActionStr() );
int limit = findQuery.getLimitArg();

DBCursor cursor = action.getCursor();
maybeHasMoreResults = true;
for( int i = 0; i < maxFindResults; ++i )
	{
	if( cursor.hasNext() )
		{
		//Map data = cursor.next().toMap();
		DBObject data = cursor.next();
		documentDataList.add( data );
		documentDataMap.put( data.get( "_id" ), data );
		}
	else
		{
		if( i == limit )
			{
			//debug( "--3--" );
			maybeHasMoreResults = true;
			}
		else
			{
			//debug( "--4--" + i + ":" + limit );
			maybeHasMoreResults = false;		
			}
		cursor.close();
		break;
		}	
	}

columnNameList = MMongoUtil.getNameListFromDataList( documentDataList );
//drawTable( documentDataList, columnNameList );
}
//--------------------------------------------------------------------------------
public Object getLastEditedDocument()
{
return document;
}
//--------------------------------------------------------------------------------
private void onEdit( MEditAction action )
{
lastEditActionString = action.getAction();
document = documentDataMap.get( action.getIdAsObject() );
}
//--------------------------------------------------------------------------------
public void updateDocument( Object _id, String editingFieldName, Object newValue )
{
BasicDBObject query = new BasicDBObject( "_id", _id );
BasicDBObject update = new BasicDBObject( "$set", new BasicDBObject( editingFieldName, newValue ) );

String updateStr = null;
if( newValue instanceof Integer )
	{
	if( !numberIntEnabled() )
		{
		int intValue = ( ( Integer )newValue ).intValue();
		updateIntField( _id.toString(), editingFieldName, intValue );
		return;
		}
	else
		{
		updateStr = "{ \"$set\" : { \"" + editingFieldName + "\" : NumberInt( " + newValue + " ) } }";
		}
	}
else
	{
	updateStr = MMongoUtil.toJson( getDB(), update );
	}

String collName = getCollName();

MActionManager.getInstance().executeAction( "db." + collName + ".update(" +
	MMongoUtil.toJson( getDB(), query ) + "," +
	updateStr +
	",false, false )" );

reloadDocument();
}
//--------------------------------------------------------------------------------
public void  updateIntField( String oidStr, String fieldName, int value )
{
MActionManager.getInstance().executeAction( "mj update int field " + getCollName() + " " + oidStr + " " + fieldName + " " + value );
reloadDocument();
}
//--------------------------------------------------------------------------------
public void setMaxFindResults( int i )
{
maxFindResults = i;
}
//--------------------------------------------------------------------------------
public void reloadDocument()
{
MActionManager.getInstance().executeAction( getLastFindActionString() );
MActionManager.getInstance().executeAction( getLastEditActionString() );
}
//--------------------------------------------------------------------------------
public boolean hasPrevItems()
{
if( lastFindAction == null )
	{
	return false;
	}
else
	{
	MFindQuery fq = lastFindAction.getFindQuery();
	MFindQuery prevQuery = MMongoUtil.getPrevItemsQuery( db, fq, maxFindResults );
	if( prevQuery == null )
		{
		return false;
		}
	else
		{
		return true;
		}
	}
}
//--------------------------------------------------------------------------------
public boolean hasNextItems()
{
if( lastFindAction == null )
	{
	return false;
	}
else
	{
	return maybeHasMoreResults;
	}
}
//--------------------------------------------------------------------------------
private void onNextItems()
{
if( !hasNextItems() )
	{
	return;
	}
else
	{
		//hasMoreResults is true here
	MFindQuery fq = lastFindAction.getFindQuery();
	MFindQuery nextQuery = MMongoUtil.getNextItemsQuery( db, fq, maxFindResults );
	debug( nextQuery );
	MActionManager.getInstance().executeAction( MMongoUtil.findQueryToString( db, nextQuery ) );
	}
}
//--------------------------------------------------------------------------------
private void onPrevItems()
{
if( !hasPrevItems() )
	{
	return;
	}
else
	{
	MFindQuery fq = lastFindAction.getFindQuery();
	MFindQuery prevQuery = MMongoUtil.getPrevItemsQuery( db, fq, maxFindResults );
	if( prevQuery != null )
		{
		MActionManager.getInstance().executeAction( MMongoUtil.findQueryToString( db, prevQuery ) );
		}
	}
}
//--------------------------------------------------------------------------------
private void onDisconnect()
{
mongo = null;
db = null;
collName = null;
//dbNameList = null;
documentDataList = null;
documentDataMap = null;
columnNameList = null;
document = null;
lastFindAction = null;
lastEditActionString = "";
int sortOrder = sort_order_default;
MHistory findHistory = new MHistory();
connectAction.close();
connectAction = null;
}
//--------------------------------------------------------------------------------
public void update( final Object e, final Object source )
{
//threadPool.addCommand( new MCommand() {	public void execute(){ //-----------------

MEvent event = ( MEvent )e;
if( event.getEventName().indexOf( event_connect + "_end" ) == 0 )
	{
	onConnect( ( MConnectAction )source );	
	}
else if( event.getEventName().indexOf( event_showdbs + "_end" ) == 0 )
	{
	MShowDBAction action = ( MShowDBAction )source;
	onShowDbs( action );
	}
else if( event.getEventName().indexOf( event_use + "_end" ) == 0 )
	{
	MUseAction action = ( MUseAction )source;
	onUse( action );
	}
else if( event.getEventName().indexOf( event_find + "_end" ) == 0 )
	{
	MFindAction action = ( MFindAction )source;
	try
		{
		onFind( action );	
		}
	catch( IOException ex )
		{
		MEventManager.getInstance().fireErrorEvent( ex );
		}
	}
else if( event.getEventName().indexOf( event_mj_edit + "_end" ) == 0 )
	{
	MEditAction action = ( MEditAction )source;
	onEdit( action );
	}
else if( event.getEventName().indexOf( event_mj_prev_items + "_end" ) == 0 )
	{
	onPrevItems();
	}
else if( event.getEventName().indexOf( event_mj_next_items + "_end" ) == 0 )
	{
	onNextItems();
	}
else if( event.getEventName().indexOf( event_disconnect + "_end" ) == 0 )
	{
	onDisconnect();
	}
/*
else if( event.getEventName().indexOf( event_find + "_end" ) == 0 )
	{
	MFindAction action = ( MFindAction )source;
	onFind( action );
	}
*/
//} public void breakCommand(){}	} ); //------------

}
//--------------------------------------------------------------------------------
}
