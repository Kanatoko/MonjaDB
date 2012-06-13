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
private static final Pattern pattern1 = Pattern.compile( "[^-a-zA-Z0-9!\"#$%&')(*+,./:;<\\]>?@\\[\\\\^_`~|]=" );

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
	Double d = ( Double )o;
	return d.intValue();
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
public static boolean containsTwoWordIgnoreCase( String target, String word1, String word2 )
{
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
}
//--------------------------------------------------------------------------------
public static int indexOfWord( String value, String word )
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
private static void testIndexOfWord()
throws Exception
{
if( indexOfWord( "aa fooo", "foo" ) != -1 ){ throw new Exception(); }
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
public static void main( String[] args )
throws Exception
{
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
	}
return str;

/*
byte[] in = getBytes( inStr, MCharset.CS_ISO_8859_1 );
int len = in.length;
byte[] buf = new byte[ len ];
int buf_p = 0;
for( int i = 0; i < len; buf_p++ )
	{
	byte b = in[ i ];
	if( b == ( byte )0x25 ) //%
		{
		if( len <= ( i + 2 ) )
			{
			throw new IllegalArgumentException( "Incomplete trailing escape (%) pattern" ); 
			}
		int i1 = asciiByteToInt( in[ i + 1 ] );
		int i2 = asciiByteToInt( in[ i + 2 ] );
		if( i1 == -1 || i2 == -1 )
			{
			throw new MIllegalEncodingException2( "Illegal characters in escape (%) pattern : " + inStr ); 
			}
		byte newByte = ( byte )( i1 * 16 + i2 );
		buf[ buf_p ] = newByte;
		i += 3;
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
	}
return str;
*/
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
public static boolean isValidHostname( String host )
{
return host.matches( "^[a-zA-Z0-9]{1}[-a-zA-Z0-9\\.]{1,}\\.[a-zA-Z]{2,}$" );
}
//--------------------------------------------------------------------------------
public static int indexOf( String target, String patternStr, String charset )
{
try
	{
	return indexOf( target.getBytes( charset ), patternStr );
	}
catch( Exception e )
	{
	return -1;
	}
}
//--------------------------------------------------------------------------------
public static int indexOf( String target, String patternStr )
{
return indexOf( target, patternStr, "ISO-8859-1" );
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
public static int indexOf( byte[] target, String patternStr )
{
	//とりあえずASCIIについてのみ考える
byte[] lowerArray = latin1Bytes( patternStr.toLowerCase() );
byte[] upperArray = latin1Bytes( patternStr.toUpperCase() );

int bodyLen = target.length;
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
			if( target[ i + k ] != lowerArray[ k ] && target[ i + k ] != upperArray[ k ] )
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
