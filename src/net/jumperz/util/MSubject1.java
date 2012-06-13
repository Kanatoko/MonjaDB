package net.jumperz.util;

import java.util.*;

public interface MSubject1
{
//private MSubject1 subject = new MSubject1Impl();
//--------------------------------------------------------------------------
public void notify1();
public void register1( MObserver1 observer );
public void removeObservers1();
public void removeObserver1( MObserver1 observer );
//--------------------------------------------------------------------------
/*
//--------------------------------------------------------------------------------------------
public void notify1()
{
subject.notify1();
}
//----------------------------------------------------------------
public void register1( MObserver1 observer )
{
subject.register1( observer );
}
//----------------------------------------------------------------
public void removeObservers1()
{
subject.removeObservers1();
}
//----------------------------------------------------------------
public void removeObserver1( MObserver1 observer )
{
subject.removeObserver1( observer );
}
*/
}
