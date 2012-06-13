package net.jumperz.app.MMonjaDB.eclipse.view;

import org.bson.types.BSONTimestamp;
import org.bson.types.CodeWScope;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.DialogMessageArea;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Composite;

import com.mongodb.*;
import com.mongodb.util.JSON;

import java.text.Collator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import net.jumperz.util.*;
import java.io.*;
import net.jumperz.gui.*;
import net.jumperz.mongo.MFindQuery;
import net.jumperz.mongo.MMongoUtil;

import net.jumperz.app.MMonjaDB.eclipse.MUtil;
import net.jumperz.app.MMonjaDB.eclipse.dialog.*;
import net.jumperz.app.MMonjaDBCore.MDataManager;
import net.jumperz.app.MMonjaDBCore.MOutputView;
import net.jumperz.app.MMonjaDBCore.action.*;
import net.jumperz.app.MMonjaDBCore.action.mj.MEditAction;
import net.jumperz.app.MMonjaDBCore.action.mj.MShowAllDbStatsAction;
import net.jumperz.app.MMonjaDBCore.event.*;
import java.util.*;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import java.util.regex.*;

public class MDocumentList
extends MAbstractView
implements MOutputView
{
private Table table;
private Image image;
private String dbName;
private String collName;

private Action copyAction;
private Action pasteAction;
private Action editDocumentAction;
private Action editFieldAction;
private Action copyFieldAction;
private Action copyAsJsonAction;
private Action copyAsStringAction;
private Action removeAction;
private Action insertBlankAction;
private Action insertJsonAction;
private Action reloadAction;
private Action forwardAction;
private Action backAction;
private Action prevItemsAction;
private Action nextItemsAction;
private Combo historyCombo;
private Combo grepCombo;
private Button prevItemsButton;
private Button nextItemsButton;
private Label naviLabel;
private int mouseUpX;
private int mouseUpY;
private Set grepStrSet = new LinkedHashSet();
//--------------------------------------------------------------------------------
public MDocumentList()
{
MEventManager.getInstance().register2( this );
}
//--------------------------------------------------------------------------------
public void dispose()
{
eventManager.removeObserver2( this );
super.dispose();
}
//--------------------------------------------------------------------------------
private void showPrevItems()
{
executeAction( "mj prev items" );
}
//--------------------------------------------------------------------------------
private void showNextItems()
{
executeAction( "mj next items" );
}
//--------------------------------------------------------------------------------
private void copyField()
{
if( mouseUpX == -1 || mouseUpY == -1 )
	{
	return;
	}

Point point = new Point( mouseUpX, mouseUpY );
final TableItem item = table.getItem( point );
if( item == null )
	{
	return;
	}
int columnIndex = -1;
for( int i = 0; i < table.getColumnCount(); ++i )
	{
	if( item.getBounds( i ).contains( point ) )
		{
		columnIndex = i;
		break;
		}
	}
if( columnIndex == -1 ) //ObjectId
	{
	return;
	}

TableItem[] items = table.getSelection();
if( items != null )
	{
	StringBuffer buf = new StringBuffer();
	for( int i = 0; i < items.length; ++i )
		{
		if( i > 0 )
			{
			buf.append( "\n" );
			}
		buf.append( items[ i ].getText( columnIndex ) );
		}
	MSwtUtil.copyToClipboard( buf.toString() );
	}
}
//--------------------------------------------------------------------------------
private void copyAsJson()
{
TableItem[] items = table.getSelection();
if( items == null || items.length == 0 )
	{
	return;
	}
StringBuffer buf = new StringBuffer();
for( int i = 0; i < items.length; ++i )
	{
	BasicDBObject documentData = ( BasicDBObject )items[ i ].getData();
	if( i > 0 )
		{
		buf.append( ",\n" );
		}
	buf.append( MMongoUtil.toJson( dataManager.getDB(), documentData ) );
	}
MSwtUtil.copyToClipboard( buf.toString() );
}
//--------------------------------------------------------------------------------
private void copyAsString()
{
TableItem[] items = table.getSelection();
if( items == null || items.length == 0 )
	{
	return;
	}
StringBuffer buf = new StringBuffer();
for( int i = 0; i < items.length; ++i )
	{
	if( i > 0 )
		{
		buf.append( "\n" );
		}
	for( int k = 0; k < table.getColumnCount(); ++k )
		{
		if( k > 0 )
			{
			buf.append( " " );
			}
		String value = items[ i ].getText( k );
		buf.append( value );
		}
	}
MSwtUtil.copyToClipboard( buf.toString() );
}
//--------------------------------------------------------------------------------
private void editField()
{
if( mouseUpX > -1 && mouseUpY > -1 )
	{
	onTableDoubleClick( mouseUpX, mouseUpY );
	}
}
//--------------------------------------------------------------------------------
private void copy()
{
executeAction( "mj copy" );
}
//--------------------------------------------------------------------------------
private void onCopy()
{
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//-----

TableItem[] items = table.getSelection();
if( items == null || items.length == 0 )
	{
	return;
	}

List copiedDocumentList = new ArrayList();
for( int i = 0; i < items.length; ++i )
	{
	copiedDocumentList.add( items[ i ].getData() );
	}
debug( copiedDocumentList );
dataManager.setCopiedDocumentList( copiedDocumentList );
dataManager.setCopiedCollName( collName );

}});//----
}
//--------------------------------------------------------------------------------
private void paste()
{
executeAction( "mj paste" );
}
//--------------------------------------------------------------------------------
private void remove()
{
MessageBox dialog = new MessageBox( shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
dialog.setText("Confirm Remove");
dialog.setMessage("Do you really want to remove?");
int returnCode = dialog.open();
if( returnCode == SWT.OK )
	{
	executeAction( "mj remove" );
	}
}
//--------------------------------------------------------------------------------
private void onRemove()
{
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//-----

TableItem[] items = table.getSelection();
if( items == null || items.length == 0 )
	{
	return;
	}
for( int i = 0; i < items.length; ++i )
	{
	BasicDBObject data = ( BasicDBObject )items[ i ].getData();
	BasicDBObject removeCond = new BasicDBObject();
	removeCond.put( "_id", data.get( "_id" ) );
	String objectStr = MMongoUtil.toJson( dataManager.getDB(), removeCond, true );
	executeAction( "db." + collName + ".remove(" + objectStr + ")" );
	}
dataManager.reloadDocument();
}});//----
}
//--------------------------------------------------------------------------------
private void onPaste()
{
List copiedDocumentList = dataManager.getCopiedDocumentList();
DBCollection coll = dataManager.getDB().getCollection( collName );

for( int i = 0; i < copiedDocumentList.size(); ++i )
	{
	BasicDBObject data = ( BasicDBObject )copiedDocumentList.get( i );
	if( dataManager.getCopiedCollName().equals( collName ) )
		{
		data.remove( "_id" );
		}
	coll.insert( data, WriteConcern.SAFE );
	/*
	String insertedObjectStr = MMongoUtil.toJson( dataManager.getDB(), data, true );
	debug( insertedObjectStr );
	executeAction( "db." + collName + ".insert(" + insertedObjectStr + ")" );
	*/
	}

dataManager.reloadDocument();
}
//--------------------------------------------------------------------------------
private void insertJsonDocument()
{
Map dialogData = new HashMap();
MInsertJsonDialog dialog = new MInsertJsonDialog( shell, dialogData );
int result = dialog.open();

if( dialogData.containsKey( "json" ) )
	{
	String jsonStr = ( String )dialogData.get( "json" );
	BasicDBList list = MMongoUtil.parseJsonToArray( dataManager.getDB(), jsonStr );
	for( int i = 0; i < list.size(); ++i )
		{
		executeAction( "db." + collName + ".insert(" + MMongoUtil.toJson( dataManager.getDB(), list.get( i ), true ) + ")" );
		}
	reload();
	}
}
//--------------------------------------------------------------------------------
private void insertBlankDocument()
{
executeAction( "db." + collName + ".insert({})" );
reload();
}
//--------------------------------------------------------------------------------
public void init2()
{
parent.setLayout( new FormLayout() );

table = new Table( parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI );
table.addMouseListener(new MouseAdapter() {
	@Override
	public void mouseDown(MouseEvent e) {
	onMouseDown( e );
	}
});
table.setHeaderVisible( true );
table.setLinesVisible( true );
FormData d1 = new FormData();
d1.top = new FormAttachment(0, 32);
d1.left = new FormAttachment( 0, 1 );
d1.right = new FormAttachment( 100, -1 );
d1.bottom = new FormAttachment( 100, -1 );
table.setLayoutData( d1 );

menuManager = new MenuManager();
Menu contextMenu = menuManager.createContextMenu( table );
table.setMenu( contextMenu );
historyCombo = new Combo(parent, SWT.NONE);
historyCombo.setToolTipText("Find Query. Hit Enter to apply.");
historyCombo.addKeyListener(new KeyAdapter() {
	public void keyPressed(KeyEvent e) {
	if( e.keyCode == 13 )
		{
		String text = historyCombo.getText();
		if( text != null && text.length() > 0 )
			{
			executeAction( text );		
			}
		}
	}
});
FormData fd_combo = new FormData();
fd_combo.bottom = new FormAttachment(table, -3);
fd_combo.left = new FormAttachment(0, 4);
fd_combo.right = new FormAttachment(30, 0);
historyCombo.setLayoutData(fd_combo);

	//listeners
table.addListener( SWT.MouseDoubleClick, this );
table.addListener( SWT.Selection, this );
table.addListener( SWT.KeyDown, this );

image = MUtil.getImage( parent.getShell().getDisplay(), "table.png" );

final MDocumentList documentList = this;

{	//editDocumentAction
editDocumentAction = new Action(){ public void run() { //--------
editDocument();
}};//-----
editDocumentAction.setText( "Edit Document" );
setActionImage( editDocumentAction, "table_edit.png" );
menuManager.add( editDocumentAction );
}

{	//editFieldAction
editFieldAction = new Action(){ public void run() { //--------
editField();
}};//-----
editFieldAction.setText( "Edit Field" );
setActionImage( editFieldAction, "page_edit.png" );
menuManager.add( editFieldAction );
}

menuManager.add( new Separator() );

{	//copyAction
copyAction = new Action(){ public void run() { //--------
copy();
}};//-----
copyAction.setText( "Copy\tCtrl+C" );
setActionImage( copyAction, "page_copy.png" );
menuManager.add( copyAction );
}

{	//pasteAction
pasteAction = new Action(){ public void run() { //--------
paste();
}};//-----
pasteAction.setText( "Paste\tCtrl+V" );
setActionImage( pasteAction, "page_white_paste_table.png" );
menuManager.add( pasteAction );
}

{	//insertBlankAction
insertBlankAction = new Action(){ public void run() { //--------
insertBlankDocument();
}};//-----
insertBlankAction.setText( "Insert Blank Document" );
setActionImage( insertBlankAction, "table_add.png" );
menuManager.add( insertBlankAction );
}

{	//insertJsonAction
insertJsonAction = new Action(){ public void run() { //--------
insertJsonDocument();
}};//-----
insertJsonAction.setText( "Insert JSON" );
setActionImage( insertJsonAction, "table_add.png" );
menuManager.add( insertJsonAction );
}

menuManager.add( new Separator() );

{
copyFieldAction =  new Action(){ public void run() { //--------
copyField();
}};//-----
copyFieldAction.setText( "Copy Field(s)" );
menuManager.add( copyFieldAction );
}

{
copyAsJsonAction =  new Action(){ public void run() { //--------
copyAsJson();
}};//-----
copyAsJsonAction.setText( "Copy Documents As JSON" );
menuManager.add( copyAsJsonAction );
}

{
copyAsStringAction =  new Action(){ public void run() { //--------
copyAsString();
}};//-----
copyAsStringAction.setText( "Copy Documents As String" );
menuManager.add( copyAsStringAction );
}

menuManager.add( new Separator() );

{	//removeAction
removeAction = new Action(){ public void run() { //--------
remove();
}};//-----
removeAction.setText( "Remove\tDEL" );
setActionImage( removeAction, "table_delete.png" );
menuManager.add( removeAction );
}

menuManager.add( new Separator() );

{	//reloadAction
reloadAction = new Action(){ public void run() { //--------
reload();
}};//-----
reloadAction.setText("Reload\tF5/Ctrl+R");
reloadAction.setToolTipText("Reload Documents");
initAction( reloadAction, "table_refresh.png", menuManager );
}

menuManager.add( new Separator() );

{	//backAction
backAction = new Action(){ public void run() { //--------
MHistory findHistory = dataManager.getFindHistory();
if( !findHistory.atBegin() )
	{
	findHistory.back();
	documentList.executeAction( findHistory.current() + "" );
	}
}};//-----
backAction.setText( "Back" );
backAction.setToolTipText( "Previous Find Query" );
initAction( backAction, "bullet_left.png", menuManager );
}

{	//forwardAction
forwardAction = new Action(){ public void run() { //--------
MHistory findHistory = dataManager.getFindHistory();
if( !findHistory.atEnd() )
	{
	findHistory.forward();
	documentList.executeAction( findHistory.current() + "" );
	}
}};//-----
forwardAction.setText( "Forward" );
forwardAction.setToolTipText( "Next Find Query" );
initAction( forwardAction, "bullet_right.png", menuManager );
}

menuManager.add( new Separator() );

{	//prevItemsAction
prevItemsAction = new Action(){ public void run() { //--------
showPrevItems();
}};//-----
prevItemsAction.setText( "Previous Items" );
prevItemsAction.setToolTipText( "Show Previous Results" );
setActionImage( prevItemsAction, "page_back.png" );
addActionToDropDownMenu( prevItemsAction );
menuManager.add( prevItemsAction );
}

{	//prevItemsButton
prevItemsButton = new Button( parent, SWT.FLAT);
prevItemsButton.addSelectionListener(new SelectionAdapter() {
	public void widgetSelected(SelectionEvent e) {
	showPrevItems();
	}
});
FormData fd_btnNewButton = new FormData();
fd_btnNewButton.right = new FormAttachment(table, -40, SWT.RIGHT );
prevItemsButton.setLayoutData(fd_btnNewButton);
Image image = MUtil.getImage( parent.getShell().getDisplay(), "page_back.png" );
prevItemsButton.setImage( image );
}

{	//nextItemsAction
nextItemsAction = new Action(){ public void run() { //--------
showNextItems();
}};//-----
nextItemsAction.setText( "Next Items" );
nextItemsAction.setToolTipText( "Show Next Results" );
setActionImage( nextItemsAction, "page_forward.png" );
addActionToDropDownMenu( nextItemsAction );
menuManager.add( nextItemsAction );
}

{	//nextItemsButton
nextItemsButton = new Button( parent, SWT.FLAT );
nextItemsButton.addSelectionListener(new SelectionAdapter() {
	public void widgetSelected(SelectionEvent e) {
	showNextItems();
	}
});
FormData fd_btnNewButton = new FormData();
fd_btnNewButton.right = new FormAttachment(table, -10, SWT.RIGHT );
nextItemsButton.setLayoutData(fd_btnNewButton);
Image image = MUtil.getImage( parent.getShell().getDisplay(), "page_forward.png" );
nextItemsButton.setImage( image );
}

naviLabel = new Label(parent, SWT.NONE);
FormData fd_naviLabel = new FormData();
fd_naviLabel.top = new FormAttachment(historyCombo, 6, SWT.TOP);
//fd_naviLabel.left = new FormAttachment(prevItemsButton, -66, SWT.LEFT);
fd_naviLabel.right = new FormAttachment(prevItemsButton, -6, SWT.LEFT);
naviLabel.setLayoutData(fd_naviLabel);
naviLabel.setText("");

grepCombo = new Combo(parent, SWT.NONE);
grepCombo.setToolTipText("Grep(RegEx). Hit Enter to apply");
FormData fd_grepCombo = new FormData();
fd_grepCombo.right = new FormAttachment(50);
fd_grepCombo.bottom = new FormAttachment(table, -3);
fd_grepCombo.left = new FormAttachment(30, 6);
grepCombo.setLayoutData(fd_grepCombo);
grepCombo.addKeyListener(new KeyAdapter() {
	public void keyPressed(KeyEvent e) {
	if( e.keyCode == 13 )
		{
		grep();
		}
	}
});

initActionsAndButtons();
}
//--------------------------------------------------------------------------------
private void grep()
{
try
	{
	String grepStr = grepCombo.getText();
	Pattern grepPattern = Pattern.compile( grepStr, Pattern.CASE_INSENSITIVE );
	drawTable( dataManager.getDocumentDataList(), dataManager.getColumnNameList(), grepPattern );
	
	if( !grepStrSet.contains( grepStr ) )
		{
		grepStrSet.add( grepStr );
		grepCombo.removeAll();
		Iterator p = grepStrSet.iterator();
		while( p.hasNext() )
			{
			grepCombo.add( ( String )p.next() );
			}
		grepCombo.setText( grepStr );
		}
	}
catch( Exception e )
	{
		//ok
	}
}
//--------------------------------------------------------------------------------
private void reload()
{
dataManager.reloadDocument();
}
//--------------------------------------------------------------------------------
private void editDocument()
{
TableItem[] items = table.getSelection();
if( items.length == 1 )
	{
	try
		{
		MDocumentEditor documentEditor = null;
		IWorkbenchPage[] pages = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();
		for( int i = 0; i < pages.length; ++i )
			{
			IViewReference[] views = pages[ i ].getViewReferences();
			for( int k = 0; k < views.length; ++k )
				{
				String viewId = views[ k ].getId();
				if( viewId.equals( MDocumentEditor.class.getName() ) )
					{
					documentEditor = ( MDocumentEditor )views[ k ].getView( true );
					pages[ i ].showView( viewId );
					break;
					}
				}
			}
		
		if( documentEditor == null )
			{
			documentEditor = ( MDocumentEditor )PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView( MDocumentEditor.class.getName() );		
			}
		
		int selectedIndex = table.getSelectionIndex();
		String objectIdStr = items[ 0 ].getText( 0 );
		executeAction( "mj edit " + objectIdStr );
		documentEditor.setFocus();
		documentEditor.activate();
		}
	catch( Exception e )
		{
		MEventManager.getInstance().fireErrorEvent( e );
		return;
		}
	}
}
//--------------------------------------------------------------------------------
public void setFocus()
{
}
//--------------------------------------------------------------------------------
private void onTableSelect()
{
if( !isActive() )
	{
	return;
	}

	//system.indexes is ignored
if( dataManager.getCollName().equals( "system.indexes" ) )
	{
	return;
	}

TableItem[] items = table.getSelection();
if( items.length == 1 )
	{
	int selectedIndex = table.getSelectionIndex();
	String objectIdStr = items[ 0 ].getText( 0 );
	executeAction( "mj edit " + objectIdStr );
	}
updateGui();
}
//--------------------------------------------------------------------------------
private void updateGui()
{
TableItem[] items = table.getSelection();
boolean oneItem = false;
boolean multiItem = false;
if( items.length == 1 )
	{
	oneItem = true;
	}
else if( items.length > 1 )
	{
	multiItem = true;
	}

editDocumentAction.setEnabled( oneItem );
copyAsJsonAction.setEnabled( multiItem || oneItem );
copyAsStringAction.setEnabled( multiItem || oneItem );
copyAction.setEnabled( multiItem || oneItem );
pasteAction.setEnabled( dataManager.getCopiedDocumentList().size() > 0 );
removeAction.setEnabled( multiItem || oneItem );

boolean collSelected = false;
debug( dataManager.getCollName() );
if( dataManager.isConnected() && dataManager.getCollName() != null && dataManager.getCollName().length() > 0 )
	{
	collSelected = true;
	}
insertBlankAction.setEnabled( collSelected );
insertJsonAction.setEnabled( collSelected );
}
//--------------------------------------------------------------------------------
private void updateDocument( TableItem item, int columnIndex, Class clazz, String value )
{
String objectIdStr = item.getText( 0 );
String fieldName = table.getColumn( columnIndex ).getText();

Object newValue = MMongoUtil.getValueByCurrentType( value, clazz );

dataManager.updateDocument( new ObjectId( objectIdStr ), fieldName, newValue );
/*
BasicDBObject query = new BasicDBObject( "_id", new ObjectId( objectIdStr ) );
BasicDBObject update = new BasicDBObject( "$set", new BasicDBObject( fieldName, newValue ) );

String updateStr = null;
if( newValue.getClass() == Integer.class )
	{
	updateStr = "{ $set : { \"" + fieldName + "\" : Number} }";
	}
else
	{
	updateStr = MMongoUtil.toJson( dataManager.getDB(), update );
	}

actionManager.executeAction( "db." + collName + ".update(" +
	MMongoUtil.toJson( dataManager.getDB(), query ) + "," +
	updateStr +
	",false, false )" );
*/

}
//--------------------------------------------------------------------------------
private Class getCurrentClass( TableItem item, int columnIndex )
{
String objectIdStr = item.getText( 0 );
ObjectId oid = new ObjectId( objectIdStr );

	//check data type
final DBObject currentData = ( DBObject)dataManager.getDocumentDataMap().get( oid );
String fieldName = table.getColumn( columnIndex ).getText();
Class clazz = null;
if( currentData.containsField( fieldName ) )
	{
	Object currentValue = currentData.get( fieldName );
	if( currentValue != null )
		{
		clazz = currentValue.getClass();	
		}
	}
return clazz;
}
//--------------------------------------------------------------------------------
private void onMouseDown( final MouseEvent event )
{
boolean fieldSelected = false;
Point point = new Point( event.x, event.y );
final TableItem item = table.getItem( point );
if( item == null )
	{
	}
else
	{
	int column = -1;
	for( int i = 0; i < table.getColumnCount(); ++i )
		{
		if( item.getBounds( i ).contains( point ) )
			{
			column = i;
			break;
			}
		}
	
	if( column == -1 )
		{
		mouseUpX = -1;
		mouseUpY = -1;
		}
	else
		{
		if( table.getSelection().length > 0 )
			{
			fieldSelected = true;
			}
		mouseUpX = event.x;
		mouseUpY = event.y;
		
		String columnName = table.getColumns()[ column ].getText();
		copyFieldAction.setText( "Copy Field ('" + columnName + "') Value" );
		}
	}

editFieldAction.setEnabled( fieldSelected );
copyFieldAction.setEnabled( fieldSelected );

updateGui();
}
//--------------------------------------------------------------------------------
private void onTableDoubleClick( final Event event )
{
onTableDoubleClick( event.x, event.y );
}
//--------------------------------------------------------------------------------
private void onTableDoubleClick( final int x, final int y )
{
	//system.indexes is ignored
if( dataManager.getCollName().equals( "system.indexes" ) )
	{
	return;
	}

final TableEditor editor = new TableEditor( table );

shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

Point point = new Point( x, y );
final TableItem item = table.getItem( point );
if( item == null )
	{
	return;
	}
int columnIndex = -1;
for( int i = 0; i < table.getColumnCount(); ++i )
	{
	if( item.getBounds( i ).contains( point ) )
		{
		columnIndex = i;
		break;
		}
	}
if( columnIndex == -1 || columnIndex == 0 ) //ObjectId
	{
	return;
	}

final Class clazz = getCurrentClass( item, columnIndex );
if( clazz == CodeWScope.class
 || clazz == BSONTimestamp.class
 || clazz == byte[].class
 || clazz == MinKey.class
 || clazz == MaxKey.class
  )
	{
	return;
	}

final Text text = new Text( table, SWT.NONE | SWT.BORDER );
text.setText( item.getText( columnIndex ) );
//text.selectAll();

editor.horizontalAlignment = SWT.LEFT;
editor.grabHorizontal = true;
editor.setEditor( text, item, columnIndex );

final int selectedColumn = columnIndex;
Listener textListener = new Listener(){
public void handleEvent( final Event e )
{
switch( e.type )
	{
	case SWT.FocusOut :
		updateDocument( item, selectedColumn, clazz, text.getText() );
		text.dispose();
		break;
	case SWT.Traverse :
		switch( e.detail )
			{
			case SWT.TRAVERSE_RETURN :
				//item.setText( selectedColumn, text.getText() );
				updateDocument( item, selectedColumn, clazz, text.getText() );
				//break;
			case SWT.TRAVERSE_ESCAPE :
				text.dispose();
				e.doit = false;
				//break;
			}
		break;
	}
}
};

text.addListener( SWT.FocusOut, textListener );
text.addListener( SWT.Traverse, textListener );
//text.selectAll();
text.setFocus();

}});//*****
}
//--------------------------------------------------------------------------------
private void onTableKeyDown( Event e )
{
if( ( ( e.stateMask & SWT.CTRL ) == SWT.CTRL ) )
	{
	if( e.keyCode == 'c' )
		{
		copy();
		e.doit = false;
		}
	else if( e.keyCode == 'v' )
		{
		paste();
		e.doit = false;
		}
	else if( e.keyCode == 'r' )
		{
		reload();
		e.doit = false;
		}
	}
else
	{
	if( e.keyCode == 127 ) //delete
		{
		remove();
		e.doit = false;
		}
	else if( e.keyCode == SWT.F5 )
		{
		reload();
		e.doit = false;
		}
	}
}
// --------------------------------------------------------------------------------
protected void handleEvent2( Event event )
{
if( event.widget == table )
	{
	switch( event.type )
		{
		case SWT.Selection:
			onTableSelect();
			break;
		case SWT.MouseDoubleClick:
			onTableDoubleClick( event );
			break;
		case SWT.KeyDown:
			onTableKeyDown( event );
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
private void onTableColumnSelect( TableColumn column )
{
final String columnName = column.getText();

executeAction( "mj sort by " + columnName + " " + dataManager.getSortOrder() );

/*
	//sort order
if( sortOrder == 1 )
	{
	sortOrder = -1;
	}
else
	{
	sortOrder = 1;
	}

final int _sortOrder = sortOrder;

drawTable( dataManager.getDocumentDataList(), dataManager.getColumnNameList() );
*/
}
// --------------------------------------------------------------------------------
private void onTableColumnResize()
{
MSwtUtil.setTableColumnWidthToProperties( getTablePrefix(), table, prop );
}
//--------------------------------------------------------------------------------
private void clearTableSwt()
{
	//reset table
TableColumn[] columns = table.getColumns();
for( int i = 0; i < columns.length;  ++i )
	{
	columns[ i ].dispose();
	}
table.removeAll();
}
//--------------------------------------------------------------------------------
private String getTablePrefix()
{
return DOCUMENTLIST_TABLE + "_" + dbName + "_" + collName;
}
//--------------------------------------------------------------------------------
private void drawTable( final java.util.List _dataList, final java.util.List _columnNameList )
{
drawTable( _dataList, _columnNameList, null );
}
//--------------------------------------------------------------------------------
private void drawTable( final java.util.List _dataList, final java.util.List _columnNameList, final Pattern grepPattern )
{
final MDocumentList documentList = this;
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

clearTableSwt();

if( _dataList.size() == 0 )
	{
	return;
	}

try
	{
		//set columns
	//java.util.List columnNameList = MSwtUtil.getNameListFromDataList( _dataList );
	//debug( columnNameList );

	{
	for( int i = 0; i < _columnNameList.size(); ++i )
		{
		String columnName = ( String )_columnNameList.get( i );
		TableColumn column = new TableColumn( table, SWT.NONE );
		column.setText( columnName );
		}
	
	//MSwtUtil.getTableColumnWidthFromProperties( getTablePrefix() , table, prop, new int[]{ 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40 } );
	MSwtUtil.getTableColumnWidthFromProperties2( getTablePrefix() , table, prop );
	MSwtUtil.addListenerToTableColumns2( table, documentList );
	}
	
		//draw items
	{
	for( int i = 0; i < _dataList.size(); ++i )
		{
		BasicDBObject documentData = ( BasicDBObject )_dataList.get( i );
		
		if( grepPattern != null )
			{
				//grep
			boolean grepMatches = false;
			for( int k = 0; k < _columnNameList.size(); ++k )
				{
				String columnName = ( String )_columnNameList.get( k );
				Object value = documentData.get( columnName );
				if( value == null )
					{
					}
				else
					{
					if( grepPattern.matcher( value.toString() ).find() )
						{
						grepMatches = true;
						break;
						}
					}
				}
			if( !grepMatches )
				{
				continue;
				}
			}
		
			//draw item
		TableItem item = new TableItem( table, SWT.NONE );
		item.setImage( image );
		item.setData( documentData );
		for( int k = 0; k < _columnNameList.size(); ++k )
			{
			String columnName = ( String )_columnNameList.get( k );
			Object value = documentData.get( columnName );
			if( value == null )
				{
				if( documentData.containsKey( columnName ) )
					{
					item.setText( k, "null" );
					}
				else
					{
					item.setText( k, "" );				
					}
				}
			else
				{
				/*
				if( value instanceof java.util.Date )
					{
					DateFormat df = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
					item.setText( k, df.format( ( Date  )value ) );
					}
				else
				*/
					{
					item.setText( k, value.toString() );				
					}
				}
			}
		}
	}
	
	}
catch( Exception e )
	{
	e.printStackTrace();
	MEventManager.getInstance().fireErrorEvent( e );
	}

}});//*****
}
//--------------------------------------------------------------------------------
private void onFind( MFindAction action )
{
dbName = action.getDB().getName();
collName = action.getCollection().getName();

final java.util.List documentDataList = dataManager.getDocumentDataList();
drawTable( documentDataList, dataManager.getColumnNameList() );

final MHistory history= dataManager.getFindHistory();
final java.util.List historyList = history.getList();
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//-----
historyCombo.removeAll();
for( int i = 0; i < historyList.size(); ++i )
	{
	historyCombo.add( historyList.get( i ) + "" );
	}
historyCombo.select( history.getPos() );

boolean hasPrevItems = dataManager.hasPrevItems();
prevItemsAction.setEnabled( hasPrevItems );
prevItemsButton.setEnabled( hasPrevItems );

boolean hasNextItems = dataManager.hasNextItems();
nextItemsAction.setEnabled( hasNextItems );
nextItemsButton.setEnabled( hasNextItems );

}});//-----

