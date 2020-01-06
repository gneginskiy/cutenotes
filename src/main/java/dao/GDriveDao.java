package dao;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import util.DriveQuickstart;

//todo: get rid of physical file in gdrive. create an adapter for a ram file (ugly workaround for gdrive api).
public class GDriveDao implements NotesDataDao {

    private final Drive service;
    private final java.io.File tmpFile;
    private final FileSystemDao tmpFileDao;

    public GDriveDao(@NotNull String fileNameParam) {
        String fileName = toGdriveFileName(fileNameParam);
        this.service = DriveQuickstart.getDriveService();
        this.tmpFileDao = new FileSystemDao(fileName);
        this.tmpFile = tmpFileDao.getFile();
        create(fileNameParam, "");
    }

    @Override
    public void create(@NotNull String fileNameParam, @NotNull String content) {
        String fileName = toGdriveFileName(fileNameParam);

        if (getFileId(fileName) != null) {
            System.out.println("File already exists");
            tmpFileDao.update(fileName, read(fileNameParam));
            return;
        }
        tmpFileDao.update(fileName,content);

        File gdriveFileBody = new File();
        gdriveFileBody.setName(tmpFile.getName());
        FileContent mediaContent = new FileContent("*/*", tmpFile);
        File file;
        try {
            file = service.files().create(gdriveFileBody, mediaContent).execute();
            System.out.println("File ID: " + file.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public String read(@NotNull String fileNameParam) {
        String fileName = toGdriveFileName(fileNameParam);

        try {
            String content = IOUtils.toString(service.files().get(Objects.requireNonNull(getFileId(fileName)))
                .executeMedia().getContent(), Charset.defaultCharset());

            System.out.println(Instant.now() + " read at: " + this.getClass() + " content >> " + content + " |");
            return content;
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public void update(@NotNull String fileNameParam, @NotNull String content) {
        System.out.println(Instant.now() + " updated at: " + this.getClass() + " content >> " + content + " |");

        String fileName = toGdriveFileName(fileNameParam);
        tmpFileDao.update(fileName, content);

        String fileId = getFileId(tmpFile.getName());
        if (fileId == null) {
            create(fileName, content);
        } else {
            File body = new File();
            body.setName(tmpFile.getName());
            FileContent mediaContent = new FileContent("*/*", tmpFile);
            File file;
            try {
                file = service.files().update(fileId, body, mediaContent).execute();
                System.out.println("File ID: " + file.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean delete(@NotNull String fileNameParam) {
        String fileName = toGdriveFileName(fileNameParam);

        String fileId = getFileId(fileName);
        if (fileId == null) {
            throw new RuntimeException("File not found");
        } else {
            try {
                service.files().delete(fileId).execute();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    private String toGdriveFileName(String fileName) {
        return fileName + "-gdrive-tmp";
    }

    private String getFileId(String fileName) {
        try {
            List request = service.files().list();
            FileList files = request.execute();
            for (File file : files.getFiles()) {
                if (file.getName().equals(fileName)) {
                    return file.getId();
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
        return null;
    }
}
