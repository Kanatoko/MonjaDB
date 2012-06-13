package net.jumperz.util;

import java.util.*;

public class MSubject1Impl
implements MSubject1
{
private List Observers;
//--------------------------------------------------------------------------
public MSubject1Impl()
{
Observers = new ArrayList();
}
//--------------------------------------------------------------------------
public void notify1()
{
List tmpList = null;
synchronized( Observers )
	{
	tmpList = new ArrayList( Observers );
	}

int size = tmpList.size();
for( int i = 0; i < size; ++i )
	{
	MObserver1 Observer = ( MObserver1 )tmpList.get( i );
	Observer.update();
	}
}
//--------------------------------------------------------------------------
public void register1( MObserver1 observer )
{
synchronized( Observers )
	{
	Observers.add( observer );
	}
}
//--------------------------------------------------------------------------
public void removeObservers1()
{
synchronized( Observers )
	{
	Observers.clear();
	}
}
//--------------------------------------------------------------------------
public void removeObserver1( MObserver1 observer )
{
synchronized( Observers )
	{
	Observers.remove( observer );
	}
}
// --------------------------------------------------------------------------------
public List getObservers()
{
return Observers;
}
//--------------------------------------------------------------------------
}