backAction.setEnabled( !history.atBegin() );
reloadAction.setEnabled( true );
forwardAction.setEnabled( !history.atEnd() );

	//naviLabel
setNaviLabel();
}
//--------------------------------------------------------------------------------
private void setNaviLabel()
{
String labelString = "";
MFindAction findAction = dataManager.getLastFindAction();
int itemCount = dataManager.getDocumentDataList().size();
if( itemCount > 0 && findAction != null )
	{
	MFindQuery findQuery = findAction.getFindQuery();
	int start = 1;
	int end = 0;
	int skip = findQuery.getSkipArg();
	if( skip == -1 )
		{
		skip = 0;
		}
	start = skip + 1;
	int limit = findQuery.getLimitArg();
	if( itemCount < limit )
		{
		limit = itemCount;
		}
	if( limit >= 0 )
		{
		end = skip + limit;
		}
	else
		{
		end = skip + itemCount;
		}
	labelString = start + "-" + end;
	}

final String s = labelString;
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//-----
naviLabel.setText( s );
naviLabel.getParent().layout();
//naviLabel.computeSize( SWT.DEFAULT, SWT.DEFAULT, true );
}});
}
//--------------------------------------------------------------------------------
private void initActionsAndButtons()
{
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//-----

reloadAction.setEnabled( false );
backAction.setEnabled( false );
forwardAction.setEnabled( false );
prevItemsAction.setEnabled( false );
prevItemsButton.setEnabled( false );
nextItemsAction.setEnabled( false );
nextItemsButton.setEnabled( false );
editDocumentAction.setEnabled( false );
editFieldAction.setEnabled( false );
copyFieldAction.setEnabled( false );
copyAsJsonAction.setEnabled( false );
copyAsStringAction.setEnabled( false );
copyAction.setEnabled( false );
pasteAction.setEnabled( false );
removeAction.setEnabled( false );

}});//-----
}
//--------------------------------------------------------------------------------
private void onSort()
{
drawTable( dataManager.getDocumentDataList(), dataManager.getColumnNameList() );
}
//--------------------------------------------------------------------------------
private void onEdit( MEditAction action )
{
final String id = action.getIdAsString();

shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

TableItem[] items = table.getItems();
for( int i = 0; i < items.length; ++i )
	{
	if( items[ i ].getText( 0 ).equals( id ) )
		{
		table.select( i );
		break;
		}
	}

}});//********
}
//--------------------------------------------------------------------------------
private void onUse()
{
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//-----
clearTableSwt();
}});//-----

