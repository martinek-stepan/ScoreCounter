package martinek.stepan.scorecounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;


public class SQLiteHelper extends SQLiteOpenHelper
{

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "ScoreCounterDB";
    private static final String KEY_INDEX = "`index`";
    private static final String KEY_ID = "`id`";
    private static final String KEY_PLAYER_ID = "`player_id`";
    private static final String KEY_NAME = "`name`";
    private static final String KEY_TEXT_COLOR = "`textColor`";
    private static final String KEY_BACK_COLOR = "`backColor`";
    private static final String KEY_SCROLLED = "`scrolled`";
    private static final String KEY_VALUE = "`value`";
    private static final String TABLE_PLAYERS = "`players`";
    private static final String TABLE_SCORE = "`score`";
    private static final String TABLE_GAMEMODES = "`game_modes`";
    private static final String KEY_ONLY_POSITIVE = "`only_positive_score`";
    private static final String KEY_STRICT_ROUNDS = "`strict_rounds`";
    private static final String KEY_FINNISH_ROUND = "`finnish_round`";
    private static final String KEY_LOWEST_SCORE_WIN = "`lowest_score_win`";
    private static final String KEY_WINNING_SCORE = "`winning_score`";
    private static final String KEY_ROUNDS_COUNT = "`rounds_count`";
    private static final String KEY_CONSTANT_SCORE = "`constant_score`";

    public SQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String CREATE_PLAYERS_TABLE = "CREATE TABLE " + TABLE_PLAYERS + " ( " +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_INDEX + " INTEGER, " +
                KEY_NAME + " TEXT, " +
                KEY_SCROLLED + " INTEGER, " +
                KEY_TEXT_COLOR + " INTEGER, " +
                KEY_BACK_COLOR + " INTEGER )";


        String CREATE_SCORE_TABLE = "CREATE TABLE " + TABLE_SCORE + " ( " +
                KEY_PLAYER_ID + " INTEGER, " +
                KEY_INDEX + " INTEGER, " +
                KEY_VALUE + " INTEGER, " +
                KEY_TEXT_COLOR + " INTEGER, " +
                "PRIMARY KEY (" + KEY_PLAYER_ID + ", " + KEY_INDEX + "), " +
                "FOREIGN KEY(" + KEY_PLAYER_ID + ") REFERENCES " + TABLE_PLAYERS + "(" + KEY_ID + ") ON DELETE CASCADE)";

        String CREATE_GAMEMODES_TABLE = "CREATE TABLE " + TABLE_GAMEMODES + " ( " +
                KEY_NAME + " VARCHAR(200) PRIMARY KEY, " +
                KEY_ONLY_POSITIVE + " INTEGER, " +
                KEY_STRICT_ROUNDS + " INTEGER, " +
                KEY_FINNISH_ROUND + " INTEGER, " +
                KEY_LOWEST_SCORE_WIN + " INTEGER, " +
                KEY_WINNING_SCORE + " INTEGER, " +
                KEY_ROUNDS_COUNT + " INTEGER, " +
                KEY_CONSTANT_SCORE + " INTEGER)";

