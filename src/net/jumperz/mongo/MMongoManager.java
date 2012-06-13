package net.jumperz.mongo;

import com.mongodb.*;
import com.mongodb.ServerAddress.*;
import java.io.*;
import java.net.*;
import java.util.*;
import net.jumperz.util.*;

public class MMongoManager
{
private static final MMongoManager instance = new MMongoManager();

private volatile Mongo mongo;
private volatile MongoOptions options = new MongoOptions();
//--------------------------------------------------------------------------------
public static MMongoManager getInstance()
{
return instance;
}
//--------------------------------------------------------------------------------
private MMongoManager()
{
//private

options.connectionsPerHost = 10;
options.threadsAllowedToBlockForConnectionMultiplier = 30;
}
//--------------------------------------------------------------------------------
public void setMongoOptions( MongoOptions op )
{
options = op;
}
//--------------------------------------------------------------------------------
public boolean initialized()
{
return mongo != null;
}
//--------------------------------------------------------------------------------
public synchronized Mongo getMongo()
{
if( mongo == null )
	{
	init();
	}
return mongo;
}
//--------------------------------------------------------------------------------
public void init()
{
try
	{
	init( "127.0.0.1" );
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
}
//--------------------------------------------------------------------------------
public void init( String mongoStr )
throws IOException
{
if( mongoStr == null || mongoStr.equals( "" ) )
	{
	mongoStr = "127.0.0.1";
	}

if( mongoStr.indexOf( ',' ) > -1 )
	{
	mongo = MMongoUtil.getReplMongo( mongoStr, options );
	}
else
	{
	if( options != null )
		{
		mongo = new Mongo( mongoStr, options );
		}
	else
		{
		mongo = new Mongo( mongoStr );	
		}
	}
}
//--------------------------------------------------------------------------------
}