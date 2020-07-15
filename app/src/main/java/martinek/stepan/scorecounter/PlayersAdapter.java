package martinek.stepan.scorecounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;


public class PlayersAdapter extends ArrayAdapter<Player>
{

    Context context;
    int resource;
    List<Player> objects;
    int defaultBackgroundColor[] = {Color.argb(128, 160, 160, 160),Color.argb(225, 160, 160, 160)};
    int defaultTextColor = 0xF0FFFFFF;
    Player switchPlayer;
    private PlayersAdapter _this;


    public PlayersAdapter(Context context, int resource)
    {
        super(context, resource, ScoreCounterApp.players);

        this.context = context;
        this.resource = resource;
        this.objects = ScoreCounterApp.players;
        _this = this;
        switchPlayer = null;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView==null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(resource, parent, false);
        }

        final Player player = objects.get(position);

        final TextView name = (TextView)convertView.findViewById(R.id.playerName);
        name.setText(player.name);


        if (player.textColor != -1)
            name.setTextColor(player.textColor);
        else
            name.setTextColor(defaultTextColor);

        name.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (ScoreCounterApp.isUseLongClickForEditing())
                    OnClick(position);
                return true;
            }
        });
        name.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (switchPlayer == null)
                {
                    if (!ScoreCounterApp.isUseLongClickForEditing())
                        OnClick(position);
                    return;
                }

                if (objects.indexOf(switchPlayer) != position)
                {
                    ScoreCounterApp.dbHelper.switchPlayer(position, objects.indexOf(switchPlayer));
                    Collections.swap(ScoreCounterApp.players, position, objects.indexOf(switchPlayer));
                }

                switchPlayer = null;
                notifyDataSetChanged();
            }
        });

        final ListView scoreView = (ListView)convertView.findViewById(R.id.scoreView);
        LinearLayout llScore = (LinearLayout) convertView.findViewById(R.id.llAddScore);
        LinearLayout llScoreDyn = (LinearLayout) convertView.findViewById(R.id.llAddScoreDyn);

        if (llScoreDyn == null)
        {
            LayoutInflater li = LayoutInflater.from(context);
            View buttonView = li.inflate(R.layout.player_column_button, null);
            scoreView.addFooterView(buttonView);
            llScoreDyn = (LinearLayout) buttonView.findViewById(R.id.llAddScoreDyn);
        }

        if (ScoreCounterApp.isAlwaysVisibleAddScoreButton())
        {
            llScore.setVisibility(View.GONE);
            llScoreDyn.setVisibility(View.VISIBLE);
            llScore = llScoreDyn;
        }
        else
        {
            llScore.setVisibility(View.VISIBLE);
            llScoreDyn.setVisibility(View.GONE);
        }

        scoreView.setAdapter(player.getScoreAdapter());
        scoreView.setSelection(player.scrolledPosition);

        scoreView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
                int first = scoreView.getFirstVisiblePosition();
                int count = scoreView.getChildCount();

                if (scrollState == SCROLL_STATE_IDLE || (first + count > scoreView.getAdapter().getCount()))
                {
                    player.scrolledPosition = first;
                    ScoreCounterApp.dbHelper.updatePlayer(position);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {

            }
        });

        final ImageButton buttPlus = (ImageButton) llScore.findViewById(R.id.buttAddScorePlus);
        ImageButton buttMinus = (ImageButton) llScore.findViewById(R.id.buttAddScoreMinus);

        if (ScoreCounterApp.getGameMode().getConstantScore() == null || ScoreCounterApp.getGameMode().isOnlyPositiveScore())
            buttMinus.setVisibility(View.GONE);
        else
            buttMinus.setVisibility(View.VISIBLE);

        buttMinus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final int scoreCount = player.addScore(context, -ScoreCounterApp.getGameMode().getConstantScore());
                scoreView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        scoreView.smoothScrollToPosition(scoreCount);
                    }
                });
            }
        });

        buttPlus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (ScoreCounterApp.getGameMode().getConstantScore() != null)
                {
                    int score = ScoreCounterApp.getGameMode().getConstantScore();

                    final int scoreCount = player.addScore(context, score);
                    scoreView.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            scoreView.smoothScrollToPosition(scoreCount);
                        }
                    });
                } else
                {
                    ScoreCounterApp.ShowScoreEditDialog(context, null, new ScoreCounterApp.TextEditDialogCB()
                    {
                        @Override
                        public void onPositiveButtonClick(DialogInterface dialog, int id, EditText userInput)
                        {
                            int score;
                            try
                            {
                                score = Integer.parseInt(userInput.getText().toString());
                            } catch (Exception e)
                            {
                                dialog.cancel();
                                return;
                            }

                            final int scoreCount = player.addScore(context, score);
                            scoreView.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    scoreView.smoothScrollToPosition(scoreCount);
                                }
                            });
                        }
                    });
                }
            }
        });

        if (position == ScoreCounterApp.players.indexOf(switchPlayer))
        {
            convertView.setBackgroundResource(R.drawable.backrepeat);
        }
        else
        {
            if (player.backGroundColor != -1)
                convertView.setBackgroundColor(player.backGroundColor);
            else
                convertView.setBackgroundColor(defaultBackgroundColor[position % 2]);
        }


        return convertView;

    }

    private void OnClick(final int position)
    {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.player_edit_menu, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);


        final int currentTextColor = objects.get(position).textColor == -1 ? defaultTextColor : objects.get(position).textColor;
        final int currentBackColor = objects.get(position).backGroundColor == -1 ? defaultBackgroundColor[position % 2] : objects.get(position).backGroundColor;
        final TextView tvColorText = (TextView)promptsView.findViewById(R.id.colorTextPlayer);
        final TextView tvColorBack = (TextView)promptsView.findViewById(R.id.colorBackgroudPlayer);
        tvColorText.setBackgroundColor(currentTextColor);
        tvColorBack.setBackgroundColor(currentBackColor);

        alertDialogBuilder
                .setCancelable(true)
                .setNegativeButton("Done",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                dialog.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        Button btTextColor = (Button)promptsView.findViewById(R.id.btTextColorPlayer);
        btTextColor.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(context, currentTextColor, new AmbilWarnaDialog.OnAmbilWarnaListener()
                {
                    @Override
                    public void onCancel(AmbilWarnaDialog ambilWarnaDialog)
                    {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog ambilWarnaDialog, int color)
                    {
                        objects.get(position).textColor = color;
                        tvColorText.setBackgroundColor(color);
                        ScoreCounterApp.dbHelper.updatePlayer(position);
                        _this.notifyDataSetChanged();
                    }
                });

                dialog.show();
            }
        });

        Button btBackGroundColor = (Button)promptsView.findViewById(R.id.btPlayerBackgroundColor);
        btBackGroundColor.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(context, currentBackColor, new AmbilWarnaDialog.OnAmbilWarnaListener()
                {
                    @Override
                    public void onCancel(AmbilWarnaDialog ambilWarnaDialog)
                    {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog ambilWarnaDialog, int color)
                    {
                        objects.get(position).backGroundColor = color;
                        tvColorBack.setBackgroundColor(color);
                        ScoreCounterApp.dbHelper.updatePlayer(position);
                        _this.notifyDataSetChanged();
                    }
                });

                dialog.show();
            }
        });

        Button btResetColor = (Button)promptsView.findViewById(R.id.btResetColorPlayer);
        btResetColor.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                objects.get(position).backGroundColor = -1;
                objects.get(position).textColor = -1;
                tvColorText.setBackgroundColor(defaultTextColor);
                tvColorBack.setBackgroundColor(defaultBackgroundColor[position % 2]);
                ScoreCounterApp.dbHelper.updatePlayer(position);
                _this.notifyDataSetChanged();
            }
        });

        Button btSwitchPlayer = (Button)promptsView.findViewById(R.id.btSwitchPlayer);
        btSwitchPlayer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switchPlayer = objects.get(position);
                _this.notifyDataSetChanged();
                alertDialog.cancel();
            }
        });

        final Button btChangeName = (Button)promptsView.findViewById(R.id.btEditPlayerName);
        btChangeName.setText("Edit name: " + ScoreCounterApp.players.get(position).name);
        btChangeName.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ScoreCounterApp.ShowNameEditDialog(context, ScoreCounterApp.players.get(position).name, new ScoreCounterApp.TextEditDialogCB()
                {
                    @Override
                    public void onPositiveButtonClick(DialogInterface dialog, int id, EditText userInput)
                    {
                        ScoreCounterApp.players.get(position).name = userInput.getText().toString();
                        btChangeName.setText("Edit name: " + userInput.getText().toString());
                        ScoreCounterApp.dbHelper.updatePlayer(position);
                        _this.notifyDataSetChanged();
                    }
                });
            }
        });

        Button btDelete = (Button)promptsView.findViewById(R.id.btDeletePlayer);
        btDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder
                        .setCancelable(true)
                        .setTitle("Are you sure?")
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        if (switchPlayer == objects.get(position))
                                            switchPlayer = null;
                                        ScoreCounterApp.dbHelper.deletePlayer(position);
                                        objects.remove(position);
                                        _this.notifyDataSetChanged();
                                        alertDialog.cancel();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        dialog.cancel();
                                    }
                                });

                AlertDialog dialog = alertDialogBuilder.create();
                dialog.show();
            }
        });

    }

}
