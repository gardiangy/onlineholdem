package hu.onlineholdem.restclient.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;

public class GameListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Game> games;
    private Map<Game, List<Player>> players;

    public GameListAdapter(Context context, List<Game> games, Map<Game, List<Player>> players) {
        this.context = context;
        this.games = games;
        this.players = players;
    }

    @Override
    public int getGroupCount() {
        return games.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return players.get(games.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return games.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return players.get(games.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean b, View view, ViewGroup viewGroup) {
        Game game = (Game) getGroup(groupPosition);
        String header = game.getGameName();
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.list_group, null);
        }
        TextView lblListHeader = (TextView) view.findViewById(R.id.listHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(header);

        TextView headerPlayerNum = (TextView) view
                .findViewById(R.id.listHeaderPlayerNum);
        headerPlayerNum.setText(game.getPlayers().size() + " / " + game.getMaxPlayerNumber());

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View view, ViewGroup viewGroup) {

        final Player child = (Player) getChild(groupPosition, childPosition);
        String childText = child.getPlayerName();
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.list_item, null);
        }
        TextView txtListChild = (TextView) view
                .findViewById(R.id.listItem);

        txtListChild.setText(childText);
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }

    public void refreshData(List<Game> games, Map<Game, List<Player>> players) {
        this.games = games;
        this.players = players;
    }
}
