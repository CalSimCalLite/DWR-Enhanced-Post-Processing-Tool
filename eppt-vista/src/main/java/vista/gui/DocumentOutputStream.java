/*
 * Enhanced Post Processing Tool (EPPT) Copyright (c) 2019.
 *
 * EPPT is copyrighted by the State of California, Department of Water Resources. It is licensed
 * under the GNU General Public License, version 2. This means it can be
 * copied, distributed, and modified freely, but you may not restrict others
 * in their ability to copy, distribute, and modify it. See the license below
 * for more details.
 *
 * GNU General Public License
 */
package vista.gui;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * An OutputStream implementation that places it's output in a swing text model
 * (Document). The Document can be either a plain text or styled document
 * implementation. If styled, the attributes assigned to the output stream will
 * be used in the display of the output.
 *
 * @author Timothy Prinzing
 * @version 1.1 02/05/99
 */
public class DocumentOutputStream extends OutputStream
{

	private byte[] one = new byte[1];
	private Document doc;
	private AttributeSet a;

	/**
	 * Constructs an output stream that will output to the given document with
	 * the given set of character attributes.
	 *
	 * @param doc the document to write to.
	 * @param a   the character attributes to use for the written text.
	 */
	public DocumentOutputStream(Document doc, AttributeSet a)
	{
		this.doc = doc;
		this.a = a;
	}

	/**
	 * Constructs an output stream that will output to the given document with
	 * whatever the default attributes are.
	 *
	 * @param doc the document to write to.
	 */
	public DocumentOutputStream(Document doc)
	{
		this(doc, null);
	}

	/**
	 * Writes the specified byte to this output stream.
	 * <p>
	 * Subclasses of <code>OutputStream</code> must provide an implementation
	 * for this method.
	 *
	 * @param b the <code>byte</code>.
	 * @throws IOException if an I/O error occurs.
	 * @since JDK1.0
	 */
	public void write(int b) throws IOException
	{
		one[0] = (byte) b;
		write(one, 0, 1);
	}

	/**
	 * Writes <code>len</code> bytes from the specified byte array starting at
	 * offset <code>off</code> to this output stream.
	 * <p>
	 * The <code>write</code> method of <code>OutputStream</code> calls the
	 * write method of one argument on each of the bytes to be written out.
	 * Subclasses are encouraged to override this method and provide a more
	 * efficient implementation.
	 *
	 * @param b   the data.
	 * @param off the start offset in the data.
	 * @param len the number of bytes to write.
	 * @throws IOException if an I/O error occurs.
	 * @since JDK1.0
	 */
	public void write(byte[] b, int off, int len) throws IOException
	{
		try
		{
			doc.insertString(doc.getLength(), new String(b, off, len), a);
		}
		catch(BadLocationException ble)
		{
			throw new IOException(ble.getMessage());
		}
	}
}
