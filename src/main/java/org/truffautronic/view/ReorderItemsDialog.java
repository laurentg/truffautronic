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

package org.truffautronic.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.truffautronic.components.ListItemTransferHandler;

public class ReorderItemsDialog<T> extends JDialog {
	private static final long serialVersionUID = 1L;

	private DefaultListModel<T> listModel;
	private List<T> list;

	public ReorderItemsDialog(String header, Frame frame, List<T> aList) {
		super(frame, header);

		this.list = aList;
		listModel = new DefaultListModel<T>();
		for (T t : list)
			listModel.addElement(t);

		// Center: reorder JList
		JList<T> jList = new JList<T>(listModel);
		add(jList, BorderLayout.CENTER);
		jList.setDropMode(DropMode.INSERT);
		jList.setDragEnabled(true);
		jList.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		jList.setTransferHandler(new ListItemTransferHandler<T>());

		// Bottom: buttons
		JPanel buttonsPanel = new JPanel(new FlowLayout());
		add(buttonsPanel, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		buttonsPanel.add(okButton);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				list = new ArrayList<T>();
				for (int i = 0; i < listModel.size(); i++)
					list.add(listModel.get(i));
				setVisible(false);
			}
		});
		JButton cancelButton = new JButton("Cancel");
		buttonsPanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				list = null;
				setVisible(false);
			}
		});

		setPreferredSize(new Dimension(300, 400));
		setModal(true);
		pack();
	}

	public List<T> reorder() {
		setVisible(true);
		return list;
	}
}
