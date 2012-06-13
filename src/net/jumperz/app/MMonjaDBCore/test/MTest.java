package net.jumperz.app.MMonjaDBCore.test;

import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.action.*;
import net.jumperz.app.MMonjaDBCore.event.*;

import com.mongodb.*;
import net.jumperz.mongo.*;
import java.util.*;

public class MTest
{
static Mongo mongo;
static DB db;
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
mongo = new Mongo( "192.168.3.205" );
db = mongo.getDB( "test2" );

testParseJsonToArray();
testSort();
testLimit1();
testSkip2();
testClone();
testFindQueryToString();
testSkip1();
testParseFindQuery();
testSkipLimit2();
testGetValueByType();
testGetAction();
testConnectAction();
testFindAction();
testSkipLimit();
System.out.println( "OK" );
System.exit( 0 );
}
//--------------------------------------------------------------------------------
public static void testParseJsonToArray()
throws Exception
{

{
BasicDBList l = MMongoUtil.parseJsonToArray( db, "{a:1}" );
if( l.size() != 1 ){ throw new Exception(); };
BasicDBObject o = (BasicDBObject)l.get( 0 );
if( !o.get( "a" ).equals( new Double( 1 ) ) ){ throw new Exception(); }
}

{
BasicDBList l = MMongoUtil.parseJsonToArray( db, "{a:1},{b:'foobar'}" );
if( l.size() != 2 ){ throw new Exception(); };
BasicDBObject o = (BasicDBObject)l.get( 0 );
if( !o.get( "a" ).equals( new Double( 1 ) ) ){ throw new Exception(); }
BasicDBObject o2 = (BasicDBObject)l.get( 1 );
if( !o2.get( "b" ).equals( "foobar" ) ){ throw new Exception(); }
}

{
BasicDBList l = MMongoUtil.parseJsonToArray( db, "[{a:1},{b:'foobar'}]" );
if( l.size() != 2 ){ throw new Exception(); };
BasicDBObject o = (BasicDBObject)l.get( 0 );
if( !o.get( "a" ).equals( new Double( 1 ) ) ){ throw new Exception(); }
BasicDBObject o2 = (BasicDBObject)l.get( 1 );
if( !o2.get( "b" ).equals( "foobar" ) ){ throw new Exception(); }
}

{
BasicDBList l = MMongoUtil.parseJsonToArray( db, "[{a:1},\r\n{b:\t'foobar'}]" );
if( l.size() != 2 ){ throw new Exception(); };
BasicDBObject o = (BasicDBObject)l.get( 0 );
if( !o.get( "a" ).equals( new Double( 1 ) ) ){ throw new Exception(); }
BasicDBObject o2 = (BasicDBObject)l.get( 1 );
if( !o2.get( "b" ).equals( "foobar" ) ){ throw new Exception(); }
}
}
//--------------------------------------------------------------------------------
public static void testSort()
throws Exception
{

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find().sort({a:1})" );
BasicDBObject sortArg = q.getSortArg();
if( !sortArg.get( "a" ).equals( new Double( 1 ) ) ){ throw new Exception(); }
}

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find().sort({a:1,b:-1})" );
BasicDBObject sortArg = q.getSortArg();
if( !sortArg.get( "a" ).equals( new Double( 1 ) ) ){ throw new Exception(); }
if( !sortArg.get( "b" ).equals( new Double( -1 ) ) ){ throw new Exception(); }
}

}
//--------------------------------------------------------------------------------
public static void testFindQueryToString()
throws Exception
{

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({'a':1},{b:1},10,20).skip(23).limit(10)" );
if( !MMongoUtil.findQueryToString( db, q ).equals( "db.test.find( { \"a\" : 1 }, { \"b\" : 1 }, 10, 20 ).skip( 23 ).limit( 10 )" ) ){ throw new Exception(); }
}

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({'a':1},{b:1},10,20).skip(23).limit(10).sort({a:-1})" );
if( !MMongoUtil.findQueryToString( db, q ).equals( "db.test.find( { \"a\" : 1 }, { \"b\" : 1 }, 10, 20 ).skip( 23 ).limit( 10 ).sort( { \"a\" : -1 } )" ) ){ throw new Exception(); }
}

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({'a':1},{b:1}).limit(10).sort({a:-1}).skip(23)" );
p( MMongoUtil.findQueryToString( db, q ) );
if( !MMongoUtil.findQueryToString( db, q ).equals( "db.test.find( { \"a\" : 1 }, { \"b\" : 1 } ).limit( 10 ).sort( { \"a\" : -1 } ).skip( 23 )" ) ){ throw new Exception(); }
}

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({'a':'hoge\tfoo'},{b:1,_id : ObjectId('4e3a0d5cce7ad68f274f3873'),},10,20).skip(23).limit(10)" );
if( !MMongoUtil.findQueryToString( db, q ).equals( "db.test.find({\"a\" : \"hoge\\tfoo\"},{\"b\" : 1,\"_id\" : ObjectId(\"4e3a0d5cce7ad68f274f3873\")},10,20).skip( 23 ).limit( 10 )" ) ){ throw new Exception(); }
}

}
//--------------------------------------------------------------------------------
public static void testParseFindQuery()
throws Exception
{
{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({'a':1}).skip(23).limit(10)" );
List findArg = q.getFindArg();
if( findArg.size() != 1 ){ throw new Exception(); }
BasicDBObject arg1 = ( BasicDBObject )findArg.get( 0 );
if( !arg1.equals( new BasicDBObject( "a", new Double( 1 ) ) ) ){ throw new Exception(); }
if( q.getLimitArg() != 10 ){ throw new Exception(); }
if( q.getSkipArg() != 23 ){ throw new Exception(); }
}

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({'a':1}).limit(23).skip(10)" );
List findArg = q.getFindArg();
if( findArg.size() != 1 ){ throw new Exception(); }
BasicDBObject arg1 = ( BasicDBObject )findArg.get( 0 );
if( !arg1.equals( new BasicDBObject( "a", new Double( 1 ) ) ) ){ throw new Exception(); }
if( q.getLimitArg() != 23 ){ throw new Exception(); }
if( q.getSkipArg() != 10 ){ throw new Exception(); }
}

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({a:'foo'})" );
List findArg = q.getFindArg();
if( findArg.size() != 1 ){ throw new Exception(); }
BasicDBObject arg1 = ( BasicDBObject )findArg.get( 0 );
if( !arg1.equals( new BasicDBObject( "a", "foo" ) ) ){ throw new Exception(); }
if( q.getLimitArg() != -1 ){ throw new Exception(); }
if( q.getSkipArg() != -1 ){ throw new Exception(); }
}

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find()" );
List findArg = q.getFindArg();
if( findArg.size() != 0 ){ throw new Exception(); }
if( q.getLimitArg() != -1 ){ throw new Exception(); }
if( q.getSkipArg() != -1 ){ throw new Exception(); }
}

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({a:'foo'},{b:1})" );
List findArg = q.getFindArg();
if( findArg.size() != 2 ){ throw new Exception(); }
BasicDBObject arg1 = ( BasicDBObject )findArg.get( 0 );
if( !arg1.equals( new BasicDBObject( "a", "foo" ) ) ){ throw new Exception(); }
BasicDBObject arg2 = ( BasicDBObject )findArg.get( 1 );
if( !arg2.equals( new BasicDBObject( "b", new Integer( 1 ) ) ) ){ throw new Exception(); }
if( q.getLimitArg() != -1 ){ throw new Exception(); }
if( q.getSkipArg() != -1 ){ throw new Exception(); }
}

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({a:'foo',b:'baz'})" );
List findArg = q.getFindArg();
if( findArg.size() != 1 ){ throw new Exception(); }
BasicDBObject arg1 = ( BasicDBObject )findArg.get( 0 );
BasicDBObject test = new BasicDBObject();
test.put( "a", "foo" );
test.put( "b", "baz" );
if( !arg1.equals( test ) ){ throw new Exception(); }
if( q.getLimitArg() != -1 ){ throw new Exception(); }
if( q.getSkipArg() != -1 ){ throw new Exception(); }
}

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({a:'foo',b:'baz'},{a:1},10)" );
List findArg = q.getFindArg();
if( findArg.size() != 3 ){ throw new Exception(); }
BasicDBObject arg1 = ( BasicDBObject )findArg.get( 0 );
BasicDBObject test = new BasicDBObject();
test.put( "a", "foo" );
test.put( "b", "baz" );
if( !findArg.get( 2 ).equals( new Double( 10 ) ) ){ throw new Exception(); }

