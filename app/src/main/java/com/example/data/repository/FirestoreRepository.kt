package com.example.data.repository

import com.example.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        const val ORG_ID = "bd_seva_org"
    }

    // ==========================================
    // 1. SERVICES
    // ==========================================
    fun getServices(): Flow<List<ServiceItem>> = callbackFlow {
        val listener = firestore.collection("services")
            .whereEqualTo("organizationId", ORG_ID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ServiceItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addService(service: ServiceItem): Result<Unit> = runCatching {
        val docRef = firestore.collection("services").document()
        docRef.set(service.copy(id = docRef.id, organizationId = ORG_ID, createdAt = Timestamp.now())).await()
    }

    suspend fun updateService(service: ServiceItem): Result<Unit> = runCatching {
        firestore.collection("services").document(service.id).set(service).await()
    }

    suspend fun deleteService(serviceId: String): Result<Unit> = runCatching {
        firestore.collection("services").document(serviceId).delete().await()
    }

    suspend fun submitServiceRequest(request: ServiceRequest): Result<Unit> = runCatching {
        val docRef = firestore.collection("serviceRequests").document()
        docRef.set(request.copy(id = docRef.id, organizationId = ORG_ID, createdAt = Timestamp.now())).await()
    }

    fun getServiceRequests(): Flow<List<ServiceRequest>> = callbackFlow {
        val listener = firestore.collection("serviceRequests")
            .whereEqualTo("organizationId", ORG_ID)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ServiceRequest::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateServiceRequestStatus(id: String, status: String): Result<Unit> = runCatching {
        firestore.collection("serviceRequests").document(id).update("status", status).await()
    }

    // Default 12 Humanitarian Categories Seeding
    suspend fun seedDefaultServicesIfEmpty(): Result<Unit> = runCatching {
        val snap = firestore.collection("services")
            .whereEqualTo("organizationId", ORG_ID)
            .limit(1)
            .get()
            .await()

        if (snap.isEmpty) {
            val defaultServices = listOf(
                "রক্তদান সেবা" to "জরুরি রোগীদের জন্য রক্তদাতা খুঁজে দেওয়া ও রক্তদান শিবিরের আয়োজন।",
                "চিকিৎসা সহায়তা" to "অসহায় রোগীদের বিনামূল্যে চিকিৎসা, ওষুধ প্রদান ও অ্যাম্বুলেন্স সহায়তা।",
                "খাদ্য বিতরণ" to "দুস্থ, ক্ষুধার্ত ও ছিন্নমূল মানুষের মাঝে নিয়মিত পুষ্টিকর খাদ্য বিতরণ কর্মসূচি।",
                "শিক্ষা সহায়তা" to "মেধাবী দরিদ্র শিক্ষার্থীদের বই, খাতা, বৃত্তি ও শিক্ষা উপকরণ প্রদান।",
                "শীতবস্ত্র বিতরণ" to "শীতকালে শীতার্তদের মাঝে কম্বল, সোয়েটার ও গরম কাপড় বিতরণ।",
                "ত্রাণ ও পুনর্বাসন" to "বন্যা, ঘূর্ণিঝড় ও প্রাকৃতিক দুর্যোগে ক্ষতিগ্রস্তদের জরুরি ত্রাণ ও ঘর নির্মাণ।",
                "জরুরি সহায়তা" to "যেকোনো আকস্মিক দুর্ঘটনা বা সংকটে তাৎক্ষণিক ভলান্টিয়ার ও আর্থিক সাহায্য।",
                "দরিদ্র পরিবারকে সহায়তা" to "কর্মসংস্থানের সুযোগ সৃষ্টি, ক্ষুদ্র অনুদান ও স্বাবলম্বী করার উদ্যোগ।",
                "গৃহনির্মাণ ও মেরামত" to "গৃহহীন অসহায় মানুষের নিরাপদ আশ্রয় ও ভাঙা ঘর মেরামতে সহায়তা।",
                "পরিবেশ ও বৃক্ষরোপণ" to "পরিবেশ সুরক্ষা, প্লাস্টিক বর্জন সচেতনতা ও দেশব্যাপী বৃক্ষরোপণ অভিযান।",
                "স্বেচ্ছাসেবক সেবা" to "সামাজিক উৎসব, পরিচ্ছন্নতা অভিযান ও মানবিক কাজে নিবেদিত ভলান্টিয়ার টিম।",
                "অন্যান্য মানবিক সেবা" to "যেকোনো মানবিক ডাকে সাড়া দিয়ে মানবতার কল্যাণে নিরবচ্ছিন্ন সেবা।"
            )

            for ((titleBn, desc) in defaultServices) {
                val doc = firestore.collection("services").document()
                doc.set(
                    ServiceItem(
                        id = doc.id,
                        title = titleBn,
                        titleBn = titleBn,
                        description = desc,
                        categoryKey = titleBn,
                        isActive = true,
                        organizationId = ORG_ID,
                        createdAt = Timestamp.now()
                    )
                ).await()
            }
        }
    }

    // ==========================================
    // 2. BLOOD DONATION
    // ==========================================
    fun getBloodRequests(): Flow<List<BloodRequest>> = callbackFlow {
        val listener = firestore.collection("bloodRequests")
            .whereEqualTo("organizationId", ORG_ID)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(BloodRequest::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createBloodRequest(request: BloodRequest): Result<Unit> = runCatching {
        val docRef = firestore.collection("bloodRequests").document()
        docRef.set(request.copy(id = docRef.id, organizationId = ORG_ID, createdAt = Timestamp.now())).await()
    }

    suspend fun updateBloodRequestStatus(id: String, status: String): Result<Unit> = runCatching {
        firestore.collection("bloodRequests").document(id).update("status", status).await()
    }

    fun getBloodDonors(): Flow<List<UserModel>> = callbackFlow {
        val listener = firestore.collection("users")
            .whereEqualTo("organizationId", ORG_ID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(UserModel::class.java)?.copy(uid = doc.id)
                }?.filter { it.bloodGroup.isNotBlank() } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // ==========================================
    // 3. HUMANITARIAN ACTIVITIES
    // ==========================================
    fun getActivities(): Flow<List<ActivityItem>> = callbackFlow {
        val listener = firestore.collection("activities")
            .whereEqualTo("organizationId", ORG_ID)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ActivityItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createActivity(activity: ActivityItem): Result<Unit> = runCatching {
        val docRef = firestore.collection("activities").document()
        docRef.set(activity.copy(id = docRef.id, organizationId = ORG_ID, createdAt = Timestamp.now())).await()
    }

    suspend fun updateActivity(activity: ActivityItem): Result<Unit> = runCatching {
        firestore.collection("activities").document(activity.id).set(activity).await()
    }

    suspend fun deleteActivity(activityId: String): Result<Unit> = runCatching {
        firestore.collection("activities").document(activityId).delete().await()
    }

    // ==========================================
    // 4. FINANCE (CONTRIBUTIONS, DONATIONS, EXPENSES)
    // ==========================================
    fun getContributions(): Flow<List<Contribution>> = callbackFlow {
        val listener = firestore.collection("contributions")
            .whereEqualTo("organizationId", ORG_ID)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Contribution::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addContribution(contribution: Contribution): Result<Unit> = runCatching {
        val docRef = firestore.collection("contributions").document()
        docRef.set(contribution.copy(id = docRef.id, organizationId = ORG_ID, createdAt = Timestamp.now())).await()
    }

    fun getDonations(): Flow<List<Donation>> = callbackFlow {
        val listener = firestore.collection("donations")
            .whereEqualTo("organizationId", ORG_ID)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Donation::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addDonation(donation: Donation): Result<Unit> = runCatching {
        val docRef = firestore.collection("donations").document()
        docRef.set(donation.copy(id = docRef.id, organizationId = ORG_ID, createdAt = Timestamp.now())).await()
    }

    fun getExpenses(): Flow<List<Expense>> = callbackFlow {
        val listener = firestore.collection("expenses")
            .whereEqualTo("organizationId", ORG_ID)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Expense::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addExpense(expense: Expense): Result<Unit> = runCatching {
        val docRef = firestore.collection("expenses").document()
        docRef.set(expense.copy(id = docRef.id, organizationId = ORG_ID, createdAt = Timestamp.now())).await()
    }

    // ==========================================
    // 5. NOTICES & EVENTS
    // ==========================================
    fun getNotices(): Flow<List<NoticeItem>> = callbackFlow {
        val listener = firestore.collection("notices")
            .whereEqualTo("organizationId", ORG_ID)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(NoticeItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createNotice(notice: NoticeItem): Result<Unit> = runCatching {
        val docRef = firestore.collection("notices").document()
        docRef.set(notice.copy(id = docRef.id, organizationId = ORG_ID, createdAt = Timestamp.now())).await()
    }

    suspend fun deleteNotice(id: String): Result<Unit> = runCatching {
        firestore.collection("notices").document(id).delete().await()
    }

    // ==========================================
    // 6. MEMBERS & VOLUNTEERS
    // ==========================================
    fun getMembers(): Flow<List<UserModel>> = callbackFlow {
        val listener = firestore.collection("users")
            .whereEqualTo("organizationId", ORG_ID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(UserModel::class.java)?.copy(uid = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateMemberStatus(uid: String, status: String): Result<Unit> = runCatching {
        firestore.collection("users").document(uid).update("status", status, "updatedAt", Timestamp.now()).await()
    }

    suspend fun updateMemberRole(uid: String, role: String, position: String = ""): Result<Unit> = runCatching {
        val updates = mutableMapOf<String, Any>(
            "role" to role,
            "updatedAt" to Timestamp.now()
        )
        if (position.isNotBlank()) {
            updates["position"] = position
        }
        firestore.collection("users").document(uid).update(updates).await()
    }

    fun getVolunteers(): Flow<List<VolunteerItem>> = callbackFlow {
        val listener = firestore.collection("volunteers")
            .whereEqualTo("organizationId", ORG_ID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(VolunteerItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addVolunteer(volunteer: VolunteerItem): Result<Unit> = runCatching {
        val docRef = firestore.collection("volunteers").document()
        docRef.set(volunteer.copy(id = docRef.id, organizationId = ORG_ID)).await()
    }

    suspend fun updateVolunteer(volunteer: VolunteerItem): Result<Unit> = runCatching {
        firestore.collection("volunteers").document(volunteer.id).set(volunteer).await()
    }

    suspend fun deleteVolunteer(id: String): Result<Unit> = runCatching {
        firestore.collection("volunteers").document(id).delete().await()
    }

    // ==========================================
    // 7. 1-TO-1 MESSAGING
    // ==========================================
    fun getConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = firestore.collection("conversations")
            .whereEqualTo("organizationId", ORG_ID)
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Conversation::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.lastMessageTime } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = firestore.collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(
        senderId: String,
        senderName: String,
        recipientId: String,
        recipientName: String,
        text: String,
        imageUrl: String = ""
    ): Result<Unit> = runCatching {
        val convId = getConversationId(senderId, recipientId)
        val now = Timestamp.now()

        // Ensure conversation document
        val convRef = firestore.collection("conversations").document(convId)
        val convData = mapOf(
            "id" to convId,
            "participantIds" to listOf(senderId, recipientId),
            "participantNames" to mapOf(senderId to senderName, recipientId to recipientName),
            "lastMessage" to if (text.isNotBlank()) text else "📷 ছবি পাঠানো হয়েছে",
            "lastMessageTime" to now,
            "organizationId" to ORG_ID
        )
        convRef.set(convData, com.google.firebase.firestore.SetOptions.merge()).await()

        // Add message
        val msgRef = convRef.collection("messages").document()
        val chatMessage = ChatMessage(
            id = msgRef.id,
            conversationId = convId,
            senderId = senderId,
            senderName = senderName,
            text = text.trim(),
            imageUrl = imageUrl.trim(),
            isSeen = false,
            timestamp = now
        )
        msgRef.set(chatMessage).await()
    }

    fun getConversationId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    // ==========================================
    // 8. NOTIFICATIONS
    // ==========================================
    fun getNotifications(userId: String): Flow<List<NotificationItem>> = callbackFlow {
        val listener = firestore.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(NotificationItem::class.java)?.copy(id = doc.id)
                }?.filter { it.targetUserId.isEmpty() || it.targetUserId == userId } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createNotification(notification: NotificationItem): Result<Unit> = runCatching {
        val docRef = firestore.collection("notifications").document()
        docRef.set(notification.copy(id = docRef.id, timestamp = Timestamp.now())).await()
    }
}
