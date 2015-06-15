package org.jbnd.notes;

import org.jbnd.notes.NoteBoard.Note;


/**
 * An interface that needs to be implemented by objects that want to register
 * themselves with the <tt>NoteBoard</tt>, to receive notifications of posted
 * <tt>Note</tt>s.
 * 
 * @version 1.0 Dec 19, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see NoteBoard
 */
public interface NoteWatcher{

	/**
	 * This method is called when a <tt>Note</tt> of the type this watcher is
	 * interested in is posted on the <tt>NoteBoard</tt>.
	 * 
	 * @param n The posted <tt>Note</tt>.
	 * @see NoteBoard
	 */
	void notePosted(Note n);
}
