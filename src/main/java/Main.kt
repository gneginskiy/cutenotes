import com.google.common.collect.ImmutableList
import controller.NotesDataManager
import dao.FileSystemDao
import dao.GDriveDao
import dao.NotesDataDao
import view.NotesUI

fun main() {
    Main().start()
}

class Main {
    private val filename = "notesdata.txt"
    private val title = "Cute notes :3"

    private fun getStorageAccessors(): List<NotesDataDao>? {
        return ImmutableList.of(
                GDriveDao(filename),
                FileSystemDao(filename)
        )
    }

    fun start() {
        val notesDataManager = NotesDataManager(getStorageAccessors(), filename)
        notesDataManager.actualizeVersions();
        NotesUI(title, notesDataManager)
    }
}
