/*
 * Copyright (c) 2005 Henning Vitting and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Henning Vitting - Initial API and implementation
 *   
  */
package com.vitting.rcpsudoku.jfc.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import com.vitting.rcpsudoku.jfc.MainWindow;
import com.vitting.rcpsudoku.jfc.ModelDirtyWarning;
import com.vitting.rcpsudoku.model.SudokuBase;



/**
 *
 * Handle New Action
 */
public class NewAction extends JMenuItem implements ActionListener, IAction{

	private static final long serialVersionUID = 1L;
	MainWindow window;
	SudokuBase base;
	/**
	 * Constructor
	 * 
	 * @param window MainWindow 
	 */
	public NewAction(MainWindow window) {
		this.window = window;
		base = SudokuBase.getSingleInstance();
		setText("New");
		addActionListener(this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	    	// Check for unsaved data
	    	if (base.isModelDirty()) {
	    	    if (new ModelDirtyWarning(window).show()) {
	    		boolean result = new SaveAction(window).save();
	    		if (result == false) {
	    		    return;
	    		}
	    	    }
	    	}
	    	
		base.clear(true);
		window.setTitle(MainWindow.windowTitle);
		window.setMessage("Ready for a new game");
		window.setEditMode(true);
		window.refresh();
	}
	
	/* (non-Javadoc)
	 * @see com.vitting.rcpsudoku.jfc.actions.IAction#controlEnabled()
	 */
	public void controlEnabled() {
		// The menu is enabled when the model is loaded
		setEnabled(base.isLoaded());
	}

}
