package net.jumperz.app.MMonjaDB.eclipse.view;

import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.bson.types.ObjectId;
import org.bson.types.Symbol;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Composite;

import com.mongodb.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import net.jumperz.util.*;
import java.io.*;
import java.math.BigDecimal;

import net.jumperz.app.MMonjaDB.eclipse.MUtil;
import net.jumperz.app.MMonjaDB.eclipse.dialog.*;
import net.jumperz.app.MMonjaDBCore.MDataManager;
import net.jumperz.app.MMonjaDBCore.MOutputView;
import net.jumperz.app.MMonjaDBCore.action.*;
import net.jumperz.app.MMonjaDBCore.action.mj.*;
import net.jumperz.app.MMonjaDBCore.event.*;
import net.jumperz.gui.MSwtUtil;
import net.jumperz.mongo.MMongoUtil;

import java.util.*;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class MDocumentEditor
extends MAbstractView
implements MOutputView
{
private Tree tree;
private Image documentImage;
private Image oidImage;
private Image intImage;
private Image doubleImage;
private Image stringImage;
private Image dateImage;
private Image longImage;
private Image boolImage;
private Image listImage;
private Image mapImage;
private Image nullImage;
private Image jsImage;

private Text valueText;
private Text nameText;
private Combo typeCombo;
private Composite editorComposite;
private Button updateButton;
private Object _id;

private Map fieldNameTreeItemMap;
private SashForm sashForm;
private Map typeComboIndexMap = new HashMap();
private Object editingData;
private String editingFieldName;
private long initializedTime;

//--------------------------------------------------------------------------------
public MDocumentEditor()
{
eventManager.register2( this );
}
//--------------------------------------------------------------------------------
public void activate()
{
final IViewPart _view = this;
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//----
if( tree.getItemCount() > 0 )
	{
	_view.setFocus();
	tree.setSelection( tree.getItem( 0 ) );
	tree.setFocus();
	}
}});//-------
}
//--------------------------------------------------------------------------------
public void dispose()
{
eventManager.removeObserver2( this );
super.dispose();
}
// --------------------------------------------------------------------------------
protected void handleEvent2( Event event )
{
if( event.widget == tree )
	{
	switch( event.type )
		{
		case SWT.KeyDown:
			break;
		case SWT.Selection:
			onTreeItemSelect();
			break;
		}
	}
else if( event.widget == typeCombo )
	{
	verifyData();
	}
else if( MSwtUtil.getTreeColumns( tree ).contains( event.widget ) )
	{
	switch( event.type )
		{
		case SWT.Selection:
			onTreeColumnSelect( ( TreeColumn )event.widget );
			break;
		case SWT.Resize:
			onTreeColumnResize();
			break;
		}
	}
}
//--------------------------------------------------------------------------------
private void onTreeColumnResize()
{
MSwtUtil.setTreeColumnWidthToProperties( "documentTree", tree, prop );
}
//--------------------------------------------------------------------------------
private void onTreeColumnSelect( TreeColumn column )
{

}
//--------------------------------------------------------------------------------
private void onTreeItemSelect()
{
TreeItem[] selectedItems = tree.getSelection();
if( selectedItems.length != 1 )
	{
	return;
	}
TreeItem selectedItem = selectedItems[ 0 ];

if( selectedItem == tree.getItem( 0 ) ) //root
	{
	shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****
	debug( Thread.currentThread() );
	nameText.setText( "" );
	valueText.setText( "" );
	typeCombo.select( 0 );
	updateButton.setEnabled( false );
	}});//*******
	return;
	}

String fieldName = ( String )selectedItem.getData( "fieldName" );

