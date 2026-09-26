package view;

import static view.Messages.tr;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** The auto-hiding tabs bar: the tabs, a "new tab" button and a pin that keeps the bar shown. */
class TabStrip {

  private final TabHeader header;
  private final JScrollPane headerScroll;
  private final FlatButton newTab;
  private final FlatButton pin;
  private final JPanel buttons = new JPanel(new GridBagLayout());
  private final JPanel panel = new JPanel(new BorderLayout());
  private final transient Revealer revealer;
  private transient TabRequests requests = TabRequests.NONE;
  private transient Consumer<String> notifier = message -> {};

  TabStrip(
      Consumer<String> select,
      BiConsumer<String, String> rename,
      Consumer<List<String>> reorder,
      Function<String, String> colorOf,
      BiConsumer<String, String> setColor,
      JComponent layoutRoot) {
    this.header =
        new TabHeader(
            new TabHeader.Actions(
                select,
                rename,
                reorder,
                id -> requests.close().accept(id),
                id -> requests.closeOthers().accept(id),
                () -> requests.newTab().run(),
                colorOf,
                setColor));
    this.headerScroll = TabPanes.header(header);
    this.newTab =
        new FlatButton(VectorIcon.Kind.PLUS, 14, tr("tab.new"), () -> requests.newTab().run());
    this.pin = new FlatButton(VectorIcon.Kind.PIN, 14, tr("tab.pin"), this::togglePin);
    Box row = Box.createHorizontalBox();
    row.add(newTab);
    row.add(Box.createHorizontalStrut(2));
    row.add(pin);
    buttons.add(row);
    headerScroll.addMouseWheelListener(
        e -> {
          var bar = headerScroll.getHorizontalScrollBar();
          bar.setValue(bar.getValue() + e.getUnitsToScroll() * bar.getUnitIncrement());
        });
    panel.add(headerScroll, BorderLayout.CENTER);
    panel.add(buttons, BorderLayout.EAST);
    this.revealer = new Revealer(panel, layoutRoot, header::isEditing);
  }

  void setRequests(TabRequests requests) {
    this.requests = requests;
  }

  /** Where short status messages go ("Tabs bar pinned"). */
  void setNotifier(Consumer<String> notifier) {
    this.notifier = notifier;
  }

  /** Shows a short message (a toast in the window). */
  void announce(String message) {
    notifier.accept(message);
  }

  JComponent component() {
    return panel;
  }

  TabHeader header() {
    return header;
  }

  Revealer revealer() {
    return revealer;
  }

  boolean pinned() {
    return revealer.isPinned();
  }

  void applyTheme(UiPalette p) {
    header.applyTheme(p);
    panel.setBackground(p.chrome());
    buttons.setBackground(p.chrome());
    buttons.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, p.border()),
            BorderFactory.createEmptyBorder(0, 4, 0, 6)));
    newTab.applyPalette(p);
    pin.applyPalette(p);
  }

  void togglePin() {
    boolean on = !revealer.isPinned();
    revealer.setPinned(on);
    pin.setOn(on);
    pin.setGlyph(on ? VectorIcon.Kind.PIN_ON : VectorIcon.Kind.PIN);
    pin.setToolTipText(on ? tr("tab.pinned") : tr("tab.pin"));
    notifier.accept(on ? tr("toast.tabsPinned") : tr("toast.tabsUnpinned"));
  }
}
