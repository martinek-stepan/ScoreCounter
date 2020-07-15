package martinek.stepan.scorecounter;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class Player
{
    public String name;
    public int scrolledPosition;
    int textColor;
    int backGroundColor;
    private ScoreAdapter scoreAdapter;
    private List<Score> scoreList;


    public Player(String name,Context context)
    {
        scrolledPosition = 0;
        this.name = name;
        scoreList = new ArrayList<>();
        scoreAdapter = new ScoreAdapter(context, R.layout.score_item, scoreList, this);
        textColor = -1;
        backGroundColor = -1;
    }

    public Player(Cursor cursor, List<Score> scoreList, Context context)
    {
        name = cursor.getString(2);
        textColor = cursor.getInt(3);
        backGroundColor = cursor.getInt(4);
        scrolledPosition = cursor.getInt(5);
        this.scoreList = scoreList;
        scoreAdapter = new ScoreAdapter(context, R.layout.score_item, scoreList, this);
    }

    public ScoreAdapter getScoreAdapter()
    {
        return scoreAdapter;
    }

    public int addScore(Context context, int score)
    {
        Score s = new Score(score);
        scoreList.add(s);
        scoreAdapter.notifyDataSetChanged();
        ScoreCounterApp.dbHelper.addScore(ScoreCounterApp.players.indexOf(this), s, scoreList.indexOf(s));
        ScoreCounterApp.getGameMode().CheckGameMode(this, context);
        return scoreAdapter.getCount();
    }

    public int getScoreCount()
    {
        return scoreList.size();
    }

    public int getScoreSum()
    {
        int scoreSum = 0;
        for (Score s : scoreList)
            scoreSum += s.value;

        return scoreSum;
    }

    public void clearScore()
    {
        ScoreCounterApp.dbHelper.deleteAllScore(ScoreCounterApp.players.indexOf(this));
        scoreList.clear();
        scoreAdapter.notifyDataSetChanged();
    }
}
