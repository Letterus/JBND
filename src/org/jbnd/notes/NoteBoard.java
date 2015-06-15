package org.jbnd.notes;

import java.util.*;


/**
 * Provides a way for objects to post <tt>Note</tt>s that other objects (
 * <tt>NoteWatcher</tt> implementations) can observe, with no coupling between
 * the posting and watching objects. Posting <tt>Note</tt>s is straightforward,
 * done through the <tt>postNote(...)</tt> methods. To be notified of posted
 * <tt>Note</tt>s the <tt>NoteWatcher</tt> objects must register themselves with
 * the board, using the <tt>addWatcher(...)</tt> methods.
 * <p>
 * <tt>NoteBoard</tt>s can not be instantiated externally, however the same
 * effect can be achieved by using the <tt>addWatcher(String, NoteWatcher)</tt>
 * method. This will register watchers only for a specific kind of note type.
 * 
 * @version 1.0 Dec 19, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see Note
 */
public final class NoteBoard{
	
	//	a Map containing different NoteBoards, depending on the
	//	Note type they are managing
	//	the String key of the Map is the note type the board is specialized in
	//	the NoteBoard value of the map is the specialized board
	private static final Map<String, NoteBoard> boards = new HashMap<String, NoteBoard>();
	
	//	a NoteBoard on which ALL Notes are posted, regardless of their type
	private static final NoteBoard defaultBoard = new NoteBoard();

	
	/**
	 * Posts a note on the <tt>NoteBoard</tt>, that all registered
	 * <tt>NoteWatcher</tt>s that are not note type specific will be notified
	 * of, as well as <tt>NoteWatcher</tt>s specific to the note type of the
	 * note being posted.
	 * 
	 * @param noteType The type of the <tt>Note</tt> to be posted.
	 * @param source The object posting the <tt>Note</tt>.
	 * @see Note
	 */
	public static void postNote(String noteType, Object source){
		postNote(noteType, source, Collections.EMPTY_MAP);
	}
	
	/**
	 * Posts a note on the <tt>NoteBoard</tt>, that all registered
	 * <tt>NoteWatcher</tt>s that are not note type specific will be notified
	 * of, as well as <tt>NoteWatcher</tt>s specific to the note type of the
	 * note being posted.
	 * 
	 * @param noteType The type of the <tt>Note</tt> to be posted.
	 * @param source The object posting the <tt>Note</tt>.
	 * @param info Additional information of the <tt>Note</tt>.
	 * @see Note
	 */
	public static void postNote(String noteType, Object source, Map<?, ?> info){
		if(noteType == null)
			throw new IllegalArgumentException("Can not post a Note with a null type");
		if(source == null)
			throw new IllegalArgumentException("Can not post a Note with a null source");
		if(info == null)
			info = Collections.EMPTY_MAP;
		
		//	the note to post
		Note n = new Note(noteType, source, info);
		//	post it !
		defaultBoard._postNote(n);
		NoteBoard specializedBoard = boards.get(noteType);
		if(specializedBoard != null)
			specializedBoard._postNote(n);
	}
	
	/**
	 * Registers the given <tt>NoteWatcher</tt> with the board so
	 * that it will be notified when <tt>Note</tt>s are posted. When
	 * registering a watcher with this method, it will be registered
	 * to receive all posted <tt>Note</tt>s, regardless of their type.
	 * 
	 * @param watcher	The watcher to register.
	 */
	public static void addWatcher(NoteWatcher watcher){
		addWatcher(null, watcher);
	}
	
	/**
	 * Registers the given <tt>NoteWatcher</tt> with the board so that it will
	 * be notified when <tt>Note</tt>s of the given <tt>noteType</tt> are
	 * posted. Calling this method with the <tt>noteType</tt> parameter being
	 * <tt>null</tt> will register the watcher for all <tt>Note</tt>s,
	 * regardless of their type.
	 * 
	 * @param noteType The type of note for which to register the watcher, the
	 *            watcher will be notified only of <tt>Note</tt>s of the same
	 *            type.
	 * @param watcher The watcher to register.
	 */
	public static void addWatcher(String noteType, NoteWatcher watcher){
		if(watcher == null)
			throw new IllegalArgumentException("Can not register a null watcher");
		
		if(noteType == null){
			//	note type is null, add the watcher to the default board
			defaultBoard.watchers.add(watcher);
		}else{
			//	get the specialized board
			NoteBoard b = boards.get(noteType);
			//	if not there, create one
			if(b == null){
				b = new NoteBoard();
				boards.put(noteType, b);
			}
			//	add the watcher to the specialized board.
			b.watchers.add(watcher);
		}
	}
	
