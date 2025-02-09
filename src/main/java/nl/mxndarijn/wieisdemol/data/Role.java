package nl.mxndarijn.wieisdemol.data;

import nl.mxndarijn.wieisdemol.managers.database.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Optional;

public enum Role {
    SPELER("player", ChatColor.GOLD + "Speler", ChatColor.GOLD + "Speler-Peacekeeper", "gold-block", CustomInventoryOverlay.ROLES_PLAYER.getUnicodeCharacter(), Material.GOLD_BLOCK, ChatColor.GOLD + "Spelers", ChatColor.GRAY + "Hebben gewonnen", PlayerData.UserDataType.SPELERWINS),
    MOL("mol", ChatColor.DARK_AQUA + "Mol", ChatColor.DARK_AQUA + "Mol-Peacekeeper", "diamond-block", CustomInventoryOverlay.ROLES_MOLE.getUnicodeCharacter(), Material.DIAMOND_BLOCK, ChatColor.DARK_AQUA + "Mollen", ChatColor.GRAY + "Hebben gewonnen", PlayerData.UserDataType.MOLWINS),
    EGO("ego", ChatColor.GRAY + "Ego", ChatColor.GRAY + "Ego-Peacekeeper", "emerald-block", CustomInventoryOverlay.ROLES_EGO.getUnicodeCharacter(), Material.AIR, ChatColor.GRAY + "" + ChatColor.UNDERLINE + "Ego", ChatColor.GRAY + "heeft gewonnen", PlayerData.UserDataType.EGOWINS),
    SHAPESHIFTER("shapeshifter", ChatColor.GREEN + "Shapeshifter", ChatColor.GREEN + "Shapeshifter-Peacekeeper", "emerald-block", CustomInventoryOverlay.ROLES_SHAPESHIFTER.getUnicodeCharacter(), Material.AIR, ChatColor.GREEN + "Shapeshifter", ChatColor.GRAY + "heeft gewonnen", PlayerData.UserDataType.EGOWINS);
    private final String rolName;
    private final String peacekeeperName;
    private final String roleType;
    private final String headKey;
    private final String unicode;
    private final Material type;
    private final String title;
    private final String subTitle;
    private final PlayerData.UserDataType winType;

    Role(String rolType, String normalName, String peacekeeperName, String headKey, String unicode, Material type, String title, String subTitle, PlayerData.UserDataType winType) {
        this.roleType = rolType;
        this.rolName = normalName;
        this.peacekeeperName = peacekeeperName;
        this.headKey = headKey;
        this.unicode = unicode;
        this.type = type;
        this.title = title;
        this.subTitle = subTitle;
        this.winType = winType;
    }

    public static Optional<Role> getRoleByType(String type) {
        for (Role value : values()) {
            if (value.roleType.equalsIgnoreCase(type))
                return Optional.of(value);
        }
        return Optional.empty();
    }

    public String getRolName() {
        return rolName;
    }

    public String getPeacekeeperName() {
        return peacekeeperName;
    }

    public String getRoleType() {
        return roleType;
    }

    public String getHeadKey() {
        return headKey;
    }

    public String getUnicode() {
        return unicode;
    }

    public Material getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public PlayerData.UserDataType getWinType() {
        return winType;
    }
}
