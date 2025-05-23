package com.example.bibbia2;

import android.content.Context;
import android.util.Log;

public class BibleApiExample {
    private static final String TAG = "BibleApiExample";
    private BibleApiClient apiClient;

    public BibleApiExample(Context context, String apiKey) {
        this.apiClient = new BibleApiClient(context, apiKey);
    }

    public void demonstrateApiUsage() {
        // Example 1: Get all available Bibles
        getBiblesExample();
    }

    private void getBiblesExample() {
        Log.d(TAG, "Fetching all available Bibles...");
        
        apiClient.getBibles(new BibleApiClient.BiblesCallback() {
            @Override
            public void onSuccess(Bible[] bibles) {
                Log.d(TAG, "Successfully fetched " + bibles.length + " Bibles");
                
                for (Bible bible : bibles) {
                    Log.d(TAG, "Bible: " + bible.getName() + " (" + bible.getAbbreviation() + ")");
                    if (bible.getLanguage() != null) {
                        Log.d(TAG, "  Language: " + bible.getLanguage().getName());
                    }
                }

                // After getting bibles, fetch books for the first bible
                if (bibles.length > 0) {
                    getBooksExample(bibles[0].getId());
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching Bibles: " + error);
            }
        });
    }

    private void getBooksExample(String bibleId) {
        Log.d(TAG, "Fetching books for Bible ID: " + bibleId);
        
        apiClient.getBooks(bibleId, new BibleApiClient.BooksCallback() {
            @Override
            public void onSuccess(Book[] books) {
                Log.d(TAG, "Successfully fetched " + books.length + " books");
                
                for (Book book : books) {
                    Log.d(TAG, "Book: " + book.getName() + " (" + book.getAbbreviation() + ")");
                    if (book.getChapters() != null) {
                        Log.d(TAG, "  Chapters: " + book.getChapters().size());
                    }
                }

                // After getting books, fetch chapters for the first book
                if (books.length > 0) {
                    getChaptersExample(bibleId, books[0].getId());
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching books: " + error);
            }
        });
    }

    private void getChaptersExample(String bibleId, String bookId) {
        Log.d(TAG, "Fetching chapters for Bible ID: " + bibleId + ", Book ID: " + bookId);
        
        apiClient.getChapters(bibleId, bookId, new BibleApiClient.ChaptersCallback() {
            @Override
            public void onSuccess(Chapter[] chapters) {
                Log.d(TAG, "Successfully fetched " + chapters.length + " chapters");
                
                for (Chapter chapter : chapters) {
                    Log.d(TAG, "Chapter: " + chapter.getNumber() + " - " + chapter.getReference());
                }

                // After getting chapters, fetch verses for the first chapter
                if (chapters.length > 0) {
                    getVersesExample(bibleId, chapters[0].getId());
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching chapters: " + error);
            }
        });
    }

    private void getVersesExample(String bibleId, String chapterId) {
        Log.d(TAG, "Fetching verses for Bible ID: " + bibleId + ", Chapter ID: " + chapterId);
        
        apiClient.getVerses(bibleId, chapterId, new BibleApiClient.VersesCallback() {
            @Override
            public void onSuccess(Verse[] verses) {
                Log.d(TAG, "Successfully fetched " + verses.length + " verses");
                
                for (Verse verse : verses) {
                    Log.d(TAG, "Verse: " + verse.getReference());
                }

                // After getting verses, fetch a passage (using first verse ID as passage ID)
                if (verses.length > 0) {
                    getPassageExample(bibleId, verses[0].getId());
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching verses: " + error);
            }
        });
    }

    private void getPassageExample(String bibleId, String passageId) {
        Log.d(TAG, "Fetching passage for Bible ID: " + bibleId + ", Passage ID: " + passageId);
        
        apiClient.getPassage(bibleId, passageId, new BibleApiClient.PassageCallback() {
            @Override
            public void onSuccess(Passage passage, Passage.Meta meta) {
                Log.d(TAG, "Successfully fetched passage");
                Log.d(TAG, "Passage Reference: " + passage.getReference());
                Log.d(TAG, "Passage Content: " + passage.getContent());
                Log.d(TAG, "Verse Count: " + passage.getVerseCount());
                
                if (meta != null) {
                    Log.d(TAG, "Meta FUMS: " + meta.getFums());
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching passage: " + error);
            }
        });
    }

    // Method to clean up when done
    public void cleanup() {
        if (apiClient != null) {
            apiClient.cancelAllRequests();
        }
    }
}