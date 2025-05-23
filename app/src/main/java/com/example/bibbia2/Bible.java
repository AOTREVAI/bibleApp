package com.example.bibbia2;


import java.util.List;

public class Bible {
    private String id;
    private String dblId;
    private String abbreviation;
    private String abbreviationLocal;
    private Language language;
    private List<Country> countries;
    private String name;
    private String nameLocal;
    private String description;
    private String descriptionLocal;
    private String relatedDbl;
    private String type;
    private String updatedAt;
    private List<AudioBible> audioBibles;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDblId() {
        return dblId;
    }

    public void setDblId(String dblId) {
        this.dblId = dblId;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviationLocal() {
        return abbreviationLocal;
    }

    public void setAbbreviationLocal(String abbreviationLocal) {
        this.abbreviationLocal = abbreviationLocal;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public List<Country> getCountries() {
        return countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameLocal() {
        return nameLocal;
    }

    public void setNameLocal(String nameLocal) {
        this.nameLocal = nameLocal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionLocal() {
        return descriptionLocal;
    }

    public void setDescriptionLocal(String descriptionLocal) {
        this.descriptionLocal = descriptionLocal;
    }

    public String getRelatedDbl() {
        return relatedDbl;
    }

    public void setRelatedDbl(String relatedDbl) {
        this.relatedDbl = relatedDbl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<AudioBible> getAudioBibles() {
        return audioBibles;
    }

    public void setAudioBibles(List<AudioBible> audioBibles) {
        this.audioBibles = audioBibles;
    }

    // Nested Language class
    public static class Language {
        private String id;
        private String name;
        private String nameLocal;
        private String script;
        private String scriptDirection;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNameLocal() {
            return nameLocal;
        }

        public void setNameLocal(String nameLocal) {
            this.nameLocal = nameLocal;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public String getScriptDirection() {
            return scriptDirection;
        }

        public void setScriptDirection(String scriptDirection) {
            this.scriptDirection = scriptDirection;
        }
    }

    // Nested Country class
    public static class Country {
        private String id;
        private String name;
        private String nameLocal;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNameLocal() {
            return nameLocal;
        }

        public void setNameLocal(String nameLocal) {
            this.nameLocal = nameLocal;
        }
    }

    // Nested AudioBible class
    public static class AudioBible {
        private String id;
        private String name;
        private String nameLocal;
        private String description;
        private String descriptionLocal;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNameLocal() {
            return nameLocal;
        }

        public void setNameLocal(String nameLocal) {
            this.nameLocal = nameLocal;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescriptionLocal() {
            return descriptionLocal;
        }

        public void setDescriptionLocal(String descriptionLocal) {
            this.descriptionLocal = descriptionLocal;
        }
    }
}