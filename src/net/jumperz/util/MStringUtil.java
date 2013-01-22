package net.jumperz.util;

import java.util.regex.*;
import java.util.*;
import java.io.*;
import java.nio.charset.UnsupportedCharsetException;
import java.security.*;
import java.text.*;
import java.net.*;
import java.beans.*;
import net.jumperz.net.*;
import net.jumperz.net.exception.*;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;


public final class MStringUtil
{
private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
private static final Pattern pattern1 = Pattern.compile( "[^-a-zA-Z0-9!\"#$%&')(*+,./:;<\\]>?@\\[\\\\^_`~|= ]" );

/*
 * * and ?
 */
//--------------------------------------------------------------------------------
public static boolean simpleMatch( String patternStr, String target )
{
StringBuffer buf = new StringBuffer( 100 );
buf.append( "^" );
while( true )
	{
	int index1 = patternStr.indexOf( '*' );
	int index2 = patternStr.indexOf( '?' );
	if( index1 == -1 && index2 == -1 )
		{
		if( patternStr.length() > 0 )
			{
			buf.append( "\\Q" );
			buf.append( patternStr );
			buf.append( "\\E" );		
			}
		break;
		}
	
	int index;
	char c;
	if( index1 == -1 )
		{
		index = index2;
		c = '?';
		}
	else if( index2 == -1 )
		{
		index = index1;
		c = '*';
		}
	else
		{
		if( index1 <= index2 )
			{
			index = index1;
			c = '*';
			}
		else
			{
			index = index2;
			c = '?';
			}
		}
	
	if( index > 0 )
		{
		buf.append( "\\Q" );
		buf.append( patternStr.substring( 0, index ) );
		buf.append( "\\E" );	
		}

	if( c == '*' )
		{
		buf.append( ".*" );
		}
	else if( c == '?' )
		{
		buf.append( "." );
		}
	
	patternStr = patternStr.substring( index + 1 );
	}

buf.append( "$" );

Pattern pattern = Pattern.compile( buf.toString(), Pattern.CASE_INSENSITIVE );
return pattern.matcher( target ).find();
}
//--------------------------------------------------------------------------------
public static void testParseParentheses()
throws Exception
{
if( !parseParentheses( "hogehoge" ).equals( "hogehoge" ) ){ ex(); }
if( !parseParentheses( ")" ).equals( " " ) ){ ex(); }
if( !parseParentheses( "))" ).equals( "  " ) ){ ex(); }
if( !parseParentheses( "hoge)" ).equals( "hoge " ) ){ ex(); }
if( !parseParentheses( "hoge()" ).equals( "hoge()" ) ){ ex(); }
if( !parseParentheses( "hoge( fuga() )" ).equals( "hoge( fuga() )" ) ){ ex(); }
if( !parseParentheses( "hoge( fuga() ) )" ).equals( "hoge( fuga() )  " ) ){ ex(); }
if( !parseParentheses( "hoge(fuga()))" ).equals( "hoge(fuga()) " ) ){ ex(); }
if( !parseParentheses( "hoge() fuga()" ).equals( "hoge() fuga()" ) ){ ex(); }
if( !parseParentheses( "hoge() ( fuga() )" ).equals( "hoge() ( fuga() )" ) ){ ex(); }

if( !parseParentheses( "(" ).equals( " " ) ){ ex(); }
if( !parseParentheses( "((" ).equals( "  " ) ){ ex(); }
if( !parseParentheses( "hogehoge((" ).equals( "hogehoge  " ) ){ ex(); }
if( !parseParentheses( "hogehoge(()" ).equals( "hogehoge ()" ) ){ ex(); }
if( !parseParentheses( "hogehoge((()" ).equals( "hogehoge  ()" ) ){ ex(); }

}
//--------------------------------------------------------------------------------
public static String parseParentheses( String value )
{
if( value.indexOf( ')' ) == -1
 && value.indexOf( '(' ) == -1
  )
	{
	return value;
	}
else
	{
	
	{
	int count = 0;
	char[] buf = new char[ value.length() ];
	for( int i = 0; i < value.length(); ++i )
		{
		final char c = value.charAt( i );
		if( c == ')' )
			{
			if( count > 0 )
				{
				-- count;
				buf[ i ] = c;
				}
			else
				{
				buf[ i ] = ' ';
				}
			}
		else if( c == '(' )
			{
			++count;
			buf[ i ] = c;
			}
		else
			{
			buf[ i ] = c;
			}
		}
	value = new String( buf );
	}
	
	{
	int count = 0;
	char[] buf = new char[ value.length() ];
	for( int i = value.length() - 1; i >= 0; --i )
		{
		final char c = value.charAt( i );
		if( c == '(' )
			{
			if( count > 0 )
				{
				-- count;
				buf[ i ] = c;
				}
			else
				{
				buf[ i ] = ' ';
				}
			}
		else if( c == ')' )
			{
			++count;
			buf[ i ] = c;
			}
		else
			{
			buf[ i ] = c;
			}
		}
	
	
	String result = new String( buf );
	//debug( result );
	return result;
	}
	
	}
}
//--------------------------------------------------------------------------------
public static boolean containsExt( String target, String ext ) 
{
Pattern pattern = Pattern.compile( "\\Q" + ext + "\\E" + "([^a-zA-Z0-9]{1}|$)", Pattern.CASE_INSENSITIVE );
Matcher matcher = pattern.matcher( target );
return matcher.find();
}
//--------------------------------------------------------------------------------
public static List loadListFromString( String s )
{
String[] array = s.split( "[\\r|\\n]+" );
List l = new ArrayList( array.length );
for( int i = 0; i < array.length; ++i )
	{
	if( !isComment( array[ i ] ) )
		{
		l.add( array[ i ] );
		}
	}
return l;
}
//--------------------------------------------------------------------------------


/*
 *  * only
 */
//--------------------------------------------------------------------------------
public static boolean simpleMatch2( String patternStr, String target )
{
StringBuffer buf = new StringBuffer( 100 );
buf.append( "^" );
while( true )
	{
	int index1 = patternStr.indexOf( '*' );
	if( index1 == -1 )
		{
		if( patternStr.length() > 0 )
			{
			buf.append( "\\Q" );
			buf.append( patternStr );
			buf.append( "\\E" );		
			}
		break;
		}
	
	int index = index1;
	if( index > 0 )
		{
		buf.append( "\\Q" );
		buf.append( patternStr.substring( 0, index ) );
		buf.append( "\\E" );	
		}

	buf.append( ".*" );
	patternStr = patternStr.substring( index + 1 );
	}

buf.append( "$" );

Pattern pattern = Pattern.compile( buf.toString(), Pattern.CASE_INSENSITIVE );
return pattern.matcher( target ).find();
}
//--------------------------------------------------------------------------------
public static void testSimpleMatch()
throws Exception
{
if( ! simpleMatch( "www.example.jp", "www.example.jp" ) ) { throw new Exception(); }
if( ! simpleMatch( "WWW.example.jp", "www.example.jp" ) ) { throw new Exception(); }
if( ! simpleMatch( "*.example.jp", "www.example.jp" ) ) { throw new Exception(); }
if( ! simpleMatch( "???.example.jp", "www.example.jp" ) ) { throw new Exception(); }
if( ! simpleMatch( "???.example.*.jp", "www.example.ne.jp" ) ) { throw new Exception(); }
if( ! simpleMatch( "*jp", "www.example.ne.jp" ) ) { throw new Exception(); }
if( ! simpleMatch( "www.???.com", "www.aaa.com" ) ) { throw new Exception(); }
if( ! simpleMatch( "www.*.com", "www.aaa.com" ) ) { throw new Exception(); }
if( ! simpleMatch( "*", "www.example.com" ) ) { throw new Exception(); }

if( simpleMatch( "???.example.jp", "ww.example.jp" ) ) { throw new Exception(); }
if( simpleMatch( "www.example.jp", "ww.example.jp.xxx" ) ) { throw new Exception(); }
if( simpleMatch( "*.example.jp", "www.example.jp.xxx" ) ) { throw new Exception(); }
if( simpleMatch( "*.example.jp", "ww.example.com" ) ) { throw new Exception(); }

if( ! simpleMatch2( "www.example.jp", "www.example.jp" ) ) { throw new Exception(); }
if( ! simpleMatch2( "*.example.jp", "www.example.jp" ) ) { throw new Exception(); }
if( ! simpleMatch2( "*jp", "www.example.ne.jp" ) ) { throw new Exception(); }
if( ! simpleMatch2( "www.*.com", "www.aaa.com" ) ) { throw new Exception(); }
if( ! simpleMatch2( "*", "www.example.com" ) ) { throw new Exception(); }
if( ! simpleMatch2( "/hoge/*", "/hoge/" ) ) { throw new Exception(); }
if( ! simpleMatch2( "/hoge/*", "/hoge/index.html" ) ) { throw new Exception(); }
if( ! simpleMatch2( "*", "/hoge/index.html" ) ) { throw new Exception(); }

if( simpleMatch2( "www.example.jp", "ww.example.jp.xxx" ) ) { throw new Exception(); }
if( simpleMatch2( "*.example.jp", "www.example.jp.xxx" ) ) { throw new Exception(); }
if( simpleMatch2( "*.example.jp", "ww.example.com" ) ) { throw new Exception(); }

/*
{
Set set = new HashSet();
set.add( "www.example.jp" );
set.add( "*.example.com" );
set.add( "???.example.net" );

if( ! simpleMatch( set, "www.example.jp" ) ){ throw new Exception(); }
if( ! simpleMatch( set, "www.example.com" ) ){ throw new Exception(); }
if( ! simpleMatch( set, "www2.example.com" ) ){ throw new Exception(); }
if( ! simpleMatch( set, "www.example.net" ) ){ throw new Exception(); }
if(   simpleMatch( set, "www2.example.jp" ) ){ throw new Exception(); }
if(   simpleMatch( set, "ww.example.net" ) ){ throw new Exception(); }
if(   simpleMatch( set, "www.example.com.jp" ) ){ throw new Exception(); }
}*/
}
//--------------------------------------------------------------------------------
public static boolean isAscii( String s )
{
for( int i = 0; i < s.length(); ++i )
	{
	if( s.charAt( i ) >= 128 )
		{
		return false;
		}
	}
return true;
}
// --------------------------------------------------------------------------------
public static int parseInt( Object o, int defaultValue )
{
return parseInt( o + "", defaultValue );
}
// --------------------------------------------------------------------------------
public static int parseInt( Object o )
{
if( o instanceof Double )
	{
	return ( ( Double )o ).intValue();
	}
else if( o instanceof Integer )
	{
	return ( ( Integer)o ).intValue();
	}
return parseInt( o + "" );
}
// --------------------------------------------------------------------------------
public static long parseLong( String s )
{
try
	{
	if( s == null || s.equals( "" ) )
		{
		return 0;
		}
	else
		{
		return Long.parseLong( s );
		}
	}
catch( NumberFormatException e )
	{
	return 0;
	}
}
//--------------------------------------------------------------------------------
public static boolean containsFewAscii( String s )
{
float asciiCount = 0;
float nonAsciiCount = 0;
float length = ( float )s.length();
for( int i = 0; i < length; ++i )
	{
	char c = s.charAt( i );
	if( c < 128 )
		{
		asciiCount++;
		}
	else
		{
		nonAsciiCount++;
		}
	}

if( ( nonAsciiCount / length ) > 0.6 )
	{
	return true;
	}
else
	{
	return false;
	}
}
//--------------------------------------------------------------------------------
public static List getInnerTextAsList( String s, String tagFrom, String tagTo )
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
		int index1 = MStringUtil.indexOf( s, tagFrom );
		if( index1 == -1 )
			{
			break;
			}
		
		s = s.substring( index1 + tagFrom.length() );

		int index2 = MStringUtil.indexOf( s, tagTo );
		if( index2 == -1 )
			{
			break;
			}
		l.add( s.substring( 0, index2 ) );
		s = s.substring( index2 + tagTo.length() );
		}
	return l;	
	}
