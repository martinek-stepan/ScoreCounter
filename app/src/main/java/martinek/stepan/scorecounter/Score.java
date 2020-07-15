package martinek.stepan.scorecounter;

import android.database.Cursor;

public class Score
{
    int value;
    int color;
    int defaultColor;

    public Score(int value)
    {
        this.value = value;
        this.color = -1;
        this.defaultColor = -1;
    }

    public Score(Cursor cusor)
    {
        value = cusor.getInt(1);
        color = cusor.getInt(2);
        this.defaultColor = -1;
    }
}
