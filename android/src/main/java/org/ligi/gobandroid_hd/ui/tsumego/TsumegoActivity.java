package org.ligi.gobandroid_hd.ui.tsumego;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;
import org.ligi.axt.listeners.ActivityFinishingOnClickListener;
import org.ligi.axt.listeners.DialogDiscardingOnClickListener;
import org.ligi.gobandroid_hd.App;
import org.ligi.gobandroid_hd.R;
import org.ligi.gobandroid_hd.events.GameChangedEvent;
import org.ligi.gobandroid_hd.events.TsumegoSolved;
import org.ligi.gobandroid_hd.logic.Cell;
import org.ligi.gobandroid_hd.logic.GoGame;
import org.ligi.gobandroid_hd.logic.GoMove;
import org.ligi.gobandroid_hd.ui.GoActivity;
import org.ligi.gobandroid_hd.ui.review.SGFMetaData;
import org.ligi.tracedroid.logging.Log;

public class TsumegoActivity extends GoActivity {

    private GoMove finishing_move;
    private TsumegoGameExtrasFragment myTsumegoExtrasFragment;
    private List<GoMove> on_path_moves = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTitle(R.string.tsumego);
    }

    private GoMove getFinishingMove() {
        if (finishing_move == null) {
            finishing_move = getCorrectMove(getGame().findFirstMove());
        }

        return finishing_move;
    }

    private boolean isFinishingMoveKnown() {
        return getFinishingMove() != null;
    }

    private void recursive_add_on_path_moves(GoMove act) {
        if (on_path_moves == null) {
            on_path_moves = new ArrayList<>();
        }
        on_path_moves.add(act);
        if (act.hasNextMove()) {
            for (GoMove child : act.getNextMoveVariations())
                recursive_add_on_path_moves(child);
        }
    }

    private boolean isOnPath() {
        if (on_path_moves == null) {
            Log.i("isOnPath null");
            // build a on path List to do a fast isOnPath() later
            recursive_add_on_path_moves(getGame().findFirstMove());
        }
        Log.i("isOnPath null" + on_path_moves.contains(getGame().getActMove()) + " " + on_path_moves.size());
        return on_path_moves.contains(getGame().getActMove());
    }

    @Override
    public GoGame.MoveStatus doMoveWithUIFeedback(Cell cell) {

        final GoGame.MoveStatus res = super.doMoveWithUIFeedback(cell);

        // if the move was valid and we have a counter move -> we will play it
        if (res == GoGame.MoveStatus.VALID) {
            if (getGame().getActMove().hasNextMove()) {
                getGame().jump(getGame().getActMove().getnextMove(0));
            }
        }

        getBus().post(GameChangedEvent.INSTANCE);
        return res;
    }

    private GoMove getCorrectMove(GoMove act_mve) {
        if (isCorrectMove(act_mve)) {
            return act_mve;
        }

        for (GoMove next_moves : act_mve.getNextMoveVariations()) {
            GoMove res = getCorrectMove(next_moves);
            if (res != null) {
                return res;
            }
        }

        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (getGame() == null) { // there was no game - fallback to main menu
            App.getTracker().trackException("tsumego start getGame() returned null in onCreate", false);
            finish();
            // startActivity(new Intent(this, gobandroid.class));
            return super.onCreateOptionsMenu(menu);
        }

        this.getMenuInflater().inflate(R.menu.ingame_tsumego, menu);
        menu.findItem(R.id.menu_game_hint).setVisible(isFinishingMoveKnown() && isOnPath());
        menu.findItem(R.id.menu_game_undo).setVisible(!getGame().getActMove().isFirstMove());
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (!super.onOptionsItemSelected(item)) {
            switch (item.getItemId()) {

                case R.id.menu_game_hint:

                    TsumegoHintAlert.show(this, getFinishingMove());
                    break;
            }
        }

        return false;
    }

    @Override
    public void initializeStoneMove() {
        // disable stone move - not wanted in tsumego
    }

    @Override
    public void requestUndo() {
        getBoard().setMove_stone_mode(false);
        // we do not want to keep user-variations in tsumego mode- but we want
        // to keep tsumego variation
        getGame().undo(isOnPath());

        // remove the counter-move if any
        if (!getGame().isBlackToMove()) {
            getGame().undo(isOnPath());
        }
    }

    @Override
    public Fragment getGameExtraFragment() {
        if (myTsumegoExtrasFragment == null) {
            myTsumegoExtrasFragment = new TsumegoGameExtrasFragment();
        }

        return myTsumegoExtrasFragment;
    }

    private boolean isCorrectMove(GoMove move) {
        return (move.getComment().trim().toUpperCase().startsWith("CORRECT") || // gogameguru style
                move.getComment().toUpperCase().contains("RIGHT") // goproblem.com style
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        finishing_move = null;

        if (getGame() == null) { // there was no game - fallback to main menu
            App.getTracker().trackException("tsumego start getGame() returned null in onCreate", false);
            finish();
            // startActivity(new Intent(this, gobandroid.class));
            return;
        }

        recursive_add_on_path_moves(getGame().findFirstMove());

        // try to find the correct solution
        if (!isFinishingMoveKnown()) {
            new AlertDialog.Builder(this).setMessage(R.string.tsumego_sgf_no_solution).setNegativeButton(R.string.ok, new DialogDiscardingOnClickListener()).setPositiveButton(R.string.go_back, new ActivityFinishingOnClickListener(this)).show();
        }

        float myZoom = TsumegoHelper.INSTANCE.calcZoom(getGame(), true);

        getBoard().setZoom(myZoom);
        getBoard().setZoomPOI(TsumegoHelper.INSTANCE.calcPOI(getGame(), true));
        onGameChanged(null);

        //NaDraChg getSlidingMenu().showContent();
    }

    private GoMove last_move;

    @Override
    public void onGameChanged(GameChangedEvent gameChangedEvent) {
        super.onGameChanged(gameChangedEvent);
        if (getGame().getActMove().equals(last_move)) {
            // TODO find the real cause why we got here to often for the same move
            // mainly a problem for writing the moments - otherwise too many moments where written for the same
            // solved tsumego

            return;
        }
        last_move = getGame().getActMove();

        if (myTsumegoExtrasFragment != null) {
            myTsumegoExtrasFragment.setOffPathVisibility(!isOnPath());
            myTsumegoExtrasFragment.setCorrectVisibility(isCorrectMove(getGame().getActMove()));
        }
        if (isCorrectMove(getGame().getActMove())) {
            SGFMetaData meta = new SGFMetaData(getGame().getMetaData().getFileName());
            meta.setIsSolved(true);
            meta.persist();

            getBus().post(new TsumegoSolved(getGame()));
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                supportInvalidateOptionsMenu();
            }
        });
    }

    @Override
    public boolean isAsk4QuitEnabled() {
        return false;
    }

}
