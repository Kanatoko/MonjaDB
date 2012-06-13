package net.jumperz.app.MMonjaDBCore.event;

import java.util.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;

public class MEvent
extends MAbstractLogAgent
{
private Map data = new HashMap();
private String eventName;

//--------------------------------------------------------------------------------
public MEvent( String eventName )
{
this.eventName = eventName;
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return eventName;
}
//--------------------------------------------------------------------------------
public void setData( Map m )
{
data = m ;
}
//--------------------------------------------------------------------------------
public Map getData()
{
return data;
}
//--------------------------------------------------------------------------------
}