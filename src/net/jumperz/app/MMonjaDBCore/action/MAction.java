package net.jumperz.app.MMonjaDBCore.action;

import net.jumperz.app.MMonjaDBCore.MInputView;
import net.jumperz.util.*;

public interface MAction
extends MCommand
{
public boolean parse( String action );
//public void setContext( Object context );
public void setOriginView( MInputView view );
public MInputView getOriginView();
}