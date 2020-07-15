package martinek.stepan.scorecounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ScoreAdapter extends ArrayAdapter<Score>
{
    Context context;
    int resource;
    List<Score> objects;
    ScoreAdapter _this;
    Player player;
    public ScoreAdapter(Context context, int resource, List<Score> objects, Player player) {
        super(context, resource, objects);

        this.context = context;
        this.resource = resource;
        this.objects = objects;
        this.player = player;
        _this = this;
    }

    @Override public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView==null)
        {
            // inflate the layout
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(resource, parent, false);
        }

        TextView roundText = (TextView)convertView.findViewById(R.id.roundText);
        roundText.setText(""+ (position + 1)+".");

        final TextView scoreText = (TextView)convertView.findViewById(R.id.scoreText);
        scoreText.setText(objects.get(position).value < 0 ? ("- " + (objects.get(position).value*-1)) : ("+ " + objects.get(position).value));
        if (position == 0)
            scoreText.setVisibility(View.GONE);
        else
            scoreText.setVisibility(View.VISIBLE);

        TextView sumText = (TextView)convertView.findViewById(R.id.sumText);
        sumText.setText((position == 0 ? "" : "= ") + getSum(position));

        if (!ScoreCounterApp.isUseSubtotals() && position != objects.size() - 1 && position != 0)
            sumText.setVisibility(View.GONE);
        else
            sumText.setVisibility(View.VISIBLE);

        scoreText.setTextColor(player.textColor);
        sumText.setTextColor(player.textColor);
        roundText.setTextColor(player.textColor);

        if (objects.get(position).color == -1)
        {
            objects.get(position).color = scoreText.getTextColors().getDefaultColor();
            objects.get(position).defaultColor = scoreText.getTextColors().getDefaultColor();
        }
        else
        {
            scoreText.setTextColor(objects.get(position).color);
            sumText.setTextColor(objects.get(position).color);
            roundText.setTextColor(objects.get(position).color);
        }


        convertView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (ScoreCounterApp.isUseLongClickForEditing())
                    OnClick(position);
                return true;
            }
        });
        convertView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!ScoreCounterApp.isUseLongClickForEditing())
                    OnClick(position);
            }
        });


        return convertView;
    }

    private int getSum(int position)
    {
        if (position >= objects.size())
            return -1;

        int sum = 0;
        for (int i=0; i <= position; i++)
            sum += objects.get(i).value;

        return sum;
    }

    private void OnClick(final int position)
    {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.score_edit_menu, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);


        final TextView tvColorText = (TextView)promptsView.findViewById(R.id.colorTextScore);
        tvColorText.setBackgroundColor(objects.get(position).color);

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

        Button btTextColor = (Button)promptsView.findViewById(R.id.btTextColorScore);
        btTextColor.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(context, objects.get(position).color, new AmbilWarnaDialog.OnAmbilWarnaListener()
                {
                    @Override
                    public void onCancel(AmbilWarnaDialog ambilWarnaDialog)
                    {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog ambilWarnaDialog, int color)
                    {
                        objects.get(position).color = color;
                        tvColorText.setBackgroundColor(color);
                        ScoreCounterApp.dbHelper.updateScore(ScoreCounterApp.players.indexOf(player), objects.get(position), position);
                        _this.notifyDataSetChanged();
                    }
                });

                dialog.show();
            }
        });

        Button btResetColor = (Button)promptsView.findViewById(R.id.btResetColorScore);
        btResetColor.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                objects.get(position).color = objects.get(position).defaultColor;
                tvColorText.setBackgroundColor(objects.get(position).color);
                ScoreCounterApp.dbHelper.updateScore(ScoreCounterApp.players.indexOf(player), objects.get(position), position);
                _this.notifyDataSetChanged();
            }
        });

        Button btDelete = (Button)promptsView.findViewById(R.id.btDeleteScore);
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
                                        objects.remove(position);
                                        ScoreCounterApp.dbHelper.deleteScore(ScoreCounterApp.players.indexOf(player), position);
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

        final Button btChangeValue = (Button)promptsView.findViewById(R.id.btEditValue);
        btChangeValue.setText("Edit value: " + objects.get(position).value);
        btChangeValue.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ScoreCounterApp.ShowScoreEditDialog(context, objects.get(position).value, new ScoreCounterApp.TextEditDialogCB()
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

                        objects.get(position).value = score;
                        btChangeValue.setText("Edit value: " + score);
                        ScoreCounterApp.dbHelper.updateScore(ScoreCounterApp.players.indexOf(player), objects.get(position), position);
                        _this.notifyDataSetChanged();
                    }
                });
            }
        });
    }

}
