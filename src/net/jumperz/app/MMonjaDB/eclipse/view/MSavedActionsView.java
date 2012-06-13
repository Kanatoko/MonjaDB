/*
 * Created on Mar 3, 2012
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.jumperz.app.MMonjaDB.eclipse.view;

import net.jumperz.app.MMonjaDB.eclipse.MUtil;
import net.jumperz.app.MMonjaDB.eclipse.dialog.MActionDialog;
import net.jumperz.app.MMonjaDB.eclipse.dialog.MPromptDialog;
import net.jumperz.app.MMonjaDBCore.MOutputView;
import net.jumperz.app.MMonjaDBCore.event.MEvent;
import net.jumperz.app.MMonjaDBCore.event.MEventManager;
import java.util.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;

import com.mongodb.util.JSON;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class MSavedActionsView
extends MAbstractView
implements MOutputView
{
private Tree tree;

private Action newAction, newFolderAction, editAction, removeAction, executeAction;
private Display display;
//private
//--------------------------------------------------------------------------------
public MSavedActionsView()
{
MEventManager.getInstance().register2( this );
}
//--------------------------------------------------------------------------------
private void onExecute()
{
final MSavedActionsView view = this;

TreeItem[] selected = tree.getSelection();
if( selected == null || selected.length != 1 )
	{
	}
else
	{
	Map data = ( Map )selected[ 0 ].getData();
	if( isFolderItem( selected[ 0 ] ) )
		{
		//do nothing
		}
	else
		{
		String actions = data.get( "actions" ) + "";
		String[] array = actions.split( "(\\r|\\n)+" );
		if( array.length > 0 )
			{
			for( int i = 0; i < array.length; ++i )
				{
				String actionStr = array[ i ];
				if( actionStr.length() > 0 )
					{
					executeAction( array[ i ] );		
					}
				}
			}
		}
	}
}
//--------------------------------------------------------------------------------
private void editSavedAction( TreeItem item )
{
MActionDialog dialog = new MActionDialog( shell, ( Map )item.getData() );
int result = dialog.open();
if( result ==  0 )
	{
	drawItem( item );
	tree.setSelection( item );
	}
}
//--------------------------------------------------------------------------------
private void onEdit()
{
TreeItem[] selected = tree.getSelection();
if( selected == null || selected.length != 1 )
	{
	}
else
	{
	Map data = ( Map )selected[ 0 ].getData();
	if( isFolderItem( selected[ 0 ] ) )
		{
		Set dataSet = new HashSet();
		MPromptDialog dialog = new MPromptDialog( shell, dataSet, "Rename Folder", "Name :", ( String )data.get( "name" ) );
		dialog.open();
		if( dataSet.size() > 0 )
			{
			data.put( "name", dataSet.iterator().next() );
			drawItem( selected[ 0 ] );
			}
		}
	else
		{
		editSavedAction( selected[ 0 ] );
		}
	}
}
//--------------------------------------------------------------------------------
private void onRemove()
{
TreeItem[] selected = tree.getSelection();
if( selected == null || selected.length != 1 || isRootItem( selected[ 0 ] ) )
	{
	}
else
	{
	MessageBox dialog = new MessageBox( shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
	dialog.setText("Confirm Remove");
	dialog.setMessage("Do you really want to remove?");
	int returnCode = dialog.open();
	if( returnCode == SWT.OK )
		{
		selected[ 0 ].dispose();
		}
	}
}
//--------------------------------------------------------------------------------
private void onTreeSelect()
{
TreeItem[] selected = tree.getSelection();
boolean itemSelected = false;
boolean actionSelected = false;
boolean rootSelected = false;
if( selected == null || selected.length == 0 )
	{
	itemSelected = false;
	}
else
	{
	itemSelected = true;
	if( isFolderItem( selected[ 0 ] ) )
		{
		actionSelected = false;
		
		if( isRootItem( selected[ 0 ] ) )
			{
			rootSelected = true;
			}
		}
	else
		{
		actionSelected = true;
		}
	}

editAction.setEnabled( itemSelected );
executeAction.setEnabled( actionSelected );
newAction.setEnabled( itemSelected );
newFolderAction.setEnabled( itemSelected );
removeAction.setEnabled( itemSelected && !rootSelected );
}
//--------------------------------------------------------------------------------
private void onTableKeyDown( Event e )
{
/*
if( ( ( e.stateMask & SWT.CTRL ) == SWT.CTRL ) )
	{

	}
else
*/
	{
	if( e.keyCode == 127 )
		{
		onRemove();
		}
	else if( e.keyCode == 13 )
		{
		if( (e.stateMask & SWT.SHIFT) != 0 )//Shift + Enter
			{
			onExecute();		
			}
		else
			{
			onEdit();
			}
		}
	}
}
// --------------------------------------------------------------------------------
protected void handleEvent2( Event event )
{
if( event.widget == tree )
	{
	switch( event.type )
		{
		/*
		}
		case SWT.Selection:
			onTableSelect();
			break;
		*/
		case SWT.MouseDoubleClick:
			onEdit();
			break;

		case SWT.KeyDown:
			onTableKeyDown( event );
			break;
		}
	}
}
//--------------------------------------------------------------------------------
public void init2()
{
parent.setLayout(new FormLayout());

tree = new Tree(parent, SWT.BORDER );
tree.addListener( SWT.KeyDown, this );
tree.addListener( SWT.MouseDoubleClick, this );

/*
tree.addKeyListener(new KeyAdapter() {
	public void keyPressed(KeyEvent e) {
	onKeyPressed( e );
	}
});
*/
tree.addSelectionListener(new SelectionAdapter() {
	public void widgetSelected(SelectionEvent e) {
	onTreeSelect();
	}
});
tree.addDisposeListener(new DisposeListener() {
	public void widgetDisposed(DisposeEvent e) {
	saveSavedActions();
	}
});
tree.setHeaderVisible(true);
FormData fd_tree_1 = new FormData();
fd_tree_1.bottom = new FormAttachment(100, 0);
fd_tree_1.right = new FormAttachment(100, -1);
fd_tree_1.top = new FormAttachment(0, 0);
fd_tree_1.left = new FormAttachment(0, 0 );
tree.setLayoutData(fd_tree_1);

loadSavedActions();

menuManager = new MenuManager();
Menu contextMenu = menuManager.createContextMenu( tree );
tree.setMenu( contextMenu );

executeAction = new Action(){ public void run(){//-----------
onExecute();
}};//-----------
executeAction.setToolTipText("Execute Saved Actions");
executeAction.setText( "Execute\tShift+Enter" );
initAction( executeAction, "cog_go.png", menuManager );

editAction = new Action(){ public void run(){//-----------
onEdit();
}};//-----------
editAction.setToolTipText("Edit Saved Actions");
editAction.setText( "Edit" );
initAction( editAction, "cog_edit.png", menuManager );

menuManager.add( new Separator() );

newAction = new Action(){ public void run(){//-----------
onNewSavedAction();
}};//-----------
newAction.setToolTipText("New");
newAction.setText( "New" );
initAction( newAction, "cog_add.png", menuManager );

newFolderAction = new Action(){ public void run(){//-----------
onNewFolder();
}};//-----------
newFolderAction.setToolTipText("Create A New Folder");
newFolderAction.setText( "New Folder" );
initAction( newFolderAction, "folder_add.png", menuManager );

menuManager.add( new Separator() );

removeAction = new Action(){ public void run(){//-----------
onRemove();
}};//-----------
removeAction.setToolTipText("Remove Selected Item");
removeAction.setText( "Remove" );
initAction( removeAction, "cog_delete.png", menuManager );

executeAction.setEnabled( false );
editAction.setEnabled( false );
newAction.setEnabled( false );
newFolderAction.setEnabled( false );
removeAction.setEnabled( false );

display = shell.getDisplay();
setupDnd();

if( tree.getItemCount() > 0 )
	{
	tree.setSelection( tree.getItems()[ 0 ] );
	onTreeSelect();
	}

/*
tree = new Tree( parent, SWT.BORDER );
tree.setHeaderVisible( true );
FormData d1 = new FormData();
d1.top = new FormAttachment( 0, 1 );
d1.left = new FormAttachment( 0, 1 );
d1.right = new FormAttachment( 100, -1 );
d1.bottom = new FormAttachment( 100, -1 );
tree.setLayoutData( d1 );
*/
/*
tree = new Tree( sashForm,  SWT.BORDER );
*/
}
//--------------------------------------------------------------------------------
private void setupDnd()
{
Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

final DragSource source = new DragSource( tree, operations );
source.setTransfer( types );

final TreeItem[] dragSourceItem = new TreeItem[ 1 ];
source.addDragListener( new DragSourceListener() {
//-----------------------------------------------------------------------
public void dragStart( DragSourceEvent event )
{
TreeItem[] selection = tree.getSelection();
if ( selection.length > 0 )// && selection[0].getItemCount() == 0 )
	{
	dragSourceItem[ 0 ] = selection[ 0 ];
	if( isRootItem( dragSourceItem[ 0 ] ) )
		{
		event.doit = false;	
		}
	else
		{
		event.doit = true;	
		}
	}
else
	{
	event.doit = false;	
	}
};
//-----------------------------------------------------------------------
public void dragSetData( DragSourceEvent event )
{
event.data = "dummy";//dragSourceItem[ 0 ];
}
//-----------------------------------------------------------------------
public void dragFinished( DragSourceEvent event )
{
if( event.detail == DND.DROP_MOVE )
dragSourceItem[0].dispose();
dragSourceItem[0] = null;
}
//-----------------------------------------------------------------------
});

DropTarget target = new DropTarget( tree, operations );
target.setTransfer( types );
target.addDropListener( new DropTargetAdapter() {
//-----------------------------------------------------------------------
public void dragOver( DropTargetEvent event )
{
event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
if( event.item != null )
	{
	TreeItem targetItem = ( TreeItem )event.item;
	Point pt = display.map( null, tree, event.x, event.y );
	Rectangle bounds = targetItem.getBounds();
	
	if( isRootItem( targetItem ) )
		{
		event.feedback |= DND.FEEDBACK_SELECT;
		}
	else if( dropOnChildOrSelf( dragSourceItem[ 0 ], targetItem ) )
		{
		event.feedback |= DND.FEEDBACK_NONE;		
		}
	else if( isFolderItem( targetItem ) )
		{
		if (pt.y < bounds.y + bounds.height / 3)
			{
			event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
			}
		else if( pt.y > bounds.y + 2 * bounds.height / 3 )
			{
			event.feedback |= DND.FEEDBACK_INSERT_AFTER;
			}
		else
			{
			event.feedback |= DND.FEEDBACK_SELECT;
			}
		}
	else
		{
		if (pt.y < bounds.y + bounds.height / 2)
			{
			event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
			}
		else
			{
			event.feedback |= DND.FEEDBACK_INSERT_AFTER;
			}		
		}
	}
}
//--------------------------------------------------------------------------------
public void drop( DropTargetEvent event )
{
if( event.data == null )
	{
	debug( "data is null" );
	event.detail = DND.DROP_NONE;
	return;
        }
//Map data = ( Map )JSON.parse( ( String ) event.data );
if( event.item == null )
	{
	debug( "target is null" );
	event.detail = DND.DROP_NONE;
	}
else
	{
	TreeItem destItem = ( TreeItem )event.item;
	TreeItem sourceItem = dragSourceItem[ 0 ];
	boolean srcIsFolder = isFolderItem( dragSourceItem[ 0 ] );
	boolean destIsFolder = isFolderItem( destItem );
	
	if( dropOnChildOrSelf( sourceItem, destItem ) )
		{
		debug( "drop on child." );
		event.detail = DND.DROP_NONE;
		return;
		}
	
	Point pt = display.map( null, tree, event.x, event.y );
	Rectangle bounds = destItem.getBounds();
	TreeItem parent = destItem.getParentItem();
	
	if( parent != null )
		{
		TreeItem[] items = parent.getItems();
		int targetItemIndex = 0;
		for( int i = 0; i < items.length; i++ )
			{
			if( items[ i ] == destItem )
				{
	 			targetItemIndex = i;
				break;
				}
			}
		TreeItem newItem = null;
		
		if( isRootItem( destItem ) )
			{
			debug( "added to root" );
			newItem = new TreeItem( destItem, SWT.NONE );
			}
		else if( destIsFolder )
			{
			if( pt.y < bounds.y + bounds.height / 3 )
				{
				newItem = new TreeItem( parent, SWT.NONE, targetItemIndex );
				}
			else if( pt.y > bounds.y + 2 * bounds.height / 3 )
				{
				newItem = new TreeItem( parent, SWT.NONE, targetItemIndex + 1 );
				}
			else
				{
				newItem = new TreeItem( destItem, SWT.NONE );
				}
			}
		else
			{
			if( pt.y < bounds.y + bounds.height / 2 )
				{
				newItem = new TreeItem( parent, SWT.NONE, targetItemIndex );
				}
			else 
				{
				newItem = new TreeItem( parent, SWT.NONE, targetItemIndex + 1 );
				}
			}
		//newItem.setData( data );
		//drawItem( newItem );
		
		copyTreeItem( sourceItem, newItem );
		}
	else
		{
		debug( "drop on root" );
		TreeItem newItem = new TreeItem( destItem, SWT.NONE );		
		copyTreeItem( sourceItem, newItem );
		}
	/*
	if( isFolderItem( targetItem ) )
		{
		
		}
	else
		{
		
		}
	*/
	
	
	/*
	*/
	/*
	else
		{
		debug( "--no" );
		TreeItem[] items = tree.getItems();
		int index = 0;
		for( int i = 0; i < items.length; i++ )
			{
			if( items[i] == targetItem )
				{
				index = i;
				break;
				}
			}
		TreeItem newItem = null;
		if( pt.y < bounds.y + bounds.height / 3 )
			{
			newItem = new TreeItem( tree, SWT.NONE, index );
			}
		else if( pt.y > bounds.y + 2 * bounds.height / 3 )
			{
			newItem = new TreeItem( tree, SWT.NONE, index + 1 );
			}
		else
			{
			newItem = new TreeItem(targetItem, SWT.NONE);
			}
		newItem.setData( data );
		drawItem( newItem );
		}
	*/
	}
}
//--------------------------------------------------------------------------------    
});

}
//--------------------------------------------------------------------------------
private void drawItem( TreeItem item )
{
Map data = ( Map )item.getData();
if( data == null )
	{
	return;
	}
else
	{
	if( data.containsKey( "type" ) && data.get( "type" ).equals( "folder" ) )
		{
		Image image = MUtil.getImage( parent.getShell().getDisplay(), "folder.png" );
		item.setImage( image );
		}
	else
		{
		Image image = MUtil.getImage( parent.getShell().getDisplay(), "cog.png" );
		item.setImage( image );		
		}
	
	if( data.containsKey( "name" ) )
		{
		item.setText( data.get( "name" ) + "" );
		}
	}

}
//--------------------------------------------------------------------------------
private boolean isRootItem( TreeItem item )
{
return item == tree.getItem( 0 );
}
//--------------------------------------------------------------------------------
private boolean isFolderItem( TreeItem item )
{
Map data = ( Map )item.getData();
if( data == null )
	{
	return false;
	}
else
	{
	if( data.containsKey( "type" ) && data.get( "type" ).equals( "folder" ) )
		{
		return true;
		}
	}
return false;
}
//--------------------------------------------------------------------------------
private void onNewFolder()
{
Set dataSet = new HashSet();
MPromptDialog dialog = new MPromptDialog( shell, dataSet, "New Folder", "Name :" );
dialog.open();

if( dataSet.size() > 0 )
	{
	TreeItem newItem = addTreeItem();
	Map folderData = new HashMap();
	folderData.put( "name", dataSet.iterator().next() );
	folderData.put( "type", "folder" );
	newItem.setData( folderData );
	drawItem( newItem );
	newItem.getParentItem().setExpanded( true );
	}
}
//--------------------------------------------------------------------------------
private TreeItem addTreeItem()
{
TreeItem newItem = null;
TreeItem[] selected = tree.getSelection();

if( selected == null )
	{
	newItem = new TreeItem( tree, SWT.NONE );
	}
else
	{
	int count = selected.length;
	
	if( count == 0 )
		{
		newItem = new TreeItem( tree, SWT.NONE );
		}
	else
		{
		TreeItem target = null;
		if( isFolderItem( selected[ 0 ] ) )
			{
			target = selected[ 0 ];
			}
		else
			{
			target = selected[ 0 ].getParentItem();	
			}
		
		if( target == null )
			{
			newItem = new TreeItem( tree, SWT.NONE );	//?	
			}
		else
			{
			newItem = new TreeItem( target , SWT.NONE );		
			}
		}
	}
return newItem;
}
//--------------------------------------------------------------------------------
private boolean dropOnChildOrSelf( TreeItem src, TreeItem dest )
{
if( src == dest )
	{
	return true;
	}
TreeItem _item = dest;
while( _item.getParentItem() != null )
	{
	TreeItem parent = _item.getParentItem();
	if( parent == src )
		{
		return true;
		}
	else
		{
		_item = parent;
		}
	}
return false;
}
//--------------------------------------------------------------------------------
private void copyTreeItem( TreeItem src, TreeItem dest )
{
Map data = ( Map )src.getData();
dest.setData( data );
drawItem( dest );

if( isFolderItem( src ) )
	{
	TreeItem[] items = src.getItems();
	if( items != null )
		{
		for( int i = 0; i < items.length; ++i )
			{
			TreeItem newItem = new TreeItem( dest, SWT.NONE );
			copyTreeItem( items[ i ], newItem );
			}
		}
	}
}
//--------------------------------------------------------------------------------
private void onNewSavedAction()
{
TreeItem newItem = addTreeItem();

Map newSavedAction = new HashMap();
newSavedAction.put( "type", "item" );
newItem.setData( newSavedAction );
MActionDialog dialog = new MActionDialog( shell, newSavedAction );
int result = dialog.open();

if( result == 0 )
	{
	drawItem( newItem );
	TreeItem parentItem = newItem.getParentItem();
	if( parentItem != null )
		{
		parentItem.setExpanded( true );
		}
	tree.setSelection( newItem );
	tree.setFocus();
	}
else
	{
	newItem.dispose();
	}
}
//--------------------------------------------------------------------------------
public void setFocus()
{
}
//--------------------------------------------------------------------------------
private void loadSavedActions()
{
String savedStr = prop.getProperty( SAVED_ACTION, null );
TreeItem root = new TreeItem( tree, SWT.NONE );
if( savedStr == null )
	{
	Map rootData = new HashMap();
	rootData.put( "type", "folder" );
	rootData.put( "name", "Saved Actions" );
	rootData.put( "root", "1" );
	root.setData( rootData );
	drawItem( root );
	}
else
	{
	java.util.List list = ( java.util.List )JSON.parse( savedStr );
	loadImpl( ( Map )list.get( 0 ), root );
	}

root.setExpanded( true );
}
//--------------------------------------------------------------------------------
private void loadImpl( Map map, TreeItem treeItem )
{
Map data = ( Map )map.get( "data" );
treeItem.setData( data );
drawItem( treeItem );

if( isFolderItem( treeItem ) )
	{
	java.util.List list = ( java.util.List )map.get( "items" );
	for( int i = 0; i < list.size(); ++i )
		{
		Map _map = ( Map )list.get( i );
		TreeItem newItem = new TreeItem( treeItem, SWT.NONE );
		loadImpl( _map, newItem );
		}
	}
}
//--------------------------------------------------------------------------------
private void saveImpl( java.util.List list, TreeItem treeItem )
{
/*
 * {
 * "data": data,
 * "items": [ ... ]
 * }
 */
Map savedData = new HashMap();
savedData.put( "data", treeItem.getData() );
list.add( savedData );

if( isFolderItem( treeItem ) )
	{
	java.util.List itemsList = new ArrayList();
	savedData.put( "items", itemsList );
	
	TreeItem[] items = treeItem.getItems();
	if( items != null )
		{
		for( int i = 0; i < items.length; ++i )
			{
			saveImpl( itemsList, items[ i ] );
			}
		}
	}
}
//--------------------------------------------------------------------------------
private void saveSavedActions()
{
java.util.List rootList = new ArrayList( 1 ); 
saveImpl( rootList, tree.getItem( 0 ) );
prop.setProperty( SAVED_ACTION, JSON.serialize( rootList ) );
}
//--------------------------------------------------------------------------------
public void dispose()
{
//saveSavedActions();

MEventManager.getInstance().removeObserver2( this );

super.dispose();
}
//--------------------------------------------------------------------------------
public void update( final Object e, final Object source )
{
//threadPool.addCommand( new MCommand() {	public void execute(){ //-----------------

final MEvent event = ( MEvent )e;
final String eventName = event.getEventName();

if( eventName.equals( event_save_actions ) )
	{
	Map data = event.getData();
	data.put( "type", "item" );
	TreeItem item = new TreeItem( tree.getItem( 0 ), SWT.NONE );
	item.setData( data );
	editSavedAction( item );
	}

//	} public void breakCommand(){}	} ); //------------
}
//--------------------------------------------------------------------------------
}
