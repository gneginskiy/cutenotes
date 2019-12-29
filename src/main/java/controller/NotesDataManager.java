package controller;

import dao.NotesDataDao;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class NotesDataManager { //todo extract to interface

    private List<NotesDataDao> storageAccessors;
    private String filename;

    public NotesDataManager(List<NotesDataDao> storageAccessors, String filename) {
        this.storageAccessors = storageAccessors;
        this.filename = filename;
    }

    public void actualizeVersions() {
        String lastVersionContent = getLastVersion();
        storageAccessors.forEach(s -> updateEverywhere(lastVersionContent));
        System.out.println(Instant.now() + " actualized");
    }

    public void updateEverywhere(String text) {
        storageAccessors.forEach(s -> s.update(filename, text));

        System.out.println(Instant.now() + " updated everywhere: "+text);
    }

    public String getLastVersion() {
        return storageAccessors.stream()
                .map(s -> s.read(filename))
                .max(Comparator.comparing(String::length))
                .orElse("");
    }
}
