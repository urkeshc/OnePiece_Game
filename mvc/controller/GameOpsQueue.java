package mvc.controller;

import mvc.model.Movable;
import java.util.concurrent.LinkedBlockingDeque;



/**
 * Effectively a Queue that enqueues and dequeues Game Operations (add/remove).
 * enqueue() may be called by main and animation threads simultaneously, therefore we
 * use a data structure from the java.util.concurrent package.
 */
public class GameOpsQueue extends LinkedBlockingDeque<GameOp> {

    public void enqueue(Movable mov, GameOp.Action action) {
        addLast(new GameOp(mov, action));
    }

    public GameOp dequeue() {
        return removeFirst();
    }

    public boolean contains(Movable movable) {
        return this.stream()
                .anyMatch(op -> op.getMovable() == movable && op.getAction() == GameOp.Action.REMOVE);
    }
}
