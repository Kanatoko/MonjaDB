package net.jumperz.sql;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.math.*;
import java.util.*;
import net.jumperz.util.*;

public class MSqlUtil
{
private static final String CHARSET = "ISO-8859-1";
public static boolean debug = false;
public static boolean ignoreTimestamp = true;

private static boolean initialized = false;
private static final Object mutex = new Object();

private static int total;
//--------------------------------------------------------------------------------
public static String getJdbcDriverClassName( String dbmsUrl )
{
if( dbmsUrl.indexOf( "jdbc:hsqldb:" ) == 0 )
	{
	return "net.jumperz.ext.org.hsqldb.jdbcDriver";
	}
else if( dbmsUrl.indexOf( "jdbc:postgresql" ) == 0 )
	{
	return "org.postgresql.Driver";
	}
else if( dbmsUrl.indexOf( "jdbc:firebirdsql" ) == 0 )
	{
	return "org.firebirdsql.jdbc.FBDriver";
	}
else if( dbmsUrl.indexOf( "jdbc:mysql" ) == 0 )
	{
	return "com.mysql.jdbc.Driver";
	}
else if( dbmsUrl.indexOf( "jdbc:interbase" ) == 0 )
	{
	return "interbase.interclient.Driver";
	}
else if( dbmsUrl.indexOf( "jdbc:sybase" ) == 0 )
	{
	return "com.sybase.jdbc.SybDriver";
	}
else if( dbmsUrl.indexOf( "jdbc:oracle" ) == 0 )
	{
	return "oracle.jdbc.driver.OracleDriver";
	}
else if( dbmsUrl.indexOf( "jdbc:sqlserver" ) == 0 )
	{
	return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	}
else if( dbmsUrl.indexOf( "jdbc:db2" ) == 0 )
	{
	return "com.ibm.db2.jcc.DB2Driver";
	//return "COM.ibm.db2.jdbc.app.DB2Driver";
	}
else if( dbmsUrl.indexOf( "jdbc:borland:dsremote" ) == 0 )
	{
	return "com.borland.datastore.jdbc.DataStoreDriver";
	}
else if( dbmsUrl.indexOf( "jdbc:derby:" ) == 0 )
	{
	return "org.apache.derby.jdbc.EmbeddedDriver";
	}
else if( dbmsUrl.indexOf( "jdbc:h2:" ) == 0 )
	{
	return "org.h2.Driver";
	}

return null;
}
// --------------------------------------------------------------------------------
public static void initDriverManager( String dbmsUrl )
{
if( !initialized )
	{
	String jdbcDriverClassName = MSqlUtil.getJdbcDriverClassName( dbmsUrl );
	try
		{
		Class.forName( jdbcDriverClassName ).newInstance();		
		}
	catch( Exception e )
		{
		}
	}

initialized = true;
}
// --------------------------------------------------------------------------------
public static Connection getConnection( String dbmsUrl, String dbmsUser, String dbmsPass )
throws SQLException
{
initDriverManager( dbmsUrl );
Connection conn = null;
int count = 0;
while( true )
	{
	try
		{
		conn = DriverManager.getConnection( dbmsUrl, dbmsUser, dbmsPass );
		if( dbmsUrl.indexOf( "9999" ) > 0 )
			{
			executeQuery( conn, "select count(*) /* " + dbmsUrl + "*/ from pg_user" );		
			}
		return conn;
		}
	catch( SQLException e )
		{
		//System.out.println( e );
		++count;
		if( conn != null )
			{
			closeConnection( conn );
			}
		
		if( count == 3 )
			{
			throw e;
			}
		else
			{
			MSystemUtil.sleep( ( count + 1  ) * 100 );
			}
		}
	}
}
//--------------------------------------------------------------------------------
public static Set getSet( Connection connection, String queryString )
throws SQLException
{
Set s = new HashSet();
ResultSet rs = MSqlUtil.executeQuery( connection, queryString );
while( rs.next() )
	{
	s.add( rs.getString( 1 ) );
	}
return s;
}
//--------------------------------------------------------------------------------
public static Map getIdNameMap( Connection connection, String queryString )
throws SQLException
{
Map m = new HashMap();
ResultSet rs = MSqlUtil.executeQuery( connection, queryString );
while( rs.next() )
	{
	m.put( rs.getString( "id" ), decode( rs.getString( "name" ) ) );
	}
return m;
}
//--------------------------------------------------------------------------------
public static List getList( Connection connection, String queryString )
throws SQLException
{
return resultSetToList( executeQuery2( connection, queryString ), new HashSet() );
}
//--------------------------------------------------------------------------------
public static List getList2( Connection connection, String queryString, Set decodeSet )
throws SQLException
{
return resultSetToList( executeQuery2( connection, queryString ), decodeSet );
}
//--------------------------------------------------------------------------------
public static List getList2( Connection connection, String queryString, MObjectArray args, Set decodeSet )
throws SQLException
{
return resultSetToList( executeQuery2( connection, queryString, args ), decodeSet );
}
//--------------------------------------------------------------------------------
public static List getPlainList( Connection connection, String queryString, Object o )
throws SQLException
{
MObjectArray array = new MObjectArray();
array.add( o );
return resultSetToPlainList( executeQuery2( connection, queryString, array ) );
}
//--------------------------------------------------------------------------------
public static List getPlainList( Connection connection, String queryString )
throws SQLException
{
return resultSetToPlainList( executeQuery2( connection, queryString, new MObjectArray() ) );
}
// --------------------------------------------------------------------------------
public static List getList2( Connection connection, String queryString )
throws SQLException
{
return resultSetToList( executeQuery2( connection, queryString, new MObjectArray() ) );
}
// --------------------------------------------------------------------------------
public static List getList2( Connection connection, String queryString, Object o )
throws SQLException
{
return resultSetToList( executeQuery2( connection, queryString, new MObjectArray( o ) ) );
}
//--------------------------------------------------------------------------------
public static List getList2( Connection connection, String queryString, MObjectArray args )
throws SQLException
{
return resultSetToList( executeQuery2( connection, queryString, args ) );
}
//--------------------------------------------------------------------------------
public static List resultSetToList( ResultSet rs )
throws SQLException
{
return resultSetToList( rs, new HashSet() );
}
// --------------------------------------------------------------------------------
public static Map resultSetToMap( ResultSet rs )
throws SQLException
{
return resultSetToMap( rs, new HashSet() );
}
// --------------------------------------------------------------------------------
public static Map resultSetToMap( ResultSet rs, Set decodeSet )
throws SQLException
{
List l = resultSetToList( rs, decodeSet );
if( l.size() > 0 )
	{
	return ( Map )l.get( 0 );
	}
else
	{
	return new MJSMap();
	}
}
//--------------------------------------------------------------------------------
public static List resultSetToPlainList( ResultSet rs )
throws SQLException
{
ResultSetMetaData md = rs.getMetaData();
int columnCount = md.getColumnCount();
List resultList = new ArrayList();
while( rs.next() )
	{
	Map data = new HashMap( columnCount );
	for( int i = 0; i < columnCount; ++i )
		{
		String columnName = md.getColumnName( i + 1 ).toLowerCase();
		String value = rs.getString( i + 1 );
		if( value == null )
			{
			value = "";
			}
		data.put( columnName, value );			
		}
	resultList.add( data );
	}

return resultList;
}
//--------------------------------------------------------------------------------
public static List resultSetToList( ResultSet rs, Set decodeSet )
throws SQLException
{
ResultSetMetaData md = rs.getMetaData();
int columnCount = md.getColumnCount();
List compList = new ArrayList();
for( int i = 0; i < columnCount; ++i )
	{
	compList.add( md.getColumnName( i + 1 ).toLowerCase() );
	}
List resultList = new ArrayList();
while( rs.next() )
	{
	//Map result = new HashMap( columnCount );
	Map result = new MJSMap( new MListComparator( compList ) );
	for( int i = 0; i < columnCount; ++i )
		{
		String columnName = md.getColumnName( i + 1 ).toLowerCase();
		
		if( ignoreTimestamp == false
	 	 && md.getColumnType( i + 1 ) == Types.TIMESTAMP
		  )
			{
			Timestamp value = rs.getTimestamp( i + 1 );
			if( value == null )
				{
				result.put( columnName, "" );
				}
			else
				{
				result.put( columnName, value );			
				}
			}
		else
			{
			String value = rs.getString( i + 1 );
			if( value == null )
				{
				value = "";
				}
			if( decodeSet.contains( columnName ) )
				{
				value = decode( value );
				}
			result.put( columnName, value );			
			}
		}
	resultList.add( result );
	}

return resultList;
}
//--------------------------------------------------------------------------------
public static Map getMap( Connection connection, String queryString )
throws SQLException
{
return resultSetToMap( executeQuery2( connection, queryString ), new HashSet() );
}
//--------------------------------------------------------------------------------
public static Map getMap2( Connection connection, String queryString, Set decodeSet )
throws SQLException
{
return resultSetToMap( executeQuery2( connection, queryString ), decodeSet );
}
//--------------------------------------------------------------------------------
public static Map getMap2( Connection connection, String queryString, MObjectArray args, Set decodeSet )
throws SQLException
{
return resultSetToMap( executeQuery2( connection, queryString, args ), decodeSet );
}
//--------------------------------------------------------------------------------
public static Map getMap2( Connection connection, String queryString, MObjectArray args )
throws SQLException
{
return resultSetToMap( executeQuery2( connection, queryString, args ) );
}
//--------------------------------------------------------------------------------
public static Map getMap2( Connection connection, String queryString, Object o )
throws SQLException
{
return resultSetToMap( executeQuery2( connection, queryString, new MObjectArray( o ) ) );
}
// --------------------------------------------------------------------------------
public static List getSimpleList( Connection connection, String queryString, MObjectArray args )
throws SQLException
{
ResultSet rs = MSqlUtil.executeQuery2( connection, queryString, args );
List l = new ArrayList();
while( rs.next() )
	{
	l.add( rs.getString( 1 ) );
	}
return l;
}
// --------------------------------------------------------------------------------
public static List getSimpleList( Connection connection, String queryString )
throws SQLException
{
ResultSet rs = MSqlUtil.executeQuery2( connection, queryString );
List l = new ArrayList();
while( rs.next() )
	{
	l.add( rs.getString( 1 ) );
	}
return l;
}
//--------------------------------------------------------------------------------
/*
//--------------------------------------------------------------------------------
public static List getList2( Connection connection, String queryString )
throws SQLException
{
return resultSetToList( executeQuery2( connection, queryString ) );
}
//--------------------------------------------------------------------------------
public static List getList2( Connection connection, String queryString, MObjectArray args )
throws SQLException
{
return resultSetToList( executeQuery2( connection, queryString, args ) );
}
//--------------------------------------------------------------------------------
public static List resultSetToList( ResultSet rs )
throws SQLException
{
ResultSetMetaData md = rs.getMetaData();

List list = new ArrayList();
int count = md.getColumnCount();

while( rs.next() )
	{
	String[] strArray = new String[ count ];
	for( int i = 0; i < count; ++i )
		{
		String tmpStr = rs.getString( i + 1 );
		if( tmpStr == null )
			{
			strArray[ i ] = "";
			}
		else
			{
			strArray[ i ] = tmpStr;
			}
		}
	list.add( strArray );
	}
return list;
}
//--------------------------------------------------------------------------------
public static List resultSetToList( ResultSet rs, String charsetName )
throws SQLException
{
ResultSetMetaData md = rs.getMetaData();

List list = new ArrayList();
int count = md.getColumnCount();

while( rs.next() )
	{
	String[] strArray = new String[ count ];
	for( int i = 0; i < count; ++i )
		{
		String tmpStr = rs.getString( i + 1 );
		if( tmpStr == null )
			{
			strArray[ i ] = "";
			}
		else
			{
			strArray[ i ] = decode( tmpStr , charsetName );
			}
		}
	list.add( strArray );
	}
return list;
}*/
//--------------------------------------------------------------------------------
public static BigDecimal getBigDecimal2( Connection connection, String queryString, MObjectArray args )
throws SQLException
{
ResultSet rs = MSqlUtil.executeQuery2( connection, queryString, args );
BigDecimal result = null;
rs.next();
String str = rs.getString( 1 );
if( str == null )
	{
	result = new BigDecimal( 0 );
	}
else
	{
	result = new BigDecimal( str );
	}
return result;
}
// --------------------------------------------------------------------------------
public static boolean getBoolean2( Connection connection, String queryString )
throws SQLException
{
return getBoolean2( connection, queryString, new MObjectArray() );
}
// --------------------------------------------------------------------------------
public static boolean getBoolean2( Connection connection, String queryString, Object o )
throws SQLException
{
return getBoolean2( connection, queryString, new MObjectArray( o ) );
}
// --------------------------------------------------------------------------------
public static boolean getBoolean2( Connection connection, String queryString, MObjectArray args )
throws SQLException
{
ResultSet rs = MSqlUtil.executeQuery2( connection, queryString, args );
if( rs.next() )
	{
	return rs.getBoolean( 1 );
	}
return false;
}
//--------------------------------------------------------------------------------
public static long getLong( Connection connection, String queryString )
throws SQLException
{
ResultSet rs = MSqlUtil.executeQuery2( connection, queryString );
rs.next();
String str = rs.getString( 1 );
long result;
if( str == null )
	{
	result = 0;
	}
else
	{
	result = Long.parseLong( str );
	}
return result;
}
//--------------------------------------------------------------------------------
public static int getInt2( Connection connection, String queryString )
throws SQLException
{
ResultSet rs = MSqlUtil.executeQuery2( connection, queryString );
rs.next();
String str = rs.getString( 1 );
int result;
if( str == null )
	{
	result = 0;
	}
else
	{
	result = Integer.parseInt( str );
	}
return result;
}
// --------------------------------------------------------------------------------
public static int getInt2( Connection connection, String queryString, int i )
throws SQLException
{
return getInt2( connection, queryString, new MObjectArray( i ) );
}
// --------------------------------------------------------------------------------
public static int getInt2( Connection connection, String queryString, Object arg )
throws SQLException
{
return getInt2( connection, queryString, new MObjectArray( arg ) );
}
//--------------------------------------------------------------------------------
public static int getInt2( Connection connection, String queryString, MObjectArray args )
throws SQLException
{
ResultSet rs = MSqlUtil.executeQuery2( connection, queryString, args );
if( !rs.next() )
	{
	return 0;
	}

String str = rs.getString( 1 );
int result;
if( str == null )
	{
	result = 0;
	}
else
	{
	result = Integer.parseInt( str );
	}
return result;
}
//--------------------------------------------------------------------------------
public static int executeUpdate2( Connection connection, String queryString, MObjectArray args )
throws SQLException
{
long start = System.currentTimeMillis();
if( queryString.indexOf( "???" ) >= 0 )
	{
	queryString = queryString.replaceFirst( "\\?\\?\\?", args.toQues() );
	}

PreparedStatement ps = connection.prepareStatement( queryString );
int index = 1;
List list = args.toList();
for( int i = 0; i < list.size(); ++i, ++index )
	{
	Object arg = list.get( i );
	if( arg == null )
		{
		ps.setString( index, null );
		}
	else if( arg instanceof Character )
		{
		ps.setString( index, arg + "" );
		}
	else if( arg instanceof String )
		{
		ps.setString( index, ( String )arg );
		}
	else if( arg instanceof Integer )
		{
		ps.setInt( index, ( ( Integer )arg ).intValue() );
		}
	else if( arg instanceof Long )
		{
		ps.setLong( index, ( ( Long )arg ).longValue() );
		}
	else if( arg instanceof Boolean )
		{
		ps.setBoolean( index, ( ( Boolean )arg ).booleanValue() );
		}
	else if( arg instanceof BigDecimal )
		{
		ps.setBigDecimal( index, ( BigDecimal )arg );
		}
	else if( arg instanceof Timestamp )
		{
		ps.setTimestamp( index, ( Timestamp )arg );
		}
	else if( arg instanceof Calendar )
		{
		Calendar c = ( Calendar )arg;
		ps.setTimestamp( index, new Timestamp( c.getTimeInMillis() ) );		
		}
	}
int result = ps.executeUpdate();
if( debug )
	{
	long passed = System.currentTimeMillis() - start;
	total += passed;
	System.out.println( queryString + " : " + passed + " : " + total );
	}
return result;
}
//--------------------------------------------------------------------------------
public static ResultSet executeQuery2( Connection connection, String queryString )
throws SQLException
{
return executeQuery2( connection, queryString, new MObjectArray() );
}
// --------------------------------------------------------------------------------
public static ResultSet executeQuery2( Connection connection, String queryString, Object o )
throws SQLException
{
return executeQuery2( connection, queryString, new MObjectArray( o ) );
}
// --------------------------------------------------------------------------------
public static int executeUpdate2( Connection connection, String queryString, int i )
throws SQLException
{
return executeUpdate2( connection, queryString, new MObjectArray( i ) );
}
// --------------------------------------------------------------------------------
public static int executeUpdate2( Connection connection, String queryString, Object o )
throws SQLException
{
return executeUpdate2( connection, queryString, new MObjectArray( o ) );
}
//--------------------------------------------------------------------------------
public static ResultSet executeQuery2( Connection connection, String queryString, MObjectArray args )
throws SQLException
{
long start = System.currentTimeMillis();
if( queryString.indexOf( "???" ) >= 0 )
	{
	queryString = queryString.replaceFirst( "\\?\\?\\?", args.toQues() );
	}

Timestamp a;
PreparedStatement ps = connection.prepareStatement( queryString );
int index = 1;
List list = args.toList();
for( int i = 0; i < list.size(); ++i, ++index )
	{
	Object arg = list.get( i );
	if( arg instanceof String )
		{
		ps.setString( index, ( String )arg );
		}
	else if( arg instanceof Integer )
		{
		ps.setInt( index, ( ( Integer )arg ).intValue() );
		}
	else if( arg instanceof Long )
		{
		ps.setLong( index, ( ( Long )arg ).longValue() );
		}
	else if( arg instanceof Boolean )
		{
		ps.setBoolean( index, ( ( Boolean )arg ).booleanValue() );
		}
	else if( arg instanceof BigDecimal )
		{
		ps.setBigDecimal( index, ( BigDecimal )arg );
		}
	else if( arg instanceof Timestamp )
		{
		ps.setTimestamp( index, ( Timestamp )arg );
		}
	else if( arg instanceof Calendar )
		{
		Calendar c = ( Calendar )arg;
		ps.setTimestamp( index, new Timestamp( c.getTimeInMillis() ) );		
		}
	}
ResultSet resultSet = ps.executeQuery();
if( debug )
	{
	long passed = System.currentTimeMillis() - start;
	total += passed;
	System.out.println( queryString + " : " + passed + " : " + total );
	}
return resultSet;
}
// --------------------------------------------------------------------------------
public static String getString2( Connection connection, String queryString, Object o )
throws SQLException
{
return getString2( connection, queryString, new MObjectArray( o ) );
}
//--------------------------------------------------------------------------------
public static String getString2( Connection connection, String queryString, MObjectArray args )
throws SQLException
{
ResultSet rs = MSqlUtil.executeQuery2( connection, queryString, args );
if( rs.next() )
	{
	return rs.getString( 1 );
	}
else
	{
	return "";
	}
}
// --------------------------------------------------------------------------------
public static void executeUpdate( Connection connection, String queryString )
throws SQLException
{
executeUpdate( connection, queryString, null );
}
//-------------------------------------------------------------------------------
public static void executeUpdate( Connection connection, String queryString, String[] args )
throws SQLException
{
PreparedStatement pStatement = connection.prepareStatement( queryString );
if( args != null )
	{
	for( int i = 0; i < args.length; ++i )
		{
		pStatement.setString( i + 1, args[ i ] );
		}
	}
pStatement.executeUpdate();
}
//-------------------------------------------------------------------------------
public static ResultSet executeQuery( Connection connection, String queryString )
throws SQLException
{
return MSqlUtil.executeQuery( connection, queryString, null );
}
//-------------------------------------------------------------------------------
public static ResultSet executeIntQuery( Connection connection, String queryString, int[] args )
throws SQLException
{
PreparedStatement pStatement = connection.prepareStatement( queryString );
if( args != null )
	{
	for( int i = 0; i < args.length; ++i )
		{
		pStatement.setInt( i + 1, args[ i ] );
		}
	}
return pStatement.executeQuery();
}
//-------------------------------------------------------------------------------
public static ResultSet executeQuery( Connection connection, String queryString, String[] args )
throws SQLException
{
PreparedStatement pStatement = connection.prepareStatement( queryString );
if( args != null )
	{
	for( int i = 0; i < args.length; ++i )
		{
		pStatement.setString( i + 1, args[ i ] );
		}
	}
return pStatement.executeQuery();
}
//--------------------------------------------------------------------------------
public static int getInt( Connection connection, String queryString )
throws SQLException
{
return getInt( connection, queryString, null );
}
//-------------------------------------------------------------------------------
public static int getInt( Connection connection, String queryString, String[] args )
throws SQLException
{
PreparedStatement pStatement = connection.prepareStatement( queryString );
if( args != null )
	{
	for( int i = 0; i < args.length; ++i )
		{
		pStatement.setString( i + 1, args[ i ] );
		}
	}
ResultSet resultSet = pStatement.executeQuery();
resultSet.next();
return resultSet.getInt( 1 );
}
//--------------------------------------------------------------------------------
public static String getString( Connection connection, String queryString )
throws SQLException
{
return MSqlUtil.getString( connection, queryString, null );
}
//--------------------------------------------------------------------------------
public static String getString( Connection connection, String queryString, String[] args )
throws SQLException
{
PreparedStatement pStatement = connection.prepareStatement( queryString );
if( args != null )
	{
	for( int i = 0; i < args.length; ++i )
		{
		pStatement.setString( i + 1, args[ i ] );
		}
	}
ResultSet resultSet = pStatement.executeQuery();

if( resultSet.next() )
	{
	return resultSet.getString( 1 );
	}
else
	{
	return "";
	}
}
//--------------------------------------------------------------------------------
public static String decode( String in )
{
try
	{
	String tmpStr = java.net.URLDecoder.decode( in, CHARSET );
	byte[] byteArray = tmpStr.getBytes( CHARSET );
	return new String( byteArray, CHARSET );
	}
catch( UnsupportedEncodingException e )
	{
	e.printStackTrace();
	}
return null;
}
//----------------------------------------------------------------------------------------
public static String encode( String in )
{
try
	{
	byte[] byteArray = in.getBytes( CHARSET );
	String tmpStr = new String( byteArray, CHARSET );
	return java.net.URLEncoder.encode( tmpStr, CHARSET );
	}
catch( UnsupportedEncodingException e )
	{
	e.printStackTrace();
	}
return null;
}
// --------------------------------------------------------------------------------
public static String getString( ResultSet rs, String columnName )
throws SQLException
{
String s = rs.getString( columnName );
if( s == null )
	{
	s = "";
	}
return s;
}
// --------------------------------------------------------------------------------
public static void closeConnection( Connection conn )
{
try
	{
	if( conn != null && !conn.isClosed() )
		{
		conn.close();
		}
	}
catch( SQLException e )
	{
	e.printStackTrace();
	}
}
//----------------------------------------------------------------------------------------
}