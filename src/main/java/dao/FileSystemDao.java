package dao;

import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;


public class FileSystemDao implements NotesDataDao {
    private final File file;

    public FileSystemDao(String fileName) {
        this.file = new java.io.File(fileName);
        create(file.getName(),"");
    }

    @Override
    public void create(@NotNull String filename, @NotNull String content) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            update(filename, content);
        }
    }

    @Override
    public void update(@NotNull String filename, @NotNull String content) {
        System.out.println(Instant.now() + " updated at: " + this.getClass() +
            ", filename " + filename + " , content >> " + content + " |");

        try (BufferedWriter bufferedWriter = Files.newWriter(file, Charset.defaultCharset())) {
            bufferedWriter.write(content);
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean delete(@NotNull String filename) {
        return file.delete();
    }

    @NotNull
    @Override
    public String read(@NotNull String filename) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file.getName()));
            for (String buffer = reader.readLine(); buffer != null; buffer = reader.readLine()) {
                sb.append(buffer).append("\n");
            }
            String content = sb.toString();

            System.out.println(Instant.now() + " read at: " + this.getClass() + " content >> " + content + " |");
            return content;
        } catch (Exception rethrown) {
            throw new RuntimeException(rethrown);
        }
    }

    //todo: hardcore crutch. refactor
    File getFile(){
        return file;
    }
}
