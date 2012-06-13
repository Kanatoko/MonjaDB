package net.jumperz.app.MMonjaDBCore;

public interface MConstants
{

public static final String event_error			= "event_error";
//public static final String event_error_parse		= "event_error_parse";
public static final String event_connect		= "event_connect";
public static final String event_disconnect		= "event_disconnect";
public static final String event_showcollections	= "event_showcollections";
public static final String event_showdbs		= "event_showdbs";
public static final String event_use			= "event_use";
public static final String event_find			= "event_find";
public static final String event_update			= "event_update";
public static final String event_save			= "event_save";
public static final String event_insert			= "event_insert";
public static final String event_remove			= "event_remove";

public static final String event_mj_all_db_stats		= "event_mj_all_db_stats";
public static final String event_mj_all_collection_stats	= "event_mj_all_collection_stats";
public static final String event_mj_sort			= "event_mj_sort";
public static final String event_mj_edit			= "event_mj_edit";
public static final String event_mj_edit_field			= "event_mj_edit_field";
public static final String event_mj_update_int			= "event_mj_update_int";
public static final String event_mj_prev_items			= "event_mj_prev_items";
public static final String event_mj_next_items			= "event_mj_next_items";
public static final String event_mj_copy			= "event_mj_copy";
public static final String event_mj_paste			= "event_mj_paste";
public static final String event_mj_remove			= "event_mj_remove";
public static final String event_mj_ssh_connect			= "event_mj_ssh_connect";

	//internal
public static final String event_save_actions		= "event_save_actions";

public static final String data_type			= "dataType";
public static final String data_type_mongo		= "mongo";
public static final String data_type_db			= "db";
public static final String data_type_collection		= "collection";
public static final String data_type_document		= "document";

public static final int action_cond_none						= -2;
public static final int action_cond_not_connected_or_connected_to_different_host	= -1;
public static final int action_cond_connected						= 0;
public static final int action_cond_db							= 1;
public static final int action_cond_collection						= 2;

public static final int sort_order_default		= 0;
public static final int sort_order_asc			= 1;
public static final int sort_order_desc			= -1;

//public static final int default_batch_size		= 200;
public static final int default_max_results		= 500;
public static final int default_ssh_port		= 22;
public static final int default_mongo_port		= 27017;

//eclipse
public static final int BUTTON_HEIGHT	= 25;
public static final int BUTTON_WIDTH	= 65;
public static final int BUTTON1_RIGHT	= -80;
public static final int BUTTON1_BOTTOM	= -10;
public static final int BUTTON2_RIGHT	= -10;
public static final int BUTTON2_BOTTOM	= -10;
public static final int BUTTON3_RIGHT	= -150;
public static final int BUTTON3_BOTTOM	= -10;

public static final String CONNECT_DIALOG_HOST = "connect_dialog_host";
public static final String CONNECT_DIALOG_DB = "connect_dialog_db";
public static final String CONNECT_DIALOG_PORT = "connect_dialog_port";
public static final String CONNECT_DIALOG_SSH = "connect_dialog_ssh";
public static final String CONNECT_DIALOG_SSH_KEY = "connect_dialog_ssh_key";
public static final String CONNECT_DIALOG_NORMAL_CONNECTION = "connect_dialog_normal_connection";

public static final String DEFAULT_CONFIG_FILE_NAME = "monjadb.conf";
public static final String DBLIST_TABLE	= "dblist_table";
public static final String COLLLIST_TABLE = "colllist_table";
public static final String DOCUMENTLIST_TABLE	= "documentlist_table";
public static final String DOCUMENT_COMPOSITE_WEIGHT = "documentEditor.editorComposite.weight";
public static final String JAVASCRIPT_COMPOSITE_WEIGHT = "javascript.editorComposite.weight";
public static final String ACTIONLOG_COMPOSITE_WEIGHT = "actionLog.sash.weight";
public static final int MAX_SAVED_ACTION_LOG = 1000;
public static final String ACTION_LOG_LIST = "actionLogList";
public static final String BATCH_SIZE = "Batch Size ( 'limit' value of the find queries )";
public static final String SAVED_ACTION = "savedAction";
public static final String CONSOLE_NAME = "MonjaDB Log";

	//eclipse pref
public static final String PREF_MAX_FIND_RESULTS = "maxFindResults";
public static final String PREF_REMEMBER_LAST_LOCATION	= "rememberLastLocation";
}