        // create players table
        db.execSQL(CREATE_PLAYERS_TABLE);
        // create score table
        db.execSQL(CREATE_SCORE_TABLE);
        // create game modes table
        db.execSQL(CREATE_GAMEMODES_TABLE);
    }

    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        super.onConfigure(db);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            db.setForeignKeyConstraintsEnabled(true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
        {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMEMODES);

        // create fresh books table
        this.onCreate(db);
    }

    void addPlayer(Player player)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, player.name);
        values.put(KEY_INDEX, ScoreCounterApp.players.indexOf(player));
        values.put(KEY_SCROLLED, player.scrolledPosition);
        values.put(KEY_TEXT_COLOR, player.textColor);
        values.put(KEY_BACK_COLOR, player.backGroundColor);

        db.insert(TABLE_PLAYERS, null, values);

        db.close();
    }

    void updatePlayer(int index)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        Player player = ScoreCounterApp.players.get(index);

        if (player == null)
            return;

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, player.name);
        values.put(KEY_SCROLLED, player.scrolledPosition);
        values.put(KEY_TEXT_COLOR, player.textColor);
        values.put(KEY_BACK_COLOR, player.backGroundColor);

        db.update(TABLE_PLAYERS,
                values,
                KEY_INDEX + " = ?",
                new String[]{String.valueOf(ScoreCounterApp.players.indexOf(player))});

        db.close();
    }

    void switchPlayer(int index1, int index2)
    {
        String query = "SELECT " + KEY_ID + ", " + KEY_INDEX + " FROM " + TABLE_PLAYERS + " WHERE " + KEY_INDEX + " IN(" + index1 + "," + index2 + ")";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst())
        {
            String first = "UPDATE " + TABLE_PLAYERS + " SET " + KEY_INDEX + " = " + index1 + " WHERE id = ";
            String second = "UPDATE " + TABLE_PLAYERS + " SET " + KEY_INDEX + " = " + index2 + " WHERE id = ";
            if (cursor.getInt(1) == index2)
                first += "" + cursor.getInt(0);
            else
                second += "" + cursor.getInt(0);

            if (!cursor.moveToNext())
                return;

            if (cursor.getInt(1) == index2)
                first += "" + cursor.getInt(0);
            else
                second += "" + cursor.getInt(0);

            db.execSQL(first);
            db.execSQL(second);
        }
        cursor.close();
        db.close();
    }

    void deletePlayer(int index)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_PLAYERS,
                KEY_INDEX + " = ?",
                new String[]{String.valueOf(index)});

        db.execSQL("UPDATE " + TABLE_PLAYERS + " SET " + KEY_INDEX + " = " + KEY_INDEX + "-1 WHERE " + KEY_INDEX + " > " + index);
        db.close();
    }

    void loadPlayers(Context context)
    {
        String query = "SELECT " + KEY_ID + ", " +
                KEY_INDEX + ", " +
                KEY_NAME + ", " +
                KEY_TEXT_COLOR + ", " +
                KEY_BACK_COLOR + ", " +
                KEY_SCROLLED +
                " FROM " + TABLE_PLAYERS + " ORDER BY " + KEY_INDEX;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        ScoreCounterApp.players.clear();

        if (cursor.moveToFirst())
        {
            do
            {
                List<Score> scoreList = loadScores(cursor.getInt(0));
                Player player = new Player(cursor, scoreList, context);
                ScoreCounterApp.players.add(cursor.getInt(1), player);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    void addScore(int playerIndex, Score score, int index)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_PLAYERS + " WHERE " + KEY_INDEX + " = " + playerIndex + " ORDER BY " + KEY_INDEX, null);

        int playerId;
        if (cursor.moveToFirst())
        {
            playerId = cursor.getInt(0);
        } else
        {
            cursor.close();
            db.close();
            return;
        }

        cursor.close();

        ContentValues values = new ContentValues();
        values.put(KEY_PLAYER_ID, playerId);
        values.put(KEY_INDEX, index);
        values.put(KEY_VALUE, score.value);
        values.put(KEY_TEXT_COLOR, score.color);

        db.replace(TABLE_SCORE, null, values);
        db.close();
    }

    void updateScore(int playerIndex, Score score, int index)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_PLAYERS + " WHERE " + KEY_INDEX + " = " + playerIndex, null);

        int playerId;
        if (cursor.moveToFirst())
        {
            playerId = cursor.getInt(0);
        } else
            return;

        cursor.close();

        Player player = ScoreCounterApp.players.get(playerIndex);

        if (player == null)
        {
            db.close();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_INDEX, index);
        values.put(KEY_VALUE, score.value);
        values.put(KEY_TEXT_COLOR, score.color);

        db.update(TABLE_SCORE,
                values,
                KEY_PLAYER_ID + " = ? AND " + KEY_INDEX + " = ?",
                new String[]{String.valueOf(playerId), String.valueOf(index)});

        db.close();
    }

    void deleteScore(int playerIndex, int index)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_PLAYERS + " WHERE " + KEY_INDEX + " = " + playerIndex, null);

        int playerId;
        if (cursor.moveToFirst())
        {
            playerId = cursor.getInt(0);
        } else
        {
            cursor.close();
            db.close();
            return;
        }
        cursor.close();

        db.delete(TABLE_SCORE,
                KEY_PLAYER_ID + " = ? AND " + KEY_INDEX + " = ?",
                new String[]{String.valueOf(playerId), String.valueOf(index)});

        db.execSQL("UPDATE " + TABLE_SCORE + " SET " + KEY_INDEX + " = " + KEY_INDEX + "-1 WHERE " + KEY_INDEX + " > " + index + " AND " + KEY_PLAYER_ID + " = " + playerId);
        db.close();
    }

    void deleteAllScore(int playerIndex)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_PLAYERS + " WHERE " + KEY_INDEX + " = " + playerIndex, null);

        int playerId;
        if (cursor.moveToFirst())
        {
            playerId = cursor.getInt(0);
        } else
        {
            cursor.close();
            db.close();
            return;
        }
        cursor.close();
        db.delete(TABLE_SCORE,
                KEY_PLAYER_ID + " = ?",
                new String[]{String.valueOf(playerId)});

        db.close();
    }

    List<Score> loadScores(int playerId)
    {
        List<Score> list = new ArrayList<>();

        String query = "SELECT " +
                KEY_INDEX + ", " +
                KEY_VALUE + ", " +
                KEY_TEXT_COLOR +
                " FROM " + TABLE_SCORE + " WHERE " + KEY_PLAYER_ID + " = " + playerId + " ORDER BY " + KEY_INDEX;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst())
        {
            do
            {
                list.add(cursor.getInt(0), new Score(cursor));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return list;
    }

    List<GameMode> loadGameModes()
    {
        List<GameMode> list = new ArrayList<>();

        String query = "SELECT " + KEY_NAME + ", "
                + KEY_ONLY_POSITIVE + ", " + KEY_STRICT_ROUNDS + ", " + KEY_FINNISH_ROUND + ", "
                + KEY_LOWEST_SCORE_WIN + ", " + KEY_WINNING_SCORE + ", " + KEY_ROUNDS_COUNT + ", " + KEY_CONSTANT_SCORE
                + " FROM " + TABLE_GAMEMODES + " ORDER BY " + KEY_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst())
        {
            do
            {
                list.add(new GameMode(cursor));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return list;
    }

    GameMode loadGameMode(String name)
    {
        String query = "SELECT " + KEY_NAME + ", "
                + KEY_ONLY_POSITIVE + ", " + KEY_STRICT_ROUNDS + ", " + KEY_FINNISH_ROUND + ", "
                + KEY_LOWEST_SCORE_WIN + ", " + KEY_WINNING_SCORE + ", " + KEY_ROUNDS_COUNT + ", " + KEY_CONSTANT_SCORE
                + " FROM " + TABLE_GAMEMODES + "WHERE " + KEY_NAME + " LIKE '" + name + "' ORDER BY " + KEY_NAME + " LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        GameMode gameMode = null;
        if (cursor.moveToFirst())
        {
            gameMode = new GameMode(cursor);
        }
        cursor.close();
        db.close();

        return gameMode;
    }

    void deleteGameMode(String name)
    {

        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_GAMEMODES, KEY_NAME + " = ?", new String[]{name});
        db.close();
    }

    void updateGameMode(GameMode gameMode, String oldName)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, gameMode.getName());
        values.put(KEY_LOWEST_SCORE_WIN, gameMode.isLowestScoreWin());
        values.put(KEY_ONLY_POSITIVE, gameMode.isOnlyPositiveScore());
        values.put(KEY_STRICT_ROUNDS, gameMode.isStrictRounds());
        values.put(KEY_FINNISH_ROUND, gameMode.isFinnishRound());
        values.put(KEY_WINNING_SCORE, gameMode.getWinningScore());
        values.put(KEY_ROUNDS_COUNT, gameMode.getRoundsCount());
        values.put(KEY_CONSTANT_SCORE, gameMode.getConstantScore());

        db.update(TABLE_GAMEMODES,
                values,
                KEY_NAME + " = ?",
                new String[]{oldName});
    }

    void insertGameMode(GameMode gameMode)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, gameMode.getName());
        values.put(KEY_LOWEST_SCORE_WIN, gameMode.isLowestScoreWin() ? 1 : 0);
        values.put(KEY_ONLY_POSITIVE, gameMode.isOnlyPositiveScore() ? 1 : 0);
        values.put(KEY_STRICT_ROUNDS, gameMode.isStrictRounds() ? 1 : 0);
        values.put(KEY_FINNISH_ROUND, gameMode.isFinnishRound() ? 1 : 0);
        values.put(KEY_WINNING_SCORE, gameMode.getWinningScore());
        values.put(KEY_ROUNDS_COUNT, gameMode.getRoundsCount());
        values.put(KEY_CONSTANT_SCORE, gameMode.getConstantScore());

        db.insert(TABLE_GAMEMODES, null, values);

    }
}