catch( Exception e )
	{
	return new ArrayList();
	}
}
//--------------------------------------------------------------------------------
public static void debug( Object o )
{
System.err.println( o );
}
//--------------------------------------------------------------------------------
public static String getMiddleString( String target, String word1, String word2 )
{
if( !containsWordsIgnoreCase( target, new String[]{ word1, word2 } ) )
	{
	return null;
	}
int index1 = indexOfIgnoreCase( target, word1 );
String substr = target.substring( index1 + word1.length() );
int index2 = indexOfIgnoreCase( substr, word2 );
return substr.substring( 0, index2 );
}
//--------------------------------------------------------------------------------
public static boolean containsWordsIgnoreCase( String target, String[] words )
{
for( int i = 0; i < words.length; ++i )
	{
	int index = indexOfWord( target, words[ i ] );
	if( index == -1 )
		{
		return false;
		}
	else
		{
		target = target.substring( index + words[ i ].length() );		
		}
	}
return true;
}
//--------------------------------------------------------------------------------
public static boolean containsTwoWordIgnoreCase( String target, String word1, String word2 )
{
return containsWordsIgnoreCase( target, new String[]{ word1, word2 } );
/*
while( true )
	{
	int index1 = indexOfWord( target, word1 );
	if( index1 == -1 )
		{
		return false;
		}
	else
		{
		target = target.substring( index1 + word1.length() );
		//debug( "target:--" + target + "--" );
		int index2 = indexOfWord( target, word2 );
		if( index2 >= 0 )
			{
			return true;
			}
		}
	}
*/

}
//--------------------------------------------------------------------------------
public static int indexOfWord( String value, String word ) //ignore case!!!
{
int targetHead = 0;
int wordLen = word.length();
String target = value;
while( true )
	{
	final int index = MStringUtil.indexOf( target, word );
	if( index == -1 )
		{
		return -1;
		}
	else
		{
		int wordPreIndex = targetHead + index -1;
		if( wordPreIndex == - 1 )
			{
			wordPreIndex = 0;
			}
		int wordPostIndex = targetHead + index + wordLen + 1;
		if( wordPostIndex > value.length() )
			{
			wordPostIndex = value.length();
			}
		String foo = value.substring( wordPreIndex, wordPostIndex );
		if( MRegEx.containsIgnoreCase( foo, MRegEx.WORD_HEAD + word + MRegEx.WORD_TAIL ) )
			{
			return targetHead + index;
			}
		else
			{
			targetHead += index + wordLen + 1;
			if( targetHead >= value.length() )
				{
				return -1;
				}
			else
				{
				target = value.substring( targetHead );			
				}
			}
		}
	}
}
//--------------------------------------------------------------------------------
public static boolean containsWordIgnoreCase( String value, String word )
{
if( indexOfWord( value, word ) == -1 )
	{
	return false;
	}
else
	{
	return true;
	}
}
//--------------------------------------------------------------------------------
private static void testContainsTwoWordIgnoreCase()
throws Exception
{
if( containsTwoWordIgnoreCase( " hoge", "hoge", "fuga" ) ){ throw new Exception(); }
if( containsTwoWordIgnoreCase( " hoge ", "hoge", "fuga" ) ){ throw new Exception(); }
if( containsTwoWordIgnoreCase( "hoge", "hoge", "fuga" ) ){ throw new Exception(); }
if( containsTwoWordIgnoreCase( "hog", "hoge", "fuga" ) ){ throw new Exception(); }
if( !containsTwoWordIgnoreCase( "hoge fuga", "hoge", "fuga" ) ){ throw new Exception(); }
if( containsTwoWordIgnoreCase( "ahoge fuga", "hoge", "fuga" ) ){ throw new Exception(); }
if( containsTwoWordIgnoreCase( "hogea fuga", "hoge", "fuga" ) ){ throw new Exception(); }
if( containsTwoWordIgnoreCase( "hogefuga", "hoge", "fuga" ) ){ throw new Exception(); }
if( containsTwoWordIgnoreCase( "hoge afuga", "hoge", "fuga" ) ){ throw new Exception(); }
if( containsTwoWordIgnoreCase( "hoge fugaa", "hoge", "fuga" ) ){ throw new Exception(); }
if( containsTwoWordIgnoreCase( "fuga hoge", "hoge", "fuga" ) ){ throw new Exception(); }
if( containsTwoWordIgnoreCase( "fuga hogefuga", "hoge", "fuga" ) ){ throw new Exception(); }
if( !containsTwoWordIgnoreCase( "fuga hoge fuga", "hoge", "fuga" ) ){ throw new Exception(); }
if( !containsTwoWordIgnoreCase( "Hoge fuga", "hoge", "fuga" ) ){ throw new Exception(); }
if( !containsTwoWordIgnoreCase( "Hoge Fuga", "hoge", "fuga" ) ){ throw new Exception(); }
if( !containsTwoWordIgnoreCase( "Hoge Fuga", "hoge", "fUga" ) ){ throw new Exception(); }
if( !containsTwoWordIgnoreCase( "Hoge Fuga", "hogE", "fUga" ) ){ throw new Exception(); }
if( !containsTwoWordIgnoreCase( "gyoe hoge fuga", "hoge", "fuga" ) ){ throw new Exception(); }
}
//--------------------------------------------------------------------------------
private static void testIndexOfIgnoreCase()
throws Exception
{
if( indexOfIgnoreCase( "fuga fuga", "fuga" ) != 0 ){ ex(); }
if( indexOfIgnoreCase( "fug", "fuga" ) != -1 ){ ex(); }
if( indexOfIgnoreCase( "fugaa", "fuga" ) != 0 ){ ex(); }
if( indexOfIgnoreCase( "hoge fuga", "fuga" ) != 5 ){ ex(); }
if( indexOfIgnoreCase( "hogea fuga", "fuga" ) != 6 ){ ex(); }
if( indexOfIgnoreCase( "hogeaa fuga", "fuga" ) != 7 ){ ex(); }

if( indexOfIgnoreCase( "fuga fuga", "FUGA" ) != 0 ){ ex(); }
if( indexOfIgnoreCase( "fug", "FUGA" ) != -1 ){ ex(); }
if( indexOfIgnoreCase( "fugaa", "FUGA" ) != 0 ){ ex(); }
if( indexOfIgnoreCase( "hoge fuga", "FUGA" ) != 5 ){ ex(); }
if( indexOfIgnoreCase( "hogea fuga", "FUGA" ) != 6 ){ ex(); }
if( indexOfIgnoreCase( "hogeaa fuga", "FUGA" ) != 7 ){ ex(); }

if( indexOfIgnoreCase( "FUGA FUGA", "fuga" ) != 0 ){ ex(); }
if( indexOfIgnoreCase( "FUG", "fuga" ) != -1 ){ ex(); }
if( indexOfIgnoreCase( "FUGAA", "fuga" ) != 0 ){ ex(); }
if( indexOfIgnoreCase( "HOGE FUGA", "fuga" ) != 5 ){ ex(); }
if( indexOfIgnoreCase( "HOGEA FUGA", "fuga" ) != 6 ){ ex(); }
if( indexOfIgnoreCase( "HOGEAA FUGA", "fuga" ) != 7 ){ ex(); }
}
//--------------------------------------------------------------------------------
private static void testIndexOf3()
throws Exception
{
if( indexOf3( "fuga fuga", "fuga" ) != 0 ){ ex(); }
if( indexOf3( "fug", "fuga" ) != -1 ){ ex(); }
if( indexOf3( "fugaa", "fuga" ) != 0 ){ ex(); }
if( indexOf3( "hoge fuga", "fuga" ) != 5 ){ ex(); }
if( indexOf3( "hogea fuga", "fuga" ) != 6 ){ ex(); }
if( indexOf3( "hogeaa fuga", "fuga" ) != 7 ){ ex(); }
if( indexOf3( "hogeaa fug", "fuga" ) != -1 ){ ex(); }

if( indexOf3( "hogeaa fug fuga", "fuga" ) != 11 ){ ex(); }
if( indexOf3( "hogeaa fug fuGa", "Fuga" ) != 11 ){ ex(); }
if( indexOf3( "hogeaa fug fUga", "fugA" ) != 11 ){ ex(); }

if( indexOf3( "fuga fuga", "FUGA" ) != 0 ){ ex(); }
if( indexOf3( "fug", "FUGA" ) != -1 ){ ex(); }
if( indexOf3( "fugaa", "FUGA" ) != 0 ){ ex(); }
if( indexOf3( "hoge fuga", "FUGA" ) != 5 ){ ex(); }
if( indexOf3( "hogea fuga", "FUGA" ) != 6 ){ ex(); }
if( indexOf3( "hogeaa fuga", "FUGA" ) != 7 ){ ex(); }

if( indexOf3( "FUGA FUGA", "fuga" ) != 0 ){ ex(); }
if( indexOf3( "FUG", "fuga" ) != -1 ){ ex(); }
if( indexOf3( "FUGAA", "fuga" ) != 0 ){ ex(); }
if( indexOf3( "HOGE FUGA", "fuga" ) != 5 ){ ex(); }
if( indexOf3( "HOGEA FUGA", "fuga" ) != 6 ){ ex(); }
if( indexOf3( "HOGEAA FUGA", "fuga" ) != 7 ){ ex(); }
if( indexOf3( "HOGEAA FUG", "fuga" ) != -1 ){ ex(); }

if( indexOf3( "FUGA FUGA", "fuGa" ) != 0 ){ ex(); }
if( indexOf3( "FUG", "fuGa" ) != -1 ){ ex(); }
if( indexOf3( "FUGAA", "fuGa" ) != 0 ){ ex(); }
if( indexOf3( "HOGE FUGA", "fuGa" ) != 5 ){ ex(); }
if( indexOf3( "HOGEA FUGA", "fuGa" ) != 6 ){ ex(); }
if( indexOf3( "HOGEAA FUGA", "fuGa" ) != 7 ){ ex(); }
if( indexOf3( "HOGEAA FUG", "fuGa" ) != -1 ){ ex(); }

if( indexOf3( "fuga fuga", "" ) != 0 ){ ex(); }
if( indexOf3( "", "" ) != 0 ){ ex(); }
if( indexOf3( "", "a" ) != -1 ){ ex(); }
if( indexOf3( "", "aaaaa" ) != -1 ){ ex(); }
if( indexOf3( "ab", "a" ) != 0 ){ ex(); }
if( indexOf3( "ab", "b" ) != 1 ){ ex(); }
if( indexOf3( "abc", "c" ) != 2 ){ ex(); }

if( indexOf3( "b", "b" ) != 0 ){ ex(); }

if( indexOf3( "b", "hoge" ) != -1 ){ ex(); }
if( indexOf3( "b", "ho" ) != -1 ){ ex(); }
if( indexOf3( "b", "hog" ) != -1 ){ ex(); }

if( indexOf3( "b", "bbb" ) != -1 ){ ex(); }
if( indexOf3( "b", "bb" ) != -1 ){ ex(); }
if( indexOf3( "b", "bbbb" ) != -1 ){ ex(); }

if( indexOf3( "ab", "bbb" ) != -1 ){ ex(); }
if( indexOf3( "ab", "bb" ) != -1 ){ ex(); }
if( indexOf3( "ab", "bbbb" ) != -1 ){ ex(); }
}
//--------------------------------------------------------------------------------
private static void testIndexOf2()
throws Exception
{
if( indexOf2( "fuga fuga", "fuga" ) != 0 ){ ex(); }
if( indexOf2( "fug", "fuga" ) != -1 ){ ex(); }
if( indexOf2( "fugaa", "fuga" ) != 0 ){ ex(); }
if( indexOf2( "hoge fuga", "fuga" ) != 5 ){ ex(); }
if( indexOf2( "hogea fuga", "fuga" ) != 6 ){ ex(); }
if( indexOf2( "hogeaa fuga", "fuga" ) != 7 ){ ex(); }

if( indexOf2( "hogeaa fug fuga", "fuga" ) != 11 ){ ex(); }
if( indexOf2( "hogeaa fug fuGa", "Fuga" ) != 11 ){ ex(); }
if( indexOf2( "hogeaa fug fUga", "fugA" ) != 11 ){ ex(); }

if( indexOf2( "fuga fuga", "FUGA" ) != 0 ){ ex(); }
if( indexOf2( "fug", "FUGA" ) != -1 ){ ex(); }
if( indexOf2( "fugaa", "FUGA" ) != 0 ){ ex(); }
if( indexOf2( "hoge fuga", "FUGA" ) != 5 ){ ex(); }
if( indexOf2( "hogea fuga", "FUGA" ) != 6 ){ ex(); }
if( indexOf2( "hogeaa fuga", "FUGA" ) != 7 ){ ex(); }

if( indexOf2( "FUGA FUGA", "fuga" ) != 0 ){ ex(); }
if( indexOf2( "FUG", "fuga" ) != -1 ){ ex(); }
if( indexOf2( "FUGAA", "fuga" ) != 0 ){ ex(); }
if( indexOf2( "HOGE FUGA", "fuga" ) != 5 ){ ex(); }
if( indexOf2( "HOGEA FUGA", "fuga" ) != 6 ){ ex(); }
if( indexOf2( "HOGEAA FUGA", "fuga" ) != 7 ){ ex(); }
if( indexOf2( "HOGEAA FUG", "fuga" ) != -1 ){ ex(); }

if( indexOf2( "FUGA FUGA", "fuGa" ) != 0 ){ ex(); }
if( indexOf2( "FUG", "fuGa" ) != -1 ){ ex(); }
if( indexOf2( "FUGAA", "fuGa" ) != 0 ){ ex(); }
if( indexOf2( "HOGE FUGA", "fuGa" ) != 5 ){ ex(); }
if( indexOf2( "HOGEA FUGA", "fuGa" ) != 6 ){ ex(); }
if( indexOf2( "HOGEAA FUGA", "fuGa" ) != 7 ){ ex(); }
if( indexOf2( "HOGEAA FUG", "fuGa" ) != -1 ){ ex(); }

if( indexOf2( "fuga fuga", "" ) != 0 ){ ex(); }
if( indexOf2( "", "" ) != 0 ){ ex(); }
if( indexOf2( "", "a" ) != -1 ){ ex(); }
if( indexOf2( "", "aaaaa" ) != -1 ){ ex(); }

if( indexOf2( "ab", "a" ) != 0 ){ ex(); }
if( indexOf2( "ab", "b" ) != 1 ){ ex(); }

if( indexOf2( "b", "b" ) != 0 ){ ex(); }

if( indexOf2( "b", "hoge" ) != -1 ){ ex(); }
if( indexOf2( "b", "ho" ) != -1 ){ ex(); }
if( indexOf2( "b", "hog" ) != -1 ){ ex(); }

if( indexOf2( "b", "bbb" ) != -1 ){ ex(); }
if( indexOf2( "b", "bb" ) != -1 ){ ex(); }
if( indexOf2( "b", "bbbb" ) != -1 ){ ex(); }

if( indexOf2( "ab", "bbb" ) != -1 ){ ex(); }
if( indexOf2( "ab", "bb" ) != -1 ){ ex(); }
if( indexOf2( "ab", "bbbb" ) != -1 ){ ex(); }

}
//--------------------------------------------------------------------------------
private static void testIndexOf()
throws Exception
{
if( indexOf( "fuga fuga", "fuga" ) != 0 ){ ex(); }
if( indexOf( "fug", "fuga" ) != -1 ){ ex(); }
if( indexOf( "fugaa", "fuga" ) != 0 ){ ex(); }
if( indexOf( "hoge fuga", "fuga" ) != 5 ){ ex(); }
if( indexOf( "hogea fuga", "fuga" ) != 6 ){ ex(); }
if( indexOf( "hogeaa fuga", "fuga" ) != 7 ){ ex(); }

if( indexOf( "fuga fuga", "FUGA" ) != 0 ){ ex(); }
if( indexOf( "fug", "FUGA" ) != -1 ){ ex(); }
if( indexOf( "fugaa", "FUGA" ) != 0 ){ ex(); }
if( indexOf( "hoge fuga", "FUGA" ) != 5 ){ ex(); }
if( indexOf( "hogea fuga", "FUGA" ) != 6 ){ ex(); }
if( indexOf( "hogeaa fuga", "FUGA" ) != 7 ){ ex(); }

if( indexOf( "FUGA FUGA", "fuga" ) != 0 ){ ex(); }
if( indexOf( "FUG", "fuga" ) != -1 ){ ex(); }
if( indexOf( "FUGAA", "fuga" ) != 0 ){ ex(); }
if( indexOf( "HOGE FUGA", "fuga" ) != 5 ){ ex(); }
if( indexOf( "HOGEA FUGA", "fuga" ) != 6 ){ ex(); }
if( indexOf( "HOGEAA FUGA", "fuga" ) != 7 ){ ex(); }
}
//--------------------------------------------------------------------------------
private static void testIndexOfWord()
throws Exception
{
if( indexOfWord( "foo", "foo" ) != 0 ){ throw new Exception(); }
if( indexOfWord( "aa fooo", "foo" ) != -1 ){ throw new Exception(); }
if( indexOfWord( "1foo", "foo" ) != 1 ){ throw new Exception(); }
if( indexOfWord( "foo1", "foo" ) != 0 ){ throw new Exception(); }
if( indexOfWord( "1foo1", "foo" ) != 1 ){ throw new Exception(); }
if( indexOfWord( "hoge", "hoge" ) != 0 ){ throw new Exception(); }
if( indexOfWord( "aa/hogeauu/hoge", "hoge" ) != 11 ){ throw new Exception(); }

if( indexOfWord( "aa hoge uu", "hoge" ) != 3 ){ throw new Exception(); }
if( indexOfWord( "aa hoge uu", "Hoge" ) != 3 ){ throw new Exception(); }
if( indexOfWord( "aa hoge uu", "HogE" ) != 3 ){ throw new Exception(); }
if( indexOfWord( "aa hoge/uu", "HogE" ) != 3 ){ throw new Exception(); }
if( indexOfWord( "aa Hoge/uu", "hoge" ) != 3 ){ throw new Exception(); }

if( indexOfWord( "aa hoge", "hoge" ) != 3 ){ throw new Exception(); }
if( indexOfWord( "hoge aa", "hoge" ) != 0 ){ throw new Exception(); }
if( indexOfWord( "/hoge aa", "HOGE" ) != 1 ){ throw new Exception(); }
if( indexOfWord( "hoge\\aa", "HOGE" ) != 0 ){ throw new Exception(); }
if( indexOfWord( "\\hoge\\aa", "HOGE" ) != 1 ){ throw new Exception(); }
if( indexOfWord( "a\\hoge\\aa", "HOGE" ) != 2 ){ throw new Exception(); }
if( indexOfWord( "a\\hoge", "HOGE" ) != 2 ){ throw new Exception(); }

if( indexOfWord( "aa Hoge/uu", "foo" ) != -1 ){ throw new Exception(); }
if( indexOfWord( "aa fooa", "foo" ) != -1 ){ throw new Exception(); }
if( indexOfWord( "aafoo", "foo" ) != -1 ){ throw new Exception(); }
if( indexOfWord( "aa fooo", "foo" ) != -1 ){ throw new Exception(); }
if( indexOfWord( "foofoo", "foo" ) != -1 ){ throw new Exception(); }
if( indexOfWord( "foofoofooo", "foo" ) != -1 ){ throw new Exception(); }

}
//--------------------------------------------------------------------------------
private static void testContainsWordsIgnoreCase()
throws Exception
{

if( containsWordsIgnoreCase( "hoge fuga", new String[]{ "hoge", "FUGA" } ) == false ){ ex(); }
if( containsWordsIgnoreCase( "hoge fuga", new String[]{ "hoge", "gyoe" } ) == true ){ ex(); }
if( containsWordsIgnoreCase( "hoge", new String[]{ "HOGE" } ) != true ){ ex(); }
if( containsWordsIgnoreCase( "HOGE", new String[]{ "HOG" } ) == true ){ ex(); }
if( containsWordsIgnoreCase( "hoge fuga gyoe", new String[]{ "hoge", "fuga", "gyoe" } ) != true ){ ex(); }
if( containsWordsIgnoreCase( "gyoe fuga hoge", new String[]{ "hoge", "fuga", "gyoe" } ) == true ){ ex(); }
if( containsWordsIgnoreCase( "'; select * from user", new String[]{ "select", "frOm" } ) == false ){ ex(); }
}
//--------------------------------------------------------------------------------
private static void testContainsWordIgnoreCase()
throws Exception
{
if( containsWordIgnoreCase( "aa fooo", "foo" ) == true ){ throw new Exception(); }
if( containsWordIgnoreCase( "hoge", "hoge" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "aa/hogeauu/hoge", "hoge" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "aa hoge uu", "hoge" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "aa hoge uu", "Hoge" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "aa hoge uu", "HogE" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "aa hoge/uu", "HogE" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "aa Hoge/uu", "hoge" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "aa hoge", "hoge" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "hoge aa", "hoge" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "/hoge aa", "HOGE" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "hoge\\aa", "HOGE" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "\\hoge\\aa", "HOGE" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "a\\hoge\\aa", "HOGE" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "a\\hoge", "HOGE" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "[hoge]", "HOGE" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "[hoge]", "HoGE" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "[hOge]", "HoGE" ) == false ){ throw new Exception(); }
if( containsWordIgnoreCase( "aa Hoge/uu", "foo" ) == true ){ throw new Exception(); }
if( containsWordIgnoreCase( "aa fooa", "foo" ) == true ){ throw new Exception(); }
if( containsWordIgnoreCase( "aafoo", "foo" ) == true ){ throw new Exception(); }
if( containsWordIgnoreCase( "aa fooo", "foo" ) == true ){ throw new Exception(); }
if( containsWordIgnoreCase( "foofoo", "foo" ) == true ){ throw new Exception(); }
if( containsWordIgnoreCase( "foofoofooo", "foo" ) == true ){ throw new Exception(); }
}
//--------------------------------------------------------------------------------
public static boolean hiddenInComment( String value, String word )
{
if( MRegEx.containsIgnoreCase( value, MRegEx.WORD_HEAD + word + "\\s*/\\*" )
 || MRegEx.containsIgnoreCase( value, "\\*/\\s*" + word + "\\W+?" )
  )
	{
	return true;
	}
else
	{
	return false;
	}
}
//--------------------------------------------------------------------------------
private static void testHiddenInComment()
throws Exception
{
if( hiddenInComment( "UNION/**/SELECT", "union" ) == false ){ throw new Exception(); }
if( hiddenInComment( " UNION/**/SELECT", "union" ) == false ){ throw new Exception(); }
if( hiddenInComment( "UNION/*DUMMY*/SELECT", "union" ) == false ){ throw new Exception(); }
if( hiddenInComment( "UNION /*DUMMY*/SELECT", "union" ) == false ){ throw new Exception(); }

if( hiddenInComment( "UNION/*DUMMY*/SELECT ", "select" ) == false ){ throw new Exception(); }
if( hiddenInComment( "UNION/*DUMMY*/ SELECT ", "select" ) == false ){ throw new Exception(); }
}
//--------------------------------------------------------------------------------
private static void testGetInnerTextAsList()
throws Exception
{
List l1, l2;

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
private static void testGetSubstringFrom()
throws Exception
{
if( !getSubstringFrom( "aaabbbccc", "bbb" ).equals( "bbbccc" ) ){ throw new Exception();}
if( !getSubstringFrom( "aaabbbccc", "a" ).equals( "aaabbbccc" ) ){ throw new Exception();}
if( !getSubstringFrom( "aaabbbccc", "bbb", 4 ).equals( "bbbc" ) ){ throw new Exception();}
if( !getSubstringFrom( "aaabbbccc", "bbb", 5 ).equals( "bbbcc" ) ){ throw new Exception();}
if( !getSubstringFrom( "aaabbbccc", "bbb", 6 ).equals( "bbbccc" ) ){ throw new Exception();}
if( !getSubstringFrom( "aaabbbccc", "bbb", 7 ).equals( "bbbccc" ) ){ throw new Exception();}
if( !getSubstringFrom( "aaabbbccc", "bbb", 20 ).equals( "bbbccc" ) ){ throw new Exception();}

if( !getSubstring( "aiueo", 3 ).equals( "aiu" ) ){ throw new Exception();}
if( !getSubstring( "aiueo", 4 ).equals( "aiue" ) ){ throw new Exception();}
if( !getSubstring( "aiueo", 5 ).equals( "aiueo" ) ){ throw new Exception();}
if( !getSubstring( "aiueo", 6 ).equals( "aiueo" ) ){ throw new Exception();}
if( !getSubstring( "aiueo", 7 ).equals( "aiueo" ) ){ throw new Exception();}
}
//--------------------------------------------------------------------------------
private static void testAsciiByteToInt()
throws Exception
{
if( byteToUnsignedInt( (byte)0x01 ) != 1 ){ throw new Exception(); }
if( byteToUnsignedInt( (byte)0x0a ) != 10 ){ throw new Exception(); }
if( byteToUnsignedInt( (byte)0x10 ) != 16 ){ throw new Exception(); }
if( byteToUnsignedInt( (byte)0xff ) != 255 ){ throw new Exception(); }
if( byteArrayToUnsignedInt( new byte[]{ (byte)0x00, (byte)0x00 } ) != 0 ){ throw new Exception(); }
if( byteArrayToUnsignedInt( new byte[]{ (byte)0x00, (byte)0x01 } ) != 1 ){ throw new Exception(); }
if( byteArrayToUnsignedInt( new byte[]{ (byte)0x00, (byte)0x10 } ) != 16 ){ throw new Exception(); }
if( byteArrayToUnsignedInt( new byte[]{ (byte)0x00, (byte)0x0a } ) != 10 ){ throw new Exception(); }
}
//--------------------------------------------------------------------------------
private static void testFastUrlDecode()
throws Exception
{
if( !fastUrlDecode( "%41" ).equals( "A" ) ){ throw new Exception(); }
if( !fastUrlDecode( "%41%", true ).equals( "A" ) ){ throw new Exception(); }
if( !fastUrlDecode( "%41%A", true ).equals( "AA" ) ){ throw new Exception(); }
if( !fastUrlDecode( "%41%AZ", true ).equals( "AAZ" ) ){ throw new Exception(); }
if( !fastUrlDecode( "%41%4Z", true ).equals( "A4Z" ) ){ throw new Exception(); }
if( !fastUrlDecode( "%41%41", true ).equals( "AA" ) ){ throw new Exception(); }
if( !fastUrlDecode( "dec%lare", true ).equals( "declare" ) ){ throw new Exception(); }
if( !fastUrlDecode( "de%clare", true ).equals( "declare" ) ){ throw new Exception(); }
if( !fastUrlDecode( "decl%are", true ).equals( "declare" ) ){ throw new Exception(); }
if( !fastUrlDecode( "declar%e", true ).equals( "declare" ) ){ throw new Exception(); }
if( !fastUrlDecode( "A+A", true ).equals( "A A" ) ){ throw new Exception(); }
}
//--------------------------------------------------------------------------------
public static int byteArrayToUnsignedInt( byte[] buffer )
{
int i = buffer.length -1;
int j = 0;
int sum = 0;
for( ; i >= 0; --i, ++j )
	{
	int value = byteToUnsignedInt( buffer[ i ] );
	for( int k = 0; k < j; ++k )
		{
		value *= 256;
		}
	sum += value;
	}
return sum;
}
//--------------------------------------------------------------------------------
public static int byteToUnsignedInt( byte b )
{
int i = ( int )b;
if( i < 0 )
	{
	i += 256;
	}
return i;
}
//--------------------------------------------------------------------------------
private static void testValidHost()
throws Exception
{
if( !isValidHostname( "www.example.com" ) ){ throw new Exception(); }
if( !isValidHostname( "www.example.jp" ) ){ throw new Exception(); }
if( !isValidHostname( "example.com" ) ){ throw new Exception(); }
if( !isValidHostname( "example.jp" ) ){ throw new Exception(); }
if( !isValidHostname( "aa.com" ) ){ throw new Exception(); }
if( !isValidHostname( "aa.jp" ) ){ throw new Exception(); }
if( !isValidHostname( "a1992.jp" ) ){ throw new Exception(); }
if( !isValidHostname( "a123.jp" ) ){ throw new Exception(); }
if( !isValidHostname( "www.a99e.com" ) ){ throw new Exception(); }
if( !isValidHostname( "www.xam.co.jp" ) ){ throw new Exception(); }
if( !isValidHostname( "au.nu" ) ){ throw new Exception(); }
if( !isValidHostname( "xx-um.jp" ) ){ throw new Exception(); }
if( !isValidHostname( "www.a-b.xx-um.jp" ) ){ throw new Exception(); }

if( isValidHostname( ".nu" ) ){ throw new Exception(); }
if( isValidHostname( ".9" ) ){ throw new Exception(); }
if( isValidHostname( "www,hoge,com" ) ){ throw new Exception(); }
if( isValidHostname( "123.a" ) ){ throw new Exception(); }
if( isValidHostname( "www.hoge.123" ) ){ throw new Exception(); }
if( isValidHostname( "192.168.1.1" ) ){ throw new Exception(); }

}
//--------------------------------------------------------------------------------
public static void ex()
throws Exception
{
throw new Exception();
}
//--------------------------------------------------------------------------------
public static void testRemoveSQLDoubleHyphenComments()
throws Exception
{
if( ! removeSQLDoubleHyphenComments( "hoge" ).equals( "hoge" ) ){ ex(); }
if( ! removeSQLDoubleHyphenComments( "hoge--" ).equals( "hoge" ) ){ ex(); }
if( ! removeSQLDoubleHyphenComments( "hoge--\nfuga" ).equals( "hoge fuga" ) ){ ex(); }
if( ! removeSQLDoubleHyphenComments( "hoge--foobar\nfuga" ).equals( "hoge fuga" ) ){ ex(); }
if( ! removeSQLDoubleHyphenComments( "hoge--foo--bar\nfuga" ).equals( "hoge fuga" ) ){ ex(); }
if( ! removeSQLDoubleHyphenComments( "hoge--\nhoge--\nhoge" ).equals( "hoge hoge hoge" ) ){ ex(); }
if( ! removeSQLDoubleHyphenComments( "hoge--\nhoge--fuga\nhoge" ).equals( "hoge hoge hoge" ) ){ ex(); }
if( ! removeSQLDoubleHyphenComments( "hoge--\nhoge--fuga\nhoge--fuga" ).equals( "hoge hoge hoge" ) ){ ex(); }

}
//--------------------------------------------------------------------------------
public static void testIpPrefix()
throws Exception
{
if( !getIpPrefix( "1.2.3.4" ).equals( "1." ) ){ ex(); }
if( !getIpPrefix( "1.2.3.4*" ).equals( "1." ) ){ ex(); }
if( !getIpPrefix( "1.2.3.*" ).equals( "1." ) ){ ex(); }
if( !getIpPrefix( "1.2.3.4/24" ).equals( "1." ) ){ ex(); }
}
//--------------------------------------------------------------------------------
public static void testGetMiddleString()
throws Exception
{
if( !MStringUtil.getMiddleString( "hoge fuga gyoe", "hoge", "gyoe" ).equals( " fuga " ) ){ ex(); }
if( !MStringUtil.getMiddleString( "a hoge fuga gyoe", "hoge", "gyoe" ).equals( " fuga " ) ){ ex(); }
if( !MStringUtil.getMiddleString( "a hoge fuga gyoe xx", "hoge", "gyoe" ).equals( " fuga " ) ){ ex(); }
if( !MStringUtil.getMiddleString( "a hoge fuga gyoe hoge", "hoge", "gyoe" ).equals( " fuga " ) ){ ex(); }
if( !MStringUtil.getMiddleString( "a gyoe hoge fuga gyoe hoge", "hoge", "gyoe" ).equals( " fuga " ) ){ ex(); }
if( !MStringUtil.getMiddleString( "gyoe hoge fuga gyoe hoge", "hoge", "gyoe" ).equals( " fuga " ) ){ ex(); }
}
//--------------------------------------------------------------------------------
public static void testContainsExt()
throws Exception
{
if( !containsExt( "hoge.exe", ".exe" ) ){ex();}
if( !containsExt( "hoge.exe", ".exE" ) ){ex();}
if( containsExt( "hoge.exe", ".gif" ) ){ex();}
if( containsExt( "hoge.exe1", ".exe" ) ){ex();}
if( containsExt( "hoge.exec", ".exe" ) ){ex();}
if( containsExt( "exe", ".exe" ) ){ex();}
if( containsExt( "aexe", ".exe" ) ){ex();}
if( containsExt( "aexe", ".eXe" ) ){ex();}
if( !containsExt( "hogeexe foo.exe", ".exe" ) ){ex();}
}
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
testParseSQL2();
testParseParentheses();
testContainsExt();
testGetMiddleString();
testIpPrefix();
testContainsUneven();
testIsUneven();
testRemoveSQLDoubleHyphenComments();
testContainsWordsIgnoreCase();
testReplaceWordIgnoreCase();
testRemoveSQLStrings();
testParseSQL();
testIndexOfIgnoreCase();
testIndexOf();
testIndexOf2();
testIndexOf3();
testValidHost();
testContainsTwoWordIgnoreCase();
testHiddenInComment();
testGetInnerTextAsList();
testGetSubstringFrom();
testAsciiByteToInt();
testFastUrlDecode();
testContainsWordIgnoreCase();
testIndexOfWord();
testSimpleMatch();
System.out.println( "OK." );
}
// --------------------------------------------------------------------------------
public static int getLineCountOfFile( String fileName )
{
try
	{
	String fileContent = loadStrFromFile( fileName );
	return getLineCount( fileContent );
	}
catch( IOException e )
	{
	return -1;
	}
}
// --------------------------------------------------------------------------------
public static String getHostInRequest( MHttpRequest request )
{
	//小文字でtrim済みのホスト名を返す関数

String hostField = request.getHeaderValue( "Host" );
if( hostField == null )
	{
	return "";
	}
String hostInRequest;

int index = hostField.indexOf( ":" );
if( index > 0 )
	{
	String[] array = hostField.split( ":" );
	if( array.length != 2 )
		{
		//throw new MHttpException( 400, "Invalid host" );
		return "";
		}
	hostInRequest = array[ 0 ].trim().toLowerCase();
	}
else
	{
	hostInRequest = hostField.trim().toLowerCase();
	}

return hostInRequest;
}
// --------------------------------------------------------------------------------
public static int getLineCount( String s )
{
String[] array = s.split( "[\\r\\n]{1,}" );
return array.length;
}
// --------------------------------------------------------------------------------
public static List split( String str, String d )
{
List l = new ArrayList( 20 );
while( true )
	{
	int index = str.indexOf( d );
	if( index == -1 )
		{
		l.add( str );
		return l;
		}
	else
		{
		l.add( str.substring( 0, index ) );
		str = str.substring( index + d.length() );
		}
	}
}
// --------------------------------------------------------------------------------
public static int getIntFromMap( Map m, Object key )
{
return parseInt( m.get( key ) );
}
// --------------------------------------------------------------------------------
public static List arrayToList( Object[] array )
{
List list = new ArrayList( array.length );
for( int i = 0; i < array.length; ++i )
	{
	list.add( array[ i ] );
	}
return list;
}
//--------------------------------------------------------------------------------
public static boolean isPrintable( String str )
{
Matcher matcher = pattern1.matcher( str );
if( matcher.find() )
	{
	return false;
	}
else
	{
	return true;
	}
}
//----------------------------------------------------------------------------
public static final String JavaScriptEncode2( String from, String charset )
{
StringBuffer buf = new StringBuffer( from.length() );
String str = null;
try
	{
	str = new String( from.getBytes( "ISO-8859-1" ), charset  );
	}
catch( Exception ignored )
	{
	ignored.printStackTrace();
	str = "";
	}
for( int i = 0; i < str.length(); ++i )
	{
	char c = str.charAt( i );
	int j = ( int )c;
	buf.append( "\\u" );
	String s = Integer.toHexString( j );
	if( s.length() == 1 )
		{
		buf.append( "000" );
		}
	else if( s.length() == 2 )
		{
		buf.append( "00" );
		}
	else if( s.length() == 3 )
		{
		buf.append( "0" );
		}
	buf.append( s );
	}
return buf.toString();
}
// --------------------------------------------------------------------------------
public static String[] throwableToStrArray( Throwable e )
{
ByteArrayOutputStream out = new ByteArrayOutputStream( 512 );
PrintStream ps = new PrintStream( out );
e.printStackTrace( ps );
String s = null;
try
	{
	s = out.toString( MCharset.CS_ISO_8859_1 );
	return s.split( "[\\r\\n]{1,}" );
	}
catch( UnsupportedEncodingException ignored )
	{
	return new String[]{};
	}
}
//----------------------------------------------------------------------------
public static final String JavaScriptEncode( String from )
{
from = MStringUtil.replaceAll( from,  "\\", "\\\\" );
from = MStringUtil.replaceAll( from,  "\"", "\\\"" );
from = MStringUtil.replaceAll( from,  "'", "\\'" );
from = MStringUtil.replaceAll( from,  "/", "\\/" );
from = MStringUtil.replaceAll( from,  "<", "\\x3c" );
from = MStringUtil.replaceAll( from,  ">", "\\x3e" );
from = MStringUtil.replaceAll( from,  "\r", "\\r" );
from = MStringUtil.replaceAll( from,  "\n", "\\n" );
return from;
}
//----------------------------------------------------------------------------
public static final String HTMLEncode2( String from, String charset )
{
StringBuffer buf = new StringBuffer( from.length() );
String str = null;
try
	{
	str = new String( from.getBytes( "ISO-8859-1" ), charset );
	}
catch( Exception ignored )
	{
	ignored.printStackTrace();
	}
for( int i = 0; i < str.length(); ++i )
	{
	char c = str.charAt( i );
	int j = ( int )c;
	buf.append( "&#" );
	buf.append( j );
	buf.append( ";" );
	}
return buf.toString();
}
//----------------------------------------------------------------------------
public static final String HTMLEncode( String from )
{
String str = from;
str = str.replaceAll( "&"	, "&amp;"	);
str = str.replaceAll( "<"	, "&lt;"	);
str = str.replaceAll( ">"	, "&gt;"	);
str = str.replaceAll( "\""	, "&quot;"	);
str = str.replaceAll( "'"	, "&#39;"	);
return str;
}
// --------------------------------------------------------------------------------
public static void addStrToFile( File file, String str )
throws IOException
{
addStrToFile( file.getCanonicalPath(), str );
}
// --------------------------------------------------------------------------------
public static void addStrToFile( String fileName, String str )
throws IOException
{
FileOutputStream fo = new FileOutputStream( fileName, true );
try
	{
	fo.write( str.getBytes( "ISO-8859-1" ) );
	}
finally
	{
	fo.close();
	}
}
//--------------------------------------------------------------------------------
public static File getChildFile( String dirName, String fileName )
throws IOException
{
File dir = new File( dirName );
File file = new File( dirName + "/" +fileName );

if( file.getCanonicalPath().indexOf( dir.getCanonicalPath() ) != 0 )
	{
	throw new IOException( "Invalid File Name:" + fileName );
	}
return file;
}
//--------------------------------------------------------------------------------
public static String loadStrFromFile( String fileName )
throws IOException
{
return loadStrFromFile( new File( fileName ) );
}
//--------------------------------------------------------------------------------
public static String loadStrFromFile( File file )
throws IOException
{
if( !file.exists() )
	{
	throw new FileNotFoundException( file.getCanonicalPath() );
	}
int fileSize = ( int )file.length();
byte[] buffer = new byte[ fileSize ];
InputStream is = new FileInputStream( file );
try
	{
	int r = is.read( buffer, 0, fileSize );
	if( r != fileSize )
		{
		throw new IOException( "File read error." );
		}
	}
finally
	{
	is.close();
	}
return new String( buffer, "ISO-8859-1" );
}
// --------------------------------------------------------------------------------
public static String convertCharset( String str, String charFrom, String charTo )
{
if( charFrom == null || charFrom.equals( "" )
 || charTo   == null || charTo.equals( "" )
  )
	{
	return str;
	}
try
	{
	byte[] buf = str.getBytes( "ISO-8859-1" );
	String s2 = new String( buf, charFrom );
	byte[] buf2 = s2.getBytes( charTo );
	String s3 = new String( buf2, "ISO-8859-1" );
	return s3;
	}
catch( IOException e )
	{
	e.printStackTrace();
	return str;
	}
}
// --------------------------------------------------------------------------------
public static boolean meansTrue( Object o )
{
if( o == null )
	{
	return false;
	}
String t = o.toString();
if( t.equalsIgnoreCase( "t" ) 
 || t.equalsIgnoreCase( "y" ) 
 || t.equalsIgnoreCase( "on" ) 
 || t.equalsIgnoreCase( "yes" ) 
 || t.equalsIgnoreCase( "true" ) 
 ||  t.equalsIgnoreCase( "1" ) 
  )
	{
	return true;
	}
return false;
}
// --------------------------------------------------------------------------------
public static String urlEncode( String target, String charset )
{
try
	{
	target = URLEncoder.encode( target, charset );			
	}
catch( UnsupportedEncodingException ignored )
	{
	}
return target;
}
// --------------------------------------------------------------------------------
public static String fastUrlDecode( String inStr, boolean iisMode )
{
return fastUrlDecode( inStr, MCharset.CS_ISO_8859_1, iisMode );
}
// --------------------------------------------------------------------------------
public static String fastUrlDecode( String inStr, String charset )
{
return fastUrlDecode( inStr, charset, false );
}
// --------------------------------------------------------------------------------
public static String fastUrlDecode( String inStr )
{
return fastUrlDecode( inStr, MCharset.CS_ISO_8859_1, false );
}
// --------------------------------------------------------------------------------
public static final String getId2()
throws Exception
{
return getMD5( getId() );
}
//-------------------------------------------------------------------------------
public static final String getMD2( String str )
throws Exception
{
MessageDigest md = MessageDigest.getInstance( "MD2" );
md.update( str.getBytes() );
return MStringUtil.byteToHexString( md.digest() );
}
//-------------------------------------------------------------------------------
public static final String getMD5( String str )
throws Exception
{
MessageDigest md = MessageDigest.getInstance( "MD5" );
md.update( str.getBytes() );
return MStringUtil.byteToHexString( md.digest() );
}
//---------------------------------------------------------
public static final String getId()
{
long currentTimeLong = System.currentTimeMillis();

Random random = new Random();
int r = random.nextInt( 10000 );

return String.valueOf( currentTimeLong ) + "_" + String.valueOf( r );
}
// --------------------------------------------------------------------------------
public static String toSingleBytePattern( String jPatternStr, String charset )
{
StringBuffer sbuf = new StringBuffer( jPatternStr.length() + 100 );
try
	{
		//java形式の文字列を入力とし、US-ASCII領域の文字の正規表現に変換する
	for( int i = 0; i < jPatternStr.length(); ++i )
		{
		String jStr = jPatternStr.substring( i, i + 1 );
		byte[] buf = jStr.getBytes( charset );
		if( buf.length == 1 )
			{
				//1byte文字
			sbuf.append( jStr );
			}
		else
			{
			for( int k = 0; k < buf.length; ++k )
				{
				byte b = buf[ k ];
				sbuf.append( "\\x" );
				sbuf.append( byteToHexString( b ) );
				}
			}
		}
	}
catch( UnsupportedEncodingException e )
	{
	}
return sbuf.toString();
}
// --------------------------------------------------------------------------------
public static String fastUrlDecode( String inStr, String charset, boolean iisMode )
{
byte[] in = getBytes( inStr, MCharset.CS_ISO_8859_1 );
final int len = in.length;
byte[] buf = new byte[ len ];
int buf_p = 0;
for( int i = 0; i < len; buf_p++ )
	{
	byte b = in[ i ];
	if( b == ( byte )0x25 ) //%
		{
		if( len <= ( i + 2 ) )
			{
			if( iisMode )
				{
				if( len == i + 2 )
					{
					buf[ buf_p ] = in[ i + 1 ];
					buf_p++;
					}
				break;
				}
			else
				{
				throw new IllegalArgumentException( "Incomplete trailing escape (%) pattern" ); 
				}
			}
		int i1 = asciiByteToInt( in[ i + 1 ] );
		int i2 = asciiByteToInt( in[ i + 2 ] );
		if( i1 == -1 || i2 == -1 )
			{
			if( iisMode )
				{
				//buf[ buf_p     ] = (byte)0x25;
				buf[ buf_p     ] = in[ i + 1 ];
				buf[ buf_p + 1 ] = in[ i + 2 ];
				buf_p += 1;
				i += 3;
				}
			else
				{
				throw new MIllegalEncodingException2( "Illegal characters in escape (%) pattern : " + inStr ); 			
				}
			}
		else
			{
			byte newByte = ( byte )( i1 * 16 + i2 );
			buf[ buf_p ] = newByte;
			i += 3;		
			}
		}
	else if( b == ( byte )0x2B ) // +
		{
		buf[ buf_p ] = 0x20; //space
		++i;
		}
	else
		{
		buf[ buf_p ] = b;
		++i;
		}
	}

String str = "";
try
	{
	str = new String( buf, 0, buf_p, charset );
	}
catch( UnsupportedEncodingException ignored )
	{
	//ignored.printStackTrace();
	}
return str;
}
// --------------------------------------------------------------------------------
public static int asciiByteToInt( byte in_b )
{
int b = ( int )in_b;
if( 0x30 <= b && b <= 0x39 ) //0 to 9
	{
	return ( int )b - 0x30;
	}
else if( 0x41 <= b && b <= 0x46 )
	{
	return ( int )b - 55;
	}
else if( 0x61 <= b && b <= 0x66 )
	{
	return ( int )b - 87;
	}
else
	{
	return -1;
	}
}
//--------------------------------------------------------------------------------
public static String urlDecode( String target, String charset )
throws IllegalArgumentException
{
try
	{
	//target = URLDecoder.decode( target, charset );
	target = URLDecoder.decode( target, MCharset.CS_ISO_8859_1 );
	byte[] buf = target.getBytes( MCharset.CS_ISO_8859_1 );
	return new String( buf, charset );
	}
catch( UnsupportedEncodingException ignored )
	{
	}
//catch( IllegalArgumentException ignored )
	{
	}
return target;
}
// --------------------------------------------------------------------------------
public static String urlDecode( String target )
throws IllegalArgumentException
{
try
	{
		//TODO: debug
	long start = System.currentTimeMillis();
	target = URLDecoder.decode( target, MCharset.CS_ISO_8859_1 );			
	//System.err.println( System.currentTimeMillis() - start );
	}
catch( UnsupportedEncodingException ignored )
	{
	}
//catch( IllegalArgumentException ignored )
	{
	}
return target;
}
// --------------------------------------------------------------------------------
public static String replaceFirstIgnoreCase( String target, String from, String to )
{
StringBuffer s = new StringBuffer( target.length() );
int index = indexOf( target, from );
if( index == -1 )
	{
	return target;
	}
s.append( target.substring( 0, index ) );
s.append( to );
s.append( target.substring( index + from.length() ) );
return s.toString();
}
// --------------------------------------------------------------------------------
public static String replaceFirst( String target, String from, String to )
{
StringBuffer s = new StringBuffer( target.length() );
int index = target.indexOf( from );
if( index == -1 )
	{
	return target;
	}
s.append( target.substring( 0, index ) );
s.append( to );
s.append( target.substring( index + from.length() ) );
return s.toString();
}
//--------------------------------------------------------------------------------
public static String replaceAllIgnoreCase( String target, String from, String to )
{
int len = from.length();
if( len == 0 )
	{
	return target;
	}
StringBuffer buf = new StringBuffer( target.length() );
while( true )
	{
	int pos = indexOf( target, from );
	if( pos == -1 )
		{
		break;
		}
	buf.append( target.substring( 0, pos ) );
	buf.append( to );
	target = target.substring( pos + len );
	}
buf.append( target );
return buf.toString();
}
//--------------------------------------------------------------------------------
public static String replaceAll( String target, String from, String to )
{
int len = from.length();
if( len == 0 )
	{
	return target;
	}
StringBuffer buf = new StringBuffer( target.length() );
while( true )
	{
	int pos = target.indexOf( from );
	if( pos == -1 )
		{
		break;
		}
	buf.append( target.substring( 0, pos ) );
	buf.append( to );
	target = target.substring( pos + len );
	}
buf.append( target );
return buf.toString();
}
//--------------------------------------------------------------------------------
public static String normalize( String path )
{
if( path == null )
	{
	return null;
	}
if ( path.equals( "/." ) )
	{
	return "/";
	}

String normalized = path;

while( normalized.indexOf( '\\' ) > -1 )
	{
	normalized = normalized.replace( '\\', '/' );
	}

boolean flag1 = false;
if( !normalized.startsWith( "/" ) )
	{
	normalized = "/" + normalized;
	flag1 = true;
	}

while( normalized.indexOf( "//" ) > -1 )
	{
	normalized = normalized.replaceFirst( "//", "/" );
	}

while( normalized.indexOf( "/./" ) > -1 )
	{
	normalized = normalized.replaceFirst( "/\\./", "/" );
	}

StringBuffer buf = new StringBuffer();
while( true )
	{
	int index = normalized.indexOf( "/../" );
	if( index < 0 )
		{
		break;
		}
	else if( index == 0 )
		{
		buf.append( "/.." );
		normalized = normalized.substring( 3 );
		}
	else
		{
		int index2 = normalized.lastIndexOf( '/', index - 1 );
		normalized = normalized.substring( 0, index2 ) + normalized.substring( index + 3 );
		}
	}
buf.append( normalized );

if( flag1 )
	{
	return buf.toString().substring( 1 );
	}
else
	{
	return buf.toString();
	}
}
//---------------------------------------------------------
public static final String generateRandomString()
{
Calendar cal = Calendar.getInstance();
long currentTimeLong = cal.getTimeInMillis();

Random random = new Random();
int id1 = random.nextInt( 10000 );
int id2 = random.nextInt( 10000 );

return String.valueOf( id1 ) + String.valueOf( currentTimeLong ) + String.valueOf( id2 );
}
//--------------------------------------------------------------------------------
public static final String hexDecode( String in )
{
int length = in.length();
byte[] data = new byte[ length/2 ];
for( int i = 0, j = 0; i < length; i+=2, ++j )
	{
	char c1 = in.charAt( i );
	char c2 = in.charAt( i + 1 );
	int i1 = Character.digit( c1, 16 ) * 16;
	int i2 = Character.digit( c2, 16 );
	data[ j ] = ( byte )( i1 + i2 );
	}

String out = null;
try
	{
	out = new String( data, "ISO-8859-1" );
	}
catch( UnsupportedEncodingException e )
	{
	e.printStackTrace();
	}
return  out;
}
// --------------------------------------------------------------------------------
public static final String byteToHexString( byte data )
{
StringBuffer strBuf = new StringBuffer( 2 );
int j = data;
if( j < 0 )
	{
	j += 256;
	}
String tmpStr = Integer.toHexString( j );
if( tmpStr.length() == 1 )
	{
	strBuf.append( "0" );
	}
strBuf.append( tmpStr );
return strBuf.toString();
}

