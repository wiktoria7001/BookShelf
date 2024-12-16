package com.dsw.pam.bookshelf.utils

import com.google.firebase.firestore.FirebaseFirestore

fun saveSearchQuery(query: String) {
    val db = FirebaseFirestore.getInstance()
    val searchEntry = hashMapOf(
        "query" to query,
        "timestamp" to System.currentTimeMillis()
    )

    db.collection("searches")
        .add(searchEntry)
        .addOnSuccessListener {
            println("Search query saved successfully.")
        }
        .addOnFailureListener {
            println("Error saving search query: $it")
        }
}