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



public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = "your-api-key-here";

    private BibleApiClient apiClient;
    private ScrollView scrollView;
    private LinearLayout contentLayout;
    private TextView headerTextView;
    private Button backButton;
    private Button getBiblesBtn;

   
    private enum NavigationState {
        INITIAL,
        LANGUAGE_SELECTION,
        BIBLES_LIST,
        BOOKS,
        CHAPTERS,
        VERSES_OR_PASSAGE,
        VERSES_LIST,
        SINGLE_VERSE_DISPLAY
    }

    private NavigationState currentState = NavigationState.INITIAL;
    private String selectedLanguageId;
    private String selectedLanguageName;
    private Bible selectedBible;
    private Book selectedBook;
    private Chapter selectedChapter;
   
    private SingleVerse currentDisplayingVerse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiClient = new BibleApiClient(this, API_KEY);
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
        getBiblesBtn.setOnClickListener(v -> showLanguageSelectionScreen());
    }

    private void setupInitialView() {
        currentState = NavigationState.INITIAL;
        headerTextView.setText("Bible Explorer");
        backButton.setVisibility(View.GONE);
        getBiblesBtn.setVisibility(View.VISIBLE);
        clearContent();
        currentDisplayingVerse = null;

        TextView welcomeText = new TextView(this);
        welcomeText.setText("Benvenuto in Bible Explorer!\n\nSchiaccia 'Get Bibles' per sentire l'immenso amore di nostro signore Gesù Cristo.");
        welcomeText.setTextSize(16);
        welcomeText.setPadding(16, 16, 16, 16);
        contentLayout.addView(welcomeText);
    }

    private void showLanguageSelectionScreen() {
        updateNavigationState(NavigationState.LANGUAGE_SELECTION);
        headerTextView.setText("Select a Language");
        clearContent();
        currentDisplayingVerse = null;
        selectedLanguageId = null;
        selectedLanguageName = null;

        addLanguageButton("All Languages", null);
        addLanguageButton("English", "eng");
        addLanguageButton("Italian", "ita");
        addLanguageButton("Spanish", "spa");
        addLanguageButton("French", "fra");
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
        currentDisplayingVerse = null;
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
                    showLanguageSelectionScreen();
                }
                break;
            case CHAPTERS:
                if (selectedBible != null) {
                    fetchBooks(selectedBible);
                }
                break;
            case VERSES_OR_PASSAGE:
                if (selectedBible != null && selectedBook != null) {
                    fetchChapters(selectedBible, selectedBook);
                }
                break;
            case VERSES_LIST:
                if (selectedBible != null && selectedBook != null && selectedChapter != null) {
                    showChapterOptions(selectedBible, selectedBook, selectedChapter);
                }
                break;
            case SINGLE_VERSE_DISPLAY:
                if (selectedBible != null && selectedChapter != null) {
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

    private void fetchBiblesList(String languageId, String languageDisplayName) {
        selectedLanguageId = languageId;
        selectedLanguageName = languageDisplayName;
        updateNavigationState(NavigationState.BIBLES_LIST);
        if (languageId != null && !languageDisplayName.equals("All Languages")) {
            headerTextView.setText("Bibles in " + languageDisplayName);
        } else {
            headerTextView.setText("Select a Bible Translation");
        }
        clearContent();
        showLoadingMessage("Loading Bible translations...");

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
                        Button bibleButton = createBibleButton(bible);
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
        updateNavigationState(NavigationState.VERSES_OR_PASSAGE);
        headerTextView.setText(chapter.getReference());
        clearContent();

        TextView infoText = new TextView(this);
        infoText.setText("What would you like to see?");
        infoText.setTextSize(16);
        infoText.setPadding(16, 16, 16, 16);
        contentLayout.addView(infoText);

        Button versesButton = new Button(this);
        versesButton.setText("View Verses List");
        versesButton.setLayoutParams(createButtonLayoutParams());
        versesButton.setOnClickListener(v -> fetchVersesListDisplay(bible, chapter));
        contentLayout.addView(versesButton);

        Button passageButton = new Button(this);
        passageButton.setText("Read Full Chapter");
        passageButton.setLayoutParams(createButtonLayoutParams());
        passageButton.setOnClickListener(v -> fetchPassage(bible, chapter));
        contentLayout.addView(passageButton);
    }

    private void fetchVersesListDisplay(Bible bible, Chapter chapter) {
        updateNavigationState(NavigationState.VERSES_LIST);
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
                        Button verseButton = createVerseButton(verse);
                        contentLayout.addView(verseButton);
                    }

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
        updateNavigationState(NavigationState.VERSES_OR_PASSAGE);
        headerTextView.setText("Reading " + chapter.getReference());
        clearContent();
        showLoadingMessage("Loading chapter content...");

        apiClient.getPassage(bible.getId(), chapter.getId(), new BibleApiClient.PassageCallback() {
            @Override
            public void onSuccess(Passage passage, Passage.Meta meta) {
                runOnUiThread(() -> {
                    clearContent();
                   
                    TextView infoView = new TextView(MainActivity.this);
                    infoView.setText("Reference: " + passage.getReference() +
                            "\nVerses: " + passage.getVerseCount());
                    infoView.setTextSize(14);
                    infoView.setPadding(16, 16, 16, 8);
                    infoView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));
                    contentLayout.addView(infoView);

                    TextView contentView = new TextView(MainActivity.this);
                    String htmlContent = passage.getContent();

                    if (htmlContent != null && !htmlContent.isEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            contentView.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            contentView.setText(Html.fromHtml(htmlContent));
                        }
                    } else {
                        contentView.setText("No content available for this passage.");
                    }
                    contentView.setTextSize(16);
                    contentView.setPadding(16, 8, 16, 16);
                    contentView.setLineSpacing(4, 1.2f);
                    contentLayout.addView(contentView);

                    if (passage.getCopyright() != null && !passage.getCopyright().isEmpty()) {
                        TextView copyrightView = new TextView(MainActivity.this);
                        String copyrightHtml = passage.getCopyright();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            copyrightView.setText(Html.fromHtml(copyrightHtml, Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            copyrightView.setText(Html.fromHtml(copyrightHtml));
                        }
                        copyrightView.setTextSize(12);
                        copyrightView.setPadding(16, 16, 16, 8);
                        copyrightView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));
                        contentLayout.addView(copyrightView);
                    }

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

    private Button createVerseButton(Verse verse) {
        Button button = new Button(this);
        button.setText(verse.getReference());
        button.setLayoutParams(createButtonLayoutParams());
        button.setOnClickListener(v -> {
            Toast.makeText(this, "Loading: " + verse.getReference(), Toast.LENGTH_SHORT).show();
            showSingleVerseContent(verse);
        });
        return button;
    }

   
    private void showSingleVerseContent(Verse initialVerseFromList) {
        if (selectedBible == null) {
            Toast.makeText(this, "Error: Bible context not available.", Toast.LENGTH_LONG).show();
            return;
        }
        if (initialVerseFromList == null || initialVerseFromList.getId() == null) {
            Toast.makeText(this, "Error: Verse data not available.", Toast.LENGTH_LONG).show();
            return;
        }
       
        fetchAndDisplaySingleVerse(selectedBible.getId(), initialVerseFromList.getId());
    }

   
    private void fetchAndDisplaySingleVerse(String bibleId, String verseId) {
        updateNavigationState(NavigationState.SINGLE_VERSE_DISPLAY);
        clearContent();
       
        headerTextView.setText("Loading Verse...");
        showLoadingMessage("Loading verse " + verseId + "...");

        apiClient.getSingleVerse(bibleId, verseId, new BibleApiClient.SingleVerseCallback() {
            @Override
            public void onSuccess(SingleVerse singleVerseData, SingleVerse.Meta meta) {
                runOnUiThread(() -> {
                    currentDisplayingVerse = singleVerseData;
                    clearContent();

                    if (singleVerseData == null) {
                        headerTextView.setText("Verse Error");
                        showErrorMessage("Failed to load verse data.");
                        addPrevNextButtonsToLayout(null, null);
                        return;
                    }

                    headerTextView.setText(singleVerseData.getReference() != null ? singleVerseData.getReference() : "Verse");

                    if (singleVerseData.getContent() == null || singleVerseData.getContent().isEmpty()) {
                        showErrorMessage("No content available for this verse: " + singleVerseData.getReference());
                        addPrevNextButtonsToLayout(singleVerseData.getPrevious(), singleVerseData.getNext());
                        return;
                    }

                    TextView verseContentView = new TextView(MainActivity.this);
                    String htmlContent = singleVerseData.getContent();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        verseContentView.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        verseContentView.setText(Html.fromHtml(htmlContent));
                    }
                    verseContentView.setTextSize(18);
                    verseContentView.setPadding(16, 16, 16, 16);
                    verseContentView.setLineSpacing(6, 1.3f);
                    contentLayout.addView(verseContentView);

                    addPrevNextButtonsToLayout(singleVerseData.getPrevious(), singleVerseData.getNext());

                    if (singleVerseData.getCopyright() != null && !singleVerseData.getCopyright().isEmpty()) {
                        TextView copyrightView = new TextView(MainActivity.this);
                        String copyrightHtml = singleVerseData.getCopyright();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            copyrightView.setText(Html.fromHtml(copyrightHtml, Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            copyrightView.setText(Html.fromHtml(copyrightHtml));
                        }
                        copyrightView.setTextSize(12);
                        copyrightView.setPadding(16, 8, 16, 16);
                        copyrightView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));
                        contentLayout.addView(copyrightView);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    currentDisplayingVerse = null;
                    clearContent();
                    headerTextView.setText("Verse Error");
                    showErrorMessage("Error loading verse: " + error);
                    addPrevNextButtonsToLayout(null, null);
                });
            }
        });
    }

    private void addPrevNextButtonsToLayout(SingleVerse.Navigation prevNav, SingleVerse.Navigation nextNav) {
        LinearLayout navButtonLayout = new LinearLayout(this);
        navButtonLayout.setOrientation(LinearLayout.HORIZONTAL);
        navButtonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        navButtonLayout.setPadding(16, 24, 16, 16);

       
        Button prevButton = new Button(this);
        prevButton.setText("<< Previous");
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        buttonParams.setMarginEnd(8);
        prevButton.setLayoutParams(buttonParams);

        if (prevNav != null && prevNav.getId() != null && selectedBible != null) {
            prevButton.setEnabled(true);
            prevButton.setOnClickListener(v -> fetchAndDisplaySingleVerse(selectedBible.getId(), prevNav.getId()));
        } else {
            prevButton.setEnabled(false);
        }
        navButtonLayout.addView(prevButton);

       
        Button nextButton = new Button(this);
        nextButton.setText("Next >>");
        LinearLayout.LayoutParams nextButtonParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        nextButtonParams.setMarginStart(8);
        nextButton.setLayoutParams(nextButtonParams);

        if (nextNav != null && nextNav.getId() != null && selectedBible != null) {
            nextButton.setEnabled(true);
            nextButton.setOnClickListener(v -> fetchAndDisplaySingleVerse(selectedBible.getId(), nextNav.getId()));
        } else {
            nextButton.setEnabled(false);
        }
        navButtonLayout.addView(nextButton);

        contentLayout.addView(navButtonLayout);
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
       
        TextView errorText = new TextView(this);
        errorText.setText(message);
        errorText.setTextSize(16);
        errorText.setPadding(16, 32, 16, 16);
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