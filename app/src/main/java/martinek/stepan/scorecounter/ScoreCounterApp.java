package martinek.stepan.scorecounter;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class ScoreCounterApp extends Application
{
    public static List<Player> players = new ArrayList<>();
    public static SQLiteHelper dbHelper;
    public static String selectedGameMode = "";
    private static boolean useSubtotals = true;
    private static boolean alwaysVisibleAddScoreButton = true;
    private static boolean useLongClickForEditing = true;
    private static SharedPreferences _settings;
    private static GameMode _gameMode = null;

    public static GameMode getGameMode()
    {
        if (_gameMode == null)
            dbHelper.loadGameMode("%");
        if (_gameMode == null)
        {
            _gameMode = new GameMode();
            dbHelper.insertGameMode(_gameMode);
        }
        setSelectedGameMode(_gameMode.getName());
        return _gameMode;
    }

    public static void setGameMode(GameMode gameMode)
    {
        _gameMode = gameMode;
        setSelectedGameMode(gameMode != null ? gameMode.getName() : "");
    }

    public static boolean isUseSubtotals()
    {
        return useSubtotals;
    }

    public static void setUseSubtotals(boolean useSubtotals)
    {
        ScoreCounterApp.useSubtotals = useSubtotals;
        SharedPreferences.Editor editor = _settings.edit();
        editor.putBoolean("useSubtotals", useSubtotals);
        editor.commit();
    }

    public static boolean isAlwaysVisibleAddScoreButton()
    {
        return alwaysVisibleAddScoreButton;
    }

    public static void setAlwaysVisibleAddScoreButton(boolean alwaysVisibleAddScoreButton)
    {
        ScoreCounterApp.alwaysVisibleAddScoreButton = alwaysVisibleAddScoreButton;
        SharedPreferences.Editor editor = _settings.edit();
        editor.putBoolean("alwaysVisibleAddScoreButton", alwaysVisibleAddScoreButton);
        editor.commit();
    }

    public static boolean isUseLongClickForEditing()
    {
        return useLongClickForEditing;
    }

    public static void setUseLongClickForEditing(boolean useLongClickForEditing)
    {
        ScoreCounterApp.useLongClickForEditing = useLongClickForEditing;
        SharedPreferences.Editor editor = _settings.edit();
        editor.putBoolean("useLongClickForEditing", useLongClickForEditing);
        editor.commit();
    }

    static void ShowScoreEditDialog(Context context, Integer currentValue, final TextEditDialogCB cb)
    {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.score_value_edit, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.SVEeditText);
        if (currentValue != null)
        {
            userInput.setText("" + currentValue);
            userInput.setSelection(userInput.getText().length());
        }
        userInput.setKeyListener(DigitsKeyListener.getInstance(getGameMode().isOnlyPositiveScore() ? "0123456789" : "-+0123456789"));
        userInput.addTextChangedListener(new TextWatcherAfter(new AfterTextChangedCallBack()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                if (s.length() == 0)
                    return;

                String text = "";

                int minus = 0;
                boolean ok = true;
                for (int i = 0; i < s.length(); i++)
                {
                    if (s.charAt(i) == '-')
                    {
                        minus++;
                        if (i != 0)
                            ok = false;
                    } else if (s.charAt(i) == '+')
                    {
                        minus--;
                        ok = false;
                    } else
                        text += s.charAt(i);
                }

                if (!ok)
                {
                    minus %= 2;
                    userInput.setText(minus != 0 ? "-" + text : text);
                    userInput.setSelection(userInput.getText().length());
                }
            }
        }));

        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                cb.onPositiveButtonClick(dialog, id, userInput);
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

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    static void ShowNameEditDialog(Context context, String text, final TextEditDialogCB cb)
    {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.name_edit, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.nameEditText);

        userInput.setText(text);
        userInput.setSelection(userInput.getText().length());
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                cb.onPositiveButtonClick(dialog, id, userInput);
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

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static void setSelectedGameMode(String selectedGameMode)
    {
        ScoreCounterApp.selectedGameMode = selectedGameMode;
        SharedPreferences.Editor editor = _settings.edit();
        editor.putString("selectedGameMode", selectedGameMode);
        editor.commit();
    }

    public void Init(Context mainActivityContext)
    {
        _settings = getApplicationContext().getSharedPreferences("ScoreCounterSettings", 0);
        dbHelper = new SQLiteHelper(getApplicationContext());
        useSubtotals = _settings.getBoolean("useSubtotals", false);
        alwaysVisibleAddScoreButton = _settings.getBoolean("alwaysVisibleAddScoreButton", true);
        useLongClickForEditing = _settings.getBoolean("useLongClickForEditing", false);
        selectedGameMode = _settings.getString("selectedGameMode", "%");
        dbHelper.loadPlayers(mainActivityContext);
        _gameMode = dbHelper.loadGameMode(selectedGameMode);
        if (_gameMode == null)
            getGameMode();
    }

    interface TextEditDialogCB
    {
        void onPositiveButtonClick(DialogInterface dialog, int id, EditText userInput);
    }

    interface AfterTextChangedCallBack
    {
        void afterTextChanged(Editable s);
    }


    static class TextWatcherAfter implements TextWatcher
    {
        private AfterTextChangedCallBack _cb;

        public TextWatcherAfter(AfterTextChangedCallBack cb)
        {
            _cb = cb;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {

        }

        @Override
        public void afterTextChanged(Editable s)
        {
            _cb.afterTextChanged(s);
        }
    }

}