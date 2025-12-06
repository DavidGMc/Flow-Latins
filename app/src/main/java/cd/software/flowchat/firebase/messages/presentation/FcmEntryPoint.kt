package cd.software.flowchat.firebase.messages.presentation

import cd.software.flowchat.firebase.messages.FcmRepository
import cd.software.flowchat.firebase.messages.NotificationHandler
import cd.software.flowchat.firebase.repository.AuthRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FcmEntryPoint {
    fun notificationHandler(): NotificationHandler
    fun fcmRepository(): FcmRepository
    fun authRepository(): AuthRepository
}