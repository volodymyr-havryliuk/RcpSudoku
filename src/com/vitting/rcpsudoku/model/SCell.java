/**
 * Copyright (c) 2005, 2006 Henning Vitting and others.
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
package com.vitting.rcpsudoku.model;

import java.util.BitSet;
import com.vitting.rcpsudoku.model.MCell;
import com.vitting.rcpsudoku.model.Coordinate;

/**
         * 
         * SCell (SolverCell) holds solver information for a Sudoku cell
         */
    final public class SCell {
		MCell cell;

		private BitSet originalValue;

		int index;

		/**
			* Constructor
			* 
			* @param cell
			*                MCell - the original game cell
			*/
		SCell(MCell cell) {
			this.cell = cell;
			originalValue = (BitSet) cell.getContent().getValue().clone();
			index = 0;
		}

		/**
			* Try the next possible solution for the cell
			* 
			* @return boolean - true if move possible
			*/
		public boolean moveForward() {
			int nextTry = originalValue.nextSetBit(index);
			if (nextTry == -1) {
				// No more possibilities
				return false;
			}
			cell.setValue(nextTry);
			index = nextTry + 1;
			return true;
		}

		/**
			* restore the cell content to its original value
			*/
		public void restore() {
			cell.setValue((BitSet) originalValue.clone());
			index = 0;
		}

		/**
			* @return MCell - the MCell reference
			*/
		public MCell getCell() {
			return cell;
		}
    }