executeAction( "mj edit field " +  fieldName );
}
//--------------------------------------------------------------------------------
private void setItemInfo( TreeItem treeItem, String key, Object value )
{
if( key.equals( "_id" ) && value.getClass() == Double.class )
	{
	BigDecimal bd = new BigDecimal( ( ( Double )value ).doubleValue() );
	value = bd.toString();
	}
treeItem.setText( key + " : " + value ); 
if( value instanceof Integer )
	{
	treeItem.setImage( intImage );
	}
else if( value instanceof Double )
	{
	treeItem.setImage( doubleImage );	
	}
else if( value instanceof Long )
	{
	treeItem.setImage( longImage );		
	}
else if( value instanceof Date )
	{
	treeItem.setImage( dateImage );
	}
else if( value instanceof String )
	{
	treeItem.setImage( stringImage );	
	}
else if( value instanceof ObjectId )
	{
	treeItem.setImage( oidImage );		
	}
else if( value instanceof Boolean )
	{
	treeItem.setImage( boolImage );
	}
else if( value instanceof Code )
	{
	treeItem.setImage( jsImage );
	}
}
//--------------------------------------------------------------------------------
private void drawItem( String parentFieldName, TreeItem parentItem, List data, boolean expand )
{
for( int i = 0; i < data.size(); ++i )
	{
	TreeItem newItem = new TreeItem( parentItem, SWT.NONE );
	Object value = data.get( i );
	newItem.setText( 1, getClassName( value ) );
	
	String fieldName = parentFieldName + "." + i;
	if( fieldName.startsWith( "." ) )
		{
		fieldName = fieldName.substring( 1 );
		}
	newItem.setData( "fieldName", fieldName );
	newItem.setData( "value", value );
	fieldNameTreeItemMap.put( fieldName, newItem );

	if( value instanceof Map )
		{
		newItem.setText( "["+ i + "]" );
		newItem.setImage( mapImage );
		drawItem( fieldName, newItem, ( Map )value, expand );
		}
	else if( value instanceof List )
		{
		newItem.setText( "["+ i + "]" );
		newItem.setImage( listImage );
		drawItem( fieldName, newItem, ( List )value, expand );
		}
	else
		{
		setItemInfo( newItem, "["+ i + "]", value );
		}

	if( expand )
		{
		parentItem.setExpanded( expand );
		newItem.setExpanded( expand );
		}
	}
}
//--------------------------------------------------------------------------------
private String getClassName( Object o )
{
if( o == null )
	{
	return "Null";
	}
else
	{
	return o.getClass().getName();
	}
}
//--------------------------------------------------------------------------------
private void drawItem( String parentFieldName, TreeItem parentItem, Map data, boolean expand )
{
Iterator p = data.keySet().iterator();
while( p.hasNext() )
	{
	String key = ( String )p.next();
	if( !data.containsKey( key ) )
		{
		continue;
		}
	Object value = data.get( key );
	
	TreeItem newItem = new TreeItem( parentItem, SWT.NONE );
	newItem.setText( 1, getClassName( value ) );
	
	String fieldName = parentFieldName + "." + key;
	if( fieldName.startsWith( "." ) )
		{
		fieldName = fieldName.substring( 1 );
		}
	newItem.setData( "fieldName", fieldName );
	newItem.setData( "value", value );
	fieldNameTreeItemMap.put( fieldName, newItem );
	
	if( value == null )
		{
		newItem.setText( key + " : null" );
		newItem.setImage( nullImage );
		}
	else if( value instanceof Map )
		{
		newItem.setText( key );
		newItem.setImage( mapImage );
		drawItem( fieldName, newItem, ( Map )value, expand );
		}
	else if( value instanceof List )
		{
		newItem.setText( key );
		newItem.setImage( listImage );
		drawItem( fieldName, newItem, ( List )value, expand );
		}
	else
		{
		setItemInfo( newItem, key, value );
		}
	
	if( expand )
		{
		parentItem.setExpanded( expand );
		newItem.setExpanded( expand );
		}
	}
}
//--------------------------------------------------------------------------------
private void drawTreeRoot( Tree _tree, DBObject data )
{
	//reset tree
_tree.removeAll();

if( data != null )
	{
	TreeItem root = new TreeItem( _tree, SWT.NONE );
	
	{
	String _idStr = data.get( "_id" ).toString();
	Object _idObj = data.get( "_id" );
	if( _idObj.getClass() == Double.class )
		{
		_idStr = ( new BigDecimal( ( ( Double )_idObj ).doubleValue() ) ).toString();
		}
	root.setText( _idStr );
	}
	
	root.setImage( documentImage );
	root.setData( "fieldName", "" );
	root.setData( "value", data.get( "_id" ) );
	fieldNameTreeItemMap.put( "", root );

	boolean expand = ( data.keySet().size() < 35 );
	drawItem( "", root, data.toMap(), expand );
	}

nameText.setText( "" );
typeCombo.select( 0 );
typeCombo.setEnabled( false );
valueText.setText( "" );
updateButton.setEnabled( false );
}
//--------------------------------------------------------------------------------
private void onEdit( MEditAction action )
{
_id = action.getIdAsObject();
final DBObject data = dataManager.getDocumentDataByAction( action );

fieldNameTreeItemMap = new HashMap();

shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

drawTreeRoot( tree, data );

}});//********
}
//--------------------------------------------------------------------------------
private void onFind()
{
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

tree.removeAll();

}});//********

}
//--------------------------------------------------------------------------------
private void onUse()
{
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

tree.removeAll();

}});//********

}
//--------------------------------------------------------------------------------
private void onDisconnect()
{
onUse();
}
//--------------------------------------------------------------------------------
private void onEditField( final MEditFieldAction action )
{
MSystemUtil.sleep( 100 );

final String fieldName = action.getFieldName();
editingFieldName = fieldName;
final TreeItem item = ( TreeItem )fieldNameTreeItemMap.get( fieldName );
if( item == null )
	{
	debug( "item not found" );
	//debug( fieldNameTreeItemMap );
	return;
	}
//Object document = dataManager.getLastEditedDocument();

final MDocumentEditor view = this;

shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

if( action.getOriginView() != view )
	{
	tree.select( item );
	}

if( fieldName.equals( "_id" ) )
	{
	valueText.setEditable( false );
	valueText.setEnabled( true );
	typeCombo.setEnabled( false );
	nameText.setText( fieldName );
	editingData = item.getData( "value" );
	
	{
	if( editingData.getClass() == Double.class )
		{
		valueText.setText( ( new BigDecimal( ( ( Double )editingData ).doubleValue() ) ).toString() );
		}
	else
		{
		valueText.setText( editingData.toString() );
		}
	}

	typeCombo.select(
		( ( Integer )typeComboIndexMap.get( editingData.getClass() ) ).intValue()
		);
	updateButton.setEnabled( false );
	}
else
	{
	valueText.setEditable( true );
	valueText.setEnabled( true );
	typeCombo.setEnabled( true );
	nameText.setText( fieldName );
	editingData = item.getData( "value" );
	valueText.setText( editingData + "" );
	if( editingData == null )
		{
		typeCombo.select( 11 );
		verifyData();
		}
	else
		{
		typeCombo.select(
			( ( Integer )typeComboIndexMap.get( editingData.getClass() ) ).intValue()
			);
		verifyData();		
		}
	}

}});//********

