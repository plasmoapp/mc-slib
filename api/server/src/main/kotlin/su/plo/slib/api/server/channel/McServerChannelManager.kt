package su.plo.slib.api.server.channel

/**
 * Manages custom Minecraft channels and their associated handlers.
 */
interface McServerChannelManager {

    /**
     * Gets the collection of registered channels ids.
     *
     * @return The collection of registered channels ids.
     */
    val registeredChannels: Collection<String>

    /**
     * Registers a custom channel [handler] for the specified [channel].
     *
     * @param channel The name of the custom channel to register the handler for.
     * @param handler The [McServerChannelHandler] implementation that will process messages received on the channel.
     */
    fun registerChannelHandler(channel: String, handler: McServerChannelHandler)

    /**
     * Unregisters a custom channel [handler].
     *
     * @param channel The name of the custom channel to unregister the handler for.
     * @param handler The channel handler to unregister.
     */
    fun unregisterChannelHandler(channel: String, handler: McServerChannelHandler)

    /**
     * Unregisters all custom channel handlers.
     */
    fun clear()
}
