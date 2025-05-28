package com.example.myunyt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME    = "professor_ratings.db";
    private static final int    DATABASE_VERSION = 9;
    private static final String T_PROFESSORS = "professors";
    private static final String T_RATINGS    = "ratings";
    private static final String T_USERS      = "users";

    public static final String COLUMN_ID      = "_id";
    public static final String COLUMN_NAME    = "name";
    public static final String COLUMN_FACULTY = "faculty";
    public static final String COLUMN_COURSES = "courses";
    public static final String COLUMN_IMAGE   = "image_url";

    public DatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + T_PROFESSORS + " (" +
                        COLUMN_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME    + " TEXT NOT NULL," +
                        COLUMN_FACULTY + " TEXT NOT NULL," +
                        COLUMN_COURSES + " TEXT," +
                        COLUMN_IMAGE   + " TEXT)");

        db.execSQL(
                "CREATE TABLE " + T_RATINGS + " (" +
                        "rating_id    INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "professor_id INTEGER NOT NULL," +
                        "rating       REAL    NOT NULL," +
                        "comment      TEXT," +
                        "timestamp    DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY(professor_id) REFERENCES " + T_PROFESSORS + "(_id))");


        db.execSQL(
                "CREATE TABLE " + T_USERS + " (" +
                        "user_id  INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "email    TEXT NOT NULL UNIQUE," +
                        "password TEXT NOT NULL," +
                        "username TEXT NOT NULL)");


        db.execSQL(
                "CREATE TABLE schedules (" +
                        "        sched_id     INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  user_id      INTEGER NOT NULL," +
                        "        subject      TEXT NOT NULL," +
                        "        day          TEXT NOT NULL," +
                        "        start_min    INTEGER NOT NULL," +
                        "        end_min      INTEGER NOT NULL," +
                        "        professor_id INTEGER NOT NULL," +
                        "        confirmed    INTEGER NOT NULL DEFAULT 0," +
                        "  FOREIGN KEY(user_id)      REFERENCES users(user_id)," +
                        "        FOREIGN KEY(professor_id) REFERENCES " + T_PROFESSORS + "(_id))");

        db.execSQL(
                "CREATE TABLE votes (" +
                        "  vote_id      INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "  professor_id INTEGER NOT NULL," +
                        "  semester_id  INTEGER NOT NULL," +
                        "  rating       REAL    NOT NULL," +
                        "  comment      TEXT," +
                        "  timestamp    DATETIME DEFAULT CURRENT_TIMESTAMP," +
                        "  UNIQUE(professor_id, semester_id)," +
                        "  FOREIGN KEY(professor_id) REFERENCES " + T_PROFESSORS + "(_id))");


        insertSampleProfessors(db);
        insertSampleUsers(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + T_RATINGS);
        db.execSQL("DROP TABLE IF EXISTS " + T_PROFESSORS);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        db.execSQL("DROP TABLE IF EXISTS schedules");
        db.execSQL("DROP TABLE IF EXISTS votes");// NEW
        onCreate(db);
    }


    public long addUser(String email, String password, String username) {
        ContentValues cv = new ContentValues();
        cv.put("email",    email.trim().toLowerCase());
        cv.put("password", password.trim());
        cv.put("username", username.trim());
        return getWritableDatabase().insert(T_USERS, null, cv);
    }

    public boolean isValidUser(String email, String password) {
        String sql = "SELECT 1 FROM " + T_USERS +
                " WHERE email = ? AND password = ? LIMIT 1";
        try (Cursor c = getReadableDatabase().rawQuery(
                sql,
                new String[]{ email.trim().toLowerCase(), password.trim() })) {
            return c.moveToFirst();
        }
    }

    public String getUsername(String email) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT username FROM " + T_USERS +
                        " WHERE email = ? LIMIT 1",
                new String[]{ email.trim().toLowerCase() })) {
            return c.moveToFirst() ? c.getString(0) : "";
        }
    }

    public int getUserId(String email) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT user_id FROM users WHERE email = ? LIMIT 1",
                new String[] { email.toLowerCase(Locale.ROOT) })) {
            return c.moveToFirst() ? c.getInt(0) : -1;
        }
    }


    public long saveDraftSchedule(int userId, String subject, String day,
                                  int startMin, int endMin, int professorId) {
        ContentValues cv = new ContentValues();
        cv.put("user_id",      userId);
        cv.put("subject",      subject);
        cv.put("day",          day);
        cv.put("start_min",    startMin);
        cv.put("end_min",      endMin);
        cv.put("professor_id", professorId);
        cv.put("confirmed",    0);
        return getWritableDatabase().insert("schedules", null, cv);
    }

    public int clearDraftSchedule(int userId) {
        return getWritableDatabase()
                .delete("schedules", "user_id=? AND confirmed=0",
                        new String[]{ String.valueOf(userId) });
    }

    public void confirmSchedule(int userId) {
        getWritableDatabase().execSQL(
                "UPDATE schedules SET confirmed = 1 " +
                        "WHERE user_id = ? AND confirmed = 0",
                new Object[]{ userId });
    }

    public boolean isScheduleConfirmed(int userId) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT 1 FROM schedules " +
                        "WHERE user_id=? AND confirmed=1 LIMIT 1",
                new String[]{ String.valueOf(userId) })) {
            return c.moveToFirst();
        }
    }

    public Cursor getConfirmedBlocks(int userId) {
        return getReadableDatabase().rawQuery(
                "SELECT s.day, s.start_min, s.end_min, s.subject, p.name " +
                        "FROM schedules s JOIN professors p ON p._id = s.professor_id " +
                        "WHERE s.user_id=? AND s.confirmed = 1 " +
                        "ORDER BY CASE s.day " +
                        "  WHEN 'Mon' THEN 1 WHEN 'Tue' THEN 2 WHEN 'Wed' THEN 3 " +
                        "  WHEN 'Thu' THEN 4 WHEN 'Fri' THEN 5 END, s.start_min",
                new String[]{ String.valueOf(userId) });
    }

    public Cursor getMyProfessors() {
        return getReadableDatabase().rawQuery(
                "SELECT DISTINCT p._id, p.name " +
                        "FROM professors p " +
                        "JOIN schedules s ON s.professor_id = p._id " +
                        "WHERE s.confirmed = 1", null);
    }


    public Cursor getConfirmedBlocks() {
        return getReadableDatabase().rawQuery(
                "SELECT s.day, s.start_min, s.end_min, s.subject, p.name " +
                        "FROM schedules s JOIN professors p ON p._id = s.professor_id " +
                        "WHERE s.confirmed = 1 " +
                        "ORDER BY CASE s.day " +                    // Mon-Fri order
                        "  WHEN 'Mon' THEN 1 WHEN 'Tue' THEN 2 WHEN 'Wed' THEN 3 " +
                        "  WHEN 'Thu' THEN 4 WHEN 'Fri' THEN 5 END, s.start_min", null);
    }

    public boolean hasVoted(int professorId, int semesterId) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT 1 FROM votes WHERE professor_id=? AND semester_id=? LIMIT 1",
                new String[]{ String.valueOf(professorId),
                        String.valueOf(semesterId)})) {
            return c.moveToFirst();
        }
    }


    public long addVote(int professorId, float rating, String comment) {
        int sem = SemesterUtils.currentSemesterId();
        if (sem == 0 || hasVoted(professorId, sem)) return -1;

        ContentValues cv = new ContentValues();
        cv.put("professor_id", professorId);
        cv.put("semester_id",  sem);
        cv.put("rating",       rating);
        cv.put("comment",      comment);
        return getWritableDatabase().insert("votes", null, cv);
    }

    public Cursor getProfessorsByFaculty(String faculty) {

        String sql =
                "SELECT p." + COLUMN_ID      + ", " +
                        "       p." + COLUMN_NAME    + ", " +
                        "       p." + COLUMN_FACULTY + ", " +
                        "       p." + COLUMN_COURSES + ", " +
                        "       p." + COLUMN_IMAGE   + ", " +
                        "       (SELECT AVG(r.rating) FROM " + T_RATINGS + " r " +
                        "         WHERE r.professor_id = p." + COLUMN_ID + ") AS average_rating " +
                        "FROM "   + T_PROFESSORS + " p " +
                        "WHERE p." + COLUMN_FACULTY + " = ? " +
                        "ORDER BY p." + COLUMN_NAME;

        return getReadableDatabase()
                .rawQuery(sql, new String[]{ faculty.trim() });
    }

    public Cursor getProfessorsFromConfirmedSchedule(String faculty) {

        String sql =
                "SELECT p." + COLUMN_ID + ", " +
                        "       p." + COLUMN_NAME + ", " +
                        "       p." + COLUMN_COURSES + ", " +
                        "       p." + COLUMN_IMAGE   + ", " +
                        "       (SELECT AVG(r.rating) FROM " + T_RATINGS + " r " +
                        "         WHERE r.professor_id = p." + COLUMN_ID + ") AS average_rating " +
                        "FROM "   + T_PROFESSORS + " p " +
                        "JOIN schedules s ON s.professor_id = p." + COLUMN_ID + " " +
                        "WHERE s.confirmed = 1 " +
                        "  AND p." + COLUMN_FACULTY + " = ? " +
                        "GROUP BY p." + COLUMN_ID + " " +
                        "ORDER BY p." + COLUMN_NAME;

        return getReadableDatabase().rawQuery(sql, new String[]{ faculty });
    }


    public float getAverageRating(int professorId) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT AVG(rating) FROM " + T_RATINGS +
                        " WHERE professor_id = ?",
                new String[]{ String.valueOf(professorId) })) {
            return c.moveToFirst() ? c.getFloat(0) : 0;
        }
    }

    public long addRating(int professorId, float rating, String comment) {
        ContentValues cv = new ContentValues();
        cv.put("professor_id", professorId);
        cv.put("rating",       rating);
        cv.put("comment",      comment == null ? "" : comment);
        return getWritableDatabase().insert(T_RATINGS, null, cv);
    }

    public Cursor getAllProfessorNames() {
        return getReadableDatabase().query(
                "professors",
                new String[]{"_id", "name"},
                null, null, null, null, "name ASC");
    }

    private void insertSampleUsers(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + T_USERS +
                " (email, password, username) VALUES " +
                "('elia.cekici@gmail.com','1234567','Elia')," +
                "('1.1@gmail.com','1','User1')");
    }

    private void insertProfessor(SQLiteDatabase db,
                                 String name,
                                 String faculty,
                                 String courses,
                                 String imageUrl) {
        ContentValues cv = new ContentValues();
        cv.put("name",     name);
        cv.put("faculty",  faculty);
        cv.put("courses",  courses);
        cv.put("image_url", imageUrl);
        db.insert(T_PROFESSORS, null, cv);
    }

    private void insertSampleProfessors(SQLiteDatabase db) {
        insertProfessor(db,"Assoc. Prof. Andreas KAKOURIS", "Economy and Business","Business Administration","image1.jpg");
        insertProfessor(db,"Dorian ALIU, PhD",              "Economy and Business","Business Administration","image2.jpg");
        insertProfessor(db,"Anela ASABELLA, Ph.D",          "Economy and Business","Business Informatics","image3.jpg");
        insertProfessor(db,"Assoc. Prof. Juna MILUKA",      "Economy and Business","Economics and Finance","image4.jpg");
        insertProfessor(db,"Jonathan PANO, MSc",            "Economy and Business","Economics and Finance","image5.jpg");

        insertProfessor(db,"Plarent RUKA, PhD",             "Law and Social Sciences","International Relation","image6.jpg");
        insertProfessor(db,"Andrea MAZELLIU, PhD Cand.",    "Law and Social Sciences","International Relation","image7.jpg");
        insertProfessor(db,"Assoc. Prof. Mirela BOGDANI",   "Law and Social Sciences","International Relation","image8.jpg");
        insertProfessor(db,"Adela DANAJ, PhD",              "Law and Social Sciences","International Relation","image9.jpg");
        insertProfessor(db,"Granit ZELA, PhD",              "Law and Social Sciences","International Relation","image10.jpg");

        insertProfessor(db,"Marjana PRIFTI SKËNDULI, PhD",  "Engineering and Architecture","Computer Science","image11.jpg");
        insertProfessor(db,"Ervin RAMOLLARI, PhD",          "Engineering and Architecture","Computer Science","image12.jpg");
        insertProfessor(db,"Esmeralda BUSHI, PhD Cand.",    "Engineering and Architecture","Calculus II","image13.jpg");
        insertProfessor(db,"Kristi GOREA, PhD Cand.",       "Engineering and Architecture","Mobile Application Development","image14.jpg");
        insertProfessor(db,"Oltiona SULA, MSc",             "Engineering and Architecture","Data Structures","image15.jpg");
    }
}
