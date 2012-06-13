package net.jumperz.mongo;

import com.mongodb.*;
import com.mongodb.util.*;
import com.mongodb.ServerAddress.*;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.*;
import java.util.*;

import org.bson.types.Code;
import org.bson.types.ObjectId;
import org.bson.types.Symbol;

import net.jumperz.sql.MSqlUtil;
import net.jumperz.util.*;

public class MMongoUtil
{
public static final String INC = "$inc";
public static final String SET = "$set";
public static final String OR  = "$or";
public static final String ADDTOSET = "$addToSet";
private static final int MAX_COUNT = 100000;
//private static final String skipMatch  = "\\.\\s*(skip\\s*\\(\\s*[0-9]+\\s*\\))(:?\\W+limit[^a-zA-Z]*)*$";
//private static final String limitMatch = "\\.\\s*(limit\\s*\\(\\s*[0-9]+\\s*\\))(:?\\W+skip[^a-zA-Z]*)*$";

//--------------------------------------------------------------------------------
public static BasicDBObject parseFindQuery2( DB db, String findQueryStr )
throws IOException
{
String collName = getCollNameFromAction( findQueryStr, "find" );
findQueryStr = findQueryStr.replaceFirst( "db." + collName, "a" );

String jsStr = MStreamUtil.streamToString( MStreamUtil.getResourceStream( "net/jumperz/mongo/parseFindQuery.txt" ) );
jsStr = jsStr.replaceFirst( "//_QUERY_", findQueryStr );
//System.out.println( jsStr );

BasicDBObject result = ( BasicDBObject )db.eval( jsStr, null );

result.remove( "find" );
result.remove( "limit" );
result.remove( "skip" );

//System.out.println( result );

return result;
}
//--------------------------------------------------------------------------------
public static MFindQuery getNextItemsQuery( DB db, MFindQuery fq, int maxFindResults )
{
int skip = fq.getSkipArg();
int limit = fq.getLimitArg();
if( limit == -1 )
	{
	limit = maxFindResults;
	}
else if( maxFindResults < limit )
	{
	limit = maxFindResults;
	}
if( skip <= 0 )
	{
	skip = limit;
	}
else
	{
	skip += limit;
	}
MFindQuery nextQuery = ( MFindQuery )fq.clone();
nextQuery.setSkipArg( skip );
nextQuery.setLimitArg( limit );
return nextQuery;
}
//--------------------------------------------------------------------------------
public static MFindQuery getPrevItemsQuery( DB db, MFindQuery fq, int maxFindResults )
{
int skip = fq.getSkipArg();
int limit = fq.getLimitArg();
if( limit == -1 )
	{
	limit = maxFindResults;
	}

if( skip <= 0 )
	{
	return null; //noop
	}
else
	{
	MFindQuery prevQuery = ( MFindQuery )fq.clone();
	if( skip >= limit )
		{
		prevQuery.setSkipArg( skip - limit );
		}
	else
		{
		prevQuery.setSkipArg( -1 );
		}
	return prevQuery;
	}
}
//--------------------------------------------------------------------------------
public static BasicDBList parseJsonToArray( DB db, String jsonStr )
{
int index1 = jsonStr.indexOf( '{' );
int index2 = jsonStr.indexOf( '[' );
if( index1 == -1 )
	{
		//invalid json
	return new BasicDBList();
	}
else
	{
	if( index2 == -1 )
		{
		jsonStr = "[" + jsonStr + "]";
		}
	else
		{
		if( index2 < index1 )
			{
				//array do nothing
			}
		else
			{
			jsonStr = "[" + jsonStr + "]";
			}
		}
	}
	
jsonStr = jsonStr.replaceAll( "\n|\r|\t", "" );
Object o = db.eval( jsonStr, null );
return ( BasicDBList )o;
}
//--------------------------------------------------------------------------------
public static MFindQuery parseFindQuery( DB db, String findQueryStr )
throws IOException
{
final String _origStr = findQueryStr;
String collName = getCollNameFromAction( findQueryStr, "find" );
findQueryStr = findQueryStr.replaceFirst( "db." + collName, "a" );

String jsStr = MStreamUtil.streamToString( MStreamUtil.getResourceStream( "net/jumperz/mongo/parseFindQuery.txt" ) );
jsStr = jsStr.replaceFirst( "//_QUERY_", findQueryStr );
//System.out.println( jsStr );

BasicDBObject result = ( BasicDBObject )db.eval( jsStr, null );

result.remove( "find" );
result.remove( "limit" );
result.remove( "skip" );
result.remove( "sort" );

//System.out.println( result );

MFindQuery fq = new MFindQuery( _origStr, result.toMap() );
fq.setCollName( collName );
return fq;
}
//--------------------------------------------------------------------------------
public static String findQueryToString( DB db, MFindQuery fq )
{
StringBuffer buf = new StringBuffer();
buf.append( "db." );
buf.append( fq.getCollName() );
buf.append( ".find(" );

String findArgJson = MRegEx.getMatch( "^\\[(.*)\\]$", toJson( db, fq.getFindArg(), true ) );
buf.append( findArgJson );
buf.append( ")" );

List invoked = fq.getInvokedFunctionNameList();
for( int i = 0; i < invoked.size(); ++i )
	{
	String functionName = ( String )invoked.get( i );
	if( functionName.equals( "skip" ) )
		{
		buf.append( ".skip( " );
		buf.append( fq.getSkipArg() );
		buf.append( " )" );
		}
	else if( functionName.equals( "limit" ) )
		{
		buf.append( ".limit( " );
		buf.append( fq.getLimitArg() );
		buf.append( " )" );
		}
	else if( functionName.equals( "sort" ) )
		{
		buf.append( ".sort( " );
		buf.append( toJson( db, fq.getSortArg() ) );
		buf.append( " )" );
		}	
	}

return buf.toString();
}
//--------------------------------------------------------------------------------
public static String toJson( DB db, Object obj, boolean removeCRLFandTAG )
{
String s = toJson( db, obj );
if( removeCRLFandTAG )
	{
	s = s.replaceAll( "\r", "" );
	s = s.replaceAll( "\n", "" );
	s = s.replaceAll( "\t", "" );
	}
return s;
}
//--------------------------------------------------------------------------------
public static String toJson( DB db, Object obj )
{
	//check class
if( obj instanceof Map
 || obj instanceof List
 )
	{
	return ( String )db.eval( "tojson(arguments[0])", new Object[]{ obj } ) ;
	}
else
	{
	return "type error";
	}
}
//--------------------------------------------------------------------------------
public static int[] getPrevValue( int skip, int limit )
{
/*
db.test.find().skip( 100 ).limit( 30 );
db.test.find().skip( 70 ).limit( 30 );
db.test.find().skip( 40 ).limit( 30 );
db.test.find().skip( 10 ).limit( 30 );
db.test.find().limit( 30 );
 */
int[] array = new int[ 2 ];
if( skip < limit )
	{
	array[ 0 ] = -1;
	array[ 1 ] = limit;
	}
else
	{
	array[ 0 ] = skip - limit;
	array[ 1 ] = limit;
	}
return array;
}
/*
//--------------------------------------------------------------------------------
public static String setSkipValue( String actionStr, int skip )
{
String match = MRegEx.getMatch( skipMatch , actionStr );
if( match.length() == 0 )
	{
	return actionStr;
	}
return "";

}
//--------------------------------------------------------------------------------
public static int getSkipFromFindQuery( String actionStr )
{
String match = MRegEx.getMatch( skipMatch , actionStr );
if( match.length() > 0 )
	{
	return MStringUtil.parseInt( MRegEx.getMatch( "[0-9]+", match ) );
	}
return -1;
}
*/
/*
//--------------------------------------------------------------------------------
public static int getLimitFromFindQuery( String actionStr )
{
String match = MRegEx.getMatch( limitMatch, actionStr );
if( match.length() > 0 )
	{
	return MStringUtil.parseInt( MRegEx.getMatch( "[0-9]+", match ) );
	}
return -1;
}
*/
//--------------------------------------------------------------------------------
public static Object getValueByCurrentType( String value, Class currentType )
//throws Exception
{
	//test code is in net.jumperz.app.MMonjaDBCore.test
try
	{
	if( currentType == Double.class )
		{
		try
			{
			return new Double( value );		
			}
		catch( Exception e )
			{
			if( value.matches( "^-?[0-9]+$" ) )
				{
				return new Long( value );
				}
			else
				{
				throw e;
				}
			}
		}
	else if( currentType == String.class )
		{
		return value;
		}
	else if( currentType == Integer.class )
		{
		return parseValue( value );
		}
	else if( currentType == Date.class )
		{
		DateFormat df = new SimpleDateFormat( "EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH );
		return df.parse( value );
		}
	else if( currentType == Boolean.class )
		{
		if( MStringUtil.meansTrue( value ) )
			{
			return new Boolean( true );
			}
		else
			{
			return parseValue( value );
			}
		}
	else if( currentType == ObjectId.class )
		{
		return new ObjectId( value );
		}
	else if( currentType == Long.class )
		{
		return new Long( value );
		}
	else if( currentType == Code.class )
		{
		return new Code( value );
		}
	else if( currentType == java.util.regex.Pattern.class )
		{
		try
			{
			return java.util.regex.Pattern.compile( value );
			}
		catch( Exception e )
			{
			return parseValue( value );
			}
		}
	else if( currentType == org.bson.types.Symbol.class )
		{
		return new Symbol( value );
		}
	else
		{
		return parseValue( value );
		}
	}
catch( Exception e )
	{
	return parseValue( value );
	}
}
//--------------------------------------------------------------------------------
private static Object parseValue( String value )
{
if( value.matches( "^-?[0-9]+$" ) )
	{
	try
		{
		return new Integer( value );
		}
	catch( Exception e )
		{
		}
	try
		{
		return new Double( value );
		}
	catch( Exception e )
		{
		}
	try
		{
		return new Long( value );
		}
	catch( Exception e )
		{
		}
	}
try
	{
	return JSON.parse( value );		
	}
catch( Exception e )
	{
	return value;
	}
}
//---------------------------------------------------j-----------------------------
public static java.util.List getNameListFromDataList( java.util.List dataList )
{
java.util.List nameList = new ArrayList();
for( int i = 0; i < dataList.size(); ++i )
	{
	Map data = ( ( BasicDBObject )dataList.get( i ) ).toMap();
	Iterator p = data.keySet().iterator();
	while( p.hasNext() )
		{
		String key = ( String )p.next();
		if( !nameList.contains( key ) )
			{
			nameList.add( key );
			}
		}
	}
return nameList;
}
//--------------------------------------------------------------------------------
public static String getCollNameFromAction( String actionStr, String actionName )
{
	//db.service.find() -> service
return MRegEx.getMatchIgnoreCase( "^db\\.([^\\(]+)\\." + actionName + "\\(", actionStr );
}
/*
//--------------------------------------------------------------------------------
public static String getArgStrFromAction( String actionStr, String actionName )
{
	//check skip
String skipStr = MRegEx.getMatch( skipMatch, actionStr );
if( skipStr.length() > 0 )
	{
	actionStr = actionStr.replaceFirst( skipMatch, "" );
	}
String limitStr = MRegEx.getMatch( limitMatch, actionStr );
if( limitStr.length() > 0 )
	{
	actionStr = actionStr.replaceFirst( limitMatch, "" );
	}
return MRegEx.getMatchIgnoreCase( "^db\\.[^\\(]+\\." + actionName + "\\((.*)\\)$", actionStr );
}
*/
/*
//--------------------------------------------------------------------------------
public static BasicDBList getListFromAction( String actionStr, String actionName )
{
String queryStr = getArgStrFromAction( actionStr, actionName );
BasicDBObject data = ( BasicDBObject ) JSON.parse( "{'dummy':[" + queryStr + "]}");
return ( BasicDBList )data.get( "dummy" );
}
*/
//--------------------------------------------------------------------------------
public static Mongo getMongo( File file, String prefix )
throws IOException
{
MProperties prop = new MProperties();
prop.load( new FileInputStream( file  ) );
String configStr = prop.getProperty( prefix + ".mongo" );
return getReplMongo( configStr );
}
//--------------------------------------------------------------------------------
private static Map dbo2jsm( DBObject data )
{
Map jsMap = new MJSMap();
Iterator p = data.keySet().iterator();
while( p.hasNext() )
	{
	String key = ( String )p.next();
	Object value = data.get( key );
	
	if( value instanceof Boolean )
		{
		Boolean b = ( Boolean )value;
		if( b.booleanValue() )
			{
			value = "t";
			}
		else
			{
			value = "f";
			}
		}
	
	if( !key.equals( "_id" ) )
		{
		jsMap.put( key, value );		
		}
	}
return jsMap;
}
//--------------------------------------------------------------------------------
/*
 * MSqlUtilの出力であるMJSMapと同じようなデータを出力するための関数
 * _idは削除される
 */
public static List getJSList( DBCursor cursor )
{
List list = getList( cursor );
List list2 = new ArrayList();
for( int i = 0; i < list.size(); ++i )
	{
	DBObject data = ( DBObject )list.get( i );
	list2.add( dbo2jsm( data ) );
	}
return list2;
}
//--------------------------------------------------------------------------------
public static List getList( DBCursor cursor )
{

try
	{
	int count = 0;
	List list = new ArrayList();
	while( cursor.hasNext() )
		{
		list.add( cursor.next() );
		++count;
		if( count == MAX_COUNT )
			{
			break;
			}
		}
	return list;
	}
finally
	{
	close( cursor );
	}
}
//--------------------------------------------------------------------------------
public static void close( DBCursor cursor )
{
if( cursor != null )
	{
	try
		{
		cursor.close();
		}
	catch( Exception ignored )
		{
		}
	}
}
//--------------------------------------------------------------------------------
public static Connection copyToTable( Connection conn, DBCursor cursor, String tableName )
throws SQLException
{
/*
 * cursorのデータを元にインメモリのデータベースを作る
 * cursorが0件の場合は作成されない
 */

boolean tableCreated = false;
List columnNameList = null;
while( cursor.hasNext() )
	{
	DBObject data = cursor.next();
	if( !tableCreated )
		{
		columnNameList = createTable( conn, data, tableName );
		tableCreated = true;
		}
	MObjectArray args = new MObjectArray();
	for( int i = 0; i < columnNameList.size(); ++i )
		{
		args.add( dateToTs( data.get( ( String )columnNameList.get( i ) ) ) );
		}
	MSqlUtil.executeUpdate2( conn, "insert into " + tableName + " values( ??? );", args );
	}

//debug System.out.println( MSqlUtil.getList( conn, "select * from " + tableName ) );
return conn;
}
//--------------------------------------------------------------------------------
private static Object dateToTs( Object value )
{
if( value instanceof java.util.Date )
	{
	java.util.Date date = ( java.util.Date )value;
	long time = date.getTime();
	Timestamp ts = new Timestamp( time );
	value = ts;
	}
return value;
}
//--------------------------------------------------------------------------------
public static Connection copyToTable( DBCursor cursor, String tableName )
throws SQLException
{
Connection conn = MSqlUtil.getConnection( "jdbc:h2:mem:", "sa", "" );
return copyToTable( conn, cursor, tableName );
}
//--------------------------------------------------------------------------------
private static List createTable( Connection conn, DBObject data, String tableName )
throws SQLException
{
List columnNameList = new ArrayList();
StringBuffer buf = new StringBuffer();
buf.append( "create table " );
buf.append( tableName );
buf.append( " (" );

Map map = data.toMap();
map.remove( "_id" );
Iterator p = map.keySet().iterator();
boolean isFirst = true;
while( p.hasNext() )
	{
	String key = ( String )p.next();
	columnNameList.add( key );
	Object value = map.get( key );
	String type = "";
	if( value instanceof String )
		{
		type = "text";
		}
	else if( value instanceof Integer || value instanceof Double )
		{
		type = "int";
		}
	else if( value instanceof java.util.Date )
		{
		type = "timestamp";
		}
	else if( value instanceof Boolean )
		{
		type = "boolean";
		}
	if( isFirst )
		{
		buf.append( "\n" );
		isFirst = false;
		}
	else
		{
		buf.append( ",\n" );
		}
	buf.append( key );
	buf.append( "\t" );
	buf.append( type );
	}
buf.append( ");" );
String sql1 = buf.toString();

try
	{
	MSqlUtil.executeUpdate( conn, sql1 );
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
return columnNameList;
}
//--------------------------------------------------------------------------------
public static DBCursor asc( DBCursor cursor, String orderBy )
{
return cursor.sort( new BasicDBObject( orderBy, new Integer( 1 ) ) );
}
//--------------------------------------------------------------------------------
public static DBCursor desc( DBCursor cursor, String orderBy )
{
return cursor.sort( new BasicDBObject( orderBy, new Integer( -1 ) ) );
}
//--------------------------------------------------------------------------------
public static List getJSList( DBCollection coll )
{
return getJSList( coll.find() );
}
//--------------------------------------------------------------------------------
public static List getList( DBCollection coll )
{
return getList( coll.find() );
}
//--------------------------------------------------------------------------------
public static List getJSList( DBCollection coll, String key, Object value )
{
DBObject query = new BasicDBObject( key, value );
DBCursor cursor = coll.find( query );
return getJSList( cursor );
}
//--------------------------------------------------------------------------------
public static List getSimpleList( DBCollection coll, String key, Object value, String fieldName )
{
DBObject query = new BasicDBObject( key, value );
DBCursor cursor = coll.find( query );
List list = new ArrayList();
while( cursor.hasNext() )
	{
	DBObject obj = cursor.next();
	list.add( obj.get( fieldName ) );
	}
return list;
}
//--------------------------------------------------------------------------------
public static List getList( DBCollection coll, String key, Object value, String fieldName )
{
DBObject query = new BasicDBObject( key, value );
DBObject keys = new BasicDBObject( fieldName, new Integer( 1 ) );
DBCursor cursor = coll.find( query, keys );
List list = new ArrayList();
while( cursor.hasNext() )
	{
	list.add( cursor.next() );
	}
return list;
}
//--------------------------------------------------------------------------------
public static List getList( DBCollection coll, String key, Object value )
{
DBObject query = new BasicDBObject( key, value );
DBCursor cursor = coll.find( query );
List list = new ArrayList();
while( cursor.hasNext() )
	{
	list.add( cursor.next() );
	}
return list;
}
//--------------------------------------------------------------------------------
public static String getOneString( DBCollection coll, String key, Object value, String fieldName )
{
DBObject obj = getOne( coll, key, value );
if( obj.containsField( fieldName ) )
	{
	return ( String )obj.get( fieldName );
	}
else
	{
	return "";
	}
}
//--------------------------------------------------------------------------------
public static Map getOneJSMap( DBCollection coll, String key, Object value )
{
return dbo2jsm( getOne( coll, key, value ) );
}
//--------------------------------------------------------------------------------
public static DBObject getOne( DBCollection coll, String key, Object value )
{
DBObject query = new BasicDBObject( key, value );
DBObject result = coll.findOne( query );
if( result != null )
	{
	return result;
	}
else
	{
	return new BasicDBObject();
	}
}
//--------------------------------------------------------------------------------
public static List getJoinResult( String joinKey, DBCursor cursor1, DBCursor cursor2 )
{
try
	{
	DBCursor cursor = cursor1;
	List resultList = new ArrayList();
	Map tmpMap = new HashMap();
	while( cursor.hasNext() )
		{
		Map map1 = ( ( DBObject )cursor.next() ).toMap();
		tmpMap.put( map1.get( joinKey ), map1 );
		}
	
	cursor = cursor2;
	while( cursor.hasNext() )
		{
		Map map2 = ( ( DBObject )cursor.next() ).toMap();
		Map map1 = ( Map )tmpMap.get( map2.get( joinKey ) );
		if( map1 != null )
			{
			map1.putAll( map2 );
			resultList.add( map1 );
			}
		}
	
	return resultList;	
	}
finally
	{
	close( cursor1 );
	close( cursor2 );
	}
}
//--------------------------------------------------------------------------------
public static List getJoinResult( String joinKey, DBCollection coll1, DBCollection coll2 )
{
List resultList = new ArrayList();
Map tmpMap = new HashMap();
DBCursor cursor = coll1.find();
while( cursor.hasNext() )
	{
	Map map1 = ( ( DBObject )cursor.next() ).toMap();
	tmpMap.put( map1.get( joinKey ), map1 );
	}

cursor = coll2.find();
while( cursor.hasNext() )
	{
	Map map2 = ( ( DBObject )cursor.next() ).toMap();
	Map map1 = ( Map )tmpMap.get( map2.get( joinKey ) );
	if( map1 != null )
		{
		map1.putAll( map2 );
		resultList.add( map1 );
		}
	}

return resultList;
}
//--------------------------------------------------------------------------------
public static int getFirstInt( DBCollection coll, String key )
{
return getFirstInt( coll, key, 0 );
}
//--------------------------------------------------------------------------------
public static int getFirstInt( DBCollection coll, String key, int defaultValue )
{
Object obj = coll.findOne();
if( obj == null )
	{
	return defaultValue;
	}

obj = ( ( DBObject )obj ).get( key );
if( obj == null )
	{
	return defaultValue;
	}

if( obj instanceof Double )
	{
	Double limitDouble = ( Double )obj;
	return (int)limitDouble.longValue();
	}
else
	{
	Integer limitInt = ( Integer )obj;
	return limitInt.intValue();
	}
}
//--------------------------------------------------------------------------------
public static Mongo getReplMongo( String mongoStr )
throws UnknownHostException
{
return getReplMongo( mongoStr, null );
}
//--------------------------------------------------------------------------------
public static Mongo getReplMongo( String mongoStr, MongoOptions options )
throws UnknownHostException
{
if( mongoStr == null || mongoStr.equals( "" ) )
	{
	mongoStr = "127.0.0.1";
	}

List addrList = new ArrayList();
String[] array = mongoStr.split( "," );
for( int i = 0; i < array.length; ++i )
	{
	int port = 27017;
	String host = "";
	int index = array[ i ].indexOf( ":" );
	if( index == -1 )
		{
		host = array[ i ];
		}
	else
		{
		host = array[ i ].substring( 0, index );
		port = MStringUtil.parseInt( array[ i ].substring( index ), 27017 );
		}
	
	addrList.add( new ServerAddress( host, port ) );
	}

if( options == null )
	{
	return new Mongo( addrList );
	}
else
	{
	return new Mongo( addrList, options );
	}
}
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
getReplMongo( "127.0.0.1:27017" );
getReplMongo( "127.0.0.1:27017, 192.168.3.100:27018, mongohost" );
}
//--------------------------------------------------------------------------------
private static void debug( Object o )
{
System.err.println( o );
}
//--------------------------------------------------------------------------------
public static WriteResult updateOne( DBCollection coll, String queryKey, Object queryValue, String updateKey, boolean updateValue )
{
return updateOne( coll, queryKey, queryValue, updateKey, new Boolean( updateValue ) );
}
/*
 * queryKey1 = queryValue1のドキュメントのupdateKey1項目の値をupdateValue1にする。
 * multi=false
 */
//--------------------------------------------------------------------------------
public static WriteResult updateOne( DBCollection coll, String queryKey, Object queryValue, String updateKey1, Object updateValue1, String updateKey2, Object updateValue2 )
{
DBObject query = new BasicDBObject( queryKey, queryValue );
DBObject setValue = new BasicDBObject();
setValue.put( updateKey1, updateValue1 );
setValue.put( updateKey2, updateValue2 );
DBObject newObj = new BasicDBObject( MMongoUtil.SET, setValue );

return coll.update( query, newObj, false, false, WriteConcern.SAFE );
}
//--------------------------------------------------------------------------------
public static WriteResult updateOne( DBCollection coll, String queryKey, Object queryValue, String updateKey1, Object updateValue1, String updateKey2, Object updateValue2, String updateKey3, Object updateValue3 )
{
DBObject query = new BasicDBObject( queryKey, queryValue );
DBObject setValue = new BasicDBObject();
setValue.put( updateKey1, updateValue1 );
setValue.put( updateKey2, updateValue2 );
setValue.put( updateKey3, updateValue3 );
DBObject newObj = new BasicDBObject( MMongoUtil.SET, setValue );

return coll.update( query, newObj, false, false, WriteConcern.SAFE );
}
//--------------------------------------------------------------------------------
public static WriteResult updateMulti( DBCollection coll, String queryKey, Object queryValue, String updateKey1, Object updateValue1, String updateKey2, Object updateValue2 )
{
DBObject setObj = new BasicDBObject();
setObj.put( updateKey1, updateValue1 );
setObj.put( updateKey2, updateValue2 );
DBObject newObj = new BasicDBObject( MMongoUtil.SET, setObj );
return coll.update( new BasicDBObject( queryKey, queryValue ), newObj, false, true, WriteConcern.SAFE );
}
//--------------------------------------------------------------------------------
public static WriteResult updateMulti( DBCollection coll, DBObject query, String updateKey1, Object updateValue1, String updateKey2, Object updateValue2 )
{
DBObject setObj = new BasicDBObject();
setObj.put( updateKey1, updateValue1 );
setObj.put( updateKey2, updateValue2 );
DBObject newObj = new BasicDBObject( MMongoUtil.SET, setObj );
return coll.update( query, newObj, false, true, WriteConcern.SAFE );
}
//--------------------------------------------------------------------------------
public static WriteResult updateMulti( DBCollection coll, DBObject query, String updateKey, Object updateValue )
{
DBObject newObj = new BasicDBObject( MMongoUtil.SET, new BasicDBObject( updateKey, updateValue ) );
return coll.update( query, newObj, false, true, WriteConcern.SAFE );
}
//--------------------------------------------------------------------------------
public static WriteResult updateMulti( DBCollection coll, String queryKey, Object queryValue, String updateKey, Object updateValue )
{
DBObject newObj = new BasicDBObject( MMongoUtil.SET, new BasicDBObject( updateKey, updateValue ) );
return coll.update( new BasicDBObject( queryKey, queryValue ), newObj, false, true, WriteConcern.SAFE );
}
//--------------------------------------------------------------------------------
public static WriteResult updateOne( DBCollection coll, String queryKey, Object queryValue, String updateKey, Object updateValue )
{
DBObject query = new BasicDBObject( queryKey, queryValue );
DBObject newObj = new BasicDBObject( MMongoUtil.SET, new BasicDBObject( updateKey, updateValue ) );
return coll.update( query, newObj, false, false, WriteConcern.SAFE );
}
//--------------------------------------------------------------------------------
public static WriteResult updateOne( DB db, String collName, String queryKey, Object queryValue, String updateKey, boolean updateValue )
{
return updateOne( db.getCollection( collName ), queryKey, queryValue, updateKey, updateValue );
}
//--------------------------------------------------------------------------------
public static WriteResult updateOne( DB db, String collName, String queryKey, Object queryValue, String updateKey, Object updateValue )
{
return updateOne( db.getCollection( collName ), queryKey, queryValue, updateKey, updateValue );
}
//--------------------------------------------------------------------------------
public static void GTE( DBObject ref, String key, int value )
{
ref.put( key, new BasicDBObject( QueryOperators.GTE, new Integer( value ) ) );
}
//--------------------------------------------------------------------------------
public static int getCount( DBCollection coll, String key1, Object value1, String key2, Object value2, String key3, Object value3 )
{
DBObject query = new BasicDBObject( key1, value1 );
query.put( key2, value2 );
query.put( key3, value3 );
return (int)coll.getCount( query );
}
//--------------------------------------------------------------------------------
public static int getCount( DBCollection coll, String key1, Object value1, String key2, Object value2 )
{
DBObject query = new BasicDBObject( key1, value1 );
query.put( key2, value2 );
return (int)coll.getCount( query );
}
//--------------------------------------------------------------------------------
public static int getCount( DBCollection coll, String key, Object value )
{
return (int)coll.getCount( new BasicDBObject( key, value  ) );
}
//--------------------------------------------------------------------------------
}
