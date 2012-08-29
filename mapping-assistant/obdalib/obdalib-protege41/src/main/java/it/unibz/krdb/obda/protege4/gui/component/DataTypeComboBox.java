package it.unibz.krdb.obda.protege4.gui.component;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.protege4.gui.IconLoader;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class DataTypeComboBox extends JComboBox {

	private static final long serialVersionUID = 1L;

	public DataTypeComboBox() {
		super(OBDAVocabulary.QUEST_DATATYPE_PREDICATES);
		setRenderer(new DataTypeRenderer());
		setPreferredSize(new Dimension(120, 23));
	}
	
	class DataTypeRenderer extends BasicComboBoxRenderer {
		
		private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (index == -1) {
				setText("<Define data type>");
			}
			
			if (value != null) {
				if (value instanceof Predicate) {
					Predicate item = (Predicate) value;
					String name = item.toString();
					if (name.contains(OBDAVocabulary.NS_XSD)) {
						name = name.replace(OBDAVocabulary.NS_XSD, "xsd:");
					} else if (name.contains(OBDAVocabulary.NS_RDFS)) {
						name = name.replace(OBDAVocabulary.NS_RDFS, "rdfs:");
					}
					setText(name);
					setIcon(IconLoader.getImageIcon("images/datarange.png"));
				}
			}
			return this;
		}
	}
}
