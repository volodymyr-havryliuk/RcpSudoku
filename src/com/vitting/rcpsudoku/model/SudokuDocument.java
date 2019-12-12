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

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Persistent infoemation for saving a Sudoku game
 * 
 */
public class SudokuDocument implements ISudokuDokument {

	private File savefile;

	Document document;

	/**
	 * @param f
	 *            file to use for save
	 */
	public SudokuDocument(File f) {
		savefile = f;
	}

	/**
	 * Load the base
	 * 
	 * @param base
	 * @throws SudokuException
	 */
	public void load(SudokuBase base) throws SudokuException {

		int row = 0;
		int column = 0;

        validateCanReadSaveFile();

        // Clear the base
		base.clear(true);

		try {
			getParsedDocument();
			// Validate the document
			Node documentElement = document.getDocumentElement();
			if (documentElement.getNodeName().equals(SUDOKU) == false) {
				throw new SudokuException(" Incorrect file format",
						SudokuException.SEVERITY_ERROR,
						SudokuException.DISPOSITION_CONTINUE);
			}
			NamedNodeMap bsdAttributes = documentElement.getAttributes();
			Node version = bsdAttributes.getNamedItem(DOCUMENT_VERSION);
			if (version == null) {
				throw new SudokuException("File descriptor missing",
						SudokuException.SEVERITY_ERROR,
						SudokuException.DISPOSITION_CONTINUE);
			}
			// Only version 1.0 supported
			if (version.getNodeValue().compareTo(CURRENT_DOCUMENT_VERSION) != 0) {
				throw new SudokuException(
						"Unsupported File Descriptor file version"
								+ version.getNodeValue(),
								SudokuException.SEVERITY_INFORMATION,
								SudokuException.DISPOSITION_CONTINUE);
								
			}

			// Parse the root Node and the only child
			NodeList rootNodes = documentElement.getChildNodes();
			if (rootNodes.getLength() != 1) {
				throw new SudokuException("Invalid File format",
						SudokuException.SEVERITY_WARNING,
						SudokuException.DISPOSITION_CONTINUE);
			}
			Node game = rootNodes.item(0);
			NodeList cells = game.getChildNodes();
			for (int i = 0; i < cells.getLength(); i++) {
				if ((cells.item(i).getNodeType() == Node.ELEMENT_NODE)
						&& (cells.item(i).getNodeName().compareTo(CELL) == 0)) {
					NamedNodeMap cellAttributes = cells.item(i).getAttributes();
					Node node = cellAttributes.getNamedItem(ROW);
					if (node != null) {
						row = Integer.parseInt(node.getNodeValue());
					}
					node = cellAttributes.getNamedItem(COLUMN);
					if (node != null) {
						column = Integer.parseInt(node.getNodeValue());
					}
					MCell cell = base.getCell(row, column);
					node = cellAttributes.getNamedItem(VALUE);
					if (node != null) {

						// Convert the string to a BitSet
						String stringValue = node.getNodeValue();
						BitSet value = new BitSet();
						if (stringValue.length() < 9) {
							for (int j = 0; j < stringValue.length(); j++) {
								int x = Character.getNumericValue(stringValue
										.charAt(j));
								if ((x > 0) && (x < 10)) {
									value.set(x - 1);
								}
							}
						} else {
							// The cell is empty
							value.set(0, 9);
						}
						cell.setValue(value);
					}
					node = cellAttributes.getNamedItem(STATUS);
					if (node != null) {
						cell.setInitialValue(node.getNodeValue().equals(
								STATUS_INITIAL));
					}
				}
			}

		} catch (SAXException sxe) {
			// Error generated during parsing
			throw new SudokuException(
					"SAXException during passing of BackupSet Descriptor Document",	sxe, 
					SudokuException.SEVERITY_ERROR,
					SudokuException.DISPOSITION_CONTINUE);
		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			throw new SudokuException(
					"Error Loading BackupSet Descriptor Document", pce,
					SudokuException.SEVERITY_ERROR,
					SudokuException.DISPOSITION_CONTINUE);
		} catch (IOException ioe) {
			// IO Error
			throw new SudokuException(
					"IOException during passing BackupSet Descriptor Document", ioe, 
					SudokuException.SEVERITY_ERROR,
					SudokuException.DISPOSITION_CONTINUE);
		}
		// Notify all cells
		base.cellsChanged(true);
	}

	private void getParsedDocument() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(getErrorHandler());
		document = builder.parse(savefile);
	}

	private void validateCanReadSaveFile() throws SudokuException {
        if (savefile.canRead() == false) {
            throw new SudokuException("Document cound not be found: "
                    + savefile.getAbsolutePath(),
                    SudokuException.DISPOSITION_CONTINUE,
                    SudokuException.SEVERITY_WARNING);
        }
    }

    private ErrorHandler getErrorHandler() {
		return new ErrorHandler() {

			// Treat warnings as fatal
			public void warning(SAXParseException exception)
					throws SAXException {
				throw exception;
			}

			// Treat warnings as fatal
			public void error(SAXParseException exception)
					throws SAXException {
				throw exception;
			}

			public void fatalError(SAXParseException exception)
					throws SAXException {
				// Ignore fatal errors (an exception is guarantied)
			}

		};
	}

	/**
	 * Save the base
	 * 
	 * @param base
	 * @throws SudokuException
	 */
	public void save(SudokuBase base) throws SudokuException {

		// Create the descriptor file
		Element game;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
			document.appendChild(document.createComment(DOCUMENT_COMMENT + " "
					+ new Date()));
			Element root = (Element) document.createElement(SUDOKU);
			document.appendChild(root);
			root.setAttribute(DOCUMENT_VERSION, CURRENT_DOCUMENT_VERSION);
			game = (Element) document.createElement(GAME);
			root.appendChild(game);
		} catch (ParserConfigurationException pce) {
			throw new SudokuException("Error Creating Initial Document", pce, 
					SudokuException.SEVERITY_ERROR,
					SudokuException.DISPOSITION_CONTINUE);
		}

		// Write the game data
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				MCell cell = base.getCell(x, y);
				// Dont save empty cells
				if (cell.isEmpty() == false) {
					Element cellElement = document.createElement(CELL);
					cellElement.setAttribute(ROW, Integer.toString(x));
					cellElement.setAttribute(COLUMN, Integer.toString(y));

					// Convert cell value to a string
					String stringValue = "";
					BitSet value = cell.getValue();
					for (int i = value.nextSetBit(0); i >= 0; i = value
							.nextSetBit(i + 1)) {
						stringValue = stringValue + (i + 1);
					}
					cellElement.setAttribute(VALUE, stringValue);
					cellElement.setAttribute(STATUS,
							cell.isInitialValue() ? STATUS_INITIAL
									: STATUS_CALCULATED);
					game.appendChild(cellElement);
				}
			}
		}

		// Use a Transformer for output
		TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(savefile);
			transformer.transform(source, result);
		} catch (Exception e) {
			throw new SudokuException(
					"Failed to write BackupSet descriptor document", e, 
					SudokuException.SEVERITY_ERROR,
					SudokuException.DISPOSITION_CONTINUE);
		}
	}
}
