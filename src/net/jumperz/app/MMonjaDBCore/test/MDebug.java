package net.jumperz.app.MMonjaDBCore.test;

import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.action.*;
import net.jumperz.app.MMonjaDBCore.action.mj.*;
import net.jumperz.app.MMonjaDBCore.event.*;

import com.mongodb.*;
import net.jumperz.mongo.*;
import java.util.*;

import org.bson.types.*;

public class MDebug
{
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
Mongo mongo = new Mongo( "127.0.0.1" );

/*
DB db = mongo.getDB( "admin" );
p( db );
p( db.authenticate( "root", "hogefuga123".toCharArray() ) + "" );
try
	{
	List dbNameList = mongo.getDatabaseNames();
	p( dbNameList );
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
*/
DB test = mongo.getDB( "test" );
p( test );
test.authenticate( "test", "test".toCharArray() );
p( test.getCollectionNames() );

DB db = mongo.getDB( "admin" );
p( db );
p( db.authenticate( "root", "hogefuga123".toCharArray() ) + "" );
try
	{
	List dbNameList = mongo.getDatabaseNames();
	p( dbNameList );
	}
catch( Exception e )
	{
	e.printStackTrace();
	}

}
//--------------------------------------------------------------------------------
public static void p( Object o )
{
System.out.println( o );
}
//--------------------------------------------------------------------------------
}