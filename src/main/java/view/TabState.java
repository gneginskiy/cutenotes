package view;

import javax.swing.JScrollPane;

final class TabState {

  String name;
  final NoteEditor area;
  final JScrollPane pane;

  TabState(String name, NoteEditor area, JScrollPane pane) {
    this.name = name;
    this.area = area;
    this.pane = pane;
  }
}
