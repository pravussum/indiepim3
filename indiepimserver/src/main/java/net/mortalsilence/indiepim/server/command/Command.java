package net.mortalsilence.indiepim.server.command;

import net.mortalsilence.indiepim.server.command.exception.CommandException;

/**
 * Created with IntelliJ IDEA.
 * User: AmIEvil
 * Date: 19.08.13
 * Time: 21:32
 * To change this template use File | Settings | File Templates.
 */
public interface Command<A extends Action<R>, R extends Result> {

    R execute(A action) throws CommandException;
    void rollback(A action, R result);
}