if( !arg1.equals( test ) ){ throw new Exception(); }
if( q.getLimitArg() != 10 ){ throw new Exception(); }
if( q.getSkipArg() != -1 ){ throw new Exception(); }
}

{
MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({a:'foo',b:'baz'},{a:1},10,20)" );
List findArg = q.getFindArg();
if( findArg.size() != 4 ){ throw new Exception(); }
BasicDBObject arg1 = ( BasicDBObject )findArg.get( 0 );
BasicDBObject test = new BasicDBObject();
test.put( "a", "foo" );
test.put( "b", "baz" );
if( !findArg.get( 2 ).equals( new Double( 10 ) ) ){ throw new Exception(); }
if( !findArg.get( 3 ).equals( new Double( 20 ) ) ){ throw new Exception(); }

if( !arg1.equals( test ) ){ throw new Exception(); }
if( q.getLimitArg() != 10 ){ throw new Exception(); }
if( q.getSkipArg() != 20 ){ throw new Exception(); }
}

{
try
	{
	MFindQuery q = MMongoUtil.parseFindQuery( db, "db.test.find({" );
	throw new Exception();
	}
catch( MongoException e )
	{
	}
}

/*
p( MMongoUtil.parseFindQuery( db, "db.test.find({hoge:123,foo:'hoge'})" ) );
p( MMongoUtil.parseFindQuery( db, "db.test.find({hoge:123,foo:'hoge'},{hoge:1})" ) );
p( MMongoUtil.parseFindQuery( db, "db.test.find({hoge:123,foo:'hoge'},{hoge:1},10)" ) );
p( MMongoUtil.parseFindQuery( db, "db.test.find({hoge:123,foo:'hoge'},{hoge:1},10,20)" ) );
*/
}
//--------------------------------------------------------------------------------
public static void testSkipLimit2()
throws Exception
{
int[] r;

r = MMongoUtil.getPrevValue( 100, 10 );
if( r[ 0 ] != 90 || r[ 1 ] != 10 ){ throw new Exception(); }

r = MMongoUtil.getPrevValue( 100, 30 );
if( r[ 0 ] != 70 || r[ 1 ] != 30 ){ throw new Exception(); }

r = MMongoUtil.getPrevValue( 70, 30 );
if( r[ 0 ] != 40 || r[ 1 ] != 30 ){ throw new Exception(); }

r = MMongoUtil.getPrevValue( 40, 30 );
if( r[ 0 ] != 10 || r[ 1 ] != 30 ){ throw new Exception(); }

r = MMongoUtil.getPrevValue( 10, 30 );
if( r[ 0 ] != -1 || r[ 1 ] != 30 ){ throw new Exception(); }
}
//--------------------------------------------------------------------------------
public static void testClone()
throws Exception
{
MFindQuery fq = null;
MFindQuery prevQuery = null;

fq = MMongoUtil.parseFindQuery( db, "db.test.find({'a':1},{b:1},10,20).skip(23).limit(10)" );
prevQuery = ( MFindQuery )fq.clone();

if( prevQuery.getSkipArg() != 23 ){ throw new Exception(); }
fq.put( "skipArg", new Integer( 100 ) );
if( fq.getSkipArg() != 100 ){ throw new Exception(); }
if( prevQuery.getSkipArg() != 23 ){ throw new Exception(); }

if( !fq.getCollName().equals( "test" ) ){ throw new Exception(); }
if( !prevQuery.getCollName().equals( "test" ) ){ throw new Exception(); }

if( prevQuery.getLimitArg() != 10 ){ throw new Exception(); }
fq.put( "limitArg", new Integer( 100 ) );
if( fq.getLimitArg() != 100 ){ throw new Exception(); }
if( prevQuery.getLimitArg() != 10 ){ throw new Exception(); }

}
//--------------------------------------------------------------------------------
public static void testSkip2()
throws Exception
{
MFindQuery fq = null;
MFindQuery prevQuery = null;

fq = MMongoUtil.parseFindQuery( db, "db.test.find({'a':1},{b:1}, 8, 50 )" );
prevQuery = MMongoUtil.getPrevItemsQuery( db, fq, 1000 );
}
//--------------------------------------------------------------------------------
public static void testSkip1()
throws Exception
{
MFindQuery fq = null;
MFindQuery prevQuery = null;

fq = MMongoUtil.parseFindQuery( db, "db.test.find({'a':1},{b:1},10,20).skip(23).limit(10)" );

prevQuery = MMongoUtil.getPrevItemsQuery( db, fq, 1000 );
if( prevQuery .getSkipArg() != 13 ){ throw new Exception(); }
//p( prevQuery );

prevQuery = MMongoUtil.getPrevItemsQuery( db, prevQuery, 1000 );
if( prevQuery .getSkipArg() != 3 ){ throw new Exception(); }
//p( prevQuery );

prevQuery = MMongoUtil.getPrevItemsQuery( db, prevQuery, 1000 );
//p( prevQuery );
if( prevQuery .getSkipArg() != -1 ){ throw new Exception(); }
if( prevQuery.getInvokedFunctionNameList().contains( "skip" ) ){ throw new Exception(); }
}
//--------------------------------------------------------------------------------
public static void testLimit1()
throws Exception
{
MFindQuery fq = null;
MFindQuery nextQuery = null;

{
fq = MMongoUtil.parseFindQuery( db, "db.test.find().limit(10)" );
nextQuery = MMongoUtil.getNextItemsQuery( db, fq, 40 );
p( nextQuery );
if( nextQuery .getSkipArg() != 10 ){ throw new Exception(); }
if( nextQuery .getLimitArg() != 10 ){ throw new Exception(); }
}

{
fq = MMongoUtil.parseFindQuery( db, "db.test.find().skip(23).limit(10)" );
nextQuery = MMongoUtil.getNextItemsQuery( db, fq, 1000 );
if( nextQuery .getSkipArg() != 33 ){ throw new Exception(); }
}

{
fq = MMongoUtil.parseFindQuery( db, "db.test.find().limit(100)" );
nextQuery = MMongoUtil.getNextItemsQuery( db, fq, 40 );
p( nextQuery );
if( nextQuery .getSkipArg() != 40 ){ throw new Exception(); }
}

}
//--------------------------------------------------------------------------------
public static void testSkipLimit()
throws Exception
{
/*
MMongoUtil.setSkip( "db.test.find().skip( 100 )", 10 ); // db.test.find().skip( 110 )
MMongoUtil.setSkip( "db.test.find().skip( 100 )", -10 ); // db.test.find().skip( 90 )
*/
/*
if( MMongoUtil.getSkipFromFindQuery( "db.test.find().skip(1)" ) != 1 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find().skip(123)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find() .skip(123)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find() . skip(123)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find().skip( 123)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find().skip(123 )" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find( a:'.skip(123)' " ) != -1 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find().skip(123)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find().skip(123).limit(1)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find().skip(123). limit(1)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find().skip(123) . limit(1)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find().skip(123) . limit( 1)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find().skip(123) . limit( 1 )" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getSkipFromFindQuery( "db.test.find().skip(123) . limit( 1 ) " ) != 123 ) { throw new Exception(); }

if( MMongoUtil.getLimitFromFindQuery( "db.test.find().limit(1)" ) != 1 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find().limit(123)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find() .limit(123)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find() . limit(123)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find().limit( 123)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find().limit(123 )" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find( a:'.limit(123)' " ) != -1 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find().limit(123)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find().limit(123).skip(1)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find().limit(123). skip(1)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find().limit(123) . skip(1)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find().limit(123) . skip( 1)" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find().limit(123) . skip( 1 )" ) != 123 ) { throw new Exception(); }
if( MMongoUtil.getLimitFromFindQuery( "db.test.find().limit(123) . skip( 1 ) " ) != 123 ) { throw new Exception(); }
*/
}
//--------------------------------------------------------------------------------
public static void testGetValueByType()
throws Exception
{
	//int
if( !( MMongoUtil.getValueByCurrentType( "123",			Integer.class ) instanceof Integer ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "a",			Integer.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "80.0",		Integer.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "[1,2]",		Integer.class ) instanceof List ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{}",			Integer.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{'a':123,'b':'iii'}",	Integer.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "9223372036854775807",	Integer.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "-9223372036854775808",Integer.class ) instanceof Double ) ){ throw new Exception(); }

	//double
