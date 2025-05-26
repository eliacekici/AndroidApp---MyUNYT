package com.example.myunyt;

import android.content.ContentValues;
import android.database.Cursor;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "professor_ratings.db";
    private static final int DATABASE_VERSION = 2;


    public static final String TABLE_PROFESSORS = "professors";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FACULTY = "faculty";
    public static final String COLUMN_COURSES = "courses";
    public static final String COLUMN_IMAGE = "image_url";


    public static final String TABLE_RATINGS = "ratings";
    public static final String COLUMN_RATING_ID = "rating_id";
    public static final String COLUMN_PROFESSOR_ID = "professor_id";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_TIMESTAMP = "timestamp";


    private static final String CREATE_PROFESSORS_TABLE =
            "CREATE TABLE " + TABLE_PROFESSORS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT NOT NULL,"
                    + COLUMN_FACULTY + " TEXT NOT NULL,"
                    + COLUMN_COURSES + " TEXT,"
                    + COLUMN_IMAGE + " TEXT"
                    + ");";

    private static final String CREATE_RATINGS_TABLE =
            "CREATE TABLE " + TABLE_RATINGS + "("
                    + COLUMN_RATING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_PROFESSOR_ID + " INTEGER NOT NULL,"
                    + COLUMN_RATING + " REAL NOT NULL,"
                    + COLUMN_COMMENT + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(" + COLUMN_PROFESSOR_ID + ") REFERENCES "
                    + TABLE_PROFESSORS + "(" + COLUMN_ID + ")"
                    + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROFESSORS_TABLE);
        db.execSQL(CREATE_RATINGS_TABLE);
        insertSampleProfessors(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFESSORS);
        onCreate(db);
    }

    private void insertSampleProfessors(SQLiteDatabase db) {
        insertProfessor(db, "Assoc. Prof. Andreas KAKOURIS", "Economy and Business", "Business Administration", "image1.jpg");
        insertProfessor(db, "Dorian ALIU, PhD", "Economy and Business", "Business Administration", "image2.jpg");
        insertProfessor(db, "Anela ASABELLA, Ph.D", "Economy and Business", "Business Informatics ", "image3.jpg");
        insertProfessor(db, "Assoc. Prof. Juna MILUKA", "Economy and Business", "Economics and Finance", "image4.jpg");
        insertProfessor(db, "Jonathan PANO, MSc", "Economy and Business", "Economics and Finance", "image5.jpg");

        insertProfessor(db, "Plarent RUKA, PhD", "Law and Social Sciences", "International Relation", "image6.jpg");
        insertProfessor(db, "Andrea MAZELLIU, PhD Cand.", "Law and Social Sciences", "International Relation", "image7.jpg");
        insertProfessor(db, "Assoc. Prof. Mirela BOGDANI", "Law and Social Sciences", "International Relation", "image8.jpg");
        insertProfessor(db, "Adela DANAJ, PhD", "Law and Social Sciences", "International Relation", "image9.jpg");
        insertProfessor(db, "Granit ZELA, PhD", "Law and Social Sciences", "International Relation", "image10.jpg");

        insertProfessor(db, "Marjana PRIFTI SKËNDULI, PhD", "Engineering and Architecture", "Computer Science", "image11.jpg");
        insertProfessor(db, "Ervin RAMOLLARI, PhD", "Engineering and Architecture", "Computer Science", "image12.jpg");
        insertProfessor(db, "Esmeralda BUSHI, PhD Cand.", "Engineering and Architecture", "Calculus II", "image13.jpg");
        insertProfessor(db, "Kristi GOREA, PhD Cand.", "Engineering and Architecture", "Mobile Application Development", "image14.jpg");
        insertProfessor(db, "Oltiona SULA, MSc", "Engineering and Architecture", "Data Structures", "image15.jpg");
    }

    private void insertProfessor(SQLiteDatabase db, String name, String faculty, String courses, String imageUrl) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_FACULTY, faculty);
        values.put(COLUMN_COURSES, courses);
        values.put(COLUMN_IMAGE, imageUrl);
        db.insert(TABLE_PROFESSORS, null, values);
    }

    public Cursor getProfessorsByFaculty(String faculty) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_FACULTY,
                COLUMN_COURSES,
                COLUMN_IMAGE,
                "(SELECT AVG(" + COLUMN_RATING + ") FROM " + TABLE_RATINGS +
                        " WHERE " + COLUMN_PROFESSOR_ID + " = " + TABLE_PROFESSORS + "." + COLUMN_ID + ") AS average_rating"
        };

        String selection = COLUMN_FACULTY + " = ?";
        String[] selectionArgs = {faculty};

        return db.query(
                TABLE_PROFESSORS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_NAME + " ASC");
    }

    public float getAverageRating(int professorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT AVG(" + COLUMN_RATING + ") FROM " + TABLE_RATINGS +
                        " WHERE " + COLUMN_PROFESSOR_ID + " = ?",
                new String[]{String.valueOf(professorId)});
        float avg = 0;
        if (cursor.moveToFirst()) {
            avg = cursor.getFloat(0);
        }
        cursor.close();
        return avg;
    }

    public long addRating(int professorId, float rating, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFESSOR_ID, professorId);
        values.put(COLUMN_RATING, rating);
        values.put(COLUMN_COMMENT, comment);
        return db.insert(TABLE_RATINGS, null, values);
    }
}
