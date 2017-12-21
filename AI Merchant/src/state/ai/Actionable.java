package state.ai;


/*
 *  Interface to be used by both merchanting agents and individual item strategies. Boolean performAction() returns true
 *  if an action was indeed performed.
 *
 *  Agents should pass performAction() to item strategy of highest priority item.
 *
 *  For item strategies, performAction() should interact with its parent agent in order to perform a flip of the given
 *  item.
*/

public interface Actionable {
    boolean performAction();
}
