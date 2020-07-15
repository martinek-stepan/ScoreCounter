package martinek.stepan.scorecounter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GameModeAdapter extends ArrayAdapter<GameMode>
{
    private Context _context;
    private int _resource;

    public GameModeAdapter(Context context, int resource)
    {
        super(context, resource);
        _context = context;
        _resource = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {

        if (convertView == null)
        {
            // inflate the layout
            LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
            convertView = inflater.inflate(_resource, parent, false);
        }

        GameMode gameMode = getItem(position);
        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(gameMode.getName());

        if (gameMode.getName().equals(ScoreCounterApp.getGameMode().getName()))
            convertView.setBackgroundResource(R.drawable.backrepeat);
        else
            convertView.setBackgroundColor(Color.TRANSPARENT);

        return convertView;
    }
}