//-------------------------------------------------------------------------------
public static final String byteToHexString( byte[] data )
{
StringBuffer strBuf = new StringBuffer( data.length * 2 );

int length = data.length;
for( int i = 0; i < length; ++i )
	{
	int j = data[ i ];
	if( j < 0 )
		{
		j += 256;
		}
	String tmpStr = Integer.toHexString( j );
	if( tmpStr.length() == 1 )
		{
		strBuf.append( "0" );
		}
	strBuf.append( tmpStr );
	}

return strBuf.toString();
}

//--------------------------------------------------------------------------------
public static final String hexEncode( String in )
{
try
	{
	String out = byteToHexString( in.getBytes( "ISO-8859-1" ) );
	return out;
	}
catch( UnsupportedEncodingException e )
	{
	e.printStackTrace();
	}
return null;
}
// --------------------------------------------------------------------------------
public static final byte[] hexStringToByteArray( String s )
{
byte[] array = new byte[ s.length()/2 ];
for( int i = 0, j = 0; ( i + 1 ) < s.length(); i+=2, j++ )
	{
	int k = Integer.parseInt( s.substring( i, i + 2 ), 16 );
	array[ j ] = ( byte )k;
	}
return array;
}
// --------------------------------------------------------------------------------
public static String byteArrayToString( byte[] b )
{
return byteArrayToString( b, MCharset.CS_ISO_8859_1 );
}
// --------------------------------------------------------------------------------
public static String byteArrayToString( byte[] b, String enc )
{
try
	{
	return new String( b, enc );
	}
catch( UnsupportedEncodingException e )
	{
	e.printStackTrace();
	return null;
	}
}
// --------------------------------------------------------------------------------
public static final byte[] getBytes( String s, String enc )
{
try
	{
	byte[] buf = s.getBytes( enc );
	return buf;
	}
catch( UnsupportedEncodingException e )
	{
	e.printStackTrace();
	return null;
	}
}
// --------------------------------------------------------------------------------
public static final byte[] getBytes( String s )
{
return getBytes( s, MCharset.CS_ISO_8859_1 );
}
//-------------------------------------------------------------------------------
public static final String jisToSjis( String jisStr )
throws Exception
{
String sjisStr = new String( jisStr.getBytes( "JIS" ) );
return sjisStr;
}
//-------------------------------------------------------------------------------
public static String getHostFromPeer( String peer )
{
int pos = peer.indexOf( ":" );
if( pos == -1 )
	{
	return peer;
	}
else
	{
	return peer.substring( 0, pos );
	}
}
//-------------------------------------------------------------------------------
public static int getPortFromPeer( String peer )
{
int pos = peer.indexOf( ":" );
if( pos == -1 )
	{
	return 80;
	}
else
	{
	String portStr = peer.substring( pos + 1 );
	return Integer.parseInt( portStr );
	}
}
// --------------------------------------------------------------------------------
public static int parseInt( String s )
{
return parseInt( s, 0 );
}
// --------------------------------------------------------------------------------
public static int parseInt( String s, int defaultValue )
{
int i;
try
	{
	i = Integer.parseInt( s );
	return i;
	}
catch( NumberFormatException e )
	{
	return defaultValue;
	}
}
//-------------------------------------------------------------------------------
public static ArrayList getErrorAddrList( net.jumperz.net.MMail mail )
throws IOException
{
ArrayList errorAddrList = new ArrayList();
String mailBody = mail.getBody();
byte[] mailBodyBuf = mailBody.getBytes();
BufferedReader reader = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( mailBodyBuf ) ) );
String line;
while( true )
	{
	line = reader.readLine();
	if( line == null )
		{
		break;
		}
	String errorAddr = MRegEx.getMatch( " ([^ ]{1,}@docomo.ne.jp)", line );
	if( !errorAddr.equals( "" ) )
		{
		errorAddrList.add( errorAddr );
		}
	}
