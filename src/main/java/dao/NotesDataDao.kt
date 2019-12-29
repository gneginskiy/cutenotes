package dao

interface NotesDataDao {
    fun create(filename: String, content: String)
    fun read(filename: String): String
    fun update(filename: String, content: String)
    fun delete(filename: String)
}
