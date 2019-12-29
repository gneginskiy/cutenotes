package dao;

import java.io.IOException;
import java.time.Instant;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import util.DriveQuickstart;

//todo: get rid of physical file in gdrive. create an adapter for a ram file (ugly workaround for gdrive api).
public class GDriveDao implements NotesDataDao {
    private final Drive service;
    private final java.io.File tmpFile;
    private final String fileName;
    private final FileSystemDao tmpFileDao;

    public GDriveDao(@NotNull String fileNameParam) throws IOException {
        this.fileName = toGdriveFileName(fileNameParam);
        this.service = DriveQuickstart.getDriveService(fileName);
        this.tmpFileDao = new FileSystemDao(fileName);
        this.tmpFile=tmpFileDao.getFile();
        create(fileNameParam,"");
    }

    private String toGdriveFileName(String fileName) {
        return fileName+"-gdrive-tmp";
    }

    @Override
    public void create(@NotNull String fileNameParam, @NotNull String content) {
        String fileName = toGdriveFileName(fileNameParam);

        if(getFileId(fileName)!=null){
            System.out.println("File already exists");
            tmpFileDao.update(fileName,read(fileNameParam));
            return;
        }
        tmpFileDao.update(fileName,content);

        File gdriveFileBody = new File();
        gdriveFileBody.setTitle(tmpFile.getName());
        FileContent mediaContent = new FileContent("*/*", tmpFile);
        File file = null;
        try {
            file = service.files().insert(gdriveFileBody, mediaContent).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File ID: " + file.getId());
    }

    @NotNull
    @Override
    public String read(@NotNull String fileNameParam) {
        String fileName = toGdriveFileName(fileNameParam);

        try {
            String content = IOUtils.toString(service.files().get(getFileId(fileName)).executeMedia().getContent());
            System.out.println(Instant.now() + " read at: "+this.getClass()+" content >> "+content+" |");
            return content;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void update(String fileNameParam, String content) {
        System.out.println(Instant.now() + " updated at: "+this.getClass()+" content >> "+content+" |");

        String fileName = toGdriveFileName(fileNameParam);
        tmpFileDao.update(fileName,content);

        String fileId = getFileId(tmpFile.getName());
        if (fileId == null) {
            create(fileName,content);
        } else {
            File body = new File();
            body.setTitle(tmpFile.getName());
            FileContent mediaContent = new FileContent("*/*", tmpFile);
            File file = null;
            try {
                file = service.files().update(fileId, body, mediaContent).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("File ID: " + file.getId());
        }
    }

    public void delete(String fileNameParam) {
        String fileName = toGdriveFileName(fileNameParam);

        String fileId = getFileId(fileName);
        if (fileId == null) {
            throw new RuntimeException("File not found");
        } else {
            try {
                service.files().delete(fileId).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileId(String fileName) {
        try {
            List request = service.files().list();
            FileList files = request.execute();
            for (File file : files.getItems()) {
                if (file.getTitle().equals(fileName)) {
                    return file.getId();
                }
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
        return null;
    }
}