//valueText.setText( docum )
}
//--------------------------------------------------------------------------------
public void update( final Object e, final Object source )
{
//threadPool.addCommand( new MCommand() {	public void execute(){ //-----------------

final MEvent event = ( MEvent )e;
final String eventName = event.getEventName();
if( event.getEventName().indexOf( event_mj_edit + "_end" ) == 0 )
	{
	MEditAction action = ( MEditAction )source;
	onEdit( action );
	}
else if( event.getEventName().indexOf( event_find + "_end" ) == 0 )
	{
	onFind();
	}
else if( event.getEventName().indexOf( event_use + "_end" ) == 0 )
	{
	onUse();
	}
else if( event.getEventName().indexOf( event_disconnect + "_end" ) == 0 )
	{
	onDisconnect();
	}
else if( event.getEventName().indexOf( event_mj_edit_field + "_end" ) == 0 )
	{
	onEditField( ( MEditFieldAction )source );
	}

//	} public void breakCommand(){}	} ); //------------
}
/*
//--------------------------------------------------------------------------------
public void createPartControl( Composite parent )
{
this.parent = parent;
shell = parent.getShell();

init2();

initialized = true;
}
*/
//--------------------------------------------------------------------------------
private void verifyData()
{
int typeIndex = typeCombo.getSelectionIndex();
if( typeIndex == -1 )
	{
	return;
	}
String value = valueText.getText();
boolean verified = false;
switch( typeIndex )
	{
	case 0://Double
		try
			{
			Double.parseDouble( value );
			verified = true;
			}
		catch( Exception e )
			{
			}
		break;
	case 1://Integer
		try
			{
			Integer.parseInt( value );
			verified = true;
			}
		catch( Exception e )
			{
			}
		break;
	case 2://Long
		try
			{
			Long.parseLong( value );
			verified = true;
			}
		catch( Exception e )
			{
			}
		break;
	case 3://String
		verified = true;
		break;
	case 4://List
		try
			{
			Object o = dataManager.getDB().eval( value, null );
			if( o instanceof BasicDBList )
				{
				verified = true;
				}
			}
		catch( Exception e )
			{
			}
		break;
	case 5://Map
		try
			{
			Object o = dataManager.getDB().eval( value, null );
			if( o instanceof BasicDBObject )
				{
				verified = true;
				}
			}
		catch( Exception e )
			{
			}
		break;
	case 6://Date
		try
			{
			DateFormat df = new SimpleDateFormat( "EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH );
			df.parse( value );
			verified = true;
			}
		catch( Exception e )
			{
			//e.printStackTrace();
			}
		break;
	case 7://ObjectId
		try
			{
			new ObjectId( value );
			verified = true;
			}
		catch( Exception e )
			{
			}
		break;
	case 8://Code
		verified = true;
		break;
	case 9://Binary always false
		break;
	case 10://Boolean
		if( value.equalsIgnoreCase( "true" )
		 || value.equalsIgnoreCase( "false" )
		  )
			{
			verified = true;
			}
		break;
	case 11://null
		if( value.equalsIgnoreCase( "null" ) )
			{
			verified = true;
			}
		break;
	case 12://Regex
		try
			{
			java.util.regex.Pattern.compile( value );
			verified = true;
			}
		catch( Exception e )
			{
			}
		break;
	case 13://Symbol not supported yet
		break;
	case 14://Code with scope not supported yet
		break;
	case 15://Timestamp not supported yet
		break;
	case 16://Minkey not supported yet
		break;
	case 17://Maxkey not supported yet
		break;
	}

updateButton.setEnabled( verified );
}
//--------------------------------------------------------------------------------
private void onUpdateButtonSelect()
{
updateDocument();
}
//--------------------------------------------------------------------------------
private void updateDocument()
{
int typeIndex = typeCombo.getSelectionIndex();
if( typeIndex == -1 )
	{
	return;
	}
String value = valueText.getText();
Object newValue = null;
String updateStr = null;
switch( typeIndex )
	{
	case 0://Double
		newValue = new Double( value );
		break;
	case 1://Integer
		newValue = new Integer( value );
		break;
	case 2://Long
		newValue = new Long( value );	
		break;
	case 3://String
		newValue = value;
		break;
	case 4://List
		newValue = dataManager.getDB().eval( value, null );
		break;
	case 5://Map
		newValue = dataManager.getDB().eval( value, null );
		break;
	case 6://Date
		try
			{
			SimpleDateFormat df = new SimpleDateFormat( "EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH );
			//df.set
			newValue = df.parse( value );	
			}
		catch( Exception e )
			{
			//e.printStackTrace();
			return;
			}
		break;
	case 7://ObjectId
		newValue = new ObjectId( value );
		break;
	case 8://Code
		newValue = new Code( value );
		break;
	case 9://Binary not implemented
		break;
	case 10://Boolean
		newValue = new Boolean( value );
		break;
	case 11://null
		newValue = null;
		break;
	case 12://Regex
		newValue = java.util.regex.Pattern.compile( value + "" );
		break;
	case 13://Symbol
		//newValue = new Symbol( value );
		break;
	case 14://Code with scope
		//newValue = new CodeWScope( value, new BasicDBObject() );
		break;
	case 15://Timestamp
		break;
	case 16://Minkey not supported yet
		break;
	case 17://Maxkey not supported yet
		break;
	}

dataManager.updateDocument( _id, editingFieldName, newValue );
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

tree = new Tree( sashForm,  SWT.BORDER | SWT.FULL_SELECTION );
tree.setHeaderVisible( true );

FormData d1 = new FormData();
d1.top = new FormAttachment( 0, 1 );
d1.left = new FormAttachment( 0, 1 );
d1.right = new FormAttachment( 100, -1 );
d1.bottom = new FormAttachment( 100, -1 );
tree.setLayoutData( d1 );

TreeColumn column1 = new TreeColumn( tree, SWT.LEFT );
TreeColumn column2 = new TreeColumn( tree, SWT.LEFT );
column2.setText( "Data Type" );

editorComposite = new Composite( sashForm, SWT.BORDER);
editorComposite.addControlListener(new ControlAdapter() {
	public void controlResized(ControlEvent e) {
	onSashResize();
	}
});

FormData fd_composite1 = new FormData();
fd_composite1.top = new FormAttachment( 0, 1);
fd_composite1.bottom = new FormAttachment( 0, 35 );
fd_composite1.right = new FormAttachment( 100, -1 );
fd_composite1.left = new FormAttachment( 0, 1);
editorComposite.setLayoutData(fd_composite1);
editorComposite.setLayout(new FormLayout());
Label nameLabel = new Label(editorComposite, SWT.NONE);
FormData fd_nameLabel = new FormData();
fd_nameLabel.right = new FormAttachment(0, 66);
fd_nameLabel.bottom = new FormAttachment(0, 32);
fd_nameLabel.top = new FormAttachment(0, 12);
fd_nameLabel.left = new FormAttachment(0, 10);
nameLabel.setLayoutData(fd_nameLabel);
nameLabel.setText("Name :");
Label valueLabel = new Label(editorComposite, SWT.NONE );
FormData fd_valueLabel = new FormData();
fd_valueLabel.top = new FormAttachment(nameLabel, 15);
fd_valueLabel.left = new FormAttachment( 0, 10 );
fd_valueLabel.bottom = new FormAttachment( nameLabel, 34, SWT.BOTTOM) ;
fd_valueLabel.right = new FormAttachment( nameLabel, 0, SWT.RIGHT );
valueLabel.setLayoutData( fd_valueLabel );
valueLabel.setText("Value :");

valueText = new Text(editorComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
valueText.addModifyListener(new ModifyListener() {
	public void modifyText(ModifyEvent e) {
	verifyData();
	}
});
valueText.setEnabled(false);
valueText.setEditable(false);
FormData fd_valueText = new FormData();
fd_valueText.top = new FormAttachment(nameLabel, 5);
fd_valueText.bottom = new FormAttachment(100, -80);
fd_valueText.right = new FormAttachment(100, -20);
fd_valueText.left = new FormAttachment( valueLabel, 0, SWT.RIGHT );
valueText.setLayoutData(fd_valueText);

updateButton = new Button(editorComposite, SWT.NONE);
updateButton.addSelectionListener(new SelectionAdapter() {
	public void widgetSelected(SelectionEvent e) {
	onUpdateButtonSelect();
	}
});
updateButton.setEnabled(false);
FormData fd_updateButton = new FormData();
fd_updateButton.left = new FormAttachment(100, -120);
fd_updateButton.right = new FormAttachment( valueText, 0, SWT.RIGHT );
updateButton.setLayoutData(fd_updateButton);
updateButton.setText("Update");

typeCombo = new Combo(editorComposite, SWT.READ_ONLY );
fd_updateButton.top = new FormAttachment(typeCombo, 10);
typeCombo.setEnabled(false);
FormData fd_typeList = new FormData();
fd_typeList.left = new FormAttachment(valueText, 0, SWT.LEFT);
fd_typeList.top = new FormAttachment(valueText, 5, SWT.BOTTOM);
//fd_typeList.bottom = new FormAttachment(valueText, 30, SWT.BOTTOM);
fd_typeList.right = new FormAttachment(valueText, 170, SWT.LEFT);
typeCombo.setLayoutData(fd_typeList);

typeCombo.add( "Double" );
typeCombo.add( "Integer" );
typeCombo.add( "Long" );
typeCombo.add( "String" );
typeCombo.add( "List (BasicDBList)" );
typeCombo.add( "Map (BasicDBObject)" );
typeCombo.add( "Date" );
typeCombo.add( "ObjectId" );
typeCombo.add( "JavaScript code" );
typeCombo.add( "Binary data" );
typeCombo.add( "Boolean" );
typeCombo.add( "Null" );
typeCombo.add( "Regular expression" );
typeCombo.add( "Symbol" );
typeCombo.add( "JavaScript code with scope" );
typeCombo.add( "Timestamp" );
typeCombo.add( "Min key" );
typeCombo.add( "Max key" );

typeCombo.addListener( SWT.Selection, this );

typeComboIndexMap.put( Double.class, new Integer( 0 ) );
typeComboIndexMap.put( Integer.class, new Integer( 1 ) );
typeComboIndexMap.put( Long.class, new Integer( 2 ) );
typeComboIndexMap.put( String.class, new Integer( 3 ) );
typeComboIndexMap.put( com.mongodb.BasicDBList.class, new Integer( 4 ) );
typeComboIndexMap.put( com.mongodb.BasicDBObject.class, new Integer( 5 ) );
typeComboIndexMap.put( java.util.Date.class, new Integer( 6 ) );
typeComboIndexMap.put( org.bson.types.ObjectId.class, new Integer( 7 ) );
typeComboIndexMap.put( org.bson.types.Code.class, new Integer( 8 ) );
typeComboIndexMap.put( byte[].class, new Integer( 9 ) );
typeComboIndexMap.put( Boolean.class, new Integer( 10 ) );
typeComboIndexMap.put( java.util.regex.Pattern.class, new Integer( 12 ) );
typeComboIndexMap.put( org.bson.types.Symbol.class, new Integer( 13 ) );
typeComboIndexMap.put( org.bson.types.CodeWScope.class, new Integer( 14 ) );
typeComboIndexMap.put( org.bson.types.BSONTimestamp.class, new Integer( 15 ) );
typeComboIndexMap.put( org.bson.types.MinKey.class, new Integer( 16 ) );
typeComboIndexMap.put( org.bson.types.MaxKey.class, new Integer( 17 ) );

Label typeLabel = new Label(editorComposite, SWT.NONE);
FormData fd_typeLabel = new FormData();
fd_typeLabel.top = new FormAttachment( typeCombo, 3, SWT.TOP );
fd_typeLabel.left = new FormAttachment(nameLabel, 0, SWT.LEFT);
typeLabel.setLayoutData(fd_typeLabel);
typeLabel.setText("Type :");

nameText = new Text(editorComposite, SWT.READ_ONLY);
nameText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
FormData fd_nameText = new FormData();
fd_nameText.top = new FormAttachment(nameLabel, -2, SWT.TOP);
fd_nameText.left = new FormAttachment( valueText, 0 ,SWT.LEFT );
fd_nameText.right = new FormAttachment( valueText, 0 ,SWT.RIGHT );
nameText.setLayoutData(fd_nameText);

MSwtUtil.getTreeColumnWidthFromProperties( "documentTree", tree, prop, new int[]{ 150, 150 } );

	//listeners
tree.addListener( SWT.MouseDoubleClick, this );
tree.addListener( SWT.Selection, this );
tree.addListener( SWT.KeyDown, this );
MSwtUtil.addListenerToTreeColumns2( tree, this );

documentImage	= MUtil.getImage( parent.getShell().getDisplay(), "table.png" );
oidImage	= MUtil.getImage( parent.getShell().getDisplay(), "bullet_star.png" );
intImage	= MUtil.getImage( parent.getShell().getDisplay(), "bullet_blue.png" );
longImage	= MUtil.getImage( parent.getShell().getDisplay(), "bullet_red.png" );
doubleImage	= MUtil.getImage( parent.getShell().getDisplay(), "bullet_orange.png" );
stringImage	= MUtil.getImage( parent.getShell().getDisplay(), "bullet_green.png" );
dateImage	= MUtil.getImage( parent.getShell().getDisplay(), "bullet_white.png" );
boolImage	= MUtil.getImage( parent.getShell().getDisplay(), "bullet_yellow.png" );
listImage	= MUtil.getImage( parent.getShell().getDisplay(), "stop_blue.png" );
mapImage	= MUtil.getImage( parent.getShell().getDisplay(), "stop_green.png" );
nullImage	= MUtil.getImage( parent.getShell().getDisplay(), "bullet_black.png" );
jsImage		= MUtil.getImage( parent.getShell().getDisplay(), "bullet_right.png" );

if( prop.containsKey( DOCUMENT_COMPOSITE_WEIGHT ) )
	{
	
	( new Thread(){ public void run() {
		//System.out.println( "e" );
		MSystemUtil.sleep( 0 );
		//System.out.println( "a" );
		shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//----
		//debug( "--" + prop.getIntArrayProperty( DOCUMENT_COMPOSITE_WEIGHT )[ 0 ] );
		sashForm.setWeights( prop.getIntArrayProperty( DOCUMENT_COMPOSITE_WEIGHT ) );
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
private void onSashResize()
{
if( System.currentTimeMillis() >= initializedTime + 3000 )
	{
	prop.setProperty( DOCUMENT_COMPOSITE_WEIGHT, sashForm.getWeights() );
	}
}
//--------------------------------------------------------------------------------
public void setFocus()
{

}
//--------------------------------------------------------------------------------
}
