package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class PanelMode {
    USER, ADMIN
}

class MainViewModel(
    val authRepo: AuthRepository = AuthRepository(),
    val firestoreRepo: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    val currentUser: StateFlow<UserModel?> = authRepo.currentUserProfile

    private val _panelMode = MutableStateFlow(PanelMode.USER)
    val panelMode: StateFlow<PanelMode> = _panelMode.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // 1. Services
    val services: StateFlow<List<ServiceItem>> = firestoreRepo.getServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serviceRequests: StateFlow<List<ServiceRequest>> = firestoreRepo.getServiceRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Blood
    val bloodRequests: StateFlow<List<BloodRequest>> = firestoreRepo.getBloodRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bloodDonors: StateFlow<List<UserModel>> = firestoreRepo.getBloodDonors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Activities
    val activities: StateFlow<List<ActivityItem>> = firestoreRepo.getActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. Finance
    val contributions: StateFlow<List<Contribution>> = firestoreRepo.getContributions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val donations: StateFlow<List<Donation>> = firestoreRepo.getDonations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = firestoreRepo.getExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialSummary: StateFlow<FinancialSummary> = combine(
        contributions, donations, expenses
    ) { contList, donList, expList ->
        val totalCont = contList.sumOf { it.paidAmount }
        val totalDon = donList.sumOf { it.amount }
        val totalExp = expList.sumOf { it.amount }
        val balance = totalCont + totalDon - totalExp
        FinancialSummary(
            totalContributions = totalCont,
            totalDonations = totalDon,
            totalExpenses = totalExp,
            currentBalance = balance
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // 5. Notices
    val notices: StateFlow<List<NoticeItem>> = firestoreRepo.getNotices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 6. Members & Volunteers
    val members: StateFlow<List<UserModel>> = firestoreRepo.getMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val volunteers: StateFlow<List<VolunteerItem>> = firestoreRepo.getVolunteers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 7. Conversations
    val conversations: StateFlow<List<Conversation>> = currentUser.flatMapLatest { user ->
        if (user != null) firestoreRepo.getConversations(user.uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 8. Notifications
    val notifications: StateFlow<List<NotificationItem>> = currentUser.flatMapLatest { user ->
        if (user != null) firestoreRepo.getNotifications(user.uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed default 12 humanitarian service categories if Firestore is empty
        viewModelScope.launch {
            firestoreRepo.seedDefaultServicesIfEmpty()
        }
    }

    fun switchPanel(mode: PanelMode) {
        val user = currentUser.value
        if (mode == PanelMode.ADMIN && user?.isAdmin() != true) {
            emitToast("অ্যাডমিন প্যানেলে প্রবেশের অনুমতি নেই।")
            return
        }
        _panelMode.value = mode
    }

    private fun emitToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }

    // --- Actions ---

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = authRepo.login(email, pass)
            res.onSuccess {
                emitToast("স্বাগতম! সফলভাবে লগইন হয়েছে।")
                onSuccess()
            }.onFailure {
                val err = it.message ?: "লগইন ব্যর্থ হয়েছে"
                emitToast(err)
                onError(err)
            }
        }
    }

    fun register(
        name: String, email: String, pass: String, phone: String,
        bloodGroup: String, address: String, photoUrl: String,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val res = authRepo.register(name, email, pass, phone, bloodGroup, address, photoUrl)
            res.onSuccess {
                emitToast("নিবন্ধন সফল হয়েছে! স্বাগতম।")
                onSuccess()
            }.onFailure {
                val err = it.message ?: "নিবন্ধন ব্যর্থ হয়েছে"
                emitToast(err)
                onError(err)
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        authRepo.logout()
        _panelMode.value = PanelMode.USER
        onLoggedOut()
    }

    fun updateProfile(
        name: String, phone: String, bloodGroup: String,
        address: String, photoURL: String, isAvailableForBlood: Boolean
    ) {
        viewModelScope.launch {
            authRepo.updateProfile(name, phone, bloodGroup, address, photoURL, isAvailableForBlood)
                .onSuccess { emitToast("প্রোফাইল সফলভাবে আপডেট হয়েছে।") }
                .onFailure { emitToast("প্রোফাইল আপডেট ব্যর্থ: ${it.message}") }
        }
    }

    // Service Actions
    fun addService(service: ServiceItem, onDone: () -> Unit) {
        viewModelScope.launch {
            firestoreRepo.addService(service)
                .onSuccess {
                    emitToast("সেবা যোগ করা হয়েছে।")
                    onDone()
                }.onFailure { emitToast("ব্যর্থ হয়েছে: ${it.message}") }
        }
    }

    fun updateService(service: ServiceItem, onDone: () -> Unit) {
        viewModelScope.launch {
            firestoreRepo.updateService(service)
                .onSuccess {
                    emitToast("সেবা তথ্য আপডেট হয়েছে।")
                    onDone()
                }.onFailure { emitToast("ব্যর্থ হয়েছে: ${it.message}") }
        }
    }

    fun deleteService(id: String) {
        viewModelScope.launch {
            firestoreRepo.deleteService(id)
                .onSuccess { emitToast("সেবা মুছে ফেলা হয়েছে।") }
                .onFailure { emitToast("মুছে ফেলতে ব্যর্থ: ${it.message}") }
        }
    }

    fun submitServiceRequest(request: ServiceRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            firestoreRepo.submitServiceRequest(request)
                .onSuccess {
                    emitToast("সেবার আবেদন জমা দেওয়া হয়েছে।")
                    onDone()
                }.onFailure { emitToast("আবেদন ব্যর্থ: ${it.message}") }
        }
    }

    fun updateServiceRequestStatus(id: String, status: String) {
        viewModelScope.launch {
            firestoreRepo.updateServiceRequestStatus(id, status)
                .onSuccess { emitToast("আবেদনের স্ট্যাটাস পরিবর্তিত হয়েছে।") }
                .onFailure { emitToast("ব্যর্থ হয়েছে: ${it.message}") }
        }
    }

    // Blood Actions
    fun createBloodRequest(request: BloodRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            firestoreRepo.createBloodRequest(request)
                .onSuccess {
                    emitToast("রক্তের আবেদন প্রকাশিত হয়েছে।")
                    // create in-app notification
                    firestoreRepo.createNotification(
                        NotificationItem(
                            title = "জরুরি রক্তের আবেদন: ${request.bloodGroup}",
                            message = "${request.patientName}-এর জন্য ${request.hospital}-এ ${request.units} ব্যাগ রক্ত প্রয়োজন। যোগাযোগ: ${request.contact}",
                            type = "blood"
                        )
                    )
                    onDone()
                }.onFailure { emitToast("আবেদন ব্যর্থ: ${it.message}") }
        }
    }

    fun updateBloodRequestStatus(id: String, status: String) {
        viewModelScope.launch {
            firestoreRepo.updateBloodRequestStatus(id, status)
                .onSuccess { emitToast("রক্তের আবেদনের অবস্থা আপডেট হয়েছে।") }
                .onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    // Activity Actions
    fun createActivity(activity: ActivityItem, onDone: () -> Unit) {
        viewModelScope.launch {
            firestoreRepo.createActivity(activity)
                .onSuccess {
                    emitToast("মানবিক কার্যক্রম প্রকাশিত হয়েছে।")
                    onDone()
                }.onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    fun updateActivity(activity: ActivityItem, onDone: () -> Unit) {
        viewModelScope.launch {
            firestoreRepo.updateActivity(activity)
                .onSuccess {
                    emitToast("কার্যক্রম তথ্য আপডেট হয়েছে।")
                    onDone()
                }.onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    fun deleteActivity(id: String) {
        viewModelScope.launch {
            firestoreRepo.deleteActivity(id)
                .onSuccess { emitToast("কার্যক্রম মুছে ফেলা হয়েছে।") }
                .onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    // Finance Actions
    fun addContribution(contribution: Contribution, onDone: () -> Unit) {
        viewModelScope.launch {
            firestoreRepo.addContribution(contribution)
                .onSuccess {
                    emitToast("মাসিক চাঁদা রেকর্ড সম্পন্ন হয়েছে।")
                    onDone()
                }.onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    fun addDonation(donation: Donation, onDone: () -> Unit) {
        viewModelScope.launch {
            firestoreRepo.addDonation(donation)
                .onSuccess {
                    emitToast("অনুদান হিসাব লিপিবদ্ধ হয়েছে।")
                    onDone()
                }.onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    fun addExpense(expense: Expense, onDone: () -> Unit) {
        viewModelScope.launch {
            firestoreRepo.addExpense(expense)
                .onSuccess {
                    emitToast("ব্যয়ের হিসাব সংরক্ষণ করা হয়েছে।")
                    onDone()
                }.onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    // Notices Actions
    fun createNotice(notice: NoticeItem, onDone: () -> Unit) {
        viewModelScope.launch {
            firestoreRepo.createNotice(notice)
                .onSuccess {
                    emitToast("নোটিশ প্রকাশিত হয়েছে।")
                    firestoreRepo.createNotification(
                        NotificationItem(
                            title = "নতুন নোটিশ: ${notice.title}",
                            message = notice.content,
                            type = "notice"
                        )
                    )
                    onDone()
                }.onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    fun deleteNotice(id: String) {
        viewModelScope.launch {
            firestoreRepo.deleteNotice(id)
                .onSuccess { emitToast("নোটিশ মুছে ফেলা হয়েছে।") }
                .onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    // Member Management Actions
    fun updateMemberStatus(uid: String, status: String) {
        viewModelScope.launch {
            firestoreRepo.updateMemberStatus(uid, status)
                .onSuccess { emitToast("সদস্যের স্ট্যাটাস পরিবর্তিত হয়েছে।") }
                .onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    fun updateMemberRole(uid: String, role: String, position: String = "") {
        val user = currentUser.value
        if (user?.isSuperAdmin() != true) {
            emitToast("শুধুমাত্র সুপার অ্যাডমিন রোল পরিবর্তন করতে পারেন।")
            return
        }
        viewModelScope.launch {
            firestoreRepo.updateMemberRole(uid, role, position)
                .onSuccess { emitToast("সদস্যের পদবি ও রোল সফলভাবে আপডেট হয়েছে।") }
                .onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    fun addVolunteer(volunteer: VolunteerItem, onDone: () -> Unit) {
        viewModelScope.launch {
            firestoreRepo.addVolunteer(volunteer)
                .onSuccess {
                    emitToast("স্বেচ্ছাসেবক নিবন্ধিত হয়েছে।")
                    onDone()
                }.onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    fun deleteVolunteer(id: String) {
        viewModelScope.launch {
            firestoreRepo.deleteVolunteer(id)
                .onSuccess { emitToast("স্বেচ্ছাসেবক তথ্য মুছে ফেলা হয়েছে।") }
                .onFailure { emitToast("ব্যর্থ: ${it.message}") }
        }
    }

    // Messaging
    fun sendMessage(recipientId: String, recipientName: String, text: String, imageUrl: String = "") {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            firestoreRepo.sendMessage(
                senderId = user.uid,
                senderName = user.name.ifBlank { "সদস্য" },
                recipientId = recipientId,
                recipientName = recipientName,
                text = text,
                imageUrl = imageUrl
            ).onFailure { emitToast("মেসেজ পাঠাতে ব্যর্থ: ${it.message}") }
        }
    }
}
