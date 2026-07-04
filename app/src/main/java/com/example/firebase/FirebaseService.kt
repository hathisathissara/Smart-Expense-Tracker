package com.example.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseService(private val context: Context) {

    private val tag = "FirebaseService"

    val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    // Safely get Firebase Auth instance
    val auth: FirebaseAuth?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.e(tag, "FirebaseAuth initialization failed: ${e.message}")
                null
            }
        } else {
            null
        }

    // Safely get Firestore instance
    val firestore: FirebaseFirestore?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e(tag, "FirebaseFirestore initialization failed: ${e.message}")
                null
            }
        } else {
            null
        }

    // Safely get Storage instance
    val storage: FirebaseStorage?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseStorage.getInstance()
            } catch (e: Exception) {
                Log.e(tag, "FirebaseStorage initialization failed: ${e.message}")
                null
            }
        } else {
            null
        }

    // Real Firebase Auth signup
    suspend fun signUpWithFirebase(email: String, password: CharArray): Boolean {
        val authInstance = auth ?: return false
        return try {
            authInstance.createUserWithEmailAndPassword(email, String(password)).await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Firebase sign up failed: ${e.message}")
            false
        }
    }

    // Real Firebase Auth signin
    suspend fun signInWithFirebase(email: String, password: CharArray): Boolean {
        val authInstance = auth ?: return false
        return try {
            authInstance.signInWithEmailAndPassword(email, String(password)).await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Firebase sign in failed: ${e.message}")
            false
        }
    }

    // Real Firebase Auth Google sign-in
    suspend fun signInWithGoogle(idToken: String): Boolean {
        val authInstance = auth ?: return false
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            authInstance.signInWithCredential(credential).await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Firebase Google sign in failed: ${e.message}")
            false
        }
    }

    // Real Firebase Auth Facebook sign-in
    suspend fun signInWithFacebook(accessToken: String): Boolean {
        val authInstance = auth ?: return false
        return try {
            val credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(accessToken)
            authInstance.signInWithCredential(credential).await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Firebase Facebook sign in failed: ${e.message}")
            false
        }
    }

    // Real Firestore push
    suspend fun syncDataToCloud(userId: String, data: Map<String, Any>): Boolean {
        val db = firestore ?: return false
        return try {
            db.collection("users").document(userId).set(data).await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Firestore sync failed: ${e.message}")
            false
        }
    }

    // Real Storage upload for backup files or profile picture
    suspend fun uploadProfilePicture(userId: String, fileBytes: ByteArray): String? {
        val storageRef = storage ?: return null
        return try {
            val ref = storageRef.reference.child("users/$userId/profile_picture.jpg")
            ref.putBytes(fileBytes).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(tag, "Firebase Storage upload failed: ${e.message}")
            null
        }
    }
}
