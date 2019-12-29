import com.google.common.collect.ImmutableList
import controller.NotesDataManager
import dao.FileSystemDao
import dao.NotesDataDao
import util.DriveQuickstart
import dao.GDriveDao
import view.NotesUI

fun main() {
    Main().start()
}

class Main {
    private val FILENAME = "notesdata.txt"
    private val TITLE = "Cute notes :3"

    fun getStorageAcessors(): List<NotesDataDao>? {
        return ImmutableList.of(
                GDriveDao(FILENAME),
                FileSystemDao(FILENAME)
        )
    }

    fun start() {
        val notesDataManager = NotesDataManager(getStorageAcessors(), FILENAME)
        notesDataManager.actualizeVersions();
        NotesUI(TITLE, notesDataManager)
    }

}
