package eu.fbk.utils.core.strings;

/**
 * Edit distance between two strings
 *
 * @author Yaroslav Nechaev (remper@me.com)
 */
public interface EditDistance<T> {
  T apply(CharSequence left, CharSequence right);
}
