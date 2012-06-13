package net.jumperz.app.MMonjaDB.eclipse.view;

import net.jumperz.app.MMonjaDB.eclipse.MUtil;
import net.jumperz.app.MMonjaDBCore.*;
import net.jumperz.app.MMonjaDBCore.event.*;
import net.jumperz.app.MMonjaDBCore.action.*;
import net.jumperz.app.MMonjaDBCore.action.mj.MEditAction;
import net.jumperz.gui.MSwtUtil;
import net.jumperz.util.MSystemUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SimpleWildcardTester;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.SashForm;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import java.util.*;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;

public class MActionView
extends MAbstractView
implements MOutputView
{

protected FormLayout formLayout;
private Table table;
private SashForm sashForm;
private Composite editorComposite;
private Button executeButton;

private Action redoAction;
private Action editAction;
private Action clearAction;
private Action executeAction;
private Action copyAction;
private Action saveAction;

private List actionLogList;
private Text text;
private long initializedTime;
//--------------------------------------------------------------------------------
public MActionView()
{
actionManager.register2( this );
}
//--------------------------------------------------------------------------------
public void dispose()
{
actionManager.removeObserver2( this );

if( actionLogList.size() > MAX_SAVED_ACTION_LOG )
	{
	actionLogList = actionLogList.subList( 0, MAX_SAVED_ACTION_LOG );
	}
String savedStr = JSON.serialize( actionLogList );
prop.setProperty( ACTION_LOG_LIST, savedStr );

super.dispose();
}
//--------------------------------------------------------------------------------
private void executeActionsOnText()
{
String editorText = text.getText();
String[] array = editorText.split( "(\\r|\\n)+" );
if( array.length > 0 )
	{
	for( int i = 0; i < array.length; ++i )
		{
		String actionStr = array[ i ];
		if( actionStr.length() > 0 )
			{
			MActionManager.getInstance().executeAction( array[ i ], this );
			}
		}
	}
}
//--------------------------------------------------------------------------------
private void onTableStateChange()
{
boolean selectedItemExists = table.getSelectionCount() > 0;

redoAction.setEnabled( selectedItemExists );
editAction.setEnabled( selectedItemExists );
copyAction.setEnabled( selectedItemExists );
saveAction.setEnabled( selectedItemExists );

boolean itemExists = table.getItemCount() > 0;
clearAction.setEnabled( itemExists );
}
//--------------------------------------------------------------------------------
private void onSashResize()
{
if( System.currentTimeMillis() >= initializedTime + 3000 )
	{
	prop.setProperty( ACTIONLOG_COMPOSITE_WEIGHT, sashForm.getWeights() );
	}
}
//--------------------------------------------------------------------------------
public void init2()
{
parent.setLayout( new FormLayout() );

sashForm = new SashForm( parent, SWT.SMOOTH | SWT.VERTICAL);
FormData fd_sashForm1 = new FormData();
fd_sashForm1.top = new FormAttachment( 0, 1 );
fd_sashForm1.left = new FormAttachment( 0, 1 );
fd_sashForm1.right = new FormAttachment( 100, -1 );
fd_sashForm1.bottom = new FormAttachment( 100, -1 );
sashForm.setLayoutData(fd_sashForm1);

table = new Table( sashForm, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
table.addSelectionListener(new SelectionAdapter() {
	public void widgetSelected(SelectionEvent e) {
	onTableStateChange();
	}
});
table.addKeyListener(new KeyAdapter() {
	public void keyPressed(KeyEvent e) {
	if( e.keyCode == 13 )
		{
		e.doit = false;
		if( (e.stateMask & SWT.SHIFT) != 0 )//Shift + Enter
			{
			repeatActionsOnTable();	
			}
		else
			{
			editActions();		
			}
		}
	}
});

FormData fd_table = new FormData();
fd_table.top = new FormAttachment( 0, 0 );
fd_table.bottom = new FormAttachment( 100, 0 );
fd_table.left = new FormAttachment( 0, 0 );
fd_table.right = new FormAttachment( 100, 0 );

table.setLayoutData(fd_table);
table.setHeaderVisible(true);
table.setLinesVisible(true);
TableColumn actionColumn = new TableColumn(table, SWT.NONE);
actionColumn.setWidth(100);
actionColumn.setText("Action");
TableColumn dateColumn = new TableColumn(table, SWT.NONE);
dateColumn.setWidth(100);
dateColumn.setText("Date");

editorComposite = new Composite( sashForm, SWT.BORDER);
editorComposite.setLayout(new FormLayout());
text = new Text(editorComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
text.addModifyListener(new ModifyListener() {
	public void modifyText(ModifyEvent e) {
	onTextStateChange();
	}
});

FormData fd_text = new FormData();
fd_text.bottom = new FormAttachment(100, -40);
fd_text.right = new FormAttachment(100);
fd_text.top = new FormAttachment(0);
fd_text.left = new FormAttachment(0);
text.setLayoutData(fd_text);
executeButton = new Button(editorComposite, SWT.NONE);
executeButton.addSelectionListener(new SelectionAdapter() {
	public void widgetSelected(SelectionEvent e) {
	executeActionsOnText();
	}
});
executeButton.setEnabled( false );

FormData fd_executeButton = new FormData();
fd_executeButton.top = new FormAttachment(text, 6);
fd_executeButton.left = new FormAttachment(text, -120, SWT.RIGHT );
fd_executeButton.right = new FormAttachment(100, -10);
executeButton.setLayoutData(fd_executeButton);
executeButton.setText("Execute");
editorComposite.addControlListener(new ControlAdapter() {
	public void controlResized(ControlEvent e) {
	onSashResize();
	}
});

MSwtUtil.getTableColumnWidthFromProperties( "actionListTable" , table, prop, new int[]{ 200, 100 } );
MSwtUtil.addListenerToTableColumns2( table, this );

//table.addListener( SWT.KeyDown, this );
table.addListener( SWT.MouseDoubleClick, this );

menuManager = new MenuManager();
Menu contextMenu = menuManager.createContextMenu( table );
table.setMenu( contextMenu );

	//executeTableAction
{
redoAction = new Action(){ public void run(){//------------
repeatActionsOnTable();
}};//------------
redoAction.setToolTipText( "Redo Selected Actions" );
redoAction.setText( "Redo\tShift+Enter" );
initAction( redoAction, "table_go.png", menuManager );
redoAction.setEnabled( false );
}

	//editAction
{
editAction = new Action(){ public void run(){//------------
editActions();
}};//------------
editAction.setToolTipText( "Edit Actions on The Text Editor" );
editAction.setText( "Edit\tEnter" );
initAction( editAction, "pencil.png", menuManager );
editAction.setEnabled( false );
}

dropDownMenu.add( new Separator() );
menuManager.add( new Separator() );

	//executeAction
{
executeAction = new Action(){ public void run(){//------------
executeActionsOnText();
}};//------------
executeAction.setToolTipText( "Execute Actions on the Textarea" );
executeAction.setText( "Execute" );
setActionImage( executeAction,  "bullet_go.png" );
addActionToToolBar( executeAction );
executeAction.setEnabled( false );
dropDownMenu.add( executeAction );
}

dropDownMenu.add( new Separator() );
menuManager.add( new Separator() );

	//copyAction
{
copyAction = new Action(){ public void run(){//------------
copyActions();
}};//------------
copyAction.setToolTipText( "Copy Actions to Clipboard" );
copyAction.setText( "Copy" );
setActionImage( copyAction,  "page_copy.png" );
addActionToToolBar( copyAction );
copyAction.setEnabled( false );
dropDownMenu.add( copyAction );
menuManager.add( copyAction );
}

dropDownMenu.add( new Separator() );
menuManager.add( new Separator() );

	//clearAction
{
clearAction = new Action(){ public void run(){//------------
clearActions();
}};//------------
clearAction.setToolTipText( "Clear All" );
clearAction.setText( "Clear All" );
initAction( clearAction,  "table_delete.png", menuManager );
clearAction.setEnabled( false );
}

	//saveAction
{
saveAction = new Action(){ public void run(){//------------
saveActions();
}};//------------
saveAction.setToolTipText( "Save Action" );
saveAction.setText( "Save" );
initAction( saveAction,  "cog_add.png", menuManager );
saveAction.setEnabled( false );
}

	//load actionLogList
if( prop.containsKey( ACTION_LOG_LIST ) )
	{
	String savedStr = prop.getProperty( ACTION_LOG_LIST );
	actionLogList = ( java.util.List )JSON.parse( savedStr );
	for( int i = 0; i < actionLogList.size(); ++i )
		{
		Map actionLog = ( Map )actionLogList.get( i );
		addActionToTable( actionLog );
		}
	}
else
	{
	actionLogList = new LinkedList();
	}

if( prop.containsKey( ACTIONLOG_COMPOSITE_WEIGHT ) )
	{
	
	( new Thread(){ public void run() {
		MSystemUtil.sleep( 0 );
		shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//----
		sashForm.setWeights( prop.getIntArrayProperty( ACTIONLOG_COMPOSITE_WEIGHT ) );
		}});//----
	}} ).start();

	}
else
	{
	sashForm.setWeights( new int[]{ 70, 30 } );
	}
initializedTime = System.currentTimeMillis();

}
//--------------------------------------------------------------------------------
private void saveActions()
{
	//activate MSavedActionsView
try
	{
	MSavedActionsView savedActionsView = ( MSavedActionsView )PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView( "net.jumperz.app.MMonjaDB.eclipse.view.MSavedActionsView" );
	savedActionsView.setFocus();
	}
catch( Exception e )
	{
	MEventManager.getInstance().fireErrorEvent( e );
	return;
	}

MEvent e = new MEvent( event_save_actions );
Map data = new HashMap();

StringBuffer buf = new StringBuffer();

TableItem[] items = table.getSelection();
if( items != null )
	{
	for( int i = 0; i < items.length; ++i )
		{
		Map actionLog = ( Map )items[ i ].getData();
		if( actionLog != null )
			{
			if( buf.length() > 0 )
				{
				buf.append( "\r\n" );
				}
			buf.append( actionLog.get( "actionStr" ) );
			}
		}
	}

String actions = buf.toString();
if( actions.length() > 0 )
	{
	data.put( "actions", actions );
	e.setData( data );
	eventManager.fireEvent( e );
	}
}
//--------------------------------------------------------------------------------
private void copyActions()
{
	//copy selected actions on the table to clipboard
TableItem[] selectedItems = table.getSelection();
StringBuffer buf = new StringBuffer( 1024 );
for( int i = 0; i < selectedItems.length; ++i )
	{
	buf.append( selectedItems[ i ].getText( 0 ) );
	buf.append( "\r\n" );
	}
MSwtUtil.copyToClipboard( buf.toString() );
}
//--------------------------------------------------------------------------------
private void onTextStateChange()
{
boolean textExists = text.getText().length() > 0;

executeAction.setEnabled( textExists );
executeButton.setEnabled( textExists );
}
//--------------------------------------------------------------------------------
private void clearActions()
{
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//-----
table.removeAll();
onTableStateChange();
actionLogList.clear();
text.setText( "" );
}});//-----
}
//--------------------------------------------------------------------------------
private void editActions()
{
final MActionView actionLogView = this;
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****
TableItem[] items = table.getSelection();
StringBuffer buf = new StringBuffer( 2048 );
if( items != null )
	{
	for( int i = 0; i < items.length; ++i )
		{
		Map actionLog = ( Map )items[ i ].getData();
		if( actionLog != null )
			{
			buf.append( actionLog.get( "actionStr" ) );
			buf.append( "\r\n" );
			}
		}
	}

text.setText( buf.toString() );
text.setFocus();

}});//********
}
//--------------------------------------------------------------------------------
private void repeatActionsOnTable()
{
final MActionView actionLogView = this;
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****
TableItem[] items = table.getSelection();
if( items != null )
	{
	for( int i = 0; i < items.length; ++i )
		{
		Map actionLog = ( Map )items[ i ].getData();
		if( actionLog != null )
			{
			MActionManager.getInstance().executeAction( actionLog.get( "actionStr" ) + "", actionLogView );
			}
		}
	}
}});//********
}
//--------------------------------------------------------------------------------
private void onTableDoubleClick( Event event )
{
//repeatActionsOnTable();
editActions();
}
//--------------------------------------------------------------------------------
private void onTableColumnSelect( TableColumn column )
{

}
//--------------------------------------------------------------------------------
private void onTableColumnResize()
{
MSwtUtil.setTableColumnWidthToProperties( "actionListTable", table, prop );
}
// --------------------------------------------------------------------------------
protected void handleEvent2( Event event )
{
if( event.widget == table )
	{
	switch( event.type )
		{
		case SWT.MouseDoubleClick:
			onTableDoubleClick( event );
			break;
		}
	}
else if( MSwtUtil.getTableColumns( table ).contains( event.widget ) )
	{
	switch( event.type )
		{
		case SWT.Selection:
			onTableColumnSelect( ( TableColumn )event.widget );
			break;
		case SWT.Resize:
			onTableColumnResize();
			break;
		}
	}
}
//--------------------------------------------------------------------------------
private void addActionToTable( Map actionLog )
{
TableItem item = new TableItem( table, SWT.NONE );
item.setText( 0, ( String )actionLog.get( "actionStr" ) );
item.setText( 1, actionLog.get( "t" ) + "" );
table.showItem( item );
item.setData( actionLog );

onTableStateChange();
}
//--------------------------------------------------------------------------------
private void onAction( final String actionStr )
{
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

java.util.Date now = new java.util.Date();

BasicDBObject actionLog = new BasicDBObject();
actionLog.put( "actionStr", actionStr );
actionLog.put( "t", now );
addActionToTable( actionLog );

actionLogList.add( actionLog );

}});//********
}
//--------------------------------------------------------------------------------
public void update( final Object e, final Object source )
{
//threadPool.addCommand( new MCommand() {	public void execute(){ //-----------------

if( e instanceof String )
	{
	onAction( ( String )e );
	}
else if( e instanceof MEvent )
	{
	debug( "====" + e );
	}

//	} public void breakCommand(){}	} ); //------------
}
//--------------------------------------------------------------------------------
public void setFocus()
{

}
}
