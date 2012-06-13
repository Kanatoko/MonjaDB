package net.jumperz.app.MMonjaDBCore.action;

import com.mongodb.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.action.mj.MDisconnectAction;
import net.jumperz.app.MMonjaDBCore.event.*;
/*
connect foo
connect 192.169.0.5/foo
connect 192.169.0.5:9999/foo
みたいな感じで接続する。
MongoオブジェクトとDBオブジェクト
*/
public class MConnectAction
extends MAbstractAction
{
protected String host;
protected String dbName;
protected int port;
protected Mongo mongo;
protected DB db;

//--------------------------------------------------------------------------------
public String getEventName()
{
return event_connect;
}
//--------------------------------------------------------------------------------
public int getActionCondition()
{
return action_cond_not_connected_or_connected_to_different_host;
}
//--------------------------------------------------------------------------------
public String getName()
{
return host + ":" + port;
}
//--------------------------------------------------------------------------------
public boolean parse( String action )
{
if( action.indexOf( "connect" ) == -1 )
	{
	return false;
	}

host = "127.0.0.1";
dbName = null;
port = 27017;

String dbAddr = MRegEx.getMatch( "^connect\\s+(.*)", action );
if( dbAddr.indexOf( '/' ) > -1 )
	{
	String[] array = dbAddr.split( "/" );
	if( array.length != 2 )
		{
		return false;
		}
	dbName = array[ 1 ];
	if( array[ 0 ].indexOf( ':' ) > -1 )
		{
		String[] array2 = array[ 0 ].split( ":" );
		if( array2.length != 2 )
			{
			return false;
			}
		host = array2[ 0 ];
		port = MStringUtil.parseInt( array2[ 1 ], 27017 );
		}
	else
		{
		host = array[ 0 ];
		}
	}
else
	{
	dbName = dbAddr;
	}

return true;
}
//--------------------------------------------------------------------------------
public boolean equals( Object o )
{
if( o.getClass().equals( this.getClass() ) )
	{
	MConnectAction c = ( MConnectAction )o;
	if( this.host.equals( c.host )
	 && this.port == c.port
	 && this.dbName.equals( c.dbName )
	  )
		{
		return true;
		}
	}
return false;
}
//--------------------------------------------------------------------------------
public Mongo getMongo()
{
return mongo;
}
//--------------------------------------------------------------------------------
public DB getDB()
{
return db;
}
//--------------------------------------------------------------------------------
protected void checkExistingConnection()
{
if( dataManager.connectedToDifferentHost( this ) )
	{
	( new MDisconnectAction() ).execute();
	}
}
//--------------------------------------------------------------------------------
public void executeFunction()
throws Exception
{
checkExistingConnection();
mongo = new Mongo( host, port );
db = mongo.getDB( dbName );
}
//--------------------------------------------------------------------------------
public void close()
{
mongo.close();
}
//--------------------------------------------------------------------------------
}
