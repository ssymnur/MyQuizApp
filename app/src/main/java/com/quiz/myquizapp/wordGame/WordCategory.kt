package com.quiz.myquizapp.wordGame


enum class WordCategory(val displayName: String, val url: String) {
    SANAT("Sanat", "https://raw.githubusercontent.com/ssymnur/WordGame/refs/heads/main/art.json"),
    TEKNOLOJI("Teknoloji", "https://raw.githubusercontent.com/ssymnur/WordGame/refs/heads/main/technology.json"),
    TARIH("Tarih", "https://raw.githubusercontent.com/ssymnur/WordGame/refs/heads/main/history.json"),
    SPOR("Spor", "https://raw.githubusercontent.com/ssymnur/WordGame/refs/heads/main/sports.json"),
    EDEBIYAT("Edebiyat", "https://raw.githubusercontent.com/ssymnur/WordGame/refs/heads/main/literature.json"),
    COGRAFYA("Coğrafya", "https://raw.githubusercontent.com/ssymnur/WordGame/refs/heads/main/geography.json"),
    SINEMA("Sinema", "https://raw.githubusercontent.com/ssymnur/WordGame/refs/heads/main/movie.json"),
    BILIM("Bilim", "https://raw.githubusercontent.com/ssymnur/WordGame/refs/heads/main/science.json");


    companion object {
        fun fromDisplayName(name: String): WordCategory {
            return values().firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: SANAT
        }
    }
}
