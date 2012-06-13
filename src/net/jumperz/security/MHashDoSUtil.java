package net.jumperz.security;

import java.util.*;

public class MHashDoSUtil
{
//--------------------------------------------------------------------------------
public static List getCollisionList( int len )
{
List l = new ArrayList();
getCollisionList( l, len, "" );
return l;
}
//--------------------------------------------------------------------------------
private static void getCollisionList( List list, int len, String s )
//throws Exception
{
//String[] array = new String[]{ "bB", "cc" };
//String[] array = new String[]{ "PH", "Oi" };//php5
//String[] array = new String[]{ "E1", "FY" };//dummy
String[] array = new String[]{ "Ey", "FZ" };//java

for( int i0 = 0;i0 < 2; ++i0 )
	{
	if( s.length() < len )
		{
		getCollisionList( list, len, s + array[ i0 ] );
		}
	else
		{
		String key = s + array[ i0 ];
		list.add( key );
		}
	}
}
}