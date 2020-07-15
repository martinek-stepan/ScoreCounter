package martinek.stepan.scorecounter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class EditGameModeActivity  extends AppCompatActivity
{
    private Context _context;
    private Runnable _nameChecker;
    private EditText _etName;
    private String _name;
    private boolean _hasNameChecker;
    private GameMode _gameMode;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_game_mode);
        setTitle("Game mode edit");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        _context = this;

        Bundle extras = getIntent().getExtras();

        _gameMode = extras != null ? ScoreCounterApp.dbHelper.loadGameMode(extras.getString("gameMode")) : ScoreCounterApp.getGameMode();

        _etName = (EditText) findViewById(R.id.etGameModeName);
        _etName.setText(_gameMode.getName());
        _etName.setSelection(_etName.getText().length());
        _name = _gameMode.getName();
        _hasNameChecker = false;
        _nameChecker = new Runnable()
        {
            @Override
            public void run()
            {
                _hasNameChecker = false;
                if (ScoreCounterApp.dbHelper.loadGameMode(_etName.getText().toString()) != null)
                {
                    if (!_etName.getText().toString().equals(_gameMode.getName()))
                        Toast.makeText(_context, "Game Mode name have to be unique. Current name will not be saved.", Toast.LENGTH_LONG).show();
                } else
                    _gameMode.setName(_etName.getText().toString());
            }
        };

        _etName.addTextChangedListener(new ScoreCounterApp.TextWatcherAfter(new ScoreCounterApp.AfterTextChangedCallBack()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                if (!_etName.getText().toString().equals(_name))
                {
                    _etName.removeCallbacks(_nameChecker);
                    _etName.postDelayed(_nameChecker, 1000);
                    _name = _etName.getText().toString();
                    _hasNameChecker = true;
                }
            }
        }));

        final SwitchCompat swStrictRounds = (SwitchCompat) findViewById(R.id.swStrictRounds);
        SwitchCompat swOnlyPositive = (SwitchCompat) findViewById(R.id.swOnlyPositive);
        final SwitchCompat swConstantScore = (SwitchCompat) findViewById(R.id.swConstantScore);
        SwitchCompat swWinningScore = (SwitchCompat) findViewById(R.id.swWinningScore);
        SwitchCompat swLowestScore = (SwitchCompat) findViewById(R.id.swLowestScore);
        final SwitchCompat swFinnishRound = (SwitchCompat) findViewById(R.id.swFinnishRound);
        final SwitchCompat swLimitRounds = (SwitchCompat) findViewById(R.id.swRounds);
        final EditText etRoundsCount = (EditText) findViewById(R.id.etRoundsNumber);
        final EditText etWinningScore = (EditText) findViewById(R.id.etWinningScore);
        final EditText etConstantScore = (EditText) findViewById(R.id.etConstantScore);
        etRoundsCount.setText(_gameMode.getRoundsCount() != null ? ("" + _gameMode.getRoundsCount()) : "10");
        etWinningScore.setText(_gameMode.getWinningScore() != null ? ("" + _gameMode.getWinningScore()) : "100");
        etConstantScore.setText(_gameMode.getConstantScore() != null ? ("" + _gameMode.getConstantScore()) : "1");

        swFinnishRound.setChecked(_gameMode.isFinnishRound());
        swStrictRounds.setChecked(_gameMode.isStrictRounds());
        swOnlyPositive.setChecked(_gameMode.isOnlyPositiveScore());
        swLowestScore.setChecked(_gameMode.isLowestScoreWin());
        swConstantScore.setChecked(_gameMode.getConstantScore() != null);
        swWinningScore.setChecked(_gameMode.getWinningScore() != null);
        swLimitRounds.setChecked(_gameMode.getRoundsCount() != null);

        etWinningScore.setVisibility(_gameMode.getWinningScore() != null ? View.VISIBLE : View.GONE);
        swFinnishRound.setVisibility(_gameMode.getWinningScore() != null ? View.VISIBLE : View.GONE);
        etConstantScore.setVisibility(_gameMode.getConstantScore() != null ? View.VISIBLE : View.GONE);
        etRoundsCount.setVisibility(_gameMode.getRoundsCount() != null ? View.VISIBLE : View.GONE);

        swStrictRounds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                _gameMode.setStrictRounds(isChecked);
                if (!isChecked)
                    swFinnishRound.setChecked(false);
            }
        });

        swOnlyPositive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                _gameMode.setOnlyPositiveScore(isChecked);
            }
        });

        swLowestScore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                _gameMode.setLowestScoreWin(isChecked);
            }
        });

        swFinnishRound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                _gameMode.setFinnishRound(isChecked);

                if (isChecked)
                    swStrictRounds.setChecked(true);
            }
        });

        swConstantScore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                _gameMode.setConstantScore(isChecked ? parseIntDefault(etConstantScore.getText().toString(), 1) : null);
                etConstantScore.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (isChecked)
                    etConstantScore.setText("1");
            }
        });

        etConstantScore.addTextChangedListener(new ScoreCounterApp.TextWatcherAfter(new ScoreCounterApp.AfterTextChangedCallBack()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                int constantScore = parseIntDefault(s.toString(), 1);
                if (constantScore == 0)
                    swConstantScore.setChecked(false);
                else
                    _gameMode.setConstantScore(constantScore);
            }
        }));

        swWinningScore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                _gameMode.setWinningScore(isChecked ? parseIntDefault(etWinningScore.getText().toString(), 100) : null);
                etWinningScore.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                swFinnishRound.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });


        etWinningScore.addTextChangedListener(new ScoreCounterApp.TextWatcherAfter(new ScoreCounterApp.AfterTextChangedCallBack()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                _gameMode.setWinningScore(parseIntDefault(s.toString(), 100));
            }
        }));

        swLimitRounds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                _gameMode.setRoundsCount(isChecked ? parseIntDefault(etRoundsCount.getText().toString(), 10) : null);
                etRoundsCount.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (isChecked)
                    etRoundsCount.setText("10");
            }
        });

        etRoundsCount.addTextChangedListener(new ScoreCounterApp.TextWatcherAfter(new ScoreCounterApp.AfterTextChangedCallBack()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
                int roundsCount = parseIntDefault(s.toString(), 10);
                if (roundsCount == 0)
                    swLimitRounds.setChecked(false);
                else
                    _gameMode.setRoundsCount(roundsCount);
            }
        }));
    }
    int parseIntDefault(String s, int d)
    {
        try
        {
            return Integer.parseInt(s);
        }
        catch (Exception e)
        {
            return d;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (_hasNameChecker)
        {
            if (ScoreCounterApp.dbHelper.loadGameMode(_etName.getText().toString()) != null)
                Toast.makeText(_context, "Game Mode name have to be unique. Current name will not be saved.", Toast.LENGTH_LONG).show();
            else
                _gameMode.setName(_etName.getText().toString());
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("gameMode", _gameMode.getName());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
