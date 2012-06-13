package net.jumperz.util;

import java.util.*;

public abstract class MCommand2Impl
implements MCommand2
{
private MSubject2 subject2 = new MSubject2Impl();
//-----------------------------------------------------------------------------------
public final void notify2( Object event, Object source )
{
subject2.notify2( event, source );
}
//----------------------------------------------------------------
public final void register2( MObserver2 observer )
{
subject2.register2( observer );
}
//----------------------------------------------------------------
public final void removeObservers2()
{
subject2.removeObservers2();
}
//----------------------------------------------------------------
public final void removeObserver2( MObserver2 observer )
{
subject2.removeObserver2( observer );
}
//--------------------------------------------------------------------------------
}