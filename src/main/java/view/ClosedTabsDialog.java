package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import model.TabMeta;

class ClosedTabsDialog extends JDialog {

  private static final Dimension SIZE = new Dimension(320, 360);
  private static final DateTimeFormatter FMT =
      DateTimeFormatter.ofPattern("d MMM HH-mm", Locale.ENGLISH);

  private final DefaultListModel<TabMeta> model = new DefaultListModel<>();
  private final JList<TabMeta> list = new JList<>(model);
  private final Consumer<TabMeta> onPick;
  private final Consumer<TabMeta> onDelete;

  ClosedTabsDialog(
      JFrame owner, List<TabMeta> closed, Consumer<TabMeta> onPick, Consumer<TabMeta> onDelete) {
    super(owner, "Closed tabs", false);
    this.onPick = onPick;
    this.onDelete = onDelete;
    closed.stream()
        .sorted(Comparator.comparing(TabMeta::lastModified).reversed())
        .forEach(model::addElement);
    list.setVisibleRowCount(12);
    list.setCellRenderer(new MetaRenderer());
    list.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
              pickSelected();
            }
          }
        });
    list.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            handleKey(e);
          }
        });
    getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
    setSize(SIZE);
    WindowPlacement.centerOnOwner(this);
    setAlwaysOnTop(true);
    bindCloseShortcut();
  }

  private void handleKey(KeyEvent e) {
    int code = e.getKeyCode();
    if (code == KeyEvent.VK_ENTER) {
      pickSelected();
    } else if (code == KeyEvent.VK_DELETE || code == KeyEvent.VK_BACK_SPACE) {
      deleteSelected();
    }
  }

  private void bindCloseShortcut() {
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    InputMap inputs = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, mask), "close");
    inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    getRootPane()
        .getActionMap()
        .put(
            "close",
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                dispose();
              }
            });
  }

  private void pickSelected() {
    int idx = list.getSelectedIndex();
    if (idx < 0) {
      return;
    }
    TabMeta meta = model.remove(idx);
    onPick.accept(meta);
    if (model.isEmpty()) {
      dispose();
    }
  }

  private void deleteSelected() {
    int idx = list.getSelectedIndex();
    if (idx < 0) {
      return;
    }
    TabMeta meta = model.getElementAt(idx);
    int answer =
        JOptionPane.showConfirmDialog(
            this,
            "Delete '" + meta.name() + "' permanently?",
            "Delete tab",
            JOptionPane.YES_NO_OPTION);
    if (answer == JOptionPane.YES_OPTION) {
      model.remove(idx);
      onDelete.accept(meta);
      if (model.isEmpty()) {
        dispose();
      }
    }
  }

  private static final class MetaRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
        JList<?> jlist, Object value, int index, boolean selected, boolean focused) {
      JLabel c =
          (JLabel) super.getListCellRendererComponent(jlist, value, index, selected, focused);
      TabMeta meta = (TabMeta) value;
      String date =
          LocalDateTime.ofInstant(meta.lastModified(), ZoneId.systemDefault()).format(FMT);
      c.setText(meta.name() + "  —  " + date);
      return c;
    }
  }
}
