package net.jumperz.net;

import java.util.*;

import net.jumperz.util.MStringUtil;
import net.jumperz.util.MSystemUtil;

/**
 * test: asputil_test.clj
 */
public final class MAspXUtil
{
//--------------------------------------------------------------------------------
private static final String fastUrlDecodeIgnoreError( String s )
{
try
	{
	return MStringUtil.fastUrlDecode( s );
	}
catch( Exception e )
	{
	return s;
	}
}
//--------------------------------------------------------------------------------
/*
 * The name of the returned list is decoded
 */
private static final List getPollutedParameterListImpl( final List paramList )
{
if( paramList == null )
	{
	return null;
	}
final Map map1 = new TreeMap();
final Map map2 = new TreeMap();

for( int i = 0; i < paramList.size(); ++i )
	{
	final MAbstractParameter param = ( MAbstractParameter )paramList.get( i );
	String name = null;
	boolean shouldBeDecoded = false;
	
	if( param.getType() != MParameter.MULTIPART
	 && param.getType() != MParameter.COOKIE
	  )
		{
		shouldBeDecoded = true;
		}
	
		//ignore file upload
	if( param.getType() == MParameter.MULTIPART )
		{
		MMultipartParameter multipartParam = ( MMultipartParameter )param;
		if( multipartParam.hasFilename() )
			{
			continue;
			}
		}
	
	if( shouldBeDecoded )
		{
		name  = MUnicodeUrlDecoder.decodeForOldIIS( param.getName() );
		}
	else
		{
		name = param.getName();		
		}
	
	String value = null;
	if( shouldBeDecoded )
		{
		value = MUnicodeUrlDecoder.decodeForOldIIS( param.getValue() );
		}
	else
		{
		value = param.getValue();
		}
	
	String value2 = ( String )map2.get( name );
	if( value2 != null )
		{
		StringBuffer buf = new StringBuffer( 32 );
		buf.append( value2 );
		buf.append( "," );
		buf.append( value );
		map2.put( name, buf.toString() );
		}
	else
		{
		String value1 = ( String )map1.get( name );
		if( value1 == null )
			{
			map1.put( name, value );
			}
		else
			{	//move map1 to map2
			StringBuffer buf = new StringBuffer( 32 );
			buf.append( value1 );
			buf.append( "," );
			buf.append( value );
			map2.put( name, buf.toString() );
			map1.remove( name );
			}
		}
	}

if( map2.size() == 0 )
	{
	return null;
	}

final Iterator p = map2.keySet().iterator();
final List pollutedList = new ArrayList( map2.size() );
while( p.hasNext() )
	{
	String name = ( String )p.next();
	String value = ( String )map2.get( name );
	pollutedList.add( new MParameter( name, value, MParameter.POLLUTED ) );
	}

//for test for hashmap
//Collections.sort( pollutedList );

return pollutedList;
}
//--------------------------------------------------------------------------------
private static final void removeSameNameParameters( List targetList, List fromList )
{
final Set fromNameSet = new TreeSet();
for( int i = 0; i < fromList.size(); ++i )
	{
	final MParameter fromParam = ( MParameter )fromList.get( i );
	fromNameSet.add( fastUrlDecodeIgnoreError( fromParam.getName() ) );
	}
Iterator p = fromNameSet.iterator();
while( p.hasNext() )
	{
	String fromParamName = ( String )p.next();
	for( int i = 0; i < targetList.size(); ++i )
		{
		final MParameter targetParam = ( MParameter )targetList.get( i );
		final String targetParamName = fastUrlDecodeIgnoreError( targetParam.getName() );
		if( targetParamName.equals( fromParamName ) )
			{
			targetList.remove( i );
			--i;
			}
		}
	}
}
//--------------------------------------------------------------------------------
private static final List getNewList( List origin )
{
if( origin == null )
	{
	return new ArrayList( 0 );
	}
else
	{
	return new ArrayList( origin );
	}
}
//--------------------------------------------------------------------------------
public static final List getPollutedParameterList( final MHttpRequest request )
{
Set pollutedParameterSet = new TreeSet();

MSystemUtil.addAll( pollutedParameterSet, getAspRequest             ( request ) );
MSystemUtil.addAll( pollutedParameterSet, getAspxRequestQueryString ( request ) );
MSystemUtil.addAll( pollutedParameterSet, getAspxRequestForm        ( request ) );
MSystemUtil.addAll( pollutedParameterSet, getAspxRequestParam       ( request ) );

return new ArrayList( pollutedParameterSet );
}
//--------------------------------------------------------------------------------
/* Old Version ASP Request( "foo" ):
 * if QueryString then QueryString
 * else if Body then Body
 * else if Cookie then Cookie but one so ignore Cookie
 */
public static final List getAspRequest( final MHttpRequest request )
{
final List uriParameterList    = getNewList( request.getParameterList( MAbstractParameter.URI ) );
final List bodyParameterList   = getNewList( request.getBodyParameterList() );

final List uriPollutedParameterList    = getNewList( getPollutedParameterListImpl( uriParameterList ) );

removeSameNameParameters( bodyParameterList, uriParameterList );
final List bodyPollutedParameterList   = getNewList( getPollutedParameterListImpl( bodyParameterList ) );

final List pollutedParameterList = new ArrayList();
pollutedParameterList.addAll( uriPollutedParameterList );
pollutedParameterList.addAll( bodyPollutedParameterList );

return pollutedParameterList;
}
//--------------------------------------------------------------------------------
/*
 * See only queryString
 */
public static final List getAspxRequestQueryString( final MHttpRequest request )
{
return getPollutedParameterListImpl( request.getParameterList( MAbstractParameter.URI ) );
}
//--------------------------------------------------------------------------------
/*
 * See only body ( urlencoded or multipart )
 */
public static final List getAspxRequestForm( final MHttpRequest request )
{
if( !request.hasBody() )
	{
	return null;
	}

if( request.getBodyType() == MHttpRequest.BODY_TYPE_URLENCODED )
	{
	return getPollutedParameterListImpl( MSystemUtil.avoidNullList( request.getBodyParameterList() ) );
	}
else if( request.getBodyType() == MHttpRequest.BODY_TYPE_MULTIPART )
	{
	return getPollutedParameterListImpl( MSystemUtil.avoidNullList( request.getMultipartParameterList() ) );
	}
else
	{
	return null;	
	}
}
//--------------------------------------------------------------------------------
public static final boolean isPolluted( final MHttpRequest request )
{
List result = getAspxRequestParam( request );
if( result == null )
	{
	return false;
	}
else
	{
	return result.size() > 0;
	}
}
//--------------------------------------------------------------------------------
/*
 * See all ( QueryString, Body, Cookie )
 */
public static final List getAspxRequestParam( final MHttpRequest request )
{
List paramList = new ArrayList();

	//QueryString
MSystemUtil.addAll( paramList, request.getParameterList( MAbstractParameter.URI ) );

	//Body
if( request.hasBody() )
	{
	if( request.getBodyType() == MHttpRequest.BODY_TYPE_URLENCODED )
		{
		MSystemUtil.addAll( paramList,  request.getBodyParameterList() );
		}
	else if( request.getBodyType() == MHttpRequest.BODY_TYPE_MULTIPART )
		{
		MSystemUtil.addAll( paramList, request.getMultipartParameterList() );		
		}
	}

	//Cookie
MSystemUtil.addAll( paramList, request.getCookieList() );

return getPollutedParameterListImpl( paramList );
}
//--------------------------------------------------------------------------------
/*
 * See two ( QueryString, Cookie )
 * NOT USED ....
 */
public static final List getPollutedParameterList4( final MHttpRequest request )
{
List paramList = new ArrayList();
MSystemUtil.addAll( paramList, request.getParameterList( MAbstractParameter.URI ) );
MSystemUtil.addAll( paramList, request.getCookieList() );
return getPollutedParameterListImpl( paramList );
}
//--------------------------------------------------------------------------------
}