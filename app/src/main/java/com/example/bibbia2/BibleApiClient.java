package com.example.bibbia2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import android.content.Context;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibleApiClient {
    private static final String BASE_URL = "https://api.scripture.api.bible/v1";
    private RequestQueue requestQueue;
    private Gson gson;
    private String apiKey;

    public BibleApiClient(Context context, String apiKey) {
        this.requestQueue = Volley.newRequestQueue(context);
        this.gson = new Gson();
        this.apiKey = apiKey;
    }

   
    public static class BiblesResponse {
        private List<Bible> data;

        public List<Bible> getData() { return data; }
        public void setData(List<Bible> data) { this.data = data; }
    }

    public static class BooksResponse {
        private List<Book> data;

        public List<Book> getData() { return data; }
        public void setData(List<Book> data) { this.data = data; }
    }

    public static class ChaptersResponse {
        private List<Chapter> data;

        public List<Chapter> getData() { return data; }
        public void setData(List<Chapter> data) { this.data = data; }
    }

    public static class PassageResponse {
        private Passage data;
        private Passage.Meta meta;

        public Passage getData() { return data; }
        public void setData(Passage data) { this.data = data; }
        public Passage.Meta getMeta() { return meta; }
        public void setMeta(Passage.Meta meta) { this.meta = meta; }
    }

    public static class VersesResponse {
        private List<Verse> data;

        public List<Verse> getData() { return data; }
        public void setData(List<Verse> data) { this.data = data; }
    }

    public static class SingleVerseResponse {
        private SingleVerse data;
        private SingleVerse.Meta meta;

        public SingleVerse getData() { return data; }
        public void setData(SingleVerse data) { this.data = data; }
        public SingleVerse.Meta getMeta() { return meta; }
        public void setMeta(SingleVerse.Meta meta) { this.meta = meta; }
    }

   
    public interface BiblesCallback {
        void onSuccess(Bible[] bibles);
        void onError(String error);
    }

    public interface BooksCallback {
        void onSuccess(Book[] books);
        void onError(String error);
    }

    public interface ChaptersCallback {
        void onSuccess(Chapter[] chapters);
        void onError(String error);
    }

    public interface PassageCallback {
        void onSuccess(Passage passage, Passage.Meta meta);
        void onError(String error);
    }

    public interface VersesCallback {
        void onSuccess(Verse[] verses);
        void onError(String error);
    }

    public interface SingleVerseCallback {
        void onSuccess(SingleVerse singleVerse, SingleVerse.Meta meta);
        void onError(String error);
    }

   
    public void getBibles(String languageId, BiblesCallback callback) {
        String url = BASE_URL + "/bibles";
        if (languageId != null && !languageId.isEmpty()) {
            url += "?language=" + languageId;
        }


        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            BiblesResponse biblesResponse = gson.fromJson(response.toString(), BiblesResponse.class);
                            Bible[] biblesArray = biblesResponse.getData().toArray(new Bible[0]);
                            callback.onSuccess(biblesArray);
                        } catch (Exception e) {
                            callback.onError("Error parsing bibles response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("api-key", apiKey);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    public void getBooks(String bibleId, BooksCallback callback) {
        String url = BASE_URL + "/bibles/" + bibleId + "/books";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            BooksResponse booksResponse = gson.fromJson(response.toString(), BooksResponse.class);
                            Book[] booksArray = booksResponse.getData().toArray(new Book[0]);
                            callback.onSuccess(booksArray);
                        } catch (Exception e) {
                            callback.onError("Error parsing books response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("api-key", apiKey);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    public void getChapters(String bibleId, String bookId, ChaptersCallback callback) {
        String url = BASE_URL + "/bibles/" + bibleId + "/books/" + bookId + "/chapters";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ChaptersResponse chaptersResponse = gson.fromJson(response.toString(), ChaptersResponse.class);
                            Chapter[] chaptersArray = chaptersResponse.getData().toArray(new Chapter[0]);
                            callback.onSuccess(chaptersArray);
                        } catch (Exception e) {
                            callback.onError("Error parsing chapters response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("api-key", apiKey);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    public void getPassage(String bibleId, String passageId, PassageCallback callback) {
        String url = BASE_URL + "/bibles/" + bibleId + "/passages/" + passageId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            PassageResponse passageResponse = gson.fromJson(response.toString(), PassageResponse.class);
                            callback.onSuccess(passageResponse.getData(), passageResponse.getMeta());
                        } catch (Exception e) {
                            callback.onError("Error parsing passage response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("api-key", apiKey);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    public void getVerses(String bibleId, String chapterId, VersesCallback callback) {
        String url = BASE_URL + "/bibles/" + bibleId + "/chapters/" + chapterId + "/verses";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VersesResponse versesResponse = gson.fromJson(response.toString(), VersesResponse.class);
                            Verse[] versesArray = versesResponse.getData().toArray(new Verse[0]);
                            callback.onSuccess(versesArray);
                        } catch (Exception e) {
                            callback.onError("Error parsing verses response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("api-key", apiKey);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    public void getSingleVerse(String bibleId, String verseId, SingleVerseCallback callback) {
        String url = BASE_URL + "/bibles/" + bibleId + "/verses/" + verseId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            SingleVerseResponse singleVerseResponse = gson.fromJson(response.toString(), SingleVerseResponse.class);
                            callback.onSuccess(singleVerseResponse.getData(), singleVerseResponse.getMeta());
                        } catch (Exception e) {
                            callback.onError("Error parsing single verse response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("api-key", apiKey);
                return headers;
            }
        };

        requestQueue.add(request);
    }

   
    public void cancelAllRequests() {
        requestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }
}