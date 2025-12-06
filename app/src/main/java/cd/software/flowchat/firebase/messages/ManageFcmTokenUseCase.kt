package cd.software.flowchat.firebase.messages

import cd.software.flowchat.firebase.repository.AuthRepository

class ManageFcmTokenUseCase(
    private val fcmRepository: FcmRepository,
    private val authRepository: AuthRepository
) {
    suspend fun registerToken(token: String) {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            fcmRepository.saveToken(userId, token)
        }
    }
}