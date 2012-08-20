package it.unibz.krdb.obda.protege4.gui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class SQLResultTable extends JTable {

	private static final long serialVersionUID = 1L;

	public SQLResultTable() {
		super();
		setAutoscrolls(false);
		setColumnSelectionAllowed(true);
		setCellSelectionEnabled(true);
		setDefaultRenderer(String.class, new ColumnHighlightRenderer());
		
		setPreferredScrollableViewportSize(getPreferredSize());
		setIntercellSpacing(new Dimension(1, 1));
		
		JTableHeader tableHeader = getTableHeader();
		tableHeader.setReorderingAllowed(false);
		tableHeader.addMouseListener(new ColumnHeaderAdapter(this));
	}

	/**
	 * An adapter class to capture the column index when user selects the table header
	 */
	class ColumnHeaderAdapter extends MouseAdapter {
		
		private JTable table;

		public ColumnHeaderAdapter(JTable table) {
			this.table = table;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			JTableHeader header = table.getTableHeader();
			int index = header.columnAtPoint(e.getPoint());
			ColumnHighlightRenderer renderer = (ColumnHighlightRenderer) table.getDefaultRenderer(String.class);
			renderer.setColumn(index);
			table.repaint();
		}
	}

	/**
	 * A custom cell renderer to highlight the entire column cells if user selects the table header
	 */
	class ColumnHighlightRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		
		private int selectColumn = -1;
		private int selectRow = -1;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			if (col == selectColumn || row == selectRow) {
				setBackground(Color.lightGray);
			} else if (isSelected) {
				setBackground(table.getSelectionBackground());
			} else {
				setBackground(null);
			}
			return this;
		}

		public void setColumn(int index) {
			if (selectColumn == index) {
				selectColumn = -1;
			} else {
				selectColumn = index;
			}
		}
	}
}
