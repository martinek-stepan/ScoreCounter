package martinek.stepan.scorecounter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.lucasr.twowayview.TwoWayView;


public class MainActivity extends AppCompatActivity
{

    ScoreCounterApp app;
    Context context;
    private PlayersAdapter _playerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (ScoreCounterApp)getApplication();
        context = this;

        app.Init(this);

        TwoWayView playerView = (TwoWayView) findViewById(R.id.playerView);
        _playerAdapter = new PlayersAdapter(this, R.layout.player_column);
        playerView.setAdapter(_playerAdapter);

    }

    @Override
    protected void onStart()
    {
        _playerAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();

        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem item = menu.findItem(R.id.action_gamemode);
        SubMenu subMenu = item.getSubMenu();
        MenuItem curr = subMenu.findItem(R.id.action_edit_current);
        SpannableString s = new SpannableString(ScoreCounterApp.selectedGameMode);
        //s.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, s.length(), 0);
        curr.setTitle(s);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_player:

                ScoreCounterApp.ShowNameEditDialog(this, "Player " + (ScoreCounterApp.players.size() + 1), new ScoreCounterApp.TextEditDialogCB()
                {
                    @Override
                    public void onPositiveButtonClick(DialogInterface dialog, int id, EditText userInput)
                    {
                        final TwoWayView view = (TwoWayView) findViewById(R.id.playerView);
                        Player player = new Player(userInput.getText().toString(), context);
                        ScoreCounterApp.players.add(player);
                        ScoreCounterApp.dbHelper.addPlayer(player);
                        ((PlayersAdapter) view.getAdapter()).notifyDataSetChanged();
                        view.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                view.smoothScrollToPosition(ScoreCounterApp.players.size() - 1);
                            }
                        });

                    }
                });
                return true;
            case R.id.manage_gamemodes:
                startActivity(new Intent(context, GameModeListActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(context, SettingsActivity.class));
                return true;
            case R.id.action_new_game:
                GameMode.NewGame();
                return true;
            case R.id.action_edit_current:
                startActivity(new Intent(context, EditGameModeActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