if( !( MMongoUtil.getValueByCurrentType( "123",			Double.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "a",			Double.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "80.0",		Double.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "[1,2]",		Double.class ) instanceof List ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{}",			Double.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{'a':123,'b':'iii'}",	Double.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "9223372036854775807", Double.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "-9223372036854775808",Double.class ) instanceof Double ) ){ throw new Exception(); }

	//long
if( !( MMongoUtil.getValueByCurrentType( "123",			Long.class ) instanceof Long ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "a",			Long.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "80.0",		Long.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "[1,2]",		Long.class ) instanceof List ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{}",			Long.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{'a':123,'b':'iii'}",	Long.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "9223372036854775807", Long.class ) instanceof Long ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "-9223372036854775808",Long.class ) instanceof Long ) ){ throw new Exception(); }

	//string
if( !( MMongoUtil.getValueByCurrentType( "123",			String.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "a",			String.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "80.0",		String.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "[1,2]",		String.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{}",			String.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{'a':123,'b':'iii'}",	String.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "9223372036854775807", String.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "-9223372036854775808",String.class ) instanceof String ) ){ throw new Exception(); }

	//boolean
if( !( MMongoUtil.getValueByCurrentType( "true",		Boolean.class ) instanceof Boolean ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "false",		Boolean.class ) instanceof Boolean ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "hhh",			Boolean.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "123",			Boolean.class ) instanceof Integer ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "80.0",		Boolean.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "[1,2]",		Boolean.class ) instanceof List ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{}",			Boolean.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{'a':123,'b':'iii'}",	Boolean.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "9223372036854775807", Boolean.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "-9223372036854775808",Boolean.class ) instanceof Double ) ){ throw new Exception(); }

	//list
