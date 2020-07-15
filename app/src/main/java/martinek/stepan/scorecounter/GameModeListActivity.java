package martinek.stepan.scorecounter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.Comparator;
import java.util.List;

public class GameModeListActivity extends AppCompatActivity
{
    private Context _context;
    private GameModeAdapter _adapter;

    public static boolean isNameUnique(GameModeAdapter adapter, String name)
    {
        for (int i = 0; i < adapter.getCount(); i++)
            if (adapter.getItem(i).getName().equals(name))
                return false;
        return true;
    }

    public static String generateUniqueName(GameModeAdapter adapter)
    {
        for (int i = 1; ; i++)
            if (isNameUnique(adapter, "GameMode " + i))
                return "GameMode " + i;
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        _adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode_list);
        setTitle("Game mode edit");

        _context = this;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        final SwipeMenuListView gameModeView = (SwipeMenuListView) findViewById(R.id.listGameModes);

        _adapter = new GameModeAdapter(this, android.R.layout.simple_list_item_1);
        final List<GameMode> gameModes = ScoreCounterApp.dbHelper.loadGameModes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            _adapter.addAll(gameModes);
        } else
        {
            for (GameMode mode : gameModes)
                _adapter.add(mode);
        }

        _adapter.sort(new Comparator<GameMode>()
        {
            @Override
            public int compare(GameMode lhs, GameMode rhs)
            {
                int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
                if (res == 0) res = lhs.getName().compareTo(rhs.getName());
                return res;
            }
        });

        gameModeView.setAdapter(_adapter);
        gameModeView.setMenuCreator(new SwipeMenuCreator()
        {
            @Override
            public void create(SwipeMenu menu)
            {
                // create "delete" item
                SwipeMenuItem editItem = new SwipeMenuItem(_context);
                editItem.setBackground(new ColorDrawable(Color.BLUE));
                editItem.setWidth(90);
                editItem.setIcon(R.drawable.ic_edit);
                menu.addMenuItem(editItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(_context);
                deleteItem.setBackground(new ColorDrawable(Color.RED));
                deleteItem.setWidth(90);
                deleteItem.setIcon(R.drawable.ic_delete);
                menu.addMenuItem(deleteItem);
            }
        });

        gameModeView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index)
            {
                switch (index)
                {
                    //edit
                    case 0:
                    {
                        final GameMode clickedGM = _adapter.getItem(position);
                        gameModeView.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Intent i = new Intent(_context, EditGameModeActivity.class);
                                if (!clickedGM.getName().equals(ScoreCounterApp.selectedGameMode))
                                    i.putExtra("gameMode", clickedGM.getName());
                                startActivityForResult(i, _adapter.getPosition(clickedGM));
                            }
                        }, 300);
                        break;
                    }
                    //delete
                    case 1:
                    {
                        final GameMode clickedGM = _adapter.getItem(position);
                        final GameMode currentGM = ScoreCounterApp.getGameMode();
                        gameModeView.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                _adapter.remove(clickedGM);
                                ScoreCounterApp.dbHelper.deleteGameMode(clickedGM.getName());
                                if (clickedGM.getName().equals(currentGM.getName()))
                                    ScoreCounterApp.setGameMode(!_adapter.isEmpty() ? _adapter.getItem(0) : null);
                                _adapter.notifyDataSetChanged();
                            }
                        }, 300);
                        break;
                    }
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        gameModeView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ScoreCounterApp.setGameMode(_adapter.getItem(position));
                _adapter.notifyDataSetChanged();
            }
        });

        gameModeView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        Button buttAddGameMode = (Button) findViewById(R.id.buttNewGameMode);
        buttAddGameMode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ScoreCounterApp.ShowNameEditDialog(_context, generateUniqueName(_adapter), new ScoreCounterApp.TextEditDialogCB()
                {
                    @Override
                    public void onPositiveButtonClick(DialogInterface dialog, int id, EditText userInput)
                    {
                        String name = userInput.getText().toString();

                        if (!isNameUnique(_adapter, name))
                            return;

                        GameMode gm = new GameMode(name);
                        _adapter.add(gm);
                        ScoreCounterApp.dbHelper.insertGameMode(gm);
                        _adapter.sort(new Comparator<GameMode>()
                        {
                            @Override
                            public int compare(GameMode lhs, GameMode rhs)
                            {
                                int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
                                if (res == 0) res = lhs.getName().compareTo(rhs.getName());
                                return res;
                            }
                        });
                        _adapter.notifyDataSetChanged();

                        Intent i = new Intent(_context, EditGameModeActivity.class);
                        if (!gm.getName().equals(ScoreCounterApp.selectedGameMode))
                            i.putExtra("gameMode", gm.getName());
                        startActivityForResult(i, _adapter.getPosition(gm));
                    }
                });


            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK && data != null)
        {
            GameMode gameMode = _adapter.getItem(requestCode);
            _adapter.remove(gameMode);
            _adapter.add(ScoreCounterApp.dbHelper.loadGameMode(data.getStringExtra("gameMode")));

            _adapter.sort(new Comparator<GameMode>()
            {
                @Override
                public int compare(GameMode lhs, GameMode rhs)
                {
                    int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
                    if (res == 0) res = lhs.getName().compareTo(rhs.getName());
                    return res;
                }
            });
            _adapter.notifyDataSetChanged();
        }
    }
}
