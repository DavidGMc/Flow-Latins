package cd.software.flowchat

import android.content.Context
import cd.software.flowchat.preferences.ChatPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IRCModule {

    @Provides
    @Singleton
    fun provideChatPreferences(
        @ApplicationContext context: Context
    ): ChatPreferences {
        return ChatPreferences(context)
    }

    @Provides
    @Singleton
    fun provideIRCService(
        chatPreferences: ChatPreferences
    ): IRCService {
        return IRCService(chatPreferences)
    }
}
