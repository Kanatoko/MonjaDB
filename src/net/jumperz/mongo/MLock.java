package net.jumperz.mongo;

import com.mongodb.*;

public class MLock
{
private Mongo mongo;
private String dbName;
private String collName;

private DBCollection coll;
private String lockKey;
//--------------------------------------------------------------------------------
public MLock( Mongo mongo, String dbName, String collName )
{
this.mongo = mongo;
this.dbName = dbName;
this.collName = collName;

init();
}
//--------------------------------------------------------------------------------
private void init()
{
coll = mongo.getDB( dbName ).getCollection( collName );
coll.ensureIndex( new BasicDBObject( "key", new Integer( 1 ) ), new BasicDBObject( "unique", Boolean.TRUE ) );
}
//--------------------------------------------------------------------------------
public boolean lock( String lockKey )
{
this.lockKey = lockKey;
try
	{
	BasicDBObject obj = new BasicDBObject();
	obj.put( "key", lockKey );
	obj.put( "t", new java.util.Date() );
	coll.insert( obj, WriteConcern.SAFE );
	return true;
	}
catch( Exception e )
	{
	//e.printStackTrace();
	return false;
	}
}
//--------------------------------------------------------------------------------
public void unlock()
{
if( lockKey == null )
	{
	return;
	}
try
	{
	coll.remove( new BasicDBObject( "key", lockKey ), WriteConcern.SAFE );
	}
catch( MongoException e )
	{
	e.printStackTrace();
	}
}
//--------------------------------------------------------------------------------
}