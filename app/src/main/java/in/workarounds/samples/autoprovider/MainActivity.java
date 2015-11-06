package in.workarounds.samples.autoprovider;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.workarounds.samples.autoprovider.provider.BookCursor;
import in.workarounds.samples.autoprovider.provider.BookSelector;
import in.workarounds.samples.autoprovider.provider.BookValues;
import in.workarounds.samples.autoprovider.provider.MovieCursor;
import in.workarounds.samples.autoprovider.provider.MovieSelector;
import in.workarounds.samples.autoprovider.provider.MovieValues;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private List<Book> mBooks = new ArrayList<>();
    private List<Movie> mMovies = new ArrayList<>();
    @Bind(R.id.btn_populate_data) Button btnPopulateData;
    @Bind(R.id.btn_query_books) Button btnQueryBooks;
    @Bind(R.id.btn_query_movies) Button btnQueryMovies;
    @Bind(R.id.tv_data) TextView tvData;
    @Bind(R.id.et_query_book) EditText etQueryBook;
    @Bind(R.id.et_query_movie) EditText etQueryMovie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @OnClick(R.id.btn_populate_data)
    public void populateData(View view) {
        generateDummy();
        for(Book book : mBooks) {
            insertBook(book);
        }
        for(Movie movie: mMovies) {
            insertMovie(movie);
        }
        Toast.makeText(MainActivity.this, "Data populated!!", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btn_query_books)
    public void queryBooks(View view) {
        String name = etQueryBook.getText().toString();
        BookSelector bookSelector = new BookSelector().nameContains(name);
        BookCursor bookCursor = bookSelector.query(getContentResolver());
        StringBuilder builder = new StringBuilder();
        while (bookCursor.moveToNext()) {
            builder.append(bookCursor.getObject().toString()).append("\n");
        }
        bookCursor.close();
        tvData.setText(builder.toString());
    }

    @OnClick(R.id.btn_query_movies)
    public void queryMovies(View view) {
        String director = etQueryMovie.getText().toString();
        MovieSelector movieSelector = new MovieSelector().directorContains(director);
        MovieCursor movieCursor = movieSelector.query(getContentResolver());
        StringBuilder builder = new StringBuilder();
        while (movieCursor.moveToNext()) {
            builder.append(movieCursor.getObject().toString()).append("\n");
        }
        movieCursor.close();
        tvData.setText(builder.toString());
    }

    private void generateDummy() {
        mBooks.add(new Book("Harry Potter", "J K Rowling", "Fantasy", 5f));
        mBooks.add(new Book("Gentleman Bastards", "Scott Lynch", "Fantasy", 5f));
        mBooks.add(new Book("King Killer Chronicles", "Patrick Rothfuss", "Fantasy", 5f));
        mBooks.add(new Book("1984", "George Orwell", "Fantasy", 5f));

        mMovies.add(new Movie("The Social Network", "David Fincher"));
        mMovies.add(new Movie("Pulp Fiction", "Tarantino"));
        mMovies.add(new Movie("TDK", "Christopher Nolan"));
        mMovies.add(new Movie("TDKR", "Christopher Nolan"));
        mMovies.add(new Movie("Prestige", "Christopher Nolan"));
    }

    private long insertBook(Book book) {
        BookValues bookValues = new BookValues().putObject(book);

        Uri uri = bookValues.insert(getContentResolver());
        return ContentUris.parseId(uri);
    }

    private long insertMovie(Movie movie) {
        MovieValues movieValues = new MovieValues().putObject(movie);
        Uri uri = movieValues.insert(getContentResolver());
        return ContentUris.parseId(uri);
    }


}
