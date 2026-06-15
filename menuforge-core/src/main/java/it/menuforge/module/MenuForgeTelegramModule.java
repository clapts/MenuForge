package it.menuforge.module;

/**
 * Interface for building a Telegram bot that manages a MenuForge menu.
 *
 * <p>MenuForge does NOT provide a Telegram bot implementation.
 * The integrating site creates its own bot (using any Java Telegram library,
 * e.g., TelegramBots, Pengrad) and injects MenuForge services to perform
 * the actual data operations.
 *
 * <p><strong>Recommended Telegram command mapping:</strong>
 * <ul>
 *   <li>{@code /menu} → {@link it.menuforge.service.MenuQueryService#getFullMenu()}</li>
 *   <li>{@code /aggiungi_categoria} → {@link it.menuforge.service.CategoryService#createCategory}</li>
 *   <li>{@code /aggiungi_prodotto} → {@link it.menuforge.service.MenuItemService#createItem}</li>
 *   <li>{@code /modifica_prezzo} → {@link it.menuforge.service.MenuItemService#updateItem}</li>
 *   <li>{@code /nascondi} → {@link it.menuforge.service.MenuItemService#toggleAvailability}</li>
 *   <li>{@code /mostra} → {@link it.menuforge.service.MenuItemService#toggleAvailability}</li>
 *   <li>{@code /ingredienti} → {@link it.menuforge.service.MenuItemService#setIngredients}</li>
 * </ul>
 *
 * <p><strong>Typical bot integration flow:</strong>
 * <pre>
 * {@code
 * // 1. The Telegram update handler receives a command
 * // 2. Parse the command and arguments
 * // 3. Call the appropriate MenuForge service method
 * // 4. Reply to the user with a confirmation
 *
 * @Override
 * public void handleCommand(String command, String chatId, String[] args) {
 *     switch (command) {
 *         case "/nascondi" -> {
 *             Long itemId = Long.parseLong(args[0]);
 *             menuItemService.toggleAvailability(itemId);
 *             sendMessage(chatId, "Prodotto nascosto dal menu.");
 *         }
 *         // ... other commands
 *     }
 * }
 * }
 * </pre>
 */
public interface MenuForgeTelegramModule {

    /**
     * Handles a Telegram bot command.
     *
     * @param command the command string, e.g., {@code "/nascondi"}
     * @param chatId  the Telegram chat ID to reply to
     * @param args    command arguments parsed from the message text
     */
    void handleCommand(String command, String chatId, String[] args);
}
