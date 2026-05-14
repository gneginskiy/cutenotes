package view;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

final class TabState {

  String name;
  final JTextArea area;
  final JScrollPane pane;

  TabState(String name, JTextArea area, JScrollPane pane) {
    this.name = name;
    this.area = area;
    this.pane = pane;
  }
}
