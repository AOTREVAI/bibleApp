package com.example.bibbia2;

public class SingleVerse {
    private String id;
    private String orgId;
    private String bibleId;
    private String bookId;
    private String chapterId;
    private String content;
    private String reference;
    private int verseCount;
    private String copyright;
    private Navigation next;
    private Navigation previous;

    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getBibleId() {
        return bibleId;
    }

    public void setBibleId(String bibleId) {
        this.bibleId = bibleId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public int getVerseCount() {
        return verseCount;
    }

    public void setVerseCount(int verseCount) {
        this.verseCount = verseCount;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public Navigation getNext() {
        return next;
    }

    public void setNext(Navigation next) {
        this.next = next;
    }

    public Navigation getPrevious() {
        return previous;
    }

    public void setPrevious(Navigation previous) {
        this.previous = previous;
    }

    
    public static class Navigation {
        private String id;
        private String bookId;

        
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
        }
    }

    
    public static class Meta {
        private String fums;
        private String fumsId;
        private String fumsJsInclude;
        private String fumsJs;
        private String fumsNoScript;

        
        public String getFums() {
            return fums;
        }

        public void setFums(String fums) {
            this.fums = fums;
        }

        public String getFumsId() {
            return fumsId;
        }

        public void setFumsId(String fumsId) {
            this.fumsId = fumsId;
        }

        public String getFumsJsInclude() {
            return fumsJsInclude;
        }

        public void setFumsJsInclude(String fumsJsInclude) {
            this.fumsJsInclude = fumsJsInclude;
        }

        public String getFumsJs() {
            return fumsJs;
        }

        public void setFumsJs(String fumsJs) {
            this.fumsJs = fumsJs;
        }

        public String getFumsNoScript() {
            return fumsNoScript;
        }

        public void setFumsNoScript(String fumsNoScript) {
            this.fumsNoScript = fumsNoScript;
        }
    }
}