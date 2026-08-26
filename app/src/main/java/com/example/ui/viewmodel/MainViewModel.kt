package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AnnouncementEntity
import com.example.data.local.AuditLogEntity
import com.example.data.local.BannerEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.OfferEntity
import com.example.data.local.ReferralEntity
import com.example.data.local.SettingsEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.data.local.WithdrawalEntity
import com.example.data.repository.ZyneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = ZyneRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isLoginLoading = MutableStateFlow(false)
    val isLoginLoading: StateFlow<Boolean> = _isLoginLoading.asStateFlow()

    private val _withdrawMessage = MutableStateFlow<String?>(null)
    val withdrawMessage: StateFlow<String?> = _withdrawMessage.asStateFlow()

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    val currentUserId: StateFlow<String?> = repository.currentUserId

    val currentUser: StateFlow<UserEntity?> = repository.currentUserId.flatMapLatest { id ->
        if (id != null) repository.getCurrentUserFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeOffers: StateFlow<List<OfferEntity>> = combine(
        repository.activeOffersFlow,
        _searchQuery,
        _selectedCategory
    ) { offers, query, category ->
        offers.filter { offer ->
            val matchesQuery = query.isBlank() ||
                    offer.name.contains(query, ignoreCase = true) ||
                    offer.description.contains(query, ignoreCase = true) ||
                    offer.category.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || offer.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOffers: StateFlow<List<OfferEntity>> = repository.allOffersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBanners: StateFlow<List<BannerEntity>> = repository.activeBannersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBanners: StateFlow<List<BannerEntity>> = repository.allBannersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAnnouncement: StateFlow<AnnouncementEntity?> = repository.activeAnnouncementFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appSettings: StateFlow<SettingsEntity> = repository.settingsFlow.flatMapLatest { settings ->
        flowOf(settings ?: SettingsEntity())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsEntity())

    val userTransactions: StateFlow<List<TransactionEntity>> = repository.currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserTransactionsFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserWithdrawalsFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = repository.currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserNotificationsFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUnreadCountFlow(id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userReferrals: StateFlow<List<ReferralEntity>> = repository.currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserReferralsFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin flows
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.allWithdrawalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun clearWithdrawMessage() {
        _withdrawMessage.value = null
    }

    fun loginWithGoogle(
        email: String,
        displayName: String,
        photoUrl: String,
        deviceId: String,
        referralCode: String?,
        idToken: String? = null
    ) {
        viewModelScope.launch {
            _isLoginLoading.value = true
            _loginError.value = null
            try {
                if (!idToken.isNullOrBlank()) {
                    val authResult = repository.firebaseManager.signInWithGoogleCredential(idToken)
                    if (authResult.isFailure) {
                        val authEx = authResult.exceptionOrNull()
                        _loginError.value = authEx?.message ?: "Firebase Auth failed"
                        return@launch
                    }
                }
                val result = repository.loginWithGoogle(email, displayName, photoUrl, deviceId, referralCode)
                if (result.isFailure) {
                    val ex = result.exceptionOrNull()
                    _loginError.value = ex?.message ?: "Sign-In failed"
                }
            } catch (e: Exception) {
                _loginError.value = "[${e::class.java.simpleName}] ${e.localizedMessage ?: e.message ?: "An unexpected error occurred during Sign-In"}"
            } finally {
                _isLoginLoading.value = false
            }
        }
    }

    fun logout() {
        repository.logout()
        _isAdminAuthenticated.value = false
    }

    fun authenticateAdmin(pin: String): Boolean {
        val email = currentUser.value?.email?.trim()?.lowercase()
        val isTargetAdmin = email == "buddepubasu123@gmail.com"
        if (isTargetAdmin && (pin == "1234" || pin == "admin123" || pin.isEmpty())) {
            _isAdminAuthenticated.value = true
            return true
        }
        return false
    }

    private fun isAuthorizedAdminUser(): Boolean {
        val email = currentUser.value?.email?.trim()?.lowercase()
        val fbEmail = repository.firebaseManager.getCurrentFirebaseUserEmail()?.trim()?.lowercase()
        return email == "buddepubasu123@gmail.com" || fbEmail == "buddepubasu123@gmail.com"
    }

    fun requestWithdrawal(amount: Double, upiId: String) {
        viewModelScope.launch {
            _withdrawMessage.value = null
            val userId = currentUserId.value ?: return@launch
            val result = repository.requestWithdrawal(userId, amount, upiId)
            if (result.isSuccess) {
                _withdrawMessage.value = "SUCCESS: Withdrawal request of ₹${amount.toInt()} submitted!"
            } else {
                _withdrawMessage.value = "ERROR: ${result.exceptionOrNull()?.message ?: "Failed to submit withdrawal"}"
            }
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            val userId = currentUserId.value ?: return@launch
            repository.markAllNotificationsRead(userId)
        }
    }

    fun recordOfferClick(offer: OfferEntity, affiliateUrl: String) {
        viewModelScope.launch {
            val userId = currentUserId.value ?: return@launch
            repository.recordOfferClick(
                userId = userId,
                offerId = offer.id,
                offerName = offer.name,
                affiliateUrl = affiliateUrl,
                reward = offer.rewardAmount
            )
        }
    }

    // Admin Operations - Strictly guarded by administrator authorization
    fun adminCreditReward(
        userId: String,
        amount: Double,
        offerName: String,
        note: String,
        completionReference: String = "",
        offerId: String = ""
    ) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminCreditReward(userId, amount, offerName, adminEmail, note, completionReference, offerId)
        }
    }

    fun adminRejectReward(userId: String, offerName: String, reason: String) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminRejectReward(userId, offerName, reason, adminEmail)
        }
    }

    fun adminApproveWithdrawal(withdrawalId: String) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminApproveWithdrawal(withdrawalId, adminEmail)
        }
    }

    fun adminMarkWithdrawalPaid(withdrawalId: String) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminMarkWithdrawalPaid(withdrawalId, adminEmail)
        }
    }

    fun adminRejectWithdrawal(withdrawalId: String, reason: String) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminRejectWithdrawal(withdrawalId, reason, adminEmail)
        }
    }

    fun adminSaveOffer(offer: OfferEntity) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminSaveOffer(offer, adminEmail)
        }
    }

    fun adminDeleteOffer(offerId: String) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminDeleteOffer(offerId, adminEmail)
        }
    }

    fun adminSaveBanner(banner: BannerEntity) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminSaveBanner(banner, adminEmail)
        }
    }

    fun adminDeleteBanner(bannerId: String) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminDeleteBanner(bannerId, adminEmail)
        }
    }

    fun adminSaveAnnouncement(announcement: AnnouncementEntity) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminSaveAnnouncement(announcement, adminEmail)
        }
    }

    fun adminUpdateSettings(settings: SettingsEntity) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminUpdateSettings(settings, adminEmail)
        }
    }

    fun adminToggleUserBan(userId: String) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminToggleUserBan(userId, adminEmail)
        }
    }

    fun adminResetDeviceBinding(userId: String) {
        if (!isAuthorizedAdminUser()) return
        viewModelScope.launch {
            val adminEmail = currentUser.value?.email ?: "buddepubasu123@gmail.com"
            repository.adminResetDeviceBinding(userId, adminEmail)
        }
    }
}
