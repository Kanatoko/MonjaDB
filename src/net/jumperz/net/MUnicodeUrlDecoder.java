package net.jumperz.net;

import net.jumperz.net.exception.*;

public class MUnicodeUrlDecoder
{
private static final int HEX = 16;
//-------------------------------------------------------------------------------------
public static boolean isUrlDecoded( String in )
{
if( in.indexOf( "%" ) == -1 )
	{
	return false;
	}
else if( in.indexOf( "%u") == -1 
      && in.indexOf( "%U" ) == -1 
       )
	{
	return false;
	}
return true;
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