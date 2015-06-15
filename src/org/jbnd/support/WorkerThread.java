package org.jbnd.support;

import java.util.*;
import java.util.concurrent.Callable;


/**
 * A <tt>Thread</tt> made to facilitate doing asynchronous work. Main methods of
 * the class are {@link #invoke(Runnable)} and {@link #waitTillDone()}. Those
 * two methods should be capable of handling most scenarios.
 * <p>
 * The <tt>Thread</tt> itself is by a <tt>MAX_PRIORITY</tt> daemon, but if it
 * has no work it waits, so in most cases it will be appropriate keeping only
 * one instance around. Essentially it works as a 'tasks' queue, one can pile up
 * as many tasks as desired, and they will be performed in the same sequence in
 * which they were added.
 * <p>
 * It is legal that code executing on a <tt>WorkerThread</tt> schedules a tasks
 * on the same <tt>WorkerThread</tt>, the task will in that case be added to the
 * queue and executed when it's turn comes.
 * <p>
 * A typical usage example is multithreaded data loading and display. A
 * <tt>WorkerThread</tt> can be used to obtain the data, after which that data
 * can be loaded into the GUI using another <tt>Thread</tt>, while the
 * <tt>WorkerThread</tt> continues to load another data set. Another example
 * would be using the <tt>WorkerThread</tt> in a Swing application, to off-load
 * all non-Swing-specific work off the Event Dispatch Thread, and switch back to
 * the EDT for presenting the results in the GUI.
 * 
 * @version 1.0 Oct 8, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class WorkerThread extends Thread{
	
	/**
	 * Just some testing, do not call this method.
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String args[]) throws InterruptedException{
		
		final WorkerThread t = new WorkerThread();
		t.start();
		
		// some testing
		for(int i = 0 ; i < 5 ; i++){
			final Integer integer = new Integer(i);
			System.out.println("add() "+integer);
			t.invoke(new Runnable(){
				public void run() {
					
					System.out.println("call() "+integer);
					
					if(integer == 1){
						System.out.println("embedded add()");
						t.invoke(new Runnable(){
							public void run() {
								System.out.println("embedded call()");
								try{
									Thread.sleep(2500l);
								}catch(InterruptedException e){
									e.printStackTrace();
								}
								System.out.println("embedded finished()");
							}
						});
					}
					
					try{
						Thread.sleep(2500l);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
					System.out.println("finished() "+integer);
				}
			});
			Thread.sleep(1000);
		}
		
		System.out.println("Will wait till done");
		t.waitTillDone();
		System.out.println("Done!");
	}

	/*
	 * Used in the waitTillDone() method, to determine when there are tasks
	 * to be executed, or being executed.
	 */
	private final DoneSwitch doneSwitch = new DoneSwitch();
	
	/*
	 * Task queue. Contains Runnable and Callable instances.
	 */
	private final List<Object> tasks = new LinkedList<Object>();
	
	private final Map<Callable<?>, Object> callableResults = new HashMap<Callable<?>, Object>();
	
	/*
	 * Initializes the settings for WorkerThreads being created.
	 */
	{
		setPriority(Thread.MAX_PRIORITY);
		setDaemon(true);
		setName("JBND worker thread");
	}
	
	/**
	 * The main run-loop of a <tt>WorkerThread</tt>.
	 */
	public final void run(){
		
		List<?> toExecute = null;
		
		while(true){

			// obtain and copy the tasks list synchronously.
			synchronized(this){
				
				toExecute = new ArrayList<Object>(tasks);
				tasks.clear();
				
				// if there were no tasks, wait till one comes along.
				if(toExecute.isEmpty())
					try{
						wait();
					}catch(InterruptedException e){
						continue;
					}
			}
			
			// If there were tasks, execute them, but asynchronously.
			for(Object e : toExecute)
				try{
					if(e instanceof Runnable)
						((Runnable)e).run();
					else{
						Callable<?> c = (Callable<?>)e;
						try{
							callableResults.put(c, c.call());
						}catch(Exception ex){
							callableResults.put(c, ex);
						}
					}
				}finally{
					doneSwitch.decrement();
				}
		}
	}
	
	
	
	/**
	 * The method to be called from any <tt>Thread</tt>, that will result in
	 * running the the passed <tt>Runnable</tt> on this <tt>WorkerThread</tt>.
	 * This method will returns immediately.
	 * 
	 * @param r The <tt>Runnable</tt> whose <tt>run()</tt> method will be
	 *            performed on this <tt>WorkerThread</tt>.
	 */
	public synchronized void invoke(Runnable r){
		
		if(!isAlive())
			throw new IllegalStateException(
					"invoke(...) can only be called on a WorkerThread that is alive");
		
		synchronized(doneSwitch){ 
			
			// increment the done switch
			doneSwitch.lockCount++; 

			// add a Task to the stack
			tasks.add(r);

			// notify, as this WorkerThread may be waiting
			notifyAll();
		}
	}
	
	/**
	 * Adds the given <tt>Callable</tt> to the execution queue, to be executed
	 * when the <tt>WorkerThread</tt> is able to do so; this method will not
	 * return until that happens.
	 * 
	 * @param <T> The type of <tt>Callable</tt> and of the return value.
	 * @param c The <tt>Callable</tt> to execute.
	 * @return The value returned from the <tt>Callable</tt>'s <tt>call()</tt>
	 *         method.
	 * @throws Exception The exception that the <tt>Callable</tt> possibly
	 *             throws.
	 */
	public <T> T invoke(Callable<T> c) throws Exception{
		
		synchronized(this){
			if(!isAlive())
				throw new IllegalStateException(
				"invoke(...) can only be called on a WorkerThread that is alive");

			synchronized(doneSwitch){ 

				// increment the done switch
				doneSwitch.lockCount++; 

				// add a Task to the stack
				tasks.add(c);

				// notify, as this WorkerThread may be waiting
				notifyAll();
			}
		}
		
		// wait till the computation of the return value is done
		waitTillDone();
		
		// return the computed value
		Object rVal = callableResults.remove(c);
		if(rVal instanceof Exception)
			throw (Exception)rVal;
		
		@SuppressWarnings("unchecked")	// safe to suppress
		T rValue = (T)rVal;
		return rValue;
	}
	
	/**
	 * Does the same thing as {@link #invoke(Callable)}, but catches the checked
	 * exception possibly thrown, wraps it into an unchecked exception and
	 * throws that one.
	 * 
	 * @param <T> The type of <tt>Callable</tt> and of the return value.
	 * @param c The <tt>Callable</tt> to execute.
	 * @return The value returned from the <tt>Callable</tt>'s <tt>call()</tt>
	 *         method.
	 * @throws RuntimeException If the <tt>call()</tt> method of the
	 *             <tt>Callable</tt> throws a checked exception. The thrown
	 *             exception has the checked exception as it's cause.
	 */
	public <T> T invokeUnchecked(Callable<T> c){
		try{
			return invoke(c);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * A call to this method will block the calling <tt>Thread</tt> (which can
	 * be any <tt>Thread</tt> other then <tt>this</tt>) until all the work of
	 * this <tt>WorkerThread</tt> is done. If there are multiple
	 * <tt>Thread</tt>s loading work on this single <tt>WorkerThread</tt> this
	 * method will block until all the currently scheduled work is done,
	 * regardless of it being loaded by <tt>Thread</tt>s other then this one.
	 * 
	 * @throws IllegalStateException If this method is called on the currently
	 *             executing <tt>WorkerThread</tt>.
	 */
	public void waitTillDone(){
		
		if(currentThread() == this)
			throw new IllegalStateException(
					"Can not call waitTillDone() on Thread.currentThread()");
		
		while(true){
		
			synchronized(doneSwitch){
				
				// if the done switch says that 
				if(doneSwitch.lockCount != 0)
					try{
						doneSwitch.wait();
					}catch(InterruptedException e){
						continue;
					}
			}
			
			// monitor was notified, get out of here
			return;
		}
	}
	
	/**
	 * Without blocking this method checks if there are tasks to be performed,
	 * or are being performed by this <tt>WorkerThread</tt>.
	 * 
	 * @return <tt>true</tt> if this <tt>WorkerThread</tt> is idle (not doing
	 *         anything), <tt>false</tt> otherwise.
	 */
	public boolean isIdle(){
		synchronized(doneSwitch){
			return doneSwitch.lockCount == 0;
		}
	}
	
	
	/**
	 * A mutable encapsulation of an int, used to keep count of how many locks
	 * are obtained, and also as a monitor for synchronization of that same
	 * increment. Defined to be used by the {@link WorkerThread#waitTillDone()}
	 * method.
	 * 
	 * @version 1.0 Oct 8, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	private static class DoneSwitch{
		private int lockCount = 0;
		
		/**
		 * Decrements the lock count. If that causes the
		 * count to hit zero, then notifyAll() is called
		 * on this instance of <tt>DoneSwitch</tt>.
		 */
		private synchronized void decrement(){
			lockCount--;
			if(lockCount == 0) notifyAll();
		}
	}
}