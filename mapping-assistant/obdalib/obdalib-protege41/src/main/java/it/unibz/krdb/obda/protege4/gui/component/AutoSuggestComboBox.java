package it.unibz.krdb.obda.protege4.gui.component;

import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class AutoSuggestComboBox extends JComboBox {

	private static final long serialVersionUID = 1L;

	private Vector<PredicateItem> items;

	private boolean hide_flag = false;

	public AutoSuggestComboBox(Vector<PredicateItem> items) {
		super(items);
		this.items = items;
		setEditable(true);
		setSelectedIndex(-1);
		overrideEditorBehavior();
	}

	public void overrideEditorBehavior() {
		if (getEditor().getEditorComponent() instanceof JTextField) {
			final JTextField tf = (JTextField) getEditor().getEditorComponent();
			tf.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							String text = tf.getText();
							if (text.length() == 0) {
								hidePopup();
								setModel(new DefaultComboBoxModel(items), "");
							} else {
								DefaultComboBoxModel m = getSuggestedModel(items, text);
								if (m.getSize() == 0 || hide_flag) {
									hidePopup();
									hide_flag = false;
								} else {
									setModel(m, text);
									showPopup();
								}
							}
							tf.setText(text);
						}
					});
				}
				@Override
				public void keyPressed(KeyEvent e) {
					String text = tf.getText();
					int code = e.getKeyCode();
					if (code == KeyEvent.VK_ESCAPE) {
						hide_flag = true;
					} else if (code == KeyEvent.VK_RIGHT) {
						for (int i = 0; i < items.size(); i++) {
							PredicateItem pred = items.elementAt(i);
							if (pred.getQualifiedName().startsWith(text)) {
								setSelectedIndex(-1);
								tf.setText(pred.getQualifiedName());
								return;
							}
						}
					}
				}
			});
		}
	}

	private void setModel(DefaultComboBoxModel mdl, String str) {
		setModel(mdl);
		setSelectedIndex(-1);
	}

	private static DefaultComboBoxModel getSuggestedModel(List<PredicateItem> list, String searchedText) {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		for (PredicateItem pred : list) {
			if (pred.getQualifiedName().startsWith(searchedText)) {
				m.addElement(pred);
			}
		}
		return m;
	}
}
