package net.jumperz.util;

import java.util.*;

public class MSubject2Impl
implements MSubject2
{
private List Observers;
//--------------------------------------------------------------------------
public MSubject2Impl()
{
Observers = new ArrayList();
}
//--------------------------------------------------------------------------
public void notify2( Object event, Object source )
{
List tmpList = null;
synchronized( Observers )
	{
	tmpList = new ArrayList( Observers );
	}
	
int size = tmpList.size();
for( int i = 0; i < size; ++i )
	{
	MObserver2 Observer = ( MObserver2 )tmpList.get( i );
	Observer.update( event, source );
	}
}
//--------------------------------------------------------------------------
public void register2( MObserver2 observer )
{
synchronized( Observers )
	{
	Observers.add( observer );
	}
}
//--------------------------------------------------------------------------
public void removeObservers2()
{
synchronized( Observers )
	{
	Observers.clear();
	}
}
//--------------------------------------------------------------------------
public void removeObserver2( MObserver2 observer )
{
synchronized( Observers )
	{
	Observers.remove( observer );
	}
}
//--------------------------------------------------------------------------
}
