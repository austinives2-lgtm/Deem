package com.example.data.model

import com.google.firebase.Timestamp

enum class UserRole(val roleKey: String, val titleBn: String) {
    SUPER_ADMIN("super_admin", "সুপার অ্যাডমিন"),
    ADMIN("admin", "অ্যাডমিন"),
    MEMBER("member", "সাধারণ সদস্য");

    companion object {
        fun fromString(key: String?): UserRole = when (key?.lowercase()) {
            "super_admin" -> SUPER_ADMIN
            "admin" -> ADMIN
            else -> MEMBER
        }
    }
}

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoURL: String = "",
    val organizationId: String = "bd_seva_org",
    val role: String = "member",
    val permissions: List<String> = emptyList(),
    val status: String = "active", // active, inactive
    val bloodGroup: String = "",
    val position: String = "সদস্য",
    val address: String = "",
    val donationCount: Int = 0,
    val lastDonationDate: String = "",
    val isAvailableForBlood: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    fun isSuperAdmin(superAdminUid: String = "YNxF7YAv0XYqkZHgaX6mtxAbG103"): Boolean {
        return uid == superAdminUid || role == "super_admin"
    }

    fun isAdmin(superAdminUid: String = "YNxF7YAv0XYqkZHgaX6mtxAbG103"): Boolean {
        return isSuperAdmin(superAdminUid) || role == "admin"
    }
}

data class ServiceItem(
    val id: String = "",
    val title: String = "",
    val titleBn: String = "",
    val description: String = "",
    val categoryKey: String = "",
    val iconName: String = "",
    val isActive: Boolean = true,
    val organizationId: String = "bd_seva_org",
    val createdAt: Timestamp = Timestamp.now()
)

data class ServiceRequest(
    val id: String = "",
    val serviceTitle: String = "",
    val applicantName: String = "",
    val phone: String = "",
    val location: String = "",
    val details: String = "",
    val status: String = "অপেক্ষমাণ", // অপেক্ষমাণ, অনুমোদিত, সম্পন্ন, বাতিল
    val requestedBy: String = "",
    val organizationId: String = "bd_seva_org",
    val createdAt: Timestamp = Timestamp.now()
)

data class BloodRequest(
    val id: String = "",
    val patientName: String = "",
    val bloodGroup: String = "",
    val hospital: String = "",
    val location: String = "",
    val requiredDate: String = "",
    val units: Int = 1,
    val contact: String = "",
    val urgency: String = "জরুরি", // জরুরি, সাধারণ
    val status: String = "Active", // Active, Fulfilled
    val requestedBy: String = "",
    val requestedByName: String = "",
    val organizationId: String = "bd_seva_org",
    val createdAt: Timestamp = Timestamp.now()
)

data class ActivityItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val date: String = "",
    val location: String = "",
    val participants: Int = 0,
    val beneficiaries: Int = 0,
    val budget: Double = 0.0,
    val status: String = "Ongoing", // Ongoing, Completed
    val organizationId: String = "bd_seva_org",
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class Contribution(
    val id: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val expectedAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val month: String = "",
    val paymentDate: String = "",
    val paymentMethod: String = "বিকাশ", // বিকাশ, নগদ, ব্যাংক, নগদ টাকা
    val notes: String = "",
    val organizationId: String = "bd_seva_org",
    val recordedBy: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class Donation(
    val id: String = "",
    val donorName: String = "",
    val phone: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val purpose: String = "সাধারণ তহবিল",
    val receiptNumber: String = "",
    val organizationId: String = "bd_seva_org",
    val recordedBy: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class Expense(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val authorizedBy: String = "",
    val voucherNumber: String = "",
    val notes: String = "",
    val organizationId: String = "bd_seva_org",
    val recordedBy: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class FinancialSummary(
    val totalContributions: Double = 0.0,
    val totalDonations: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val currentBalance: Double = 0.0
)

data class NoticeItem(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val type: String = "নোটিশ", // নোটিশ, জরুরি বিজ্ঞপ্তি, সভা, ইভেন্ট
    val date: String = "",
    val priority: String = "সাধারণ", // সাধারণ, জরুরি
    val organizationId: String = "bd_seva_org",
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class Conversation(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Timestamp = Timestamp.now(),
    val unreadCounts: Map<String, Int> = emptyMap(),
    val organizationId: String = "bd_seva_org"
)

data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val isSeen: Boolean = false,
    val timestamp: Timestamp = Timestamp.now()
)

data class VolunteerItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val photoURL: String = "",
    val phone: String = "",
    val skills: String = "",
    val availability: String = "সব সময়",
    val bloodGroup: String = "",
    val joiningDate: String = "",
    val status: String = "সক্রিয়", // সক্রিয়, নিষ্ক্রিয়
    val organizationId: String = "bd_seva_org"
)

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "general", // message, blood, notice, finance
    val isRead: Boolean = false,
    val targetUserId: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