if( !( MMongoUtil.getValueByCurrentType( "123",			BasicDBList.class ) instanceof Integer ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "a",			BasicDBList.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "80.0",		BasicDBList.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "[1,2]",		BasicDBList.class ) instanceof List ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{}",			BasicDBList.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{'a':123,'b':'iii'}",	BasicDBList.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "9223372036854775807", BasicDBList.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "-9223372036854775808",BasicDBList.class ) instanceof Double ) ){ throw new Exception(); }

	//map
if( !( MMongoUtil.getValueByCurrentType( "123",			BasicDBList.class ) instanceof Integer ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "a",			BasicDBList.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "80.0",		BasicDBList.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "[1,2]",		BasicDBList.class ) instanceof List ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{}",			BasicDBList.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{'a':123,'b':'iii'}",	BasicDBList.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "9223372036854775807", BasicDBList.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "-9223372036854775808",BasicDBList.class ) instanceof Double ) ){ throw new Exception(); }

	//date
if( !( MMongoUtil.getValueByCurrentType( "123",			Date.class ) instanceof Integer ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "a",			Date.class ) instanceof String ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "80.0",		Date.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "[1,2]",		Date.class ) instanceof List ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{}",			Date.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "{'a':123,'b':'iii'}",	Date.class ) instanceof Map ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "9223372036854775807", Date.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "-9223372036854775808",Date.class ) instanceof Double ) ){ throw new Exception(); }
if( !( MMongoUtil.getValueByCurrentType( "Fri Jun 24 00:16:40 JST 2011",	Date.class ) instanceof Date ) ){ throw new Exception(); }

}
//--------------------------------------------------------------------------------
public static void testConnectAction()
throws Exception
{
MConnectAction action = new MConnectAction();
if( !action.parse( "connect foo" ) ){ throw new Exception(); }
if( !action.parse( "connect 192.169.0.5/foo" ) ){ throw new Exception(); }
if( !action.parse( "connect 192.169.0.5:9999/foo" ) ){ throw new Exception(); }
}
//--------------------------------------------------------------------------------
public static void testGetAction()
throws Exception
{
MActionManager instance = MActionManager.getInstance();
if( instance.getAction( "connect foo" ).getClass() != MConnectAction.class ) { throw new Exception(); }
if( instance.getAction( "show collections" ).getClass() != MShowCollectionAction.class ) { throw new Exception(); }
if( instance.getAction( "show dbs" ).getClass() != MShowDBAction.class ) { throw new Exception(); }
if( instance.getAction( "use foo" ).getClass() != MUseAction.class ) { throw new Exception(); }
if( instance.getAction( "db.foo.find()" ).getClass() != MFindAction.class ) { throw new Exception(); }
if( instance.getAction( "db.foo.bar.find()" ).getClass() != MFindAction.class ) { throw new Exception(); }
if( instance.getAction( "db.foo.find({a:1,b:2})" ).getClass() != MFindAction.class ) { throw new Exception(); }
if( instance.getAction( "db.foo.find({},{a:1})" ).getClass() != MFindAction.class ) { throw new Exception(); }
}

