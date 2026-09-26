package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

/**
 * A list of entries on the left, the selected entry's text (read only) on the right and actions
 * below: the shape of "Recently deleted" and "Version history".
 *
 * @param <T> what an entry stands for (a note id, a version time)
 */
class ListPreviewDialog<T> extends JDialog {

  /** One row: what it stands for and how it reads. */
  record Entry<T>(T key, String label) {
    @Override
    public String toString() {
      return label;
    }
  }

  private final DefaultListModel<Entry<T>> model = new DefaultListModel<>();
  private final JList<Entry<T>> list = new JList<>(model);
  private final JTextArea preview = new JTextArea();
  private final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
  private final List<FlatButton> selectionActions = new ArrayList<>();
  private final String emptyText;

  ListPreviewDialog(JFrame owner, String title, String emptyText, Function<T, String> text) {
    super(owner, title, false);
    this.emptyText = emptyText;
    UiPalette p = UiPalette.current();
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setBackground(p.chrome());
    list.setForeground(p.fg());
    list.setFont(UiFonts.ui(Font.PLAIN, 13f));
    list.setFixedCellHeight(28);
    list.addListSelectionListener(e -> showSelected(text));
    preview.setEditable(false);
    preview.setLineWrap(true);
    preview.setWrapStyleWord(true);
    preview.setBackground(p.bg());
    preview.setForeground(p.fg());
    preview.setFont(UiFonts.ui(Font.PLAIN, 13f));
    preview.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
    JSplitPane split =
        new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, TabPanes.content(list), TabPanes.content(preview));
    split.setDividerLocation(220);
    split.setBorder(BorderFactory.createEmptyBorder());
    buttons.setBackground(p.chrome());
    buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    getContentPane().setBackground(p.bg());
    add(split, BorderLayout.CENTER);
    add(buttons, BorderLayout.SOUTH);
    setSize(new Dimension(720, 460));
    Dialogs.bindEscape(this);
    WindowPlacement.centerOnOwner(this);
  }

  /** Replaces the rows; selects the first one. */
  void setEntries(List<Entry<T>> entries) {
    model.clear();
    entries.forEach(model::addElement);
    if (entries.isEmpty()) {
      preview.setText(emptyText);
      updateButtons();
    } else {
      list.setSelectedIndex(0);
    }
  }

  List<Entry<T>> entries() {
    List<Entry<T>> all = new ArrayList<>();
    for (int i = 0; i < model.size(); i++) {
      all.add(model.get(i));
    }
    return all;
  }

  /** A button acting on the selected entry; disabled while nothing is selected. */
  FlatButton addAction(String label, Consumer<T> action) {
    FlatButton b =
        addButton(
            label,
            () -> {
              Entry<T> selected = list.getSelectedValue();
              if (selected != null) {
                action.accept(selected.key());
              }
            });
    selectionActions.add(b);
    updateButtons();
    return b;
  }

  /** A button that does not need a selection. */
  FlatButton addButton(String label, Runnable action) {
    FlatButton b = new FlatButton(label, null, action);
    b.applyPalette(UiPalette.current());
    buttons.add(b);
    return b;
  }

  String previewText() {
    return preview.getText();
  }

  void select(int index) {
    list.setSelectedIndex(index);
  }

  private void showSelected(Function<T, String> text) {
    Entry<T> selected = list.getSelectedValue();
    preview.setText(selected == null ? emptyText : text.apply(selected.key()));
    preview.setCaretPosition(0);
    updateButtons();
  }

  private void updateButtons() {
    boolean any = list.getSelectedValue() != null;
    selectionActions.forEach(b -> b.setEnabled(any));
  }
}
