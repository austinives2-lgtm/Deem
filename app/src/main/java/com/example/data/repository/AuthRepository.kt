package com.example.data.repository

import com.example.data.model.UserModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        const val SUPER_ADMIN_UID = "YNxF7YAv0XYqkZHgaX6mtxAbG103"
        const val ORG_ID = "bd_seva_org"
    }

    private val _currentUserProfile = MutableStateFlow<UserModel?>(null)
    val currentUserProfile: StateFlow<UserModel?> = _currentUserProfile.asStateFlow()

    private var profileListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                listenToUserProfile(firebaseUser.uid)
            } else {
                profileListener?.remove()
                profileListener = null
                _currentUserProfile.value = null
            }
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    private fun listenToUserProfile(uid: String) {
        profileListener?.remove()
        profileListener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    if (uid == SUPER_ADMIN_UID) {
                        _currentUserProfile.value = UserModel(
                            uid = uid,
                            name = auth.currentUser?.displayName ?: "Super Admin",
                            email = auth.currentUser?.email ?: "",
                            role = "super_admin",
                            organizationId = ORG_ID
                        )
                    }
                    return@addSnapshotListener
                }

                val user = snapshot.toObject(UserModel::class.java)
                if (user != null) {
                    val finalRole = if (uid == SUPER_ADMIN_UID) "super_admin" else user.role
                    _currentUserProfile.value = user.copy(
                        uid = uid,
                        role = finalRole
                    )
                }
            }
    }

    suspend fun login(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("লগইন ব্যর্থ হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।")
            // Ensure document exists
            checkAndSeedUserDoc(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        pass: String,
        phone: String,
        bloodGroup: String,
        address: String,
        photoUrl: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("নিবন্ধন ব্যর্থ হয়েছে।")
            val isSuper = (user.uid == SUPER_ADMIN_UID)
            val userModel = UserModel(
                uid = user.uid,
                name = name.trim(),
                email = email.trim(),
                phone = phone.trim(),
                photoURL = photoUrl.trim(),
                organizationId = ORG_ID,
                role = if (isSuper) "super_admin" else "member",
                permissions = if (isSuper) listOf("all") else emptyList(),
                status = "active",
                bloodGroup = bloodGroup.trim(),
                position = if (isSuper) "প্রধান উপদেষ্টা / সুপার অ্যাডমিন" else "সদস্য",
                address = address.trim(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            firestore.collection("users").document(user.uid).set(userModel).await()
            _currentUserProfile.value = userModel
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun checkAndSeedUserDoc(user: FirebaseUser) {
        val doc = firestore.collection("users").document(user.uid).get().await()
        if (!doc.exists()) {
            val isSuper = (user.uid == SUPER_ADMIN_UID)
            val model = UserModel(
                uid = user.uid,
                name = user.displayName ?: if (isSuper) "Super Admin" else "সদস্য",
                email = user.email ?: "",
                organizationId = ORG_ID,
                role = if (isSuper) "super_admin" else "member",
                status = "active",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            firestore.collection("users").document(user.uid).set(model).await()
        }
    }

    suspend fun updateProfile(
        name: String,
        phone: String,
        bloodGroup: String,
        address: String,
        photoURL: String,
        isAvailableForBlood: Boolean
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("লগইন করা নেই"))
        return try {
            val updates = mapOf(
                "name" to name.trim(),
                "phone" to phone.trim(),
                "bloodGroup" to bloodGroup.trim(),
                "address" to address.trim(),
                "photoURL" to photoURL.trim(),
                "isAvailableForBlood" to isAvailableForBlood,
                "updatedAt" to Timestamp.now()
            )
            firestore.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        profileListener?.remove()
        profileListener = null
        auth.signOut()
        _currentUserProfile.value = null
    }
}
