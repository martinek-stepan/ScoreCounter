package martinek.stepan.scorecounter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GameMode
{
    private String name = "default";
    private boolean onlyPositiveScore = false;
    private boolean strictRounds = false;
    private boolean finnishRound = false;
    private boolean lowestScoreWin = false;

    private Integer winningScore = null;
    private Integer roundsCount = null;
    private Integer constantScore = null;

    public GameMode(Cursor cursor)
    {
        name = cursor.getString(0);
        onlyPositiveScore = cursor.getInt(1) > 0;
        strictRounds = cursor.getInt(2) > 0;
        finnishRound = cursor.getInt(3) > 0;
        lowestScoreWin = cursor.getInt(4) > 0;

        winningScore = cursor.getInt(5);
        roundsCount = cursor.getInt(6);
        constantScore = cursor.getInt(7);

        if (winningScore != null && winningScore == 0)
            winningScore = null;
        if (roundsCount != null && roundsCount == 0)
            roundsCount = null;
        if (constantScore == 0)
            constantScore = null;
    }

    public GameMode()
    {
        name = "default";
        onlyPositiveScore = false;
        strictRounds = false;
        finnishRound = false;
        lowestScoreWin = false;

        winningScore = null;
        roundsCount = null;
        constantScore = null;
    }

    public GameMode(String name)
    {
        this();
        this.name = name;
    }

    static void ShowStandingsDialog(Context context, TreeMap<Integer, List<Player>> standing)
    {
        TableLayout tl = new TableLayout(context);
        tl.setColumnStretchable(0, false);
        tl.setColumnStretchable(1, false);
        tl.setColumnStretchable(2, true);
        tl.setColumnShrinkable(0, true);
        tl.setColumnShrinkable(1, true);
        tl.setColumnShrinkable(2, false);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            tl.setDividerDrawable(context.getResources().getDrawable(R.drawable.abc_tab_indicator_material));
            tl.setShowDividers(TableLayout.SHOW_DIVIDER_BEGINNING | TableLayout.SHOW_DIVIDER_MIDDLE | TableLayout.SHOW_DIVIDER_END);
        }
*/
        {
            TableRow tr = new TableRow(context);
            tr.setGravity(Gravity.CENTER);

            TextView place = new TextView(context);
            place.setText("Place");
            place.setGravity(Gravity.CENTER);
            place.setPadding(10, 0, 10, 0);
            tr.addView(place);


            TextView score = new TextView(context);
            score.setText("Score");
            score.setGravity(Gravity.CENTER);
            score.setPadding(10, 0, 10, 0);
            tr.addView(score);


            TextView players = new TextView(context);
            players.setText("Players");
            players.setGravity(Gravity.CENTER);
            players.setPadding(10, 0, 10, 0);
            tr.addView(players);

            tl.addView(tr);
        }
        int p = 0;
        for (Map.Entry<Integer, List<Player>> stand : standing.entrySet())
        {
            p++;
            String text = "";
            String s = "";
            for (Player player : stand.getValue())
            {
                s = "" + player.getScoreSum();
                text += player.name + ", ";
            }
            if (text.length() > 2)
                text = text.substring(0, text.length() - 2);


            TableRow tr = new TableRow(context);
            TextView place = new TextView(context);
            place.setText(p + ".");
            place.setGravity(Gravity.CENTER);
            place.setPadding(10, 0, 10, 0);
            TextView score = new TextView(context);
            score.setText(s);
            score.setGravity(Gravity.CENTER);
            score.setPadding(10, 0, 10, 0);
            TextView name = new TextView(context);
            name.setText(text);
            name.setGravity(Gravity.CENTER);
            name.setPadding(10, 0, 10, 0);
            tr.addView(place);
            tr.addView(score);
            tr.addView(name);
            tr.setGravity(Gravity.CENTER);
            tl.addView(tr, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(tl);

        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Continue",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Clear",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                GameMode.NewGame();
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static void NewGame()
    {
        for (Player p : ScoreCounterApp.players)
        {
            p.clearScore();
        }

    }

    public void CheckGameMode(Player player, Context context)
    {
        List<Player> pl = ScoreCounterApp.players;

        if (strictRounds)
        {
            int playerIndex = pl.indexOf(player);
            int scoreCount = player.getScoreCount();
            for (int i = 0; i < pl.size(); i++)
            {
                if (i == playerIndex)
                    continue;

                for (int j = pl.get(i).getScoreCount(); j < (i < playerIndex ? scoreCount : scoreCount - 1); j++)
                    pl.get(i).addScore(context, 0);
            }
        }

        if (winningScore != null && !finnishRound && (lowestScoreWin ? player.getScoreSum() <= winningScore : player.getScoreSum() >= winningScore))
        {
            //popup
            TreeMap<Integer, List<Player>> map = sortPlayersByScore(lowestScoreWin ? null : winningScore, lowestScoreWin ? winningScore : null, null);
            if (map != null)
            {
                ShowStandingsDialog(context, map);
                return;
            }
        }


        if (pl.indexOf(player) == pl.size() - 1 && winningScore != null)
        {
            TreeMap<Integer, List<Player>> map = sortPlayersByScore(lowestScoreWin ? null : winningScore, lowestScoreWin ? winningScore : null, null);
            if (map != null)
            {
                //popup
                ShowStandingsDialog(context, map);
                return;
            }
        }

        if (roundsCount != null)
        {
            TreeMap<Integer, List<Player>> map = sortPlayersByScore(null, null, roundsCount);
            if (map != null)
            {
                //popup
                ShowStandingsDialog(context, map);
            }
        }

    }

    private TreeMap<Integer, List<Player>> sortPlayersByScore(Integer maxScore, Integer minScore, Integer roundsCount)
    {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int rounds = Integer.MAX_VALUE;
        int minRoundsCount = Integer.MAX_VALUE;
        TreeMap<Integer, List<Player>> map = lowestScoreWin ? new TreeMap<Integer, List<Player>>() : new TreeMap<Integer, List<Player>>(Collections.reverseOrder());
        for (Player p : ScoreCounterApp.players)
        {
            minRoundsCount = Math.min(minRoundsCount, p.getScoreCount());

            int sum = p.getScoreSum();
            if (map.containsKey(sum))
                map.get(sum).add(p);
            else
            {
                List<Player> l = new ArrayList<>();
                l.add(p);
                map.put(sum, l);
            }
            max = Math.max(max, sum);
            min = Math.min(min, sum);
            rounds = Math.min(rounds, p.getScoreCount());
        }

        if (minRoundsCount != 0 && ((maxScore != null && max >= maxScore) || (minScore != null && min <= minScore) || (roundsCount != null && rounds == roundsCount)))
            return map;
        else
            return null;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        String oldName = this.name;
        this.name = name;
        ScoreCounterApp.dbHelper.updateGameMode(this, oldName);

        if (oldName.equals(ScoreCounterApp.selectedGameMode))
            ScoreCounterApp.selectedGameMode = name;
    }

    public boolean isOnlyPositiveScore()
    {
        return onlyPositiveScore;
    }

    public void setOnlyPositiveScore(boolean onlyPositiveScore)
    {
        this.onlyPositiveScore = onlyPositiveScore;
        ScoreCounterApp.dbHelper.updateGameMode(this, name);
    }

    public boolean isStrictRounds()
    {
        return strictRounds;
    }

    public void setStrictRounds(boolean strictRounds)
    {
        this.strictRounds = strictRounds;
        ScoreCounterApp.dbHelper.updateGameMode(this, name);
    }

    public boolean isFinnishRound()
    {
        return finnishRound;
    }

    public void setFinnishRound(boolean finnishRound)
    {
        this.finnishRound = finnishRound;
        ScoreCounterApp.dbHelper.updateGameMode(this, name);
    }

    public boolean isLowestScoreWin()
    {
        return lowestScoreWin;
    }

    public void setLowestScoreWin(boolean lowestScoreWin)
    {
        this.lowestScoreWin = lowestScoreWin;
        ScoreCounterApp.dbHelper.updateGameMode(this, name);
    }

    public Integer getWinningScore()
    {
        return winningScore;
    }

    public void setWinningScore(Integer winningScore)
    {
        this.winningScore = winningScore;
        ScoreCounterApp.dbHelper.updateGameMode(this, name);
    }

    public Integer getRoundsCount()
    {
        return roundsCount;
    }

    public void setRoundsCount(Integer roundsCount)
    {
        this.roundsCount = roundsCount;
        ScoreCounterApp.dbHelper.updateGameMode(this, name);
    }

    public Integer getConstantScore()
    {
        return constantScore;
    }

    public void setConstantScore(Integer constantScore)
    {
        this.constantScore = constantScore;
        ScoreCounterApp.dbHelper.updateGameMode(this, name);
    }
}
