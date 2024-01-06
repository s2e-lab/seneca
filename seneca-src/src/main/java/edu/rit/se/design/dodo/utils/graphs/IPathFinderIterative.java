package edu.rit.se.design.dodo.utils.graphs;

import java.util.List;

/**
 * This interface represents classes which can be used to find paths from a magic method to a sink in an SDG.
 *
 * @author Brandon Greet - brandon.greet@mail.rit.edu
 */
public interface IPathFinderIterative<T> {
    List<T> find();
}
