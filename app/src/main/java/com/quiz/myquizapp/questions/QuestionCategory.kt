package com.quiz.myquizapp.questions

enum class QuestionCategory (val displayName: String, val url: String){
        SANAT("Sanat", "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_art"),
        BILIM("Bilim", "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_science"),
        TARIH("Tarih", "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_history"),
        COGRAFYA("Coğrafya", "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_geography"),
        EDEBIYAT("Edebiyat", "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_literature"),
        SINEMA("Sinema", "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_movies"),
        SPOR("Spor", "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_sports"),
        TEKNOLOJI("Teknoloji", "https://raw.githubusercontent.com/ikrabu/Quiz/refs/heads/main/questions_technology");

        companion object {
            fun fromDisplayName(name: String): QuestionCategory {
                return values().firstOrNull { it.displayName == name } ?: TEKNOLOJI
            }
        }
    }

