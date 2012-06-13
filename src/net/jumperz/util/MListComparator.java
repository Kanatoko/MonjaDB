package net.jumperz.util;

import java.io.Serializable;
import java.util.*;

public class MListComparator
implements Comparator, Serializable
{
List list;
// --------------------------------------------------------------------------------
public MListComparator( List l )
{
list = l;
}
// --------------------------------------------------------------------------------
public int compare( Object o1, Object o2 )
{
int pos1 = list.indexOf( o1 );
int pos2 = list.indexOf( o2 );

if( pos1 == -1 && pos2 == -1 )
	{
	String s1 = ( String )o1;
	String s2 = ( String )o2;
	return s1.compareTo( s2 );
	}

if( pos1 == -1 )
	{
	pos1 = Integer.MAX_VALUE;
	}
if( pos2 == -1 )
	{
	pos2 = Integer.MAX_VALUE;
	}
return ( pos1 - pos2 );
}
// --------------------------------------------------------------------------------
}