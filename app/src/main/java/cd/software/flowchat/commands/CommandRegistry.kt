package cd.software.flowchat.commands

import NoticeCommand
import android.content.Context
import android.util.Log

/**
 * Clase que registra todos los comandos disponibles en la aplicación.
 */
object CommandRegistry {
    private val TAG = "CommandRegistry"

    /**
     * Registra todos los comandos disponibles en el CommandManager.
     */
    fun registerAllCommands(commandManager: CommandManager, context: Context) {
        Log.d(TAG, "Registrando comandos IRC...")

        // Crear instancia del comando de ayuda (necesita referencia al commandManager)
        // val helpCommand = HelpCommand(commandManager, context)

        // Lista de todos los comandos a registrar
        val commands = listOf(
            JoinCommand(context),
            NickCommand(context),
            OpCommand(context),
            NoticeCommand(context),
            TopicCommand(context),
            WhoisCommand(context),
            MsgCommand(context),
            OperCommand(context),
            MeCommand(context),
            RawCommand(context),
            VoiceCommand(context),
            DevoiceCommand(context),
            DeopCommand(context),
            PartCommand(context),
            KickCommand(context),
            BanCommand(context),
            UnbanCommand(context),
            InviteCommand(context),

            // Aquí se pueden agregar más comandos en el futuro
        )

        // Registrar todos los comandos
        commandManager.registerCommands(commands)

        Log.d(TAG, "Se registraron ${commands.size} comandos IRC")
    }
}