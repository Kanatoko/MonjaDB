package net.jumperz.app.MMonjaDBCore.event;

import java.util.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;

public class MEventManager
extends MAbstractLogAgent
implements MSubject2
{
private static final MEventManager instance = new MEventManager();

private MSubject2 subject2 = new MSubject2Impl();
//--------------------------------------------------------------------------------
public static MEventManager getInstance()
{
return instance;
}
/*
//--------------------------------------------------------------------------------
public void fireErrorEvent( Exception e, String eventName )
{
MEvent event = new MEvent( eventName );
event.getData().put( "error", e );
fireEvent( event );
}
*/
//--------------------------------------------------------------------------------
public void fireErrorEvent( Exception e, Object source )
{
MEvent event = new MEvent( event_error );
event.getData().put( "error", e );
event.getData().put( "source", source );
fireEvent( event, source );
}
//--------------------------------------------------------------------------------
public void fireErrorEvent( Exception e )
{
MEvent event = new MEvent( event_error );
event.getData().put( "error", e );
fireEvent( event );
}
//--------------------------------------------------------------------------------
public synchronized void fireEvent( MEvent event )
{
notify2( event, null );
}
//--------------------------------------------------------------------------------
public synchronized void fireEvent( MEvent event, Object source )
{
notify2( event, source );
}
//-----------------------------------------------------------------------------------
public void notify2( Object event, Object source )
{
subject2.notify2( event, source );
}
//----------------------------------------------------------------
public void register2( MObserver2 observer )
{
subject2.register2( observer );
}
//----------------------------------------------------------------
public void removeObservers2()
{
subject2.removeObservers2();
}
//----------------------------------------------------------------
public void removeObserver2( MObserver2 observer )
{
subject2.removeObserver2( observer );
}
//--------------------------------------------------------------------------------
}