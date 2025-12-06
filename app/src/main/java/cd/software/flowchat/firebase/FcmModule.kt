package cd.software.flowchat.firebase

import android.content.Context
import cd.software.flowchat.firebase.messages.FcmNotificationHandler
import cd.software.flowchat.firebase.messages.FcmRepository
import cd.software.flowchat.firebase.messages.ManageFcmTokenUseCase
import cd.software.flowchat.firebase.messages.NotificationHandler
import cd.software.flowchat.firebase.messages.data.FirebaseFcmRepository
import cd.software.flowchat.firebase.messages.presentation.FcmViewModel
import cd.software.flowchat.firebase.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FcmModule {

    @Provides
    @Singleton
    fun provideFcmRepository(firestore: FirebaseFirestore): FcmRepository {
        return FirebaseFcmRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideNotificationHandler(@ApplicationContext context: Context): NotificationHandler {
        return FcmNotificationHandler(context)
    }

    @Provides
    @Singleton
    fun provideManageFcmTokenUseCase(
        fcmRepository: FcmRepository,
        authRepository: AuthRepository
    ): ManageFcmTokenUseCase {
        return ManageFcmTokenUseCase(fcmRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideFcmViewModel(
        manageFcmTokenUseCase: ManageFcmTokenUseCase
    ): FcmViewModel {
        return FcmViewModel(manageFcmTokenUseCase)
    }
}