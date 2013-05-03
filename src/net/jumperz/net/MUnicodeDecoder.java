package net.jumperz.net;

import net.jumperz.net.exception.*;

public class MUnicodeDecoder
{
private static final int HEX = 16;
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{

if( !decode( "\\u003a" ).equals( ":" ) ){ ex(); }
if( !decode( "\\u003b" ).equals( ";" ) ){ ex(); }
if( !decode( "A\\u003b" ).equals( "A;" ) ){ ex(); }
if( !decode( "A\\u003bA" ).equals( "A;A" ) ){ ex(); }
if( !decode( "A\\u003b\\" ).equals( "A;\\" ) ){ ex(); }
if( !decode( "A\\u003b\\u" ).equals( "A;\\u" ) ){ ex(); }
if( !decode( "A\\u003b\\u0" ).equals( "A;\\u0" ) ){ ex(); }
if( !decode( "A\\u003b\\u00" ).equals( "A;\\u00" ) ){ ex(); }
if( !decode( "A\\u003b\\u003" ).equals( "A;\\u003" ) ){ ex(); }
if( !decode( "A\\u003b\\u003a" ).equals( "A;:" ) ){ ex(); }
if( !decode( "\\" ).equals( "\\" ) ){ ex(); }
if( !decode( "\\u" ).equals( "\\u" ) ){ ex(); }
if( !decode( "\\u0" ).equals( "\\u0" ) ){ ex(); }
if( !decode( "\\u00" ).equals( "\\u00" ) ){ ex(); }
if( !decode( "\\u003" ).equals( "\\u003" ) ){ ex(); }
if( !decode( "\\u003a" ).equals( ":" ) ){ ex(); }

if( !decode( "\\u<<<<<" ).equals( "\\u<<<<<" ) ){ ex(); }
if( !decode( "\\u0<<<<<" ).equals( "\\u0<<<<<" ) ){ ex(); }
if( !decode( "\\u00<<<<<" ).equals( "\\u00<<<<<" ) ){ ex(); }
if( !decode( "\\u003<<<<<" ).equals( "\\u003<<<<<" ) ){ ex(); }
if( !decode( "\\u003a<<<<<" ).equals( ":<<<<<" ) ){ ex(); }

System.out.println( "== OK ==" );
}
//--------------------------------------------------------------------------------
public static void ex()
throws Exception
{
throw new Exception();
}
//-------------------------------------------------------------------------------------
public static String decode( String in )
throws IllegalArgumentException
{
	//この関数は%uXXXXを必ず含む文字列のデコード用
	//普通の%XXだけの文字列を入力した場合はエラーになることがある（ASCII領域だけならOK）
int len = in.length();
StringBuffer buf = new StringBuffer( len );
char c1, c2, c3;
char decoded;

// hoge|uXXXX
// 0123456789
for( int i = 0; i < len; ++i )
	{
	if( i + 5 < len )
		{
		c1 = in.charAt( i );
		c2 = in.charAt( i + 1 );
		if( c1 == '\\'
		 && ( c2 == 'u' || c2 == 'U' )
		  )
			{
			int decodedInt = 0;
			boolean success = false;
			
			for( int j = 0; j < 4; ++j )
				{
				c3 = in.charAt( i + 2 + j );
				int i1 = Character.digit( c3, HEX );
				if( i1 < 0 )
					{
					break;
					}
				decodedInt += ( i1 << ( 12 - ( 4 * j ) ) );
				if( j == 3 )
					{
					success = true;
					}
				}
			if( success )
				{
				decoded = ( char )decodedInt;
				buf.append( decoded );
				i += 5;
				}
			else
				{
				buf.append( c1 );
				buf.append( c2 );
				++i;
				}
			}
		else
			{
			buf.append( c1 );
			}
		}
	else
		{
		//System.out.println( "----" );
		buf.append( in.substring( i ) );
		break;
		}
	}
//System.out.println( buf.toString() );
return buf.toString();
}
//-------------------------------------------------------------------------------------
}