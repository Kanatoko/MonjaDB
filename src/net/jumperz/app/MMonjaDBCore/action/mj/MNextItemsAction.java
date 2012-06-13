package net.jumperz.app.MMonjaDBCore.action.mj;

import java.text.Collator;
import java.util.*;
import com.mongodb.*;
import net.jumperz.util.*;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.action.MAbstractAction;
import net.jumperz.app.MMonjaDBCore.event.*;

public class MNextItemsAction
extends MAbstractAction
{
//--------------------------------------------------------------------------------
public MNextItemsAction()
{
}
//--------------------------------------------------------------------------------
public boolean parse( String action )
{
return action.matches( "^mj next items$" );
}
//--------------------------------------------------------------------------------
public void executeFunction()
throws Exception
{
}
//--------------------------------------------------------------------------------
public int getActionCondition()
{
return action_cond_collection;
}
//--------------------------------------------------------------------------------
public String getEventName()
{
return event_mj_next_items;
}
//--------------------------------------------------------------------------------
}