return errorAddrList;
}
//-------------------------------------------------------------------------------
public static boolean isComment( String line )
{
if( line.indexOf( "#" ) == 0
// || line.indexOf( " " ) == 0
// || line.indexOf( "	" ) == 0
 || line.indexOf( "//" ) == 0 
 || line.equals( "" )
  )
	{
	return true;
	}
else
	{
	return false;
	}
}
// --------------------------------------------------------------------------------
public static void saveStringToFile( String str, String fileName )
throws IOException
{
saveStringToFile( str, fileName, MCharset.CS_ISO_8859_1 );
}
// --------------------------------------------------------------------------------
public static void saveStringToFile( String str, String fileName, String charset )
throws IOException
{
OutputStream os = new FileOutputStream( fileName );
try
	{
	os.write( str.getBytes( charset ) );
	}
finally
	{
	os.close();
	}
}
//--------------------------------------------------------------------------------
public static void saveCollectionToFile( Collection collection, String fileName )
throws IOException
{
BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( fileName ) );
try
	{
	Iterator p = collection.iterator();
	while( p.hasNext() )
		{
		String str = ( String )p.next();
		out.write( str.getBytes( "ISO-8859-1" ) );
		out.write( "\r\n".getBytes( "ISO-8859-1" ) );
		}
	}
