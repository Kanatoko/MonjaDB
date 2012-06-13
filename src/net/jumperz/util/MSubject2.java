package net.jumperz.util;

import java.util.*;

public interface MSubject2
{
//--------------------------------------------------------------------------
public void notify2( Object event, Object source );
public void register2( MObserver2 observer );
public void removeObservers2();
public void removeObserver2( MObserver2 observer );
//--------------------------------------------------------------------------
}
