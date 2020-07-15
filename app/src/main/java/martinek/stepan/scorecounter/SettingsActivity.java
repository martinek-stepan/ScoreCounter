package martinek.stepan.scorecounter;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");
        //back button at header
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        SwitchCompat useSubtotals = (SwitchCompat) findViewById(R.id.swSubtotal);
        SwitchCompat dynamicAddScore = (SwitchCompat) findViewById(R.id.swScoreButton);
        SwitchCompat longClickEdit = (SwitchCompat) findViewById(R.id.swEdit);

        useSubtotals.setChecked(ScoreCounterApp.isUseSubtotals());
        useSubtotals.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                ScoreCounterApp.setUseSubtotals(isChecked);
            }
        });
        dynamicAddScore.setChecked(ScoreCounterApp.isAlwaysVisibleAddScoreButton());
        dynamicAddScore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                ScoreCounterApp.setAlwaysVisibleAddScoreButton(isChecked);
            }
        });
        longClickEdit.setChecked(ScoreCounterApp.isUseLongClickForEditing());
        longClickEdit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                ScoreCounterApp.setUseLongClickForEditing(isChecked);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
