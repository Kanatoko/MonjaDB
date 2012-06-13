package net.jumperz.util;

import java.util.*;

public class MPairValueComparator
implements Comparator
{
private boolean reverse;
//--------------------------------------------------------------------------------
public MPairValueComparator()
{
}
//--------------------------------------------------------------------------------
public MPairValueComparator( boolean b )
{
reverse = b;
}
// --------------------------------------------------------------------------------
public int compare( Object o1, Object o2 )
{
MPair pair1 = ( MPair )o1;
MPair pair2 = ( MPair )o2;

int c1 = MStringUtil.parseInt( pair1.getValue() );
int c2 = MStringUtil.parseInt( pair2.getValue() );

int result;
if( c1 < c2 )
	{
	result = -1;
	}
else if( c1 == c2 )
	{
	result = 0;
	}
else
	{
	result = 1;
	}

if( reverse )
	{
	return result * ( -1 );
	}
else
	{
	return result;
	}
}
// --------------------------------------------------------------------------------
}