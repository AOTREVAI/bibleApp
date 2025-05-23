package com.example.bibbia2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bibbia2.BibleApiClient;
import com.example.bibbia2.Bible;
import com.example.bibbia2.Book;
import com.example.bibbia2.Chapter;
import com.example.bibbia2.Passage;
import com.example.bibbia2.Verse;

public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = ""; // Replace with your actual API key

    private BibleApiClient apiClient;
    private TextView resultTextView;
    private Button getBiblesBtn, getBooksBtn, getChaptersBtn, getVersesBtn, getPassageBtn;

    private String currentBibleId = "";
    private String currentBookId = "";
    private String currentChapterId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize API client
        apiClient = new BibleApiClient(this, API_KEY);

        // Initialize UI elements
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        resultTextView = findViewById(R.id.resultTextView);
        getBiblesBtn = findViewById(R.id.getBiblesBtn);
        getBooksBtn = findViewById(R.id.getBooksBtn);
        getChaptersBtn = findViewById(R.id.getChaptersBtn);
        getVersesBtn = findViewById(R.id.getVersesBtn);
        getPassageBtn = findViewById(R.id.getPassageBtn);

        // Initially disable buttons that require previous data
        getBooksBtn.setEnabled(false);
        getChaptersBtn.setEnabled(false);
        getVersesBtn.setEnabled(false);
        getPassageBtn.setEnabled(false);
    }

    private void setupClickListeners() {
        getBiblesBtn.setOnClickListener(v -> fetchBibles());
        getBooksBtn.setOnClickListener(v -> fetchBooks());
        getChaptersBtn.setOnClickListener(v -> fetchChapters());
        getVersesBtn.setOnClickListener(v -> fetchVerses());
        getPassageBtn.setOnClickListener(v -> fetchPassage());
    }

    private void fetchBibles() {
        resultTextView.setText("Loading Bibles...");

        apiClient.getBibles(new BibleApiClient.BiblesCallback() {
            @Override
            public void onSuccess(Bible[] bibles) {
                runOnUiThread(() -> {
                    StringBuilder result = new StringBuilder("Available Bibles:\n\n");

                    for (int i = 0; i < Math.min(bibles.length, 100); i++) {
                        Bible bible = bibles[i];
                        result.append(bible.getName()).append(" (").append(bible.getAbbreviation()).append(")\n");
                        if (bible.getLanguage() != null) {
                            result.append("  Language: ").append(bible.getLanguage().getName()).append("\n");
                        }
                        result.append("  ID: ").append(bible.getId()).append("\n\n");
                    }

                    resultTextView.setText(result.toString());

                    // Enable next button and store first bible ID
                    if (bibles.length > 0) {
                        currentBibleId = bibles[0].getId();
                        getBooksBtn.setEnabled(true);
                        Toast.makeText(MainActivity.this,
                                "Using Bible: " + bibles[0].getName(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    resultTextView.setText("Error: " + error);
                    Toast.makeText(MainActivity.this, "Failed to fetch Bibles", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchBooks() {
        if (currentBibleId.isEmpty()) {
            Toast.makeText(this, "Please fetch Bibles first", Toast.LENGTH_SHORT).show();
            return;
        }

        resultTextView.setText("Loading Books...");

        apiClient.getBooks(currentBibleId, new BibleApiClient.BooksCallback() {
            @Override
            public void onSuccess(Book[] books) {
                runOnUiThread(() -> {
                    StringBuilder result = new StringBuilder("Books in Bible:\n\n");

                    for (int i = 0; i < Math.min(books.length, 15); i++) { // Show first 15
                        Book book = books[i];
                        result.append(book.getName()).append(" (").append(book.getAbbreviation()).append(")\n");
                        result.append("  ID: ").append(book.getId()).append("\n");
                        if (book.getChapters() != null) {
                            result.append("  Chapters: ").append(book.getChapters().size()).append("\n");
                        }
                        result.append("\n");
                    }

                    resultTextView.setText(result.toString());

                    // Enable next button and store first book ID
                    if (books.length > 0) {
                        currentBookId = books[0].getId();
                        getChaptersBtn.setEnabled(true);
                        Toast.makeText(MainActivity.this,
                                "Using Book: " + books[0].getName(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    resultTextView.setText("Error: " + error);
                    Toast.makeText(MainActivity.this, "Failed to fetch Books", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchChapters() {
        if (currentBibleId.isEmpty() || currentBookId.isEmpty()) {
            Toast.makeText(this, "Please fetch Bibles and Books first", Toast.LENGTH_SHORT).show();
            return;
        }

        resultTextView.setText("Loading Chapters...");

        apiClient.getChapters(currentBibleId, currentBookId, new BibleApiClient.ChaptersCallback() {
            @Override
            public void onSuccess(Chapter[] chapters) {
                runOnUiThread(() -> {
                    StringBuilder result = new StringBuilder("Chapters in Book:\n\n");

                    for (Chapter chapter : chapters) {
                        result.append("Chapter ").append(chapter.getNumber()).append("\n");
                        result.append("  Reference: ").append(chapter.getReference()).append("\n");
                        result.append("  ID: ").append(chapter.getId()).append("\n\n");
                    }

                    resultTextView.setText(result.toString());

                    // Enable next button and store first chapter ID
                    if (chapters.length > 0) {
                        currentChapterId = chapters[0].getId();
                        getVersesBtn.setEnabled(true);
                        getPassageBtn.setEnabled(true);
                        Toast.makeText(MainActivity.this,
                                "Using Chapter: " + chapters[0].getNumber(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    resultTextView.setText("Error: " + error);
                    Toast.makeText(MainActivity.this, "Failed to fetch Chapters", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchVerses() {
        if (currentBibleId.isEmpty() || currentChapterId.isEmpty()) {
            Toast.makeText(this, "Please fetch required data first", Toast.LENGTH_SHORT).show();
            return;
        }

        resultTextView.setText("Loading Verses...");

        apiClient.getVerses(currentBibleId, currentChapterId, new BibleApiClient.VersesCallback() {
            @Override
            public void onSuccess(Verse[] verses) {
                runOnUiThread(() -> {
                    StringBuilder result = new StringBuilder("Verses in Chapter:\n\n");

                    for (int i = 0; i < Math.min(verses.length, 10); i++) { // Show first 10
                        Verse verse = verses[i];
                        result.append("Verse: ").append(verse.getReference()).append("\n");
                        result.append("  ID: ").append(verse.getId()).append("\n\n");
                    }

                    resultTextView.setText(result.toString());
                    Toast.makeText(MainActivity.this,
                            "Loaded " + verses.length + " verses",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    resultTextView.setText("Error: " + error);
                    Toast.makeText(MainActivity.this, "Failed to fetch Verses", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchPassage() {
        if (currentBibleId.isEmpty() || currentChapterId.isEmpty()) {
            Toast.makeText(this, "Please fetch required data first", Toast.LENGTH_SHORT).show();
            return;
        }

        resultTextView.setText("Loading Passage...");

        // Use chapter ID as passage ID for this example
        apiClient.getPassage(currentBibleId, currentChapterId, new BibleApiClient.PassageCallback() {
            @Override
            public void onSuccess(Passage passage, Passage.Meta meta) {
                runOnUiThread(() -> {
                    StringBuilder result = new StringBuilder("Passage Content:\n\n");
                    result.append("Reference: ").append(passage.getReference()).append("\n");
                    result.append("Verse Count: ").append(passage.getVerseCount()).append("\n");
                    result.append("Copyright: ").append(passage.getCopyright()).append("\n\n");
                    result.append("Content:\n").append(passage.getContent()).append("\n");

                    if (meta != null && meta.getFums() != null) {
                        result.append("\nFUMS: ").append(meta.getFums());
                    }

                    resultTextView.setText(result.toString());
                    Toast.makeText(MainActivity.this, "Passage loaded successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    resultTextView.setText("Error: " + error);
                    Toast.makeText(MainActivity.this, "Failed to fetch Passage", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiClient != null) {
            apiClient.cancelAllRequests();
        }
    }
}