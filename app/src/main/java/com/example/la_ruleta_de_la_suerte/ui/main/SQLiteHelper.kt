class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, "ubicaciones.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE ubicaciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                latitud REAL,
                longitud REAL,
                fecha TEXT
            );
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ubicaciones")
        onCreate(db)
    }

    fun insertarUbicacion(lat: Double, lon: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("latitud", lat)
            put("longitud", lon)
            put("fecha", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        }
        db.insert("ubicaciones", null, values)
    }
}
