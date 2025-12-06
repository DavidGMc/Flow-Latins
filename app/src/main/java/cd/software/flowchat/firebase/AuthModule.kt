package cd.software.flowchat.firebase

import cd.software.flowchat.firebase.repository.AuthRepository
import cd.software.flowchat.firebase.repository.FirebaseAuthRepository
import cd.software.flowchat.firebase.repository.FirebaseProfileRepository
import cd.software.flowchat.firebase.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        firebaseProfileRepository: FirebaseProfileRepository
    ): ProfileRepository
}
