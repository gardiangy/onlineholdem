package hu.onlineholdem.restclient.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.activity.GameBrowserActivity;
import hu.onlineholdem.restclient.activity.MultiPlayerActivity;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;

public class GameListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Game> games;
    private Map<Game, List<Player>> players;
    private long joinedGameId = -1;

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
    public View getGroupView(final int groupPosition, boolean b, View view, ViewGroup viewGroup) {
        Game game = (Game) getGroup(groupPosition);
        String header = game.getGameName();
        final ViewHolder viewHolder;
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.list_group, null);
            viewHolder = new ViewHolder();
            viewHolder.playerNum = (TextView) view.findViewById(R.id.listHeaderPlayerNum);
            viewHolder.header = (TextView) view.findViewById(R.id.listHeader);
            viewHolder.header.setTypeface(null, Typeface.BOLD);
            viewHolder.id = (TextView) view.findViewById(R.id.rowId);
            viewHolder.joinBtn = (Button) view.findViewById(R.id.joinBtn);
            viewHolder.leaveBtn = (Button) view.findViewById(R.id.leaveBtn);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.header.setText(header);
        viewHolder.id.setText(game.getGameId().toString());
        viewHolder.playerNum.setText(game.getPlayers().size() + " / " + game.getMaxPlayerNumber());

        viewHolder.joinBtn.setFocusable(false);
        viewHolder.joinBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                ((GameBrowserActivity)context).joinGame(viewHolder.id.getText().toString());
//                joinedGameId = Long.valueOf(viewHolder.id.getText().toString());
                ((GameBrowserActivity)context).expandGroup(groupPosition);
            }
        });

        viewHolder.leaveBtn.setFocusable(false);
        viewHolder.leaveBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                ((GameBrowserActivity)context).leaveGame(viewHolder.id.getText().toString());
//                joinedGameId = -1;
                ((GameBrowserActivity)context).collapseGroup(groupPosition);
            }
        });

        if(joinedGameId != -1){
            viewHolder.joinBtn.setVisibility(View.GONE);
            if(Long.valueOf(viewHolder.id.getText().toString()) == joinedGameId){
                viewHolder.leaveBtn.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.joinBtn.setVisibility(View.VISIBLE);
            viewHolder.leaveBtn.setVisibility(View.GONE);
        }


        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View view, ViewGroup viewGroup) {

        final Player child = (Player) getChild(groupPosition, childPosition);
        String childText = child.getPlayerName();
        ViewHolder viewHolder;
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.header = (TextView) view.findViewById(R.id.listItem);
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.header.setText(childText);
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

    public static class ViewHolder {
        public TextView playerNum;
        public TextView header;
        public TextView id;
        public Button joinBtn;
        public Button leaveBtn;
    }

    public void setJoinedGameId(long joinedGameId) {
        this.joinedGameId = joinedGameId;
    }
}