	/**
	 * Removes the given <tt>watcher</tt> from the <tt>NoteBoard</tt>.
	 * If by any way the watcher has been added more then once to
	 * the board, it will still be completely removed.
	 * 
	 * @param watcher	The watcher to remove from the <tt>NoteBoard</tt>.
	 * @return			If the watcher was found and removed, at least once.
	 */
	public static boolean removeWatcher(NoteWatcher watcher){
		boolean removed = defaultBoard.watchers.remove(watcher);
		
		Iterator<NoteBoard> it = boards.values().iterator();
		while(it.hasNext())
			removed = it.next().watchers.remove(watcher);
		
		return removed;
	}
	
	
	/*
	 * 
	 * End of static method. Further on all NoteBoard methods etc are
	 * private, the NoteBoard class can be interacted with exclusively
	 * through static methods.
	 * 
	 */
	
	
	// a list containing watchers that are interested when a note gets posted
	private final List<NoteWatcher> watchers = new LinkedList<NoteWatcher>();
	
	// disallow external instantiation
	private NoteBoard(){}
	
	private void _postNote(Note n){
		//	iterate through all the observers and, well, DO EM!
		for(NoteWatcher watcher : watchers)
			watcher.notePosted(n);
	}

	/**
	 * Messaging means of decoupled communication between objects, exchanged
	 * through the <tt>NoteBoard</tt>. Objects can 'post' <tt>Note</tt>s using
	 * the <tt>NoteBoard</tt>, and interested objects can register themselves to
	 * receive them.
	 * <p>
	 * A <tt>Note</tt> always contains a reference to the object that posted it
	 * and a type. The type is a <tt>String</tt> used by the <tt>NoteBoard</tt>
	 * to differentiate groups of <tt>Note</tt>s, to be able to notify
	 * interested <tt>NoteWatcher</tt> objects of <tt>Note</tt>s of a certain
	 * type only.
	 * <p>
	 * Next to this a <tt>Note</tt> can contain a <tt>Map</tt> containing
	 * arbitrary information. Naturally, this information should be standardized
	 * for different note types.
	 * 
	 * @version 1.0 Dec 18, 2007
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 * @see NoteBoard
	 * @see NoteWatcher
	 */
	public static final class Note{
		
		// the type of this note
		private final String type;

		// the object that posted the note
		private final Object source;

		// possible additional information
		private final Map<?, ?> info;
		
		private Note(String type, Object source, Map<?, ?> info){
			super();
			this.type = type;
			this.source = source;
			this.info = info;
		}

		/**
		 * Returns the additional information <tt>this Note</tt> provides; the
		 * contents depend on the type of <tt>Note</tt>. The returned
		 * <tt>Map</tt> is never <tt>null</tt>, if the <tt>Note</tt> does not
		 * provide additional information, <tt>Collections.EMPTY_MAP</tt> is
		 * returned.
		 * 
		 * @return The additional information <tt>this Note</tt> provides; the
		 *         contents depend on the type of <tt>Note</tt>.
		 */
		public Map<?, ?> getInfo(){
			return info == Collections.EMPTY_MAP ? info : new HashMap<Object, Object>(info);
		}

		/**
		 * Returns the type of <tt>this Note</tt>, which provides the
		 * possibility for <tt>NoteWatcher</tt>s to receive only <tt>Note</tt>s
		 * of a specific type, and not all that are posted.
		 * 
		 * @return The type of <tt>this Note</tt>.
		 */
		public String getType(){
			return type;
		}

		/**
		 * Returns a reference to the <tt>Object</tt> that this <tt>Note</tt>
		 * comes from; it's source.
		 * 
		 * @return A reference to the <tt>Object</tt> that this <tt>Note</tt>
		 *         comes from; it's source.
		 */
		public Object getSource(){
			return source;
		}
	}
}