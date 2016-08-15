/*
 * Copyright (c) 2016 Gábor Kövesdán
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.kovesdan.minihelp;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to manage history of any type.
 * 
 * @author Gábor Kövesdán
 *
 * @param <E>
 *            the type of the objects that represent the items in the history.
 */
public class HistoryManager<E> {
	private List<E> history = new ArrayList<>();
	private int position = -1;

	/**
	 * Returns the previous item and decreases the history position.
	 * 
	 * @return the previous item to the current one or null if that is not
	 *         available.
	 */
	public E back() {
		return (position > 0) ? history.get(--position) : null;
	}

	/**
	 * Returns the next item and increases the history position.
	 * 
	 * @return the next item to the current one or null if that is not
	 *         available.
	 */
	public E forward() {
		return (history.size() > position + 1) ? history.get(++position) : null;
	}

	/**
	 * Returns the current item without changing the position.
	 * 
	 * @return the current item.
	 */
	public E current() {
		return history.get(position);
	}

	/**
	 * Updates the history when a new item is opened.
	 */
	public void navigatedTo(E e) {
		history = new ArrayList<>(history.subList(0, position + 1));
		history.add(e);
		position++;
	}

	/**
	 * Returns whether it is possible to navigate back.
	 * 
	 * @return whether navigating back is possible.
	 */
	public boolean isBackActive() {
		return (position > 0) && (history.get(position - 1) != null);
	}

	/**
	 * Returns whether it is possible to navigate forward.
	 * 
	 * @return whether navigating forward is possible.
	 */
	public boolean isForwardActive() {
		return (history.size() > position + 1) && (history.get(position + 1) != null);
	}
}
