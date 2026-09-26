package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dao.FileTabRepository;
import model.Tab;
import model.TabMeta;
import util.AutoSaver;

class ExternalEditsTest {

  static {
    System.setProperty("java.awt.headless", "true");
    Messages.useLanguage("en");
  }

  @TempDir Path data;
  private final List<String> toasts = new ArrayList<>();
  private TabsPane tabs;
  private AutoSaver saver;
  private ExternalEdits edits;
  private FileTabRepository repo;

  private void open(String text) throws Exception {
    repo = new FileTabRepository(data);
    repo.save("a", "Alpha", text);
    saver = new AutoSaver(List::of, repo);
    SwingUtilities.invokeAndWait(
        () -> {
          tabs = new TabsPane();
          tabs.openTab("a", "Alpha", text);
          saver.markSaved(tabs.snapshotOf("a"));
        });
    edits = new ExternalEdits(repo, tabs, saver, toasts::add);
    edits.check();
  }

  private void changeOnDisk(String text) throws Exception {
    repo.save("a", "Alpha", text);
    try (Stream<Path> files = Files.list(data)) {
      Path file = files.filter(p -> p.getFileName().toString().contains("a")).findFirst().get();
      Files.setLastModifiedTime(file, FileTime.from(Instant.now().plusSeconds(60)));
    }
    edits.check();
    SwingUtilities.invokeAndWait(() -> {});
  }

  @Test
  void withoutUnsavedEditsTheTabShowsTheNewText() throws Exception {
    open("old");

    changeOnDisk("from sync");

    SwingUtilities.invokeAndWait(() -> assertEquals("from sync", tabs.areaOf("a").markdown()));
    assertEquals("from sync", saver.lastSaved("a").content());
    assertEquals(1, toasts.size());
  }

  @Test
  void withUnsavedEditsBothVersionsAreKept() throws Exception {
    open("old");
    SwingUtilities.invokeAndWait(() -> tabs.areaOf("a").setMarkdown("my edit"));

    changeOnDisk("from sync");

    SwingUtilities.invokeAndWait(() -> assertEquals("my edit", tabs.areaOf("a").markdown()));
    List<TabMeta> notes = repo.listMeta();
    assertEquals(2, notes.size());
    TabMeta copy = notes.stream().filter(m -> !m.id().equals("a")).findFirst().get();
    assertEquals("Alpha (from disk)", copy.name());
    assertEquals("from sync", repo.load(copy.id()).content());
  }

  @Test
  void closedTabsAreLeftAlone() throws Exception {
    open("old");
    SwingUtilities.invokeAndWait(
        () -> {
          tabs.close("a");
          edits.apply(new Tab("a", "Alpha", "new"), new Tab("a", "Alpha", "old"));
        });

    assertTrue(toasts.isEmpty());
  }
}
