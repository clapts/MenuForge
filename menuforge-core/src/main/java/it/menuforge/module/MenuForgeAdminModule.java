package it.menuforge.module;

import it.menuforge.model.Category;
import it.menuforge.model.MenuItem;

/**
 * Interface for building a web admin panel that manages a MenuForge menu.
 *
 * <p>MenuForge does NOT provide an implementation of this interface.
 * The integrating site implements it to receive lifecycle notifications
 * and integrate menu management into its own admin UI.
 *
 * <p><strong>Typical usage in an admin panel:</strong>
 * <ol>
 *   <li>Inject {@link it.menuforge.service.CategoryService} and
 *       {@link it.menuforge.service.MenuItemService} into your admin controllers.</li>
 *   <li>Implement this interface to handle post-save events
 *       (e.g., invalidate a cache, send a push notification, log an audit entry).</li>
 * </ol>
 *
 * <p><strong>Example implementation:</strong>
 * <pre>
 * {@code
 * @Component
 * public class ArdaAdminModule implements MenuForgeAdminModule {
 *
 *     @Override
 *     public void onItemCreated(MenuItem item) {
 *         log.info("New item added: " + item.getTitle());
 *         // Invalidate page cache, send notification, etc.
 *     }
 * }
 * }
 * </pre>
 */
public interface MenuForgeAdminModule {

    /**
     * Called after a new category has been successfully created and saved.
     *
     * @param category the newly created category
     */
    void onCategoryCreated(Category category);

    /**
     * Called after a category has been successfully updated.
     *
     * @param category the updated category
     */
    void onCategoryUpdated(Category category);

    /**
     * Called after a new menu item has been successfully created and saved.
     *
     * @param item the newly created item
     */
    void onItemCreated(MenuItem item);

    /**
     * Called after a menu item has been successfully updated.
     *
     * @param item the updated item
     */
    void onItemUpdated(MenuItem item);

    /**
     * Called after the availability status of an item has been toggled.
     *
     * @param item      the item whose availability changed
     * @param available the new availability value
     */
    void onItemAvailabilityChanged(MenuItem item, boolean available);
}
