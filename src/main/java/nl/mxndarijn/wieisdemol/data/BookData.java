package nl.mxndarijn.wieisdemol.data;

import nl.mxndarijn.api.inventory.saver.InventoryManager;
import nl.mxndarijn.wieisdemol.items.Items;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public enum BookData {
    DEATH_NOTE(Items.GAME_DEATHNOTE, AvailablePerson.EXECUTOR, AvailablePerson.SELECTED_PERSON_1),
    EGO_COUNT(Items.GAME_EGOCOUNT, AvailablePerson.EXECUTOR),
    SHAPESHIFTER_COUNT(Items.GAME_SHAPESHIFTERCOUNT, AvailablePerson.EXECUTOR),
    INVCLEAR(Items.GAME_INVCLEAR, AvailablePerson.EXECUTOR, AvailablePerson.SELECTED_PERSON_1),
    SWITCH(Items.GAME_SWITCH, AvailablePerson.EXECUTOR, AvailablePerson.SELECTED_PERSON_1),
    SPELER_COUNT(Items.GAME_SPELERCOUNT, AvailablePerson.EXECUTOR),
    MOL_COUNT(Items.GAME_MOLCOUNT, AvailablePerson.EXECUTOR),
    PEACEKEEPER_CHECCKER(Items.GAME_PEACEKEEPER_CHECKER, AvailablePerson.EXECUTOR, AvailablePerson.SELECTED_PERSON_1),
    REBORN(Items.GAME_REBORN_BOOK, AvailablePerson.EXECUTOR, AvailablePerson.SELECTED_PERSON_1),
    INVCCHECK(Items.GAME_INVCHECK_BOOK, AvailablePerson.EXECUTOR, AvailablePerson.SELECTED_PERSON_1),
    TELEPORT(Items.GAME_TELEPORT_BOOK, AvailablePerson.EXECUTOR, AvailablePerson.SELECTED_PERSON_1, AvailablePerson.SELECTED_PERSON_2),
    GENERATOR(Items.GAME_GENERATOR, AvailablePerson.EXECUTOR)


    ;

    private final Items item;
    private final AvailablePerson[] persons;
    BookData(Items item, AvailablePerson... persons) {
        this.item = item;
        this.persons = persons;
    }

    public static Optional<BookData> getBookByItemStack(ItemStack is) {
        for (BookData value : values()) {
            if(InventoryManager.validateItem(value.item.getItemStack(), is)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public Items getItem() {
        return item;
    }

    public AvailablePerson[] getPersons() {
        return persons;
    }
}
