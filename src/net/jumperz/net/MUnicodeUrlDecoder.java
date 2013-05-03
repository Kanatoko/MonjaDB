package net.jumperz.net;

import net.jumperz.net.exception.*;
import net.jumperz.util.MCharset;
import net.jumperz.util.MStringUtil;

// test: MUnicodeUrlDecoderTest
public class MUnicodeUrlDecoder
{
private static final int HEX = 16;
//-------------------------------------------------------------------------------------
public static boolean isEncodedWithU( String in )
{
if( in.indexOf( "%u") > -1
 || in.indexOf( "%U" ) > -1
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
private static void appendChar( StringBuffer buf, char c )
{
if( c != '%' )
	{
	buf.append( c );
	}
}
//--------------------------------------------------------------------------------
private static int appendByte( byte[] buf, int buf_p, byte data )
{
if( data == ( byte )0x25 )
	{
	return buf_p;
	}
else if( data == ( byte )0x2B ) // + 
	{
	buf[ buf_p ] = ( byte )0x20; //space
	buf_p ++;
	}
else
	{
	buf[ buf_p ] = data;
	buf_p ++;
	}
return buf_p;
}
//-------------------------------------------------------------------------------------
public static String decodeForOldIIS( String inStr )
{
return decodeForIIS( inStr, MCharset.CS_ISO_8859_1 );
}
//-------------------------------------------------------------------------------------
public static String decodeForIIS( String inStr, String charset )
{
	//%XXと%uXXXXの混在に対応
	//古いIISのように、半端な%は消したりする
/*
■古いIIS・aspの場合
%61=ccc%u0042%41%5 -> cccBA5
%5の%が消えてる

ccc%u0042%41%5%u004X -> cccBA5u004X
%u004Xの%が消えてる

% -> 0x25
u -> 0x75
U -> 0x55
*/

byte[] in = MStringUtil.getBytes( inStr, MCharset.CS_ISO_8859_1 );
final int len = in.length;
byte[] out = new byte[ len ];
int buf_p = 0;
byte b1, b2;
for( int i = 0; i < len; )
	{
	b1 = in[ i ];
	if( b1 == ( byte )0x25 ) //%
		{
		if( i + 2 >= len )
			{
			++i;
			continue;
			}
		b2 = in[ i + 1 ];
		
			// u or U ?
		if( b2 != ( byte )0x75
		 && b2 != ( byte )0x55
		  )
			{
				//normal url encoding %XY
			int decodedInt = 0;
			
				//X
			byte X = b2;
			int iX = asciiByteToInt( X );
			if( iX < 0 )
				{
				buf_p = appendByte( out, buf_p, X );
				i += 2;
				continue;
				}
			decodedInt += iX * 16;
			
				//X
			byte Y = in[ i + 2 ];
			int iY = asciiByteToInt( Y );
			if( iY < 0 )
				{
				buf_p = appendByte( out, buf_p, X );
				buf_p = appendByte( out, buf_p, Y );
				i += 3;
				continue;
				}
			decodedInt += iY;
			
				//check
			if( decodedInt > 0x7F )
				{
				buf_p = appendByte( out, buf_p, X );
				buf_p = appendByte( out, buf_p, Y );
				i += 3;
				continue;
				}
			buf_p = appendByte( out, buf_p, ( byte )decodedInt );
			i += 3;
			}
		else
			{
				//%uABCD
			if( i + 5 >= len )
				{
				out[ buf_p ] = b2;
				buf_p ++;
				i += 2;
				continue;
				}
			int decodedInt = 0;
			
			byte A = in[ i + 2 ];
			int iA = asciiByteToInt( A );
			if( iA < 0 )
				{
				out[ buf_p ] = b2;
				buf_p ++;
				buf_p = appendByte( out, buf_p, A );
				i += 3;
				continue;
				}
			decodedInt += iA * 16 * 16 * 16;		

			byte B = in[ i + 3 ];
			int iB = asciiByteToInt( B );
			if( iB < 0 )
				{
				out[ buf_p ] = b2;
				buf_p ++;
				buf_p = appendByte( out, buf_p, A );
				buf_p = appendByte( out, buf_p, B );
				i += 4;
				continue;
				}
			decodedInt += iB * 16 * 16;		
			
			byte C = in[ i + 4 ];
			int iC = asciiByteToInt( C );
			if( iC < 0 )
				{
				out[ buf_p ] = b2;
				buf_p ++;
				buf_p = appendByte( out, buf_p, A );
				buf_p = appendByte( out, buf_p, B );
				buf_p = appendByte( out, buf_p, C );
				i += 5;
				continue;
				}
			decodedInt += iC * 16;		
			
			byte D = in[ i + 5 ];
			int iD = asciiByteToInt( D );
			if( iD < 0 )
				{
				out[ buf_p ] = b2;
				buf_p ++;
				buf_p = appendByte( out, buf_p, A );
				buf_p = appendByte( out, buf_p, B );
				buf_p = appendByte( out, buf_p, C );
				buf_p = appendByte( out, buf_p, D );
				i += 6;
				continue;
				}
			decodedInt += iD;
			
			buf_p = appendByte( out, buf_p, ( byte )decodedInt );
			i += 6;	
			}
		}
	else
		{
		buf_p = appendByte( out, buf_p, b1 );
		++i;
		}
	}

try
	{
	return new String( out, 0, buf_p, charset );
	}
catch( Exception e )
	{
	e.printStackTrace();
	return "";
	}
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
//-------------------------------------------------------------------------------------
public static String decode( String in )
throws IllegalArgumentException
{
	//この関数は%uXXXXを必ず含む文字列のデコード用
	//普通の%XXだけの文字列を入力した場合はエラーになることがある（ASCII領域だけならOK）
int len = in.length();
StringBuffer buf = new StringBuffer( len );
char c1, c2;
char decoded;
int i = 0;

while( i < len )
	{
	c1 = in.charAt( i );
	if( c1 == '%' )
		{
		if( i + 2 >= len )
			{
			throw new MIllegalEncodingException2( "Incomplete trailing escape (%) pattern : " + in ); 
			}
		c2 = in.charAt( i + 1 );
		if( c2 != 'u'
		 && c2 != 'U'
		  )
			{
				//normal url encoding
			int decodedInt = 0;
			for( int k = 0; k < 2; ++k )
				{
				char c3 = in.charAt( i + 1 + k );
				int i1 = Character.digit( c3, HEX );
				if( i1 < 0 )
					{
					throw new MIllegalEncodingException2( "Illegal characters in escape (%) pattern : " + in ); 							
					}
				decodedInt += ( i1 << 4 - ( 4 * k ) ); //上位は4bit左へシフト　下位はそのまま
				}
			
				//check
			if( decodedInt > 0x7F )
				{
				throw new MIllegalEncodingException1( "Illegal characters in escape (%) pattern : " + in ); 
				}
			decoded = ( char )decodedInt;
			buf.append( decoded );
			i += 3;
			}
		else
			{
			if( i + 5 >= len )
				{
				throw new MIllegalEncodingException2( "Incomplete trailing escape (%) pattern : " + in ); 
				}
			int decodedInt = 0;
			
			for( int j = 0; j < 4; ++j )
				{
				char c3 = in.charAt( i + 2 + j );
				int i1 = Character.digit( c3, HEX );
				if( i1 < 0 )
					{
					throw new MIllegalEncodingException2( "Illegal characters in escape (%) pattern : " + in ); 							
					}
				decodedInt += ( i1 << ( 12 - ( 4 * j ) ) );
				}
			decoded = ( char )decodedInt;
			buf.append( decoded );
			i += 6;			
			}
		}
	else
		{
		buf.append( c1 );
		++i;
		}
	}
return buf.toString();
}
//-------------------------------------------------------------------------------------
/*
public static String decode2( String in ) //old version
throws  IllegalArgumentException
{
int len = in.length();
StringBuffer buf = new StringBuffer( len );
char c1, c2;
char decoded;
int i = 0;

while( i < len )
	{
	c1 = in.charAt( i );
	if( c1 == '%' )
		{
		if( i + 5 >= len )
			{
			throw new  IllegalArgumentException( "Incomplete trailing escape (%) pattern" ); 
			}
		c2 = in.charAt( i + 1 );
		if( c2 != 'u'
		 && c2 != 'U'
		  )
			{
			throw new  IllegalArgumentException( "MUnicodeUrlDecoder: Illegal characters in escape (%) pattern" ); 
			}
		
		int decodedInt = 0;
		
		for( int j = 0; j < 4; ++j )
			{
			char c3 = in.charAt( i + 2 + j );
			int i1 = Character.digit( c3, HEX );
			if( i1 < 0 )
				{
				throw new  IllegalArgumentException( "MUnicodeUrlDecoder: Illegal characters in escape (%) pattern" ); 			
				}
			decodedInt += ( i1 << ( 12 - ( 4 * j ) ) );
			}
		decoded = ( char )decodedInt;
		buf.append( decoded );
		i += 6;
		}
	else
		{
		buf.append( c1 );
		++i;
		}
	}
return buf.toString();
}*/
//-------------------------------------------------------------------------------------
}