finally
	{
	out.close();
	}
}
//--------------------------------------------------------------------------------
public static Set loadSetFromFile( String fileName )
throws IOException
{
BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( fileName ), MCharset.CS_ISO_8859_1 ) );
String line;
Set set = new HashSet( 20 );
try
	{
	while( true )
		{
		line = reader.readLine();
		if( line == null )
			{
			break;
			}
		if( !MStringUtil.isComment( line ) )
			{
			set.add( line );
			}
		}
	}
finally
	{
	reader.close();
	}

return set;
}
//-------------------------------------------------------------------------------
public static List loadListFromFile( String fileName )
throws IOException
{
BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( fileName ), MCharset.CS_ISO_8859_1 ) );
String line;
List list = new ArrayList( 20 );
try
	{
	while( true )
		{
		line = reader.readLine();
		if( line == null )
			{
			break;
			}
		if( !MStringUtil.isComment( line ) )
			{
			list.add( line );
			}
		}
	}
finally
	{
	reader.close();
	}
return list;
}
//-------------------------------------------------------------------------------
public static final String getHMacSHAHash( String key, String data )
{
String result;

try {
 
	// Get an hmac_sha1 key from the raw key bytes
	byte[] keyBytes = key.getBytes();		
	SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);
	 
	// Get an hmac_sha1 Mac instance and initialize with the signing key
	Mac mac = Mac.getInstance( HMAC_SHA1_ALGORITHM );
	mac.init( signingKey );
 
	// Compute the hmac on input data bytes
	byte[] rawHmac = mac.doFinal( data.getBytes() );
	
	// Convert raw bytes to Hex
	//byte[] hexBytes = new Hex().encode(rawHmac);
	
	//  Covert array of Hex bytes to a String
	result = byteToHexString( rawHmac );
	//System.out.println("MAC : " + result);
 
} 
catch (Exception e) {
	//throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
	return "";
	}
