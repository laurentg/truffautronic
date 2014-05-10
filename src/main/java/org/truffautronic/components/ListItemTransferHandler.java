/*
 * Truffautronic
 * Copyright (c) 2014 Laurent GRÉGOIRE <laurent.gregoire@gmail.com>
 * & Lycée Général et Technologique François Truffaut - Challans
 *
 * Truffautronic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Truffautronic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Truffautronic.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.truffautronic.components;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class ListItemTransferHandler<T> extends TransferHandler {
	private static final long serialVersionUID = 1L;

	private final DataFlavor localObjectFlavor;
	private Object[] transferedObjects = null;
	private int[] indices = null;
	private int addIndex = -1;
	private int addCount = 0;

	public ListItemTransferHandler() {
		localObjectFlavor = new ActivationDataFlavor(Object[].class,
				DataFlavor.javaJVMLocalObjectMimeType, "ListItemArray");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Transferable createTransferable(JComponent c) {
		if (!(c instanceof JList))
			throw new RuntimeException("Transferrable must be a JList: "
					+ c.getClass());
		@SuppressWarnings("unchecked")
		JList<T> list = (JList<T>) c;
		indices = list.getSelectedIndices();
		transferedObjects = list.getSelectedValues();
		return new DataHandler(transferedObjects,
				localObjectFlavor.getMimeType());
	}

	@Override
	public boolean canImport(TransferSupport info) {
		if (!info.isDrop() || !info.isDataFlavorSupported(localObjectFlavor)) {
			return false;
		}
		return true;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return MOVE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferSupport info) {
		if (!canImport(info)) {
			return false;
		}
		JList<T> target = (JList<T>) info.getComponent();
		JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
		DefaultListModel<T> listModel = (DefaultListModel<T>) target.getModel();
		int index = dl.getIndex();
		int max = listModel.getSize();
		if (index < 0 || index > max) {
			index = max;
		}
		addIndex = index;
		try {
			Object[] values = (Object[]) info.getTransferable()
					.getTransferData(localObjectFlavor);
			addCount = values.length;
			for (int i = 0; i < values.length; i++) {
				int idx = index++;
				listModel.add(idx, (T) values[i]);
				target.addSelectionInterval(idx, idx);
			}
			return true;
		} catch (IOException | UnsupportedFlavorException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
		cleanup(c, action == MOVE);
	}

	private void cleanup(JComponent c, boolean remove) {
		if (remove && indices != null) {
			@SuppressWarnings("unchecked")
			JList<T> source = (JList<T>) c;
			DefaultListModel<T> model = (DefaultListModel<T>) source.getModel();
			if (addCount > 0) {
				for (int i = 0; i < indices.length; i++) {
					if (indices[i] >= addIndex) {
						indices[i] += addCount;
					}
				}
			}
			for (int i = indices.length - 1; i >= 0; i--) {
				model.remove(indices[i]);
			}
		}
		indices = null;
		addCount = 0;
		addIndex = -1;
	}
}
