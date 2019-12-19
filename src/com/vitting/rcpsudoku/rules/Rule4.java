package com.vitting.rcpsudoku.rules;

import java.util.BitSet;
import java.util.Vector;

import com.vitting.rcpsudoku.model.IRule;
import com.vitting.rcpsudoku.model.MCell;
import com.vitting.rcpsudoku.model.SudokuBase;
import com.vitting.rcpsudoku.model.SudokuException;

/**
 * Rule 4, Some Sudoku games, normally catagorized as extremly difficult, cannot
 * be solved using the simple rules. Rule 4 uses an Ariadne's thread like
 * algoritm to traverse the remaining unsolved cells. <br>
 * For each step in the thread, rules 1 - 3 are used again.
 */
final public class Rule4 {

    static SudokuBase base = SudokuBase.getSingleInstance();

	public int run() throws SudokuException {
		Vector unsolved = getVector();
	    // Create the first Rule4Step and run it
	    SCell[] sCells = new SCell[unsolved.size()];
	    for (int i = 0; i < sCells.length; i++) {
		sCells[i] = new SCell((MCell) unsolved.elementAt(i));
	    }
	    return new Rule4Step(sCells).runStep();
	}

	private Vector getVector() {
		Vector unsolved = new Vector(); // A Vector of unsolved MCells

		// Build the unsolved Vector
		for (int x = 0; x < 9; x++) {
		for (int y = 0; y < 9; y++) {
			MCell cell = base.getCell(x, y);
			if (cell.isValueFound() == false) {
			unsolved.add(cell);
			}
		}
		}
		return unsolved;
	}

	final public class Rule4Step {
	// The unsolved cells remaining for this step
	SCell[] cells;

	// The first cell
	SCell cell;

	int count;


	public Rule4Step(SCell[] cells) {
	    this.cells = cells;
	    // This step handles the first SCell in the array
	    cell = cells[0];
	}

	public int runStep() throws SudokuException {
	    // SCell move forward inserts the value in the MCell
	    while (cell.moveForward()) {

		// Test with rules 1 - 3
		int result = RuleRunner.internalrun();
		switch (result) {
		    case IRule.RULE_GAME_COMPLETE:
//		    { // DEBUG -- Rule4 GAME_COMPLETE
//			System.out
//				.println("     Rule4Test result: RULE_GAME_COMPLETE");
//		    }
			return result;
		    case IRule.RULE_NOT_POSSIBLE:
//		    { // DEBUG -- Rule4 NOT_POSSIBLE
//			System.out
//				.println("     Rule4Test result: RULE_NOT_POSSIBLE");
//		    }
			// The previous run had no possible result, restore the
			// cells
			// to cleanup the partial results from the run
			for (int j = 1; j < cells.length; j++) {
			    cells[j].restore();
			}
			break;
		    case IRule.RULE_NO_CHANGE:
//		    { // DEBUG -- Rule4 NO_CHANGE
//			System.out
//				.println("     Rule4Test result: RULE_NO_CHANGE");
//		    }
			// Nothing so far continue
			if (cells.length > 1) {
			    // Call next rule4step for the next cell in the
			    // unsolved array
			    SCell[] sCells = new SCell[cells.length - 1];
			    for (int j = 1; j < cells.length; j++) {
				// Pick up the intermediate results
				sCells[j - 1] = new SCell(cells[j].getCell());
			    }
			    int stepResult = new Rule4Step(sCells).runStep();
			    // debug
			    System.out.println("Nested step returned: "
				    + stepResult);
			    switch (stepResult) {
				case IRule.RULE_GAME_COMPLETE:
				    return stepResult;
				case IRule.RULE_NOT_POSSIBLE:
				    break;
				// cell.restore();
				// return stepResult;
				case IRule.RULE_NO_CHANGE:
				    break;
				// cell.restore();
				// return IRule.RULE_NO_CHANGE;
			    }
			}
		}
	    }

	    // No result found, restore the cell
	    cell.restore();
	    return IRule.RULE_NOT_POSSIBLE;
	}
    }

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
//	    { // DEBUG -- Rule 4 moveForward
//		System.out.println("(" + cell.getRow() + "." + cell.getColumn()
//		    + ") " + originalValue + " Next bit: " + nextTry);
//	    }
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
}