return result;
}
//-------------------------------------------------------------------------------
public static final String getSha1Hash( String str )
{
MessageDigest md = null;
try
	{
	md = MessageDigest.getInstance( "SHA1" );
	}
catch( NoSuchAlgorithmException e )
	{
	e.printStackTrace();
	}
md.update( str.getBytes() );
return MStringUtil.byteToHexString( md.digest() );
}
//-------------------------------------------------------------------------------
public static final String getMd5Hash( String str )
{
MessageDigest md = null;
try
	{
	md = MessageDigest.getInstance( "MD5" );
	}
catch( NoSuchAlgorithmException e )
	{
	e.printStackTrace();
	}
md.update( str.getBytes() );
return MStringUtil.byteToHexString( md.digest() );
}
//----------------------------------------------------------------------------
public static final String toDateString( long time, String s )
{
DecimalFormat format = new DecimalFormat( "00" );
Calendar cal = Calendar.getInstance();
cal.setTimeInMillis( time );

StringBuffer strbuf = new StringBuffer();
strbuf.append( cal.get( Calendar.YEAR ) );
strbuf.append( s );
strbuf.append( format.format( cal.get( Calendar.MONTH ) + 1 ) );
strbuf.append( s );
strbuf.append( format.format( cal.get( Calendar.DAY_OF_MONTH ) ) );
return strbuf.toString();
}
//----------------------------------------------------------------------------
public static final String toDateString( long time )
{
return toDateString( time, "/" );
}
//--------------------------------------------------------------------------------
public static long ipToLong( String ip )
{
String[] array = ip.split( "\\." );
if( array.length != 4 )
	{
	throw new NumberFormatException( "invalid IP address format" );
	}
long l;
l  = 256L * 256L * 256L * Integer.parseInt( array[ 0 ] );
l += 256L * 256L        * Integer.parseInt( array[ 1 ] );
l += 256L               * Integer.parseInt( array[ 2 ] );
l +=                      Integer.parseInt( array[ 3 ] );

return l;
}
// --------------------------------------------------------------------------------
public static String objectToString( Object o )
throws IOException
{
if( o == null )
	{
	return "";
	}
ByteArrayOutputStream buf = new ByteArrayOutputStream();
XMLEncoder e = new XMLEncoder( buf );
e.writeObject( o );
e.close();
String s = MStreamUtil.streamToString( new ByteArrayInputStream( buf.toByteArray() ) );
return URLEncoder.encode( s, MCharset.CS_ISO_8859_1 );
}
// --------------------------------------------------------------------------------
public static Object stringToObject( String s )
throws IOException
{
String s2 = URLDecoder.decode( s, MCharset.CS_ISO_8859_1 );
ByteArrayInputStream buf = new ByteArrayInputStream( s2.getBytes( MCharset.CS_ISO_8859_1 ) );
XMLDecoder d = new XMLDecoder( buf );
Object o = d.readObject();
d.close();

return o;
}
//--------------------------------------------------------------------------------
public static String getSubstringFrom( String s, String key )
{
int index = indexOf( s, key );
if( index > -1 )
	{
	return s.substring( index );
	}
else
	{
	return "";
	}
}
//--------------------------------------------------------------------------------
public static String getSubstringFrom( String s, String key, int length )
{
int index = indexOf( s, key );
if( index > -1 )
	{
	s = s.substring( index );
	return getSubstring( s, length );
	}
else
	{
	return "";
	}
}
//--------------------------------------------------------------------------------
public static String getSubstring( String s, int length )
{
if( s.length() > length )
	{
	return s.substring( 0, length );
	}
else
	{
	return s;
	}
}
//--------------------------------------------------------------------------------
public static String getIpPrefix( String ip )
{
return MRegEx.getMatch( "^([0-9]+\\.)", ip );
}
//--------------------------------------------------------------------------------
public static boolean isValidHostname( String host )
{
return host.matches( "^[a-zA-Z0-9]{1}[-a-zA-Z0-9\\.]{1,}\\.[a-zA-Z]{2,}$" );
}
//--------------------------------------------------------------------------------
public static int indexOf2( String target, String patternStr )
{
int targetCount = target.length();
int patternCount = patternStr.length();

if( 0 >= targetCount )
	{
	return ( patternCount == 0 ? targetCount : -1 );
	}

if( patternCount == 0 )
	{
	return 0;
	}

char[] patternUpper = patternStr.toUpperCase().toCharArray();
char[] patternLower = patternStr.toLowerCase().toCharArray();

char patternFirstUpper  = patternUpper[ 0 ];
char patternFirstLower  = patternLower[ 0 ];

int max = targetCount - patternCount;

for( int i = 0; i <= max; i++ )
	{
	/* Look for first character. */
	if( target.charAt( i ) != patternFirstUpper && target.charAt( i ) != patternFirstLower )
		{
		while( ++i <= max && target.charAt( i ) != patternFirstUpper && target.charAt( i ) != patternFirstLower );
		}

	/* Found first character, now look at the rest of v2 */
       if( i <= max )
		{
		int j = i + 1;
		int end = j + patternCount - 1;
		for (int k = 1; j < end && ( target.charAt( j ) == patternLower[k] || target.charAt( j ) == patternUpper[ k ] ) ; j++, k++);

		if (j == end)
			{
			/* Found whole string. */
			return i;
			}
		}
	}
return -1;
//return indexOfIgnoreCase( targetArray, 0, targetArray.length, patternArray, 0, patternArray.length, 0 );
}
//--------------------------------------------------------------------------------
public static int indexOfIgnoreCase( String target, String patternStr )
{
return indexOf2( target, patternStr );
}
//--------------------------------------------------------------------------------
public static int indexOf( String target, String patternStr )
{
return indexOf2( target, patternStr );
}
//--------------------------------------------------------------------------------
public static byte[] latin1Bytes( String s )
{
try
	{
	return s.getBytes( "ISO-8859-1" );
	}
catch( Exception willneverhappen )
	{
	return null;
	}
}
// --------------------------------------------------------------------------------
public static int indexOf3( String target, String patternStr )
{
if( patternStr.length() == 0 )
	{
	return 0;
	}

	//とりあえずASCIIについてのみ考える
char[] patternUpper = patternStr.toLowerCase().toCharArray();
char[] patternLower = patternStr.toUpperCase().toCharArray();

int targetCount = target.length();
int patternCount = patternStr.length();
int max = targetCount - patternCount;
for( int i = 0; i <= max ; ++i )
	{
	if( target.charAt( i ) !=  patternUpper[ 0 ] && target.charAt( i ) !=  patternLower[ 0 ] )
		{
		for( ; i <= max && target.charAt( i ) != patternUpper[ 0 ] && target.charAt( i ) != patternLower[ 0 ]; )
			{
			++i;
			}		
		}
	/*
	if( target.charAt( i ) !=  patternUpper[ 0 ] && target.charAt( i ) !=  patternLower[ 0 ] )
		{
		while( ++i <= max && target.charAt( i ) !=  patternUpper[ 0 ] && target.charAt( i ) !=  patternLower[ 0 ] );
		}
	*/
	int k = 1;
	if( i <= max )
		{
		for( ; k < patternUpper.length &&  ( target.charAt( i + k ) == patternUpper[ k ] || target.charAt( i + k ) == patternLower[ k ] ) ; ++k );
		}
	if( k == patternUpper.length )
		{
		return i;
		}
	}
return -1;
}
// --------------------------------------------------------------------------------
public static int indexOf31( String target, String patternStr )
{
if( patternStr.length() == 0 )
	{
	return 0;
	}

	//とりあえずASCIIについてのみ考える
char[] lowerArray = patternStr.toLowerCase().toCharArray();
char[] upperArray = patternStr.toUpperCase().toCharArray();

int bodyLen = target.length();
int patternLen = patternStr.length();
for( int i = 0; i < bodyLen; ++i )
	{
		//長さチェック
	if( ( bodyLen - i ) < patternLen )
		{
		break;
		}
	else
		{
		for( int k = 0; k < patternLen; ++k )
			{
			if( target.charAt( i + k ) != lowerArray[ k ] && target.charAt( i + k ) != upperArray[ k ] )
				{
				break;
				}
			else
				{
				if( k == ( patternLen - 1 ) )
					{
					return i;
					}
				}
			}
		}
	}
return -1;
}
//--------------------------------------------------------------------------------
public static void testRemoveSQLStrings()
throws Exception
{
/*
if( ! removeSQLStrings( "aaa' foo", true ).equals( "' foo" ) ){ ex(); }
if( ! removeSQLStrings( "aaa' foo 'bar'", true ).equals( "' foo ''" ) ){ ex(); }
if( ! removeSQLStrings( "aaa' foo 'bar' foo", true ).equals( "' foo '' foo" ) ){ ex(); }
if( ! removeSQLStrings( "aaa' foo 'bar", true ).equals( "' foo '" ) ){ ex(); }

if( ! removeSQLStrings( "'a''b'", false ).equals( "''" ) ){ ex(); }
if( ! removeSQLStrings( "' '' '", false ).equals( "''" ) ){ ex(); }
if( ! removeSQLStrings( "''''", false ).equals( "''" ) ){ ex(); }
if( ! removeSQLStrings( "' '' '' '", false ).equals( "''" ) ){ ex(); }

if( ! removeSQLStrings( "aaa' foo", false ).equals( "aaa'" ) ){ ex(); }
if( ! removeSQLStrings( "aaa' foo 'bar'", false ).equals( "aaa''bar'" ) ){ ex(); }
if( ! removeSQLStrings( "aaa' foo 'bar' foo", false ).equals( "aaa''bar'" ) ){ ex(); }
if( ! removeSQLStrings( "aaa' foo 'bar", false ).equals( "aaa''bar" ) ){ ex(); }
*/
}
//--------------------------------------------------------------------------------
/*
public static String removeSQLStrings( String s, boolean inString )
{
StringBuffer buf = new StringBuffer( s.length() );

while( s.length() > 0 )
	{
	if( inString )
		{
		int index = s.indexOf( "'" );
		if( index == -1 )
			{
			if( buf.length() == 0 )
				{
				return s;
				}
			else
				{
				break;			
				}
			}
		else
			{
			int index2 = s.indexOf( "''" );
			if( index2 == index )
				{
				s = s.substring( index + 2 );
				}
			else
				{
				buf.append( "'" );
				s = s.substring( index + 1 );
				inString = false;			
				}
			}
		}
	else
		{
		int index = s.indexOf( "'" );
		if( index == -1 )
			{
			buf.append( s );
			break;
			}
		else
			{
			buf.append( s.substring( 0, index + 1 ) ); //include '
			s = s.substring( index + 1 );
			inString = true;
			}
		}
	}

return buf.toString();
}*/
//--------------------------------------------------------------------------------
public static void testParseSQL()
throws Exception
{
if( ! parseSQL( "aaa/*" ).equals( "aaa" ) ){ ex(); }
if( ! parseSQL( "aaa/*foo*/" ).equals( "aaa " ) ){ ex(); }
if( ! parseSQL( "aaa/* foo */" ).equals( "aaa " ) ){ ex(); }
if( ! parseSQL( "aaa/* /*foo */" ).equals( "aaa " ) ){ ex(); }
if( ! parseSQL( "aaa/* /*foo * * * / */" ).equals( "aaa " ) ){ ex(); }
if( ! parseSQL( "aa/* /*foo * * * / */a" ).equals( "aa a" ) ){ ex(); }
if( ! parseSQL( "aaa/**/" ).equals( "aaa " ) ){ ex(); }
if( ! parseSQL( "aaa/*/" ).equals( "aaa" ) ){ ex(); }
if( ! parseSQL( "aaa/* */bbb/* */ccc" ).equals( "aaa bbb ccc" ) ){ ex(); }
if( ! parseSQL( "" ).equals( "" ) ){ ex(); }
if( ! parseSQL( "/* fffffffffffff" ).equals( "" ) ){ ex(); }
if( ! parseSQL( "aaa" ).equals( "aaa" ) ){ ex(); }
if( ! parseSQL( "/*hoge*/" ).equals( " " ) ){ ex(); }
if( ! parseSQL( "/* hoge */" ).equals( " " ) ){ ex(); }
if( ! parseSQL( "/*hoge*/'hoge'" ).equals( " ''" ) ){ ex(); }
if( ! parseSQL( "'hoge'/*hoge*/'hoge'" ).equals( "'' ''" ) ){ ex(); }

if( ! parseSQL( "hoge" ).equals( "hoge" ) ){ ex(); }
if( ! parseSQL( "hoge--" ).equals( "hoge--" ) ){ ex(); }
if( ! parseSQL( "hoge--\nfuga" ).equals( "hoge fuga" ) ){ ex(); }
if( ! parseSQL( "hoge--foobar\nfuga" ).equals( "hoge fuga" ) ){ ex(); }
if( ! parseSQL( "hoge--foo--bar\nfuga" ).equals( "hoge fuga" ) ){ ex(); }
if( ! parseSQL( "hoge--\nhoge--\nhoge" ).equals( "hoge hoge hoge" ) ){ ex(); }
if( ! parseSQL( "hoge--\nhoge--fuga\nhoge" ).equals( "hoge hoge hoge" ) ){ ex(); }
if( ! parseSQL( "hoge--\nhoge--fuga\nhoge--fuga" ).equals( "hoge hoge hoge--" ) ){ ex(); }

if( ! parseSQL( "hoge 'XXX' fuga" ).equals( "hoge '' fuga" ) ){ ex(); }
if( ! parseSQL( "''" ).equals( "''" ) ){ ex(); }
if( ! parseSQL( "'x'" ).equals( "''" ) ){ ex(); }
if( ! parseSQL( "'xxx'" ).equals( "''" ) ){ ex(); }
if( ! parseSQL( "'xxx' 1 'yyy'" ).equals( "'' 1 ''" ) ){ ex(); }
if( ! parseSQL( "''''" ).equals( "''" ) ){ ex(); }
if( ! parseSQL( "hoge 'fuga" ).equals( "hoge '" ) ){ ex(); }
if( ! parseSQL( "hoge 'fuga''" ).equals( "hoge '" ) ){ ex(); }
if( ! parseSQL( "hoge 'fuga' 'gyoe'" ).equals( "hoge '' ''" ) ){ ex(); }
if( ! parseSQL( "'hoge' 'fuga' 'gyoe'" ).equals( "'' '' ''" ) ){ ex(); }

if( ! parseSQL( "/* remove */" ).equals( " " ) ){ ex(); }
if( ! parseSQL( "/* remove -- 'foobar' */" ).equals( " " ) ){ ex(); }
if( ! parseSQL( "/* 'foo' --" ).equals( "" ) ){ ex(); }
if( ! parseSQL( "-- /* remove */ ''" ).equals( "--" ) ){ ex(); }
if( ! parseSQL( "--  '' /* " ).equals( "--" ) ){ ex(); }
if( ! parseSQL( "foo/* */bar 'xxx' baz--" ).equals( "foo bar '' baz--" ) ){ ex(); }

if( ! parseSQL( "foo#bar" ).equals( "foo--" ) ){ ex(); }
if( ! parseSQL( "foo#bar\nfoo" ).equals( "foo foo" ) ){ ex(); }

}
//--------------------------------------------------------------------------------
public static String removeSQLDoubleHyphenComments( String s )
{
StringBuffer buf = new StringBuffer( s.length() );
String[] array = s.split( "[\\r\\n]+" );
for( int i = 0; i < array.length; ++i )
	{
	String append = null;
	int index = array[ i ].indexOf( "--" );
	if( index > -1 )
		{
		append = array[ i ].substring( 0, index );
		}
	else
		{
		append = array[ i ];
		}
	
	if( append.length() > 0 )
		{
		if( i > 0 )
			{
			buf.append( ' ' );
			}
		buf.append( append );
		}
	}
return buf.toString();
}
//--------------------------------------------------------------------------------
public static String parseToCommentEnd1( String s )
{
	// parse to */
int index = s.indexOf( "*/" );
if( index == -1 )
	{
	return "";
	}
else
	{
	s = s.substring( index + 2 );
	}
return s;
}
//--------------------------------------------------------------------------------
public static String parseSQLComment2( StringBuffer buf, int index, String s ) // -- comment
{
buf.append( s.substring( 0, index ) );
s = s.substring( index + 2 );
String eol = MRegEx.getMatch( "[\\r\\n]+", s );
if( eol.length() > 0 )
	{
	buf.append( ' ' );
	s = s.substring( s.indexOf( eol ) + eol.length() );
	return s;
	}
else
	{
	buf.append( "--" ); // do not remove last --
	return "";
	}
}
//--------------------------------------------------------------------------------
public static String parseSQLComment1( StringBuffer buf, int index, String s ) // /+ comment
{
buf.append( s.substring( 0, index ) );
s = s.substring( index + 2 );

int index2 = s.indexOf( "*/" );
if( index2 > -1 )
	{
	buf.append( ' ' );
	s = s.substring( index2 + 2 );
	return s;
	}
else
	{
	return "";
	}
}
//--------------------------------------------------------------------------------
public static String parseSQLString( StringBuffer buf, int index, String s )
{
buf.append( s.substring( 0, index ) );
buf.append( '\'' ); //include '
s = s.substring( index + 1 );

while( true )
	{
	int index2 = s.indexOf( "'" );
	if( index2 == -1 )
		{
			// string not closed
		return "";
		}
	else
		{
			// ' found. check for ''
		if( s.length() == ( index2 + 1 ) )
			{
				//end of s
			buf.append( '\'' );
			return "";
			}
		else if( s.charAt( index2 + 1 ) == '\'' )
			{
			s = s.substring( index2 + 2 );			
			}
		else
			{
			buf.append( '\'' );
			s = s.substring( index2 + 1 );
			return s;
			}
		}
	}
}
//--------------------------------------------------------------------------------
public static void testParseSQL2()
throws Exception
{
if( ! parseSQL2( "hoge 'XXX' fuga" ).equals( "hoge '' fuga" ) ){ ex(); }
if( ! parseSQL2( "''" ).equals( "''" ) ){ ex(); }
if( ! parseSQL2( "'x'" ).equals( "''" ) ){ ex(); }
if( ! parseSQL2( "'xxx'" ).equals( "''" ) ){ ex(); }
if( ! parseSQL2( "'xxx' 1 'yyy'" ).equals( "'' 1 ''" ) ){ ex(); }
if( ! parseSQL2( "''''" ).equals( "''" ) ){ ex(); }
if( ! parseSQL2( "hoge 'fuga" ).equals( "hoge '" ) ){ ex(); }
if( ! parseSQL2( "hoge 'fuga''" ).equals( "hoge '" ) ){ ex(); }
if( ! parseSQL2( "hoge 'fuga' 'gyoe'" ).equals( "hoge '' ''" ) ){ ex(); }
if( ! parseSQL2( "'hoge' 'fuga' 'gyoe'" ).equals( "'' '' ''" ) ){ ex(); }

if( ! parseSQL2( "aaa/*" ).equals( "aaa" ) ){ ex(); }
if( ! parseSQL2( "aaa/*foo*/" ).equals( "aaa " ) ){ ex(); }
if( ! parseSQL2( "aaa/* foo */" ).equals( "aaa " ) ){ ex(); }
if( ! parseSQL2( "aaa/* /*foo */" ).equals( "aaa " ) ){ ex(); }
if( ! parseSQL2( "aaa/* /*foo * * * / */" ).equals( "aaa " ) ){ ex(); }
if( ! parseSQL2( "aa/* /*foo * * * / */a" ).equals( "aa a" ) ){ ex(); }
if( ! parseSQL2( "aaa/**/" ).equals( "aaa " ) ){ ex(); }
if( ! parseSQL2( "aaa/*/" ).equals( "aaa" ) ){ ex(); }
if( ! parseSQL2( "aaa/* */bbb/* */ccc" ).equals( "aaa bbb ccc" ) ){ ex(); }
if( ! parseSQL2( "" ).equals( "" ) ){ ex(); }
if( ! parseSQL2( "/* fffffffffffff" ).equals( "" ) ){ ex(); }
if( ! parseSQL2( "aaa" ).equals( "aaa" ) ){ ex(); }
if( ! parseSQL2( "/*hoge*/" ).equals( " " ) ){ ex(); }
if( ! parseSQL2( "/* hoge */" ).equals( " " ) ){ ex(); }
if( ! parseSQL2( "/*hoge*/'hoge'" ).equals( " ''" ) ){ ex(); }
if( ! parseSQL2( "'hoge'/*hoge*/'hoge'" ).equals( "'' ''" ) ){ ex(); }

if( ! parseSQL2( "hoge" ).equals( "hoge" ) ){ ex(); }
if( ! parseSQL2( "hoge--" ).equals( "hoge--" ) ){ ex(); }
if( ! parseSQL2( "hoge--\nfuga" ).equals( "hoge fuga" ) ){ ex(); }
if( ! parseSQL2( "hoge--foobar\nfuga" ).equals( "hoge fuga" ) ){ ex(); }
if( ! parseSQL2( "hoge--foo--bar\nfuga" ).equals( "hoge fuga" ) ){ ex(); }
if( ! parseSQL2( "hoge--\nhoge--\nhoge" ).equals( "hoge hoge hoge" ) ){ ex(); }
if( ! parseSQL2( "hoge--\nhoge--fuga\nhoge" ).equals( "hoge hoge hoge" ) ){ ex(); }
if( ! parseSQL2( "hoge--\nhoge--fuga\nhoge--fuga" ).equals( "hoge hoge hoge--" ) ){ ex(); }

if( ! parseSQL2( "/* remove */" ).equals( " " ) ){ ex(); }
if( ! parseSQL2( "/* remove -- 'foobar' */" ).equals( " " ) ){ ex(); }
if( ! parseSQL2( "/* 'foo' --" ).equals( "" ) ){ ex(); }
if( ! parseSQL2( "-- /* remove */ ''" ).equals( "--" ) ){ ex(); }
if( ! parseSQL2( "--  '' /* " ).equals( "--" ) ){ ex(); }
if( ! parseSQL2( "foo/* */bar 'xxx' baz--" ).equals( "foo bar '' baz--" ) ){ ex(); }

if( ! parseSQL2( "foo#bar" ).equals( "foo--" ) ){ ex(); }
if( ! parseSQL2( "foo#bar\nfoo" ).equals( "foo foo" ) ){ ex(); }
if( ! parseSQL2( "foo#bar\nfoo#hoge" ).equals( "foo foo--" ) ){ ex(); }

}
//--------------------------------------------------------------------------------
public static String parseSQL2( final String s )
{

StringBuffer buf = new StringBuffer( s.length() );
final int MODE_C_STYLE_COMMENT = 0;
final int MODE_SQL_COMMENT = 1;
final int MODE_SQL_STRING = 2;
final int MODE_DEFAULT = 3;
int mode = MODE_DEFAULT;

final int length = s.length();
for( int i = 0; i < length; ++i )
	{
	boolean isLastChar = ( i == s.length() -1 );
	char c = s.charAt( i );
	if( mode == MODE_DEFAULT )
		{
		if( c == '\'' )
			{
			mode = MODE_SQL_STRING;
			buf.append( c );
			}
		else if( c == '/' )
			{
			if( isLastChar )
				{
				buf.append( c );
				}
			else
				{
				if( s.charAt( i + 1 ) == '*' )
					{
					mode = MODE_C_STYLE_COMMENT;
					++i;
					}
				else
					{
					buf.append( c );
					}
				}
			}
		else if( c == '-' )
			{
			if( isLastChar )
				{
				buf.append( c );
				}
			else
				{
				if( s.charAt( i + 1 ) == '-' )
					{
					mode = MODE_SQL_COMMENT;
					++i;
					}
				}
			}
		else if( c == '#' )
			{
			mode = MODE_SQL_COMMENT;
			}
		else
			{
			buf.append( c );
			}
		}
	else if( mode == MODE_SQL_STRING )
		{
		if( c == '\'' )
			{
			if( isLastChar )
				{
					//end of parse
				mode = MODE_DEFAULT;
				buf.append( c );
				}
			else
				{
				if( s.charAt( i + 1 ) == '\'' )
					{
						// ''
					++i;	//skip ''
					}
				else
					{
						//end of string
					mode = MODE_DEFAULT;
					buf.append( c );
					}
				}
			}
		}
	else if( mode == MODE_C_STYLE_COMMENT )
		{
		if( c == '*' )
			{
			if( isLastChar )
				{
				}
			else
				{
				if( s.charAt( i + 1 ) == '/' )
					{
					mode = MODE_DEFAULT;
					buf.append( ' ' );
					++i;
					}
				else
					{
					}
				}
			}
		}
	else if( mode == MODE_SQL_COMMENT )
		{
		if( c == '\n' )
			{
			mode = MODE_DEFAULT;
			buf.append( ' ' );
			}
		}
	}

if( mode == MODE_SQL_COMMENT )
	{
	buf.append( "--" );
	}

return buf.toString();
}
//--------------------------------------------------------------------------------
public static String parseSQL( String s )
{
//return parseSQLSlow( s );
return parseSQL2( s );
}
//--------------------------------------------------------------------------------
public static String parseSQLSlow( String s )
{
s = s.replaceAll( "#", "--" );

int index1 = -2;
int index2 = -2;
int index3 = -2;

StringBuffer buf = new StringBuffer( s.length() );
while( s.length() > 0 )
	{
	if( index1 != -1 )
		{
		index1 = s.indexOf( "/*" );
		}
	if( index2 != -1 )
		{
		index2 = s.indexOf( "--" );
		}
	if( index3 != -1 )
		{
		index3 = s.indexOf( "'" );
		}
	
	if( index1 == -1
	 && index2 == -1
	 && index3 == -1
	  )
		{
		buf.append( s );
		break;
		}
	
	if(   index1 != -1
	 && ( index2 == -1 || index1 < index2 )
	 && ( index3 == -1 || index1 < index3 )
	  )
		{
		s = parseSQLComment1( buf, index1, s );
		}
	else if(   index2 != -1
	      && ( index1 == -1 || index2 < index1 )
	      && ( index3 == -1 || index2 < index3 )
	       )
	       {
	       s = parseSQLComment2( buf, index2, s );
	       }
	else if(   index3 != -1
	      && ( index1 == -1 || index3 < index1 )
	      && ( index2 == -1 || index3 < index2 )
	       )
	       {
	       s = parseSQLString( buf, index3, s );
	       }
	}

String result = buf.toString();
result = result.replaceAll( "\\s", " " );
return result;
}
//--------------------------------------------------------------------------------
public static boolean containsUneven( String value, String word )
{
while( true )
	{
	int index = indexOfWord( value, word );
	if( index == -1 )
		{
		return false;
		}
	else
		{
		String matchStr = value.substring( index, index + word.length() );
		//System.err.println( matchStr );
		if( isUneven( matchStr ) )
			{
			return true;
			}
		else
			{
			value = value.substring( index + word.length() );
			//System.err.println( value );
			}
		}
	}
}
//--------------------------------------------------------------------------------
public static void testContainsUneven()
throws Exception
{
if( containsUneven( "hoge fuga", "foo" ) ){ ex(); }
if( containsUneven( "hoge fuga", "hoge" ) ){ ex(); }
if( containsUneven( "Hoge fuga", "hoge" ) ){ ex(); }
if( containsUneven( "HOGE fuga", "hoge" ) ){ ex(); }
if( containsUneven( "hoge fuga", "HOGE" ) ){ ex(); }
if( containsUneven( "Hoge fuga", "HOGE" ) ){ ex(); }
if( containsUneven( "HOGE fuga", "HOGE" ) ){ ex(); }
if( containsUneven( "a hoge fuga", "foo" ) ){ ex(); }
if( containsUneven( "a hoge fuga", "hoge" ) ){ ex(); }
if( containsUneven( "a Hoge fuga", "hoge" ) ){ ex(); }
if( containsUneven( "a HOGE fuga", "hoge" ) ){ ex(); }
if( containsUneven( "a hoge fuga", "HOGE" ) ){ ex(); }
if( containsUneven( "a Hoge fuga", "HOGE" ) ){ ex(); }
if( containsUneven( "a HOGE fuga", "HOGE" ) ){ ex(); }
if( containsUneven( "a HOGE fuga hoge", "HOGE" ) ){ ex(); }

if( containsUneven( "a HOGE fuga", "fuga" ) ){ ex(); }

if( ! containsUneven( "hOGE fuga", "hoge" ) ){ ex(); }
if( ! containsUneven( "a hOGE fuga", "hoge" ) ){ ex(); }
if( ! containsUneven( "foo bar hOGE", "hoge" ) ){ ex(); }
if( ! containsUneven( "foo hoge hOGE", "hoge" ) ){ ex(); }

}
//--------------------------------------------------------------------------------
public static boolean isUneven( String s )
{
if( s.length() < 3 )
	{
	return false;
	}

if( s.matches( "^[A-Z]+$" )
 || s.matches( "^[A-Z]{1}[a-z]+$" )
 || s.matches( "^[a-z]+$" )
  )
	{
	return false;
	}
else
	{
	return true;
	}
}
//--------------------------------------------------------------------------------
public static void testIsUneven()
throws Exception
{
if( isUneven( "Hoge" ) ){ ex(); }
if( isUneven( "hoge" ) ){ ex(); }
if( isUneven( "HOGE" ) ){ ex(); }

if( ! isUneven( "HoGE" ) ){ ex(); }
if( ! isUneven( "HogE" ) ){ ex(); }
if( ! isUneven( "HoGe" ) ){ ex(); }
if( ! isUneven( "hoGE" ) ){ ex(); }
if( ! isUneven( "hoGe" ) ){ ex(); }
if( ! isUneven( "aAaaa" ) ){ ex(); }
if( ! isUneven( "aAaAa" ) ){ ex(); }
if( ! isUneven( "aAAAAa" ) ){ ex(); }
}
//--------------------------------------------------------------------------------
public static void testReplaceWordIgnoreCase()
throws Exception
{
if( ! replaceWordIgnoreCase( "foo bar baz", "bar", "xxx" ).equals( "foo xxx baz" ) ){ ex(); }
if( ! replaceWordIgnoreCase( "foo bar baz bar", "bar", "xxx" ).equals( "foo xxx baz xxx" ) ){ ex(); }
if( ! replaceWordIgnoreCase( "foo bar baz barbar", "bar", "xxx" ).equals( "foo xxx baz barbar" ) ){ ex(); }
if( ! replaceWordIgnoreCase( "foo", "bar", "xxx" ).equals( "foo" ) ){ ex(); }
if( ! replaceWordIgnoreCase( "foobar", "bar", "xxx" ).equals( "foobar" ) ){ ex(); }
if( ! replaceWordIgnoreCase( "bar bar", "bar", "xxx" ).equals( "xxx xxx" ) ){ ex(); }
if( ! replaceWordIgnoreCase( "barbarbar", "bar", "xxx" ).equals( "barbarbar" ) ){ ex(); }
if( ! replaceWordIgnoreCase( "x x x", "x", "xxx" ).equals( "xxx xxx xxx" ) ){ ex(); }
if( ! replaceWordIgnoreCase( "xxx", "x", "yyy" ).equals( "xxx" ) ){ ex(); }
if( ! replaceWordIgnoreCase( "bar", "bAr", "yyy" ).equals( "yyy" ) ){ ex(); }
}
//--------------------------------------------------------------------------------
public static String replaceWordIgnoreCase( String target, String fromWord, String to )
{
StringBuffer buf = new StringBuffer( target.length() );
int index = indexOfWord( target, fromWord );
while( index >= 0 )
	{
	buf.append( target.substring( 0, index ) );
	buf.append( to );
	target = target.substring( index + fromWord.length() );
	index = indexOfWord( target, fromWord );
	}
buf.append( target );
return buf.toString();
}
// --------------------------------------------------------------------------------
public static String cacheEncode( String str )
{
StringBuffer buf = new StringBuffer( str.length() * 3 );
for( int i = 0; i < str.length(); ++i )
	{
	String s = str.substring( i, i + 1 );
	if( s.matches( "[-_a-zA-Z0-9.]" ) )
		{
		buf.append( s );
		}
	else
		{
		buf.append( "%" );
		int k = s.charAt( 0 );
		if( k < 0 )
			{
			k += 256;
			}
		String hexRep = Integer.toHexString( k );
		if( hexRep.length() == 1 )
			{
			buf.append( "0" );
			}
		buf.append( hexRep );
		}
	}
return buf.toString();
}
// --------------------------------------------------------------------------------
public static String toLatin1String( String s, String charset )
{
if( charset == null || charset.equals( "" ) )
	{
	charset = MCharset.CS_ISO_8859_1;
	}
try
	{
	byte[] buf = s.getBytes( charset );
	return new String( buf, "ISO-8859-1" );
	}
catch( IOException e )
	{
	e.printStackTrace();
	return s;
	}
}
// --------------------------------------------------------------------------------
public static String toJavaString( String s, String charset )
{
if( charset == null || charset.equals( "" ) )
	{
	charset = MCharset.CS_ISO_8859_1;
	}
try
	{
	byte[] buf = s.getBytes( "ISO-8859-1" );
	return new String( buf, charset );
	}
catch( IOException e )
	{
	e.printStackTrace();
	return s;
	}
}
//--------------------------------------------------------------------------------
public static Date getDate( String yyyymmdd )
{
	//yyyymmdd = '2011/10/01'
return new java.util.Date(java.util.Date.parse( yyyymmdd ) );
}
//-------------------------------------------------------------------------------
}
