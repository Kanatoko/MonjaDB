package net.jumperz.app.MMonjaDB.eclipse.view;

import org.eclipse.ui.part.ViewPart;

import net.jumperz.app.MMonjaDB.eclipse.Activator;
import net.jumperz.app.MMonjaDB.eclipse.MUtil;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.action.MActionManager;
import net.jumperz.app.MMonjaDBCore.event.MEventManager;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Composite;
import net.jumperz.util.*;
import java.util.*;
import java.io.*;

public abstract class MAbstractView
extends ViewPart
implements MConstants, Listener, MInputView
{
protected Shell shell;
protected MProperties prop = MDataManager.getInstance().getProp();
private String windowName;
protected FormLayout	formLayout;
protected static FormData buttonFormData1, buttonFormData2, buttonFormData3;
protected boolean initialized = false;
protected Composite parent;
protected MEventManager eventManager = MEventManager.getInstance();
protected MActionManager actionManager = MActionManager.getInstance();
protected MDataManager dataManager = MDataManager.getInstance();
//protected MThreadPool threadPool = MDataManager.getInstance().getThreadPool();
//protected Set myActionSet = Collections.synchronizedSet( new HashSet() );
protected MenuManager menuManager;
protected IMenuManager dropDownMenu;
protected IToolBarManager toolBar;
protected IActionBars actionBars;

private MAbstractLogAgent logAgent = new MAbstractLogAgent(){};

static
{
buttonFormData1 = new FormData( BUTTON_WIDTH, BUTTON_HEIGHT );
buttonFormData1.right = new FormAttachment( 100, BUTTON1_RIGHT );
buttonFormData1.bottom = new FormAttachment( 100, BUTTON1_BOTTOM );

buttonFormData2 = new FormData( BUTTON_WIDTH, BUTTON_HEIGHT );
buttonFormData2.right = new FormAttachment( 100, BUTTON2_RIGHT );
buttonFormData2.bottom = new FormAttachment( 100, BUTTON2_BOTTOM );

buttonFormData3 = new FormData( BUTTON_WIDTH, BUTTON_HEIGHT );
buttonFormData3.right = new FormAttachment( 100, BUTTON3_RIGHT );
buttonFormData3.bottom = new FormAttachment( 100, BUTTON3_BOTTOM );
}
//--------------------------------------------------------------------------------
public void executeAction( String actionStr )
{
actionManager.executeAction( actionStr, this );
}
//--------------------------------------------------------------------------------
public MAbstractView()
{
}
//--------------------------------------------------------------------------------
protected boolean isActive()
{
return getSite().getPage().getActivePart().equals( this );
}
// --------------------------------------------------------------------------------
public void log( int logLevel, Object message )
{
logAgent.log( logLevel, message );
}
// --------------------------------------------------------------------------------
public void info( Object message )
{
log( MLogServer.log_info, message );
}
// --------------------------------------------------------------------------------
public void warn( Object message )
{
log( MLogServer.log_warn, message );
}
// --------------------------------------------------------------------------------
public void debug( Object message )
{
log( MLogServer.log_debug, message );
}
//--------------------------------------------------------------------------------
protected void setActionImage( Action action, String imageFileName )
{
Image image = MUtil.getImage( parent.getShell().getDisplay(), imageFileName );
action.setImageDescriptor( ImageDescriptor.createFromImage( image ) );
}
//--------------------------------------------------------------------------------
protected void addActionToDropDownMenu( Action action )
{
dropDownMenu.add( action );
}
//--------------------------------------------------------------------------------
protected void addActionToToolBar( Action action )
{
toolBar.add( action );
}
//--------------------------------------------------------------------------------
protected void initAction( Action action, String imageFileName )
{
initAction( action, imageFileName, null );
}
//--------------------------------------------------------------------------------
protected void initAction( Action action, String imageFileName, MenuManager menuManager )
{
try
	{
	if( imageFileName != null )
		{
		setActionImage( action, imageFileName );	
		}
	addActionToDropDownMenu( action );
	addActionToToolBar( action );
	
	if( menuManager != null )
		{
		menuManager.add( action );	
		}
	}
catch( Exception e )
	{
	eventManager.fireErrorEvent( e );
	}
}
//--------------------------------------------------------------------------------
public void createPartControl( Composite parent )
{
this.parent = parent;
shell = parent.getShell();

if( Activator.getDefault() != null )
	{
	Activator.getDefault().setShell( shell );
	}

formLayout = new FormLayout();

actionBars = getViewSite().getActionBars();
toolBar = actionBars.getToolBarManager();
dropDownMenu = actionBars.getMenuManager();

init2();

initialized = true;
}
// --------------------------------------------------------------------------------
protected void init2()
{
}
// --------------------------------------------------------------------------------
public final void handleEvent( Event event )
{
handleEvent2( event );
}
// --------------------------------------------------------------------------------
protected void handleEvent2( Event event )
{
}
//--------------------------------------------------------------------------------
}