//--------------------------------------------------------------------------------
public static void testFindAction()
throws Exception
{
MFindAction action = new MFindAction();
/*
if( !"{'a':1}".equals( MMongoUtil.getArgStrFromAction( "db.foo.find({'a':1})", "find" ) ) ){ throw new Exception(); }
if( !"{'a':1},{'b':2}".equals( MMongoUtil.getArgStrFromAction( "db.foo.find({'a':1},{'b':2})", "find" ) ) ){ throw new Exception(); }

if( !"{'a':1}".equals( MMongoUtil.getArgStrFromAction( "db.foo.find({'a':1}).skip(1)", "find" ) ) ){ throw new Exception(); }
if( !"{'a':1}".equals( MMongoUtil.getArgStrFromAction( "db.foo.find({'a':1}).limit(1)", "find" ) ) ){ throw new Exception(); }
if( !"{'a':1}".equals( MMongoUtil.getArgStrFromAction( "db.foo.find({'a':1}).skip(1).limit(123)", "find" ) ) ){ throw new Exception(); }
if( !"{'a':1}".equals( MMongoUtil.getArgStrFromAction( "db.foo.find({'a':1}).limit(123).skip(1)", "find" ) ) ){ throw new Exception(); }
*/
/*
BasicDBList list = null;
{
list = MMongoUtil.getListFromAction( "db.foo.find({'a':1},{'b':2.2,'c':'hoge'})", "find" );
if( list.size() != 2 ) { throw new Exception(); }
if( list.get( 0 ).getClass() != BasicDBObject.class ){ throw new Exception(); }
if( list.get( 1 ).getClass() != BasicDBObject.class ){ throw new Exception(); }
BasicDBObject obj1 = ( BasicDBObject )list.get( 0 );
if( !obj1.get( "a" ).equals( new Integer( 1 ) ) ) { throw new Exception(); }
BasicDBObject obj2 = ( BasicDBObject )list.get( 1 );
if( !obj2.get( "b" ).equals( new Double( 2.2 ) ) ) { throw new Exception(); }
if( !obj2.get( "c" ).equals( "hoge" ) ) { throw new Exception(); }
}

{
list = MMongoUtil.getListFromAction( "db.foo.find({'a':1},{'b':2},10,20)", "find" );
Integer obj3 = ( Integer )list.get( 2 );
if( !obj3.equals( new Integer( 10 ) ) ) { throw new Exception(); }
}
*/

if( !MMongoUtil.getCollNameFromAction( "db.service.find()", "find" ).equals( "service" ) ){ throw new Exception(); }
if( !MMongoUtil.getCollNameFromAction( "db.service.find({})", "find" ).equals( "service" ) ){ throw new Exception(); }
if( !MMongoUtil.getCollNameFromAction( "db.service.find({},{'a':1})", "find" ).equals( "service" ) ){ throw new Exception(); }
if( !MMongoUtil.getCollNameFromAction( "db.blog.posts.find()", "find" ).equals( "blog.posts" ) ){ throw new Exception(); }

}
//--------------------------------------------------------------------------------
public static void p( Object o )
{
System.out.println( o );
}
//--------------------------------------------------------------------------------
}