initActionsAndButtons();
}
//--------------------------------------------------------------------------------
private void onDisconnect()
{
onUse();
}
//--------------------------------------------------------------------------------
public void update( final Object e, final Object source )
{
//threadPool.addCommand( new MCommand() {	public void execute(){ //-----------------

final MEvent event = ( MEvent )e;
final String eventName = event.getEventName();

if( event.getEventName().indexOf( event_find + "_end" ) == 0 )
	{
	MFindAction action = ( MFindAction )source;
	onFind( action );
	}
else if( event.getEventName().indexOf( event_mj_sort + "_end" ) == 0 )
	{
	onSort();
	}
else if( event.getEventName().indexOf( event_mj_edit + "_end" ) == 0 )
	{
	MEditAction action = ( MEditAction )source;
	onEdit( action );
	}
else if( event.getEventName().indexOf( event_use + "_end" ) == 0 )
	{
	onUse();
	}
else if( event.getEventName().indexOf( event_mj_copy + "_end" ) == 0 )
	{
	onCopy();
	}
else if( event.getEventName().indexOf( event_mj_paste + "_end" ) == 0 )
	{
	onPaste();
	}
else if( event.getEventName().indexOf( event_mj_remove + "_end" ) == 0 )
	{
	onRemove();
	}
else if( event.getEventName().indexOf( event_disconnect + "_end" ) == 0 )
	{
	onDisconnect();
	}
//	} public void breakCommand(){}	} ); //------------
}
}

