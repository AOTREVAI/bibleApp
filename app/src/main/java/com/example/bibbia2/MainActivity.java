package com.example.bibbia2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.text.Html;
import android.os.Build;
// Optional, if your HTML content might contain links you want to be clickable
// import android.text.method.LinkMovementMethod;
import com.example.bibbia2.BibleApiClient;
import com.example.bibbia2.Bible;
import com.example.bibbia2.Book;
import com.example.bibbia2.Chapter;
import com.example.bibbia2.Passage;
import com.example.bibbia2.Verse;

public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = "a3ac16beb15eb10eea3703389aeeab58"; // Replace with your actual API key

    private BibleApiClient apiClient;
    private ScrollView scrollView;
    private LinearLayout contentLayout;
    private TextView headerTextView;
    private Button backButton;
    private Button getBiblesBtn;

    // Navigation state
    private enum NavigationState {
        INITIAL,
        LANGUAGE_SELECTION,
        BIBLES_LIST,
        BOOKS,
        CHAPTERS,
        VERSES_OR_PASSAGE, // This state was for choosing between list or full chapter
        VERSES_LIST,       // New state specifically for the list of verses
        SINGLE_VERSE_DISPLAY // New state for showing one verse's content
    }



    private NavigationState currentState = NavigationState.INITIAL;
    // ... (selectedBible, selectedBook, selectedChapter)
    private Verse selectedVerse; //
    private String selectedLanguageId;     // To store the ID of the selected language
    private String selectedLanguageName;   // To store the name of the selected language
    private Bible selectedBible;
    private Book selectedBook;
    private Chapter selectedChapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize API client
        apiClient = new BibleApiClient(this, API_KEY);

        // Initialize UI elements
        initializeViews();
        setupInitialView();
    }

    private void initializeViews() {
        scrollView = findViewById(R.id.scrollView);
        contentLayout = findViewById(R.id.contentLayout);
        headerTextView = findViewById(R.id.headerTextView);
        backButton = findViewById(R.id.backButton);
        getBiblesBtn = findViewById(R.id.getBiblesBtn);

        backButton.setOnClickListener(v -> navigateBack());
        // getBiblesBtn.setOnClickListener(v -> fetchBibles()); // Old
        getBiblesBtn.setOnClickListener(v -> showLanguageSelectionScreen()); // New
    }

    private void setupInitialView() {
        currentState = NavigationState.INITIAL;
        headerTextView.setText("Bible Explorer");
        backButton.setVisibility(View.GONE);
        getBiblesBtn.setVisibility(View.VISIBLE);
        clearContent();

        TextView welcomeText = new TextView(this);
        welcomeText.setText("Welcome to Bible Explorer!\n\nClick 'Get Bibles' to start exploring different Bible translations.");
        welcomeText.setTextSize(16);
        welcomeText.setPadding(16, 16, 16, 16);
        contentLayout.addView(welcomeText);
    }
    private void showLanguageSelectionScreen() {
        updateNavigationState(NavigationState.LANGUAGE_SELECTION);
        headerTextView.setText("Select a Language");
        clearContent();
        // Reset selected language in case we are navigating back
        selectedLanguageId = null;
        selectedLanguageName = null;

        // Example Languages (Ideally, fetch these from API or a more robust source)
        addLanguageButton("All Languages", null); // null for all languages
        addLanguageButton("English", "eng");
        addLanguageButton("Italian", "ita");
        addLanguageButton("Spanish", "spa");
        addLanguageButton("French", "fra");
        // Add more languages as needed
    }

    private void addLanguageButton(String languageName, String languageId) {
        Button langButton = new Button(this);
        langButton.setText(languageName);
        langButton.setLayoutParams(createButtonLayoutParams());
        langButton.setOnClickListener(v -> {
            Toast.makeText(this, "Selected Language: " + languageName, Toast.LENGTH_SHORT).show();
            fetchBiblesList(languageId, languageName);
        });
        contentLayout.addView(langButton);
    }

    private void clearContent() {
        contentLayout.removeAllViews();
    }

    private void navigateBack() {
        switch (currentState) {
            case LANGUAGE_SELECTION:
                setupInitialView();
                break;
            case BIBLES_LIST:
                showLanguageSelectionScreen();
                break;
            case BOOKS:
                if (selectedLanguageId != null || selectedLanguageName != null) {
                    fetchBiblesList(selectedLanguageId, selectedLanguageName);
                } else {
                    showLanguageSelectionScreen(); // Fallback
                }
                break;
            case CHAPTERS:
                if (selectedBible != null) {
                    fetchBooks(selectedBible);
                }
                break;
            case VERSES_OR_PASSAGE: // Navigating back from the "View Verses / Read Chapter" choice screen
                if (selectedBible != null && selectedBook != null) {
                    fetchChapters(selectedBible, selectedBook);
                }
                break;
            case VERSES_LIST: // Navigating back from the list of verses
                if (selectedBible != null && selectedBook != null && selectedChapter != null) {
                    // Go back to the screen where user chose "View Verses List" or "Read Full Chapter"
                    showChapterOptions(selectedBible, selectedBook, selectedChapter);
                }
                break;
            case SINGLE_VERSE_DISPLAY: // Navigating back from showing a single verse
                if (selectedBible != null && selectedChapter != null) {
                    // Go back to the list of verses for the current chapter
                    fetchVersesListDisplay(selectedBible, selectedChapter);
                }
                break;
            default:
                setupInitialView();
                break;
        }
    }

    private void updateNavigationState(NavigationState newState) {
        currentState = newState;
        backButton.setVisibility(newState == NavigationState.INITIAL ? View.GONE : View.VISIBLE);
        getBiblesBtn.setVisibility(newState == NavigationState.INITIAL ? View.VISIBLE : View.GONE);
    }

    // Renamed from fetchBibles to fetchBiblesList and modified
    private void fetchBiblesList(String languageId, String languageDisplayName) {
        selectedLanguageId = languageId; // Store for back navigation and context
        selectedLanguageName = languageDisplayName; // Store for display

        updateNavigationState(NavigationState.BIBLES_LIST);
        if (languageId != null && !languageDisplayName.equals("All Languages")) {
            headerTextView.setText("Bibles in " + languageDisplayName);
        } else {
            headerTextView.setText("Select a Bible Translation");
        }
        clearContent();
        showLoadingMessage("Loading Bible translations...");

        // Pass the languageId to your apiClient.
        // If languageId is null, your apiClient should fetch all bibles.
        apiClient.getBibles(languageId, new BibleApiClient.BiblesCallback() {
            @Override
            public void onSuccess(Bible[] bibles) {
                runOnUiThread(() -> {
                    clearContent();

                    if (bibles.length == 0) {
                        showErrorMessage("No Bible translations found for " + (languageDisplayName.equals("All Languages") ? "the selected criteria" : languageDisplayName) + ".");
                        return;
                    }

                    int maxBibles = Math.min(bibles.length, 100);
                    TextView infoText = new TextView(MainActivity.this);
                    infoText.setText("Found " + bibles.length + " Bible translations. Showing first " + maxBibles + ":");
                    infoText.setTextSize(14);
                    infoText.setPadding(16, 16, 16, 8);
                    infoText.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));
                    contentLayout.addView(infoText);

                    for (int i = 0; i < maxBibles; i++) {
                        Bible bible = bibles[i];
                        Button bibleButton = createBibleButton(bible); // createBibleButton remains largely the same
                        contentLayout.addView(bibleButton);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> showErrorMessage("Error loading Bibles: " + error));
            }
        });
    }
    private void fetchBooks(Bible bible) {
        selectedBible = bible;
        updateNavigationState(NavigationState.BOOKS);
        headerTextView.setText("Books in " + bible.getName());
        clearContent();
        showLoadingMessage("Loading books...");

        apiClient.getBooks(bible.getId(), new BibleApiClient.BooksCallback() {
            @Override
            public void onSuccess(Book[] books) {
                runOnUiThread(() -> {
                    clearContent();

                    if (books.length == 0) {
                        showErrorMessage("No books found in this Bible.");
                        return;
                    }

                    TextView infoText = new TextView(MainActivity.this);
                    infoText.setText("Found " + books.length + " books:");
                    infoText.setTextSize(14);
                    infoText.setPadding(16, 16, 16, 8);
                    infoText.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));
                    contentLayout.addView(infoText);

                    for (Book book : books) {
                        Button bookButton = createBookButton(book);
                        contentLayout.addView(bookButton);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> showErrorMessage("Error loading books: " + error));
            }
        });
    }

    private void fetchChapters(Bible bible, Book book) {
        selectedBook = book;
        updateNavigationState(NavigationState.CHAPTERS);
        headerTextView.setText("Chapters in " + book.getName());
        clearContent();
        showLoadingMessage("Loading chapters...");

        apiClient.getChapters(bible.getId(), book.getId(), new BibleApiClient.ChaptersCallback() {
            @Override
            public void onSuccess(Chapter[] chapters) {
                runOnUiThread(() -> {
                    clearContent();

                    if (chapters.length == 0) {
                        showErrorMessage("No chapters found in this book.");
                        return;
                    }

                    TextView infoText = new TextView(MainActivity.this);
                    infoText.setText("Found " + chapters.length + " chapters:");
                    infoText.setTextSize(14);
                    infoText.setPadding(16, 16, 16, 8);
                    infoText.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));
                    contentLayout.addView(infoText);

                    for (Chapter chapter : chapters) {
                        Button chapterButton = createChapterButton(chapter);
                        contentLayout.addView(chapterButton);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> showErrorMessage("Error loading chapters: " + error));
            }
        });
    }

    private void showChapterOptions(Bible bible, Book book, Chapter chapter) {
        selectedChapter = chapter;
        updateNavigationState(NavigationState.VERSES_OR_PASSAGE); // This state remains as the choice screen
        headerTextView.setText(chapter.getReference());
        clearContent();

        TextView infoText = new TextView(this);
        infoText.setText("What would you like to see?");
        infoText.setTextSize(16);
        infoText.setPadding(16, 16, 16, 16);
        contentLayout.addView(infoText);

        // Button to fetch verses list
        Button versesButton = new Button(this);
        versesButton.setText("View Verses List");
        versesButton.setLayoutParams(createButtonLayoutParams());
        versesButton.setOnClickListener(v -> fetchVersesListDisplay(bible, chapter)); // Changed from fetchVerses
        contentLayout.addView(versesButton);

        // Button to fetch passage content
        Button passageButton = new Button(this);
        passageButton.setText("Read Full Chapter");
        passageButton.setLayoutParams(createButtonLayoutParams());
        passageButton.setOnClickListener(v -> fetchPassage(bible, chapter));
        contentLayout.addView(passageButton);
    }

    // Renamed from fetchVerses and modified
    private void fetchVersesListDisplay(Bible bible, Chapter chapter) {
        updateNavigationState(NavigationState.VERSES_LIST); // New specific state
        headerTextView.setText("Verses in " + chapter.getReference());
        clearContent();
        showLoadingMessage("Loading verses...");

        apiClient.getVerses(bible.getId(), chapter.getId(), new BibleApiClient.VersesCallback() {
            @Override
            public void onSuccess(Verse[] verses) {
                runOnUiThread(() -> {
                    clearContent();

                    if (verses == null || verses.length == 0) {
                        showErrorMessage("No verses found in this chapter.");
                        return;
                    }

                    TextView infoText = new TextView(MainActivity.this);
                    infoText.setText("Found " + verses.length + " verses (click to view content):");
                    infoText.setTextSize(14);
                    infoText.setPadding(16, 16, 16, 8);
                    infoText.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));
                    contentLayout.addView(infoText);

                    for (Verse verse : verses) {
                        // createVerseView will now make it clickable and take the full Verse object
                        Button verseButton = createVerseButton(verse);
                        contentLayout.addView(verseButton);
                    }

                    // Optional: Add button to read full chapter from this screen too
                    Button readChapterButton = new Button(MainActivity.this);
                    readChapterButton.setText("Read Full Chapter Text");
                    readChapterButton.setLayoutParams(createButtonLayoutParams());
                    readChapterButton.setOnClickListener(v -> fetchPassage(bible, chapter));
                    contentLayout.addView(readChapterButton);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> showErrorMessage("Error loading verses: " + error));
            }
        });
    }
    private void fetchPassage(Bible bible, Chapter chapter) {
        headerTextView.setText("Reading " + chapter.getReference());
        clearContent();
        showLoadingMessage("Loading chapter content...");

        apiClient.getPassage(bible.getId(), chapter.getId(), new BibleApiClient.PassageCallback() {
            @Override
            public void onSuccess(Passage passage, Passage.Meta meta) {
                runOnUiThread(() -> {
                    clearContent();

                    // Passage info
                    TextView infoView = new TextView(MainActivity.this);
                    infoView.setText("Reference: " + passage.getReference() +
                            "\nVerses: " + passage.getVerseCount());
                    infoView.setTextSize(14);
                    infoView.setPadding(16, 16, 16, 8);
                    infoView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));
                    contentLayout.addView(infoView);

                    // Passage content - MODIFIED PART
                    TextView contentView = new TextView(MainActivity.this);
                    String htmlContent = passage.getContent();

                    if (htmlContent != null && !htmlContent.isEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            contentView.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            // noinspection deprecation
                            contentView.setText(Html.fromHtml(htmlContent));
                        }
                        // Optional: If your HTML might contain <a> tags and you want them to be clickable
                        // contentView.setMovementMethod(LinkMovementMethod.getInstance());
                    } else {
                        contentView.setText("No content available for this passage.");
                    }

                    contentView.setTextSize(16);
                    contentView.setPadding(16, 8, 16, 16);
                    contentView.setLineSpacing(4, 1.2f); // Adjust line spacing as needed for readability
                    contentLayout.addView(contentView);

                    // Copyright notice
                    if (passage.getCopyright() != null && !passage.getCopyright().isEmpty()) {
                        TextView copyrightView = new TextView(MainActivity.this);
                        // The copyright itself might also contain HTML, apply the same logic if needed
                        String copyrightHtml = passage.getCopyright();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            copyrightView.setText(Html.fromHtml(copyrightHtml, Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            // noinspection deprecation
                            copyrightView.setText(Html.fromHtml(copyrightHtml));
                        }
                        copyrightView.setTextSize(12);
                        copyrightView.setPadding(16, 16, 16, 8);
                        copyrightView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));
                        contentLayout.addView(copyrightView);
                    }

                    // Add button to view verse list
                    Button viewVersesButton = new Button(MainActivity.this);
                    viewVersesButton.setText("View Verses List");
                    viewVersesButton.setLayoutParams(createButtonLayoutParams());
                    viewVersesButton.setOnClickListener(v -> fetchVersesListDisplay(bible, chapter));
                    contentLayout.addView(viewVersesButton);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> showErrorMessage("Error loading passage: " + error));
            }
        });
    }
    // UI Helper Methods
    private Button createBibleButton(Bible bible) {
        Button button = new Button(this);
        String buttonText = bible.getName();
        if (bible.getLanguage() != null) {
            buttonText += "\n(" + bible.getLanguage().getName() + ")";
        }
        button.setText(buttonText);
        button.setLayoutParams(createButtonLayoutParams());
        button.setOnClickListener(v -> {
            Toast.makeText(this, "Selected: " + bible.getName(), Toast.LENGTH_SHORT).show();
            fetchBooks(bible);
        });
        return button;
    }

    private Button createBookButton(Book book) {
        Button button = new Button(this);
        String buttonText = book.getName();
        if (book.getChapters() != null) {
            buttonText += "\n(" + book.getChapters().size() + " chapters)";
        }
        button.setText(buttonText);
        button.setLayoutParams(createButtonLayoutParams());
        button.setOnClickListener(v -> {
            Toast.makeText(this, "Selected: " + book.getName(), Toast.LENGTH_SHORT).show();
            fetchChapters(selectedBible, book);
        });
        return button;
    }

    private Button createChapterButton(Chapter chapter) {
        Button button = new Button(this);
        button.setText("Chapter " + chapter.getNumber() + "\n" + chapter.getReference());
        button.setLayoutParams(createButtonLayoutParams());
        button.setOnClickListener(v -> {
            Toast.makeText(this, "Selected: " + chapter.getReference(), Toast.LENGTH_SHORT).show();
            showChapterOptions(selectedBible, selectedBook, chapter);
        });
        return button;
    }

    // Was createVerseView (TextView), now createVerseButton (Button)
    private Button createVerseButton(Verse verse) {
        Button button = new Button(this);
        button.setText(verse.getReference()); // e.g., "John 3:16"
        button.setLayoutParams(createButtonLayoutParams()); // Use existing layout params for consistency
        button.setOnClickListener(v -> {
            Toast.makeText(this, "Showing: " + verse.getReference(), Toast.LENGTH_SHORT).show();
            showSingleVerseContent(verse);
        });
        return button;
    }
    private void showSingleVerseContent(Verse verse) {
        selectedVerse = verse; // Store if needed for other actions, or just use the passed 'verse'
        updateNavigationState(NavigationState.SINGLE_VERSE_DISPLAY);
        headerTextView.setText(verse.getReference());
        clearContent();

        if (verse.getContent() == null || verse.getContent().isEmpty()) {
            showErrorMessage("No content available for this verse.");
            return;
        }

        // TextView to display the verse content
        TextView verseContentView = new TextView(this);
        String htmlContent = verse.getContent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            verseContentView.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY));
        } else {
            // noinspection deprecation
            verseContentView.setText(Html.fromHtml(htmlContent));
        }
        // Optional: if verse HTML contains links
        // verseContentView.setMovementMethod(LinkMovementMethod.getInstance());

        verseContentView.setTextSize(18); // Slightly larger for focused reading
        verseContentView.setPadding(16, 16, 16, 16);
        verseContentView.setLineSpacing(6, 1.3f); // Adjust for readability
        contentLayout.addView(verseContentView);

        // Optionally, add chapter's copyright if available and relevant here
        // This depends on your `Passage` object structure or if `Verse` also has copyright
        // For simplicity, we might omit it here or fetch the chapter's full passage metadata
        // if copyright is crucial at the single verse display level.
        // For now, let's assume the chapter's copyright (shown with full passage) covers it.
    }

    private LinearLayout.LayoutParams createButtonLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);
        return params;
    }

    private void showLoadingMessage(String message) {
        TextView loadingText = new TextView(this);
        loadingText.setText(message);
        loadingText.setTextSize(16);
        loadingText.setPadding(16, 32, 16, 32);
        loadingText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        contentLayout.addView(loadingText);
    }

    private void showErrorMessage(String message) {
        clearContent();
        TextView errorText = new TextView(this);
        errorText.setText(message);
        errorText.setTextSize(16);
        errorText.setPadding(16, 32, 16, 32);
        errorText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        contentLayout.addView(errorText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiClient != null) {
            apiClient.cancelAllRequests();
        }
    }
}