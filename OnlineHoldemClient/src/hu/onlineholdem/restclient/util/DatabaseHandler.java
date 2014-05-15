package hu.onlineholdem.restclient.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.onlineholdem.restclient.entity.Player;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_NAME = "online_holdem";
    private static final String TABLE_PLAYER = "players";

    private static final String KEY_ID = "_id";
    private static final String KEY_PLAYER_ID = "player_id";
    private static final String KEY_STACK_SIZE = "player_stack_size";
    private static final String KEY_ORDER = "player_order";
    private static final String KEY_POSITION = "player_position";
    private static final String KEY_STYLE = "player_style";
    private static final String KEY_IS_USER = "player_is_user";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PLAYER_TABLE = "CREATE TABLE " + TABLE_PLAYER + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_PLAYER_ID + " INTEGER, "
                + KEY_STACK_SIZE + " INTEGER, "
                + KEY_STYLE + " TEXT, "
                + KEY_IS_USER + " INTEGER, "
                + KEY_POSITION + " INTEGER, "
                + KEY_ORDER + " INTEGER" + ")";
        db.execSQL(CREATE_PLAYER_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYER);
        onCreate(db);
    }

    public void addPlayer(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PLAYER_ID, player.getPlayerId());
        int betAmount = null == player.getBetAmount() ? 0 : player.getBetAmount();
        values.put(KEY_STACK_SIZE, player.getStackSize() + betAmount);
        values.put(KEY_STYLE, player.getPlayStyle().name());
        values.put(KEY_IS_USER, player.isUser() ? 1 : 0);
        values.put(KEY_POSITION, player.getPosition());
        values.put(KEY_ORDER, player.getOrder());


        db.insert(TABLE_PLAYER, null, values);
        db.close();
    }

    public List<Map<String, String>> getPlayerDetails(){
        List<Map<String,String>> players = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_PLAYER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Map<String,String> player = new HashMap<>();
            player.put("player_id", cursor.getString(1));
            player.put("player_stack_size", cursor.getString(2));
            player.put("player_style", cursor.getString(3));
            player.put("player_is_user", cursor.getString(4).equals("1") ? "true" : "false");
            player.put("player_position", cursor.getString(5));
            player.put("player_order", cursor.getString(6));

            players.add(player);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return players;
    }


    public void resetTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYER, null, null);
        db.close();
    }
}
