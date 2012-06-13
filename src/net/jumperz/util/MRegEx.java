package net.jumperz.util;

import java.util.regex.*;
import java.util.*;

public class MRegEx
{
private static Map patternMap = new HashMap();
public static final String WORD_HEAD = "(?:\\A|\\W+?)";
public static final String WORD_TAIL = "(?:$|\\W+?)";
public static final String WORD_BETWEEN = "(?:\\W+?.*\\W+?|\\W+?)";
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
testReplaceFirst();
testReplaceAll();

if( containsIgnoreCase( "hoge fuga", "Hoge" + WORD_BETWEEN + "fuga" ) == false ) { throw new Exception(); }
if( containsIgnoreCase( "hoge  fuga", "Hoge" + WORD_BETWEEN + "fuga" ) == false ) { throw new Exception(); }
if( containsIgnoreCase( "hoge/*dummy*/fuga", "Hoge" + WORD_BETWEEN + "fuga" ) == false ) { throw new Exception(); }
if( containsIgnoreCase( "hoge gyoe  fuga", "Hoge" + WORD_BETWEEN + "fugA" ) == false ) { throw new Exception(); }
if( containsIgnoreCase( "hogeafuga", "Hoge" + WORD_BETWEEN + "fuga" ) == true ) { throw new Exception(); }
if( containsIgnoreCase( "hoge", WORD_HEAD + "hoge" ) == false ) { throw new Exception(); }
if( containsIgnoreCase( "Ahoge", WORD_HEAD + "hoge" ) == true ) { throw new Exception(); }
if( containsIgnoreCase( "x hoge", WORD_HEAD + "hoge" ) == false ) { throw new Exception(); }

testGetInnerTextAsList();

System.out.println( "OK." );
}
//--------------------------------------------------------------------------------
private static final void testReplaceAll()
throws Exception
{
if( !replaceAllIgnoreCase( "xxAABBxxxABAxxxBBAA", "[AB]{1,}", "X" ).equals( "xxXxxxXxxxX" ) ){ throw new Exception( "0" ); }
if( !replaceAllIgnoreCase( "", "[AB]{1,}", "a" ).equals( "" ) ){ throw new Exception( "1" ); }
if( !replaceAllIgnoreCase( "A**A", "\\*", "x" ).equals( "AxxA" ) ){ throw new Exception( "2" ); }
if( !replaceAllIgnoreCase( "A user=0 B", "user", "usr" ).equals( "A usr=0 B" ) ){ throw new Exception( "3" ); }
if( !replaceAllIgnoreCase( "A user=0 B", "(\\W)UsEr(\\W)", "$1usr$2" ).equals( "A usr=0 B" ) ){ throw new Exception( "4" ); }

}
//--------------------------------------------------------------------------------
private static final void testReplaceFirst()
throws Exception
{
if( !replaceFirst( "AAAbbb", "Z{1,}", "--" ).equals( "AAAbbb" ) ){ throw new Exception( "0" ); }
if( !replaceFirst( "AAAbbb", "A{1,}", "X" ).equals( "Xbbb" ) ){ throw new Exception( "1" ); }
if( !replaceFirst( "", "A{1,}", "X" ).equals( "" ) ){ throw new Exception( "2" ); }
if( !replaceFirst( "AAxxxAAxxxAA", "A{1,}", "B" ).equals( "BxxxAAxxxAA" ) ){ throw new Exception( "3" ); }
if( !replaceFirst( "xxAABBxxxABAxxxBBAA", "[AB]{1,}", "B" ).equals( "xxBxxxABAxxxBBAA" ) ){ throw new Exception( "4" ); }
}
//--------------------------------------------------------------------------------
public static String replaceAllIgnoreCase( String target, String regex, String to  )
{
return Pattern.compile( regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL ).matcher( target ).replaceAll( to );
/*
int len = regex.length();
if( len == 0 )
	{
	return target;
	}
StringBuffer buf = new StringBuffer( target.length() );
while( true )
	{
	String matchStr = getMatch( regex, target );
	len = matchStr.length();
	if( len == 0 )
		{
		break;
		}
	int pos = target.indexOf( matchStr );
	buf.append( target.substring( 0, pos ) );
	buf.append( to );
	target = target.substring( pos + len );
	}
buf.append( target );
System.out.println( buf.toString() );
return buf.toString();
*/
}
//--------------------------------------------------------------------------------
public static String replaceFirst( String target, String regex, String to  )
{
String matchStr = getMatch( regex, target );
if( matchStr.length() == 0 )
	{
	return target;
	}
else
	{
	int index = target.indexOf( matchStr );
	StringBuffer buf = new StringBuffer( target.length() );
	buf.append( target.substring( 0, index ) );
	buf.append( to );
	buf.append( target.substring( index + matchStr.length() ) );
	//System.out.println( buf.toString() );
	return buf.toString();
	}
}
//--------------------------------------------------------------------------------
private static void testGetInnerTextAsList()
throws Exception
{
List l1, l2;

l1 = new ArrayList();
l1.add( "aiueo" );
l2 = getInnerTextAsList( "<hogeaaaaaaaaaaeeeeeeeeeee>aiueo</hogeAAAAAAAAA>", "<[^>]*>", "</[a-zA-Z]*>" );
if( !l1.equals( l2 ) ){	throw new Exception( "-1" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l2 = getInnerTextAsList( "<hogeaaaaaaaaaaeeeeeeeeeee>aiueo</hoge>", "<[^>]*>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "-1" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l2 = getInnerTextAsList( "<hoge>aiueo</hoge>", "<[a-z]*>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "0" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l2 = getInnerTextAsList( "<hoge>aiueo</hoge>", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "1" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l2 = getInnerTextAsList( "a<hoge>aiueo</hoge>e", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "2" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l2 = getInnerTextAsList( "<hoge<hoge>aiueo</hoge>", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "3" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l2 = getInnerTextAsList( "<hoge>aiueo</hoge><hoge>", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "4" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l2 = getInnerTextAsList( "<hoge>aiueo</hoge><hoge>aaa", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "5" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l2 = getInnerTextAsList( "<Hoge>aiueo</hoge>", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "6" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l2 = getInnerTextAsList( "<HoGe>aiueo</hOge>", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "7" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l1.add( "12345" );
l2 = getInnerTextAsList( "<hoge>aiueo</hoge>aaa<hoge>12345</hoge>", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "8" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l1.add( "12345" );
l2 = getInnerTextAsList( "aaa<hoge>aiueo</hoge>aaa<hoge>12345</hoge>", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "9" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l1.add( "12345" );
l2 = getInnerTextAsList( "aaa<hoge>aiueo</hoge>aaa<hoge>12345</hoge>eee", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "10" );}

l1 = new ArrayList();
l1.add( "a('<hoge>aiueo')" );
l1.add( "12345" );
l2 = getInnerTextAsList( "<hoge>a('<hoge>aiueo')</hoge>aaa<hoge>12345</hoge>", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "11" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l1.add( "12345" );
l2 = getInnerTextAsList( "a</hoge>aaa<hoge>aiueo</hoge>aaa<hoge>12345</hoge>eee", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "12" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l1.add( "12345" );
l2 = getInnerTextAsList( "aaa<hoge>aiueo</hoge>aaa<hoge>12345</hoge>eee<hoge>aaaa", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "13" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l1.add( "12345" );
l2 = getInnerTextAsList( "aaa<hoge>aiueo</hoge>aaa<hoge>12345</hoge>eee</hoge>aaaa", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "14" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l1.add( "123\r\n45" );
l2 = getInnerTextAsList( "aaa<hoge>aiueo</hoge>aaa<hoge>123\r\n45</hoge>eee</hoge>aaaa", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "15" );}

l1 = new ArrayList();
l1.add( "aiueo" );
l1.add( "12345" );
l1.add( "" );
l2 = getInnerTextAsList( "aaa<hoge>aiueo</hoge>aaa<hoge>12345</hoge>eee<hoge></hoge>", "<hoge>", "</hoge>" );
if( !l1.equals( l2 ) ){	throw new Exception( "16" );}


}
//--------------------------------------------------------------------------------
public static List getInnerTextAsList( String target, String tagFrom, String tagTo )
{
if( tagFrom.equalsIgnoreCase( tagTo ) )
	{
		// :(
	return new ArrayList();
	}
try
	{
	List l = new ArrayList();
	while( true )
		{
		String matchFrom = getMatchIgnoreCase( tagFrom, target );
		if( matchFrom.equals( "" ) )
			{
			break;
			}
		int index1 = MStringUtil.indexOf( target, matchFrom );		
		target = target.substring( index1 + matchFrom.length() );
		
		String matchTo = getMatchIgnoreCase( tagTo, target );
		if( matchTo.equals( "" ) )
			{
			break;
			}
		int index2 = MStringUtil.indexOf( target, matchTo );

		l.add( target.substring( 0, index2 ) );
		target = target.substring( index2 + matchTo.length() );
		}
	return l;	
	}
catch( Exception e )
	{
	return new ArrayList();
	}
}
//------------------------------------------------------------------------------------------
public static String getMatch( String patternStr, String target )
{
Pattern pattern = Pattern.compile( patternStr, Pattern.DOTALL );
Matcher matcher = pattern.matcher( target );
if( matcher.find() )
	{
	if( matcher.groupCount() > 0 )
		{
		return matcher.group( 1 );
		}
	else
		{
		return target.substring( matcher.start(), matcher.end() );
		}
	}
else
	{
	return "";
	}
}
//--------------------------------------------------------------------------------
private static int indexOf( String target, Matcher matcher )
{
if( matcher.find() )
	{
	return target.indexOf( target.substring( matcher.start(), matcher.end() ) );
	}
else
	{
	return -1;
	}
}
//--------------------------------------------------------------------------------
public static int indexOfIgnoreCase( String target, String regex )
{
Pattern pattern = Pattern.compile( regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL );
Matcher matcher = pattern.matcher( target );
return indexOf( target, matcher );
}
//--------------------------------------------------------------------------------
public static int indexOf( String target, String regex )
{
Pattern pattern = Pattern.compile( regex, Pattern.DOTALL );
Matcher matcher = pattern.matcher( target );
return indexOf( target, matcher );
}
//--------------------------------------------------------------------------------
public static String getMatchIgnoreCase( String patternStr, String target )
{
Pattern pattern = Pattern.compile( patternStr, Pattern.CASE_INSENSITIVE | Pattern.DOTALL );
Matcher matcher = pattern.matcher( target );
if( matcher.find() )
	{
	if( matcher.groupCount() > 0 )
		{
		return matcher.group( 1 );
		}
	else
		{
		return target.substring( matcher.start(), matcher.end() );
		}
	}
else
	{
	return "";
	}
}
// --------------------------------------------------------------------------------
public static boolean containsIgnoreCase( String target, String regex )
{
Pattern pattern = Pattern.compile( regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL );
Matcher matcher = pattern.matcher( target );
return matcher.find();
}
//--------------------------------------------------------------------------------
public static boolean contains( String target, String patternStr )
{
Pattern pattern = Pattern.compile( patternStr, Pattern.DOTALL );
Matcher matcher = pattern.matcher( target );
return matcher.find();
}
//------------------------------------------------------------------------------------------
public static String[] split( String patternStr, String target )
{
return Pattern.compile( patternStr ).split( target, -1 );
}
//------------------------------------------------------------------------------